package com.example.domain

import com.example.data.entity.AssetEntity
import com.example.data.entity.MonthlyPriceEntity
import com.example.data.entity.TransactionEntity
import com.example.domain.model.HoldingsItem
import com.example.domain.model.MonthlyPerformance
import com.example.domain.model.PortfolioSummary
import com.example.domain.model.YearlyPerformance
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.math.roundToInt

object PortfolioCalculator {

    private val monthFormat = SimpleDateFormat("yyyy-MM", Locale.US)
    private val displayMonthFormat = SimpleDateFormat("MMM yyyy", Locale.US)

    fun formatYearMonthDisplay(yearMonth: String): String {
        return try {
            val date = monthFormat.parse(yearMonth)
            if (date != null) displayMonthFormat.format(date) else yearMonth
        } catch (_: Exception) {
            yearMonth
        }
    }

    /**
     * Calculates holdings for each asset based on all transactions and latest monthly prices.
     */
    fun calculateHoldings(
        assets: List<AssetEntity>,
        transactions: List<TransactionEntity>,
        monthlyPrices: List<MonthlyPriceEntity>
    ): List<HoldingsItem> {
        val txByAsset = transactions.groupBy { it.assetId }
        val pricesByAsset = monthlyPrices.groupBy { it.assetId }

        val rawHoldings = assets.map { asset ->
            val assetTx = txByAsset[asset.id]?.sortedBy { it.date } ?: emptyList()
            var buyQty = 0.0
            var buyCost = 0.0
            var sellQty = 0.0
            var sellProceeds = 0.0

            assetTx.forEach { tx ->
                if (tx.type.equals("BUY", ignoreCase = true)) {
                    buyQty += tx.quantity
                    buyCost += tx.totalAmount
                } else if (tx.type.equals("SELL", ignoreCase = true)) {
                    sellQty += tx.quantity
                    sellProceeds += tx.totalAmount
                }
            }

            val unitsHeld = (buyQty - sellQty).coerceAtLeast(0.0)
            val avgBuyPrice = if (buyQty > 0.0) buyCost / buyQty else 0.0
            val costBasis = unitsHeld * avgBuyPrice

            // Find current price: latest monthly price or latest tx price
            val assetPrices = pricesByAsset[asset.id]?.sortedByDescending { it.yearMonth }
            val latestMonthlyPrice = assetPrices?.firstOrNull()?.closingPrice
            val latestTxPrice = assetTx.lastOrNull()?.pricePerUnit

            val currentPrice = latestMonthlyPrice
                ?: latestTxPrice
                ?: (if (buyQty > 0.0) avgBuyPrice else 0.0)

            val currentValue = unitsHeld * currentPrice
            val unrealizedPnL = currentValue - costBasis
            val unrealizedPnLPercent = if (costBasis > 0.0) {
                (unrealizedPnL / costBasis) * 100.0
            } else 0.0

            val realizedPnL = (sellProceeds - (sellQty * avgBuyPrice)).coerceAtLeast(-buyCost)

            HoldingsItem(
                asset = asset,
                unitsHeld = unitsHeld,
                totalBoughtQty = buyQty,
                totalBoughtCost = buyCost,
                totalSoldQty = sellQty,
                totalSoldProceeds = sellProceeds,
                avgBuyPrice = avgBuyPrice,
                costBasis = costBasis,
                currentPrice = currentPrice,
                currentValue = currentValue,
                unrealizedPnL = unrealizedPnL,
                unrealizedPnLPercent = unrealizedPnLPercent,
                realizedPnL = realizedPnL
            )
        }

        val totalPortfolioValue = rawHoldings.sumOf { it.currentValue }
        return rawHoldings.map { holding ->
            val alloc = if (totalPortfolioValue > 0.0) {
                (holding.currentValue / totalPortfolioValue) * 100.0
            } else 0.0
            holding.copy(allocationPercent = alloc)
        }
    }

