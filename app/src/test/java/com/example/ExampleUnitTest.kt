package com.example

import com.example.data.entity.AssetEntity
import com.example.data.entity.MonthlyPriceEntity
import com.example.data.entity.TransactionEntity
import com.example.domain.PortfolioCalculator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun testPortfolioHoldingsAndPnLCalculation() {
        val asset = AssetEntity(id = 1, symbol = "AAPL", name = "Apple Inc.")
        val transactions = listOf(
            TransactionEntity(id = 1, assetId = 1, type = "BUY", date = "2026-01-10", quantity = 10.0, pricePerUnit = 150.0, fees = 5.0),
            TransactionEntity(id = 2, assetId = 1, type = "BUY", date = "2026-02-15", quantity = 10.0, pricePerUnit = 170.0, fees = 5.0),
            TransactionEntity(id = 3, assetId = 1, type = "SELL", date = "2026-03-01", quantity = 5.0, pricePerUnit = 200.0, fees = 5.0)
        )
        val prices = listOf(
            MonthlyPriceEntity(id = 1, assetId = 1, yearMonth = "2026-03", closingPrice = 210.0)
        )

        val holdings = PortfolioCalculator.calculateHoldings(listOf(asset), transactions, prices)
        assertEquals(1, holdings.size)
        val aaplHolding = holdings[0]

        // 20 bought - 5 sold = 15 held
        assertEquals(15.0, aaplHolding.unitsHeld, 0.001)

        // Total buy cost: (10*150+5) + (10*170+5) = 1505 + 1705 = 3210
        // Avg buy price: 3210 / 20 = 160.5
        assertEquals(160.5, aaplHolding.avgBuyPrice, 0.001)

        // Cost basis: 15 * 160.5 = 2407.5
        assertEquals(2407.5, aaplHolding.costBasis, 0.001)

        // Current value: 15 * 210 = 3150.0
        assertEquals(3150.0, aaplHolding.currentValue, 0.001)

        // Unrealized PnL: 3150 - 2407.5 = 742.5
        assertEquals(742.5, aaplHolding.unrealizedPnL, 0.001)
        assertTrue(aaplHolding.unrealizedPnLPercent > 30.0)
    }

    @Test
    fun testMonthlyAndYearlyPerformanceCalculation() {
        val asset = AssetEntity(id = 1, symbol = "VOO", name = "S&P 500 ETF")
        val transactions = listOf(
            TransactionEntity(id = 1, assetId = 1, type = "BUY", date = "2025-01-05", quantity = 10.0, pricePerUnit = 400.0),
            TransactionEntity(id = 2, assetId = 1, type = "BUY", date = "2026-01-05", quantity = 5.0, pricePerUnit = 450.0)
        )
        val prices = listOf(
            MonthlyPriceEntity(id = 1, assetId = 1, yearMonth = "2025-01", closingPrice = 410.0),
            MonthlyPriceEntity(id = 2, assetId = 1, yearMonth = "2025-02", closingPrice = 420.0),
            MonthlyPriceEntity(id = 3, assetId = 1, yearMonth = "2025-12", closingPrice = 450.0),
            MonthlyPriceEntity(id = 4, assetId = 1, yearMonth = "2026-01", closingPrice = 460.0)
        )

        val monthly = PortfolioCalculator.calculateMonthlyPerformance(listOf(asset), transactions, prices)
        assertTrue(monthly.isNotEmpty())

        val yearly = PortfolioCalculator.calculateYearlyPerformance(monthly)
        assertEquals(2, yearly.size)
        assertEquals("2025", yearly[0].year)
        assertEquals("2026", yearly[1].year)
        assertTrue(yearly[0].yearlyPnL > 0)
    }
}