    /**
     * Calculates Month-over-Month performance history.
     */
    fun calculateMonthlyPerformance(
        assets: List<AssetEntity>,
        transactions: List<TransactionEntity>,
        monthlyPrices: List<MonthlyPriceEntity>
    ): List<MonthlyPerformance> {
        if (assets.isEmpty() || transactions.isEmpty()) return emptyList()

        // Gather all distinct months
        val txMonths = transactions.mapNotNull {
            if (it.date.length >= 7) it.date.substring(0, 7) else null
        }
        val priceMonths = monthlyPrices.map { it.yearMonth }
        val allMonths = (txMonths + priceMonths).distinct().sorted()

        if (allMonths.isEmpty()) return emptyList()

        val pricesMap = monthlyPrices.associateBy { "${it.assetId}_${it.yearMonth}" }
        val monthlyResults = mutableListOf<MonthlyPerformance>()
        var previousMonthEndValue = 0.0
        var cumulativeInflows = 0.0

        for (m in allMonths) {
            // Transactions up to month end (all transactions on or before month m)
            val txUpToMonth = transactions.filter {
                if (it.date.length >= 7) it.date.substring(0, 7) <= m else false
            }

            // Transactions specifically in this month
            val txThisMonth = transactions.filter {
                if (it.date.length >= 7) it.date.substring(0, 7) == m else false
            }

            var netInflowThisMonth = 0.0
            txThisMonth.forEach { tx ->
                if (tx.type.equals("BUY", ignoreCase = true)) {
                    netInflowThisMonth += tx.totalAmount
                } else if (tx.type.equals("SELL", ignoreCase = true)) {
                    netInflowThisMonth -= tx.totalAmount
                }
            }
            cumulativeInflows += netInflowThisMonth

            // Calculate portfolio value & cost basis at end of month m
            var endingPortfolioValue = 0.0
            var endingInvestedCapital = 0.0

            assets.forEach { asset ->
                val assetTxUpTo = txUpToMonth.filter { it.assetId == asset.id }.sortedBy { it.date }
                var bQty = 0.0
                var bCost = 0.0
                var sQty = 0.0

                assetTxUpTo.forEach { t ->
                    if (t.type.equals("BUY", ignoreCase = true)) {
                        bQty += t.quantity
                        bCost += t.totalAmount
                    } else if (t.type.equals("SELL", ignoreCase = true)) {
                        sQty += t.quantity
                    }
                }

                val held = (bQty - sQty).coerceAtLeast(0.0)
                val avgBuy = if (bQty > 0.0) bCost / bQty else 0.0

                if (held > 0.0) {
                    val cost = held * avgBuy
                    endingInvestedCapital += cost

                    // Check closing price for this month
                    val monthPrice = pricesMap["${asset.id}_$m"]?.closingPrice
                    val fallbackPrice = if (monthPrice != null && monthPrice > 0.0) {
                        monthPrice
                    } else {
                        // Look for latest price recorded before or at month m
                        val previousPrices = monthlyPrices
                            .filter { it.assetId == asset.id && it.yearMonth <= m }
                            .sortedByDescending { it.yearMonth }
                        previousPrices.firstOrNull()?.closingPrice
                            ?: assetTxUpTo.lastOrNull()?.pricePerUnit
                            ?: avgBuy
                    }
                    endingPortfolioValue += (held * fallbackPrice)
                }
            }

            val totalPnL = endingPortfolioValue - endingInvestedCapital
            val totalPnLPercent = if (endingInvestedCapital > 0.0) {
                (totalPnL / endingInvestedCapital) * 100.0
            } else 0.0

            // MoM calculation
            val monthlyPnL: Double
            val monthlyPnLPercent: Double

            if (monthlyResults.isEmpty()) {
                // First month
                monthlyPnL = endingPortfolioValue - netInflowThisMonth
                monthlyPnLPercent = if (netInflowThisMonth > 0.0) {
                    (monthlyPnL / netInflowThisMonth) * 100.0
                } else if (endingInvestedCapital > 0.0) {
                    (monthlyPnL / endingInvestedCapital) * 100.0
                } else 0.0
            } else {
                // Subsequent months: MoM PnL = EndValue - StartValue - NetInflow
                monthlyPnL = endingPortfolioValue - previousMonthEndValue - netInflowThisMonth
                val returnBase = previousMonthEndValue + (netInflowThisMonth.coerceAtLeast(0.0))
                monthlyPnLPercent = if (returnBase > 0.0) {
                    (monthlyPnL / returnBase) * 100.0
                } else if (endingInvestedCapital > 0.0) {
                    (monthlyPnL / endingInvestedCapital) * 100.0
                } else 0.0
            }

            previousMonthEndValue = endingPortfolioValue

            monthlyResults.add(
                MonthlyPerformance(
                    yearMonth = m,
                    displayMonth = formatYearMonthDisplay(m),
                    endingPortfolioValue = endingPortfolioValue,
                    investedCapital = endingInvestedCapital,
                    cumulativeNetInflows = cumulativeInflows,
                    netInflowThisMonth = netInflowThisMonth,
                    monthlyPnL = monthlyPnL,
                    monthlyPnLPercent = monthlyPnLPercent,
                    totalPnL = totalPnL,
                    totalPnLPercent = totalPnLPercent,
                    isProfitable = monthlyPnL >= 0.0
                )
            )
        }

        return monthlyResults
    }

    /**
     * Calculates Year-over-Year performance history from monthly results.
     */
    fun calculateYearlyPerformance(
        monthlyPerformances: List<MonthlyPerformance>
    ): List<YearlyPerformance> {
        if (monthlyPerformances.isEmpty()) return emptyList()

        val byYear = monthlyPerformances.groupBy { it.yearMonth.substring(0, 4) }
        val sortedYears = byYear.keys.sorted()

        val yearlyResults = mutableListOf<YearlyPerformance>()
        var prevYearEndValue = 0.0

        for (year in sortedYears) {
            val monthsInYear = byYear[year] ?: emptyList()
            if (monthsInYear.isEmpty()) continue

            val yearEnd = monthsInYear.last()
            val totalInflowsInYear = monthsInYear.sumOf { it.netInflowThisMonth }

            val yearlyPnL: Double
            val yearlyPnLPercent: Double

            if (yearlyResults.isEmpty()) {
                // First year
                yearlyPnL = yearEnd.endingPortfolioValue - totalInflowsInYear
                val base = if (totalInflowsInYear > 0.0) totalInflowsInYear else yearEnd.investedCapital
                yearlyPnLPercent = if (base > 0.0) (yearlyPnL / base) * 100.0 else 0.0
            } else {
                yearlyPnL = yearEnd.endingPortfolioValue - prevYearEndValue - totalInflowsInYear
                val base = prevYearEndValue + totalInflowsInYear.coerceAtLeast(0.0)
                yearlyPnLPercent = if (base > 0.0) {
                    (yearlyPnL / base) * 100.0
                } else if (yearEnd.investedCapital > 0.0) {
                    (yearlyPnL / yearEnd.investedCapital) * 100.0
                } else 0.0
            }

            prevYearEndValue = yearEnd.endingPortfolioValue

            yearlyResults.add(
                YearlyPerformance(
                    year = year,
                    yearEndValue = yearEnd.endingPortfolioValue,
                    investedCapital = yearEnd.investedCapital,
                    yearlyPnL = yearlyPnL,
                    yearlyPnLPercent = yearlyPnLPercent,
                    totalPnL = yearEnd.totalPnL,
                    totalPnLPercent = yearEnd.totalPnLPercent,
                    isProfitable = yearlyPnL >= 0.0
                )
            )
        }

        return yearlyResults
    }

    /**
     * Generates portfolio summary statistics.
     */
    fun calculateSummary(
        holdings: List<HoldingsItem>,
        monthlyPerformances: List<MonthlyPerformance>,
        yearlyPerformances: List<YearlyPerformance>,
        transactionCount: Int
    ): PortfolioSummary {
        val totalCurrentValue = holdings.sumOf { it.currentValue }
        val totalInvestedCapital = holdings.sumOf { it.costBasis }
        val totalPnL = totalCurrentValue - totalInvestedCapital
        val totalPnLPercent = if (totalInvestedCapital > 0.0) {
            (totalPnL / totalInvestedCapital) * 100.0
        } else 0.0

        val totalRealizedPnL = holdings.sumOf { it.realizedPnL }

        val activeHoldings = holdings.filter { it.unitsHeld > 0.0 }
        val bestAsset = activeHoldings.maxByOrNull { it.unrealizedPnLPercent }
        val worstAsset = activeHoldings.minByOrNull { it.unrealizedPnLPercent }

        val latestMonthly = monthlyPerformances.lastOrNull()
        val latestYearly = yearlyPerformances.lastOrNull()

        return PortfolioSummary(
            totalCurrentValue = totalCurrentValue,
            totalInvestedCapital = totalInvestedCapital,
            totalPnL = totalPnL,
            totalPnLPercent = totalPnLPercent,
            totalRealizedPnL = totalRealizedPnL,
            bestAsset = bestAsset,
            worstAsset = worstAsset,
            latestMonthlyPnL = latestMonthly?.monthlyPnL ?: 0.0,
            latestMonthlyPnLPercent = latestMonthly?.monthlyPnLPercent ?: 0.0,
            currentYearPnL = latestYearly?.yearlyPnL ?: 0.0,
            currentYearPnLPercent = latestYearly?.yearlyPnLPercent ?: 0.0,
            assetCount = activeHoldings.size,
            transactionCount = transactionCount,
            lastValuationMonth = latestMonthly?.displayMonth ?: ""
        )
    }

    fun round(value: Double, decimals: Int = 2): Double {
        var multiplier = 1.0
        repeat(decimals) { multiplier *= 10 }
        return (value * multiplier).roundToInt() / multiplier
    }
}
