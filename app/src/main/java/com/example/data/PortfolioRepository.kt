package com.example.data

import com.example.data.dao.AssetDao
import com.example.data.dao.MonthlyPriceDao
import com.example.data.dao.TransactionDao
import com.example.data.entity.AssetEntity
import com.example.data.entity.MonthlyPriceEntity
import com.example.data.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow

class PortfolioRepository(
    private val assetDao: AssetDao,
    private val transactionDao: TransactionDao,
    private val monthlyPriceDao: MonthlyPriceDao
) {
    val allAssets: Flow<List<AssetEntity>> = assetDao.getAllAssets()
    val allTransactions: Flow<List<TransactionEntity>> = transactionDao.getAllTransactions()
    val allMonthlyPrices: Flow<List<MonthlyPriceEntity>> = monthlyPriceDao.getAllMonthlyPrices()

    suspend fun insertAsset(asset: AssetEntity): Long = assetDao.insertAsset(asset)
    suspend fun updateAsset(asset: AssetEntity) = assetDao.updateAsset(asset)
    suspend fun deleteAsset(asset: AssetEntity) = assetDao.deleteAsset(asset)
    suspend fun deleteAssetById(id: Long) = assetDao.deleteAssetById(id)

    suspend fun insertTransaction(transaction: TransactionEntity): Long =
        transactionDao.insertTransaction(transaction)

    suspend fun updateTransaction(transaction: TransactionEntity) =
        transactionDao.updateTransaction(transaction)

    suspend fun deleteTransaction(transaction: TransactionEntity) =
        transactionDao.deleteTransaction(transaction)

    suspend fun deleteTransactionById(id: Long) =
        transactionDao.deleteTransactionById(id)

    suspend fun setMonthlyPrice(assetId: Long, yearMonth: String, closingPrice: Double): Long {
        val entity = MonthlyPriceEntity(
            assetId = assetId,
            yearMonth = yearMonth,
            closingPrice = closingPrice,
            updatedAt = System.currentTimeMillis()
        )
        return monthlyPriceDao.insertOrUpdatePrice(entity)
    }

    suspend fun setMonthlyPricesBatch(prices: List<MonthlyPriceEntity>) {
        monthlyPriceDao.insertOrUpdatePrices(prices)
    }

    suspend fun deleteMonthlyPriceById(id: Long) =
        monthlyPriceDao.deletePriceById(id)

    suspend fun clearAllData() {
        monthlyPriceDao.deleteAllPrices()
        transactionDao.deleteAllTransactions()
        assetDao.deleteAllAssets()
    }

    /**
     * Seeds realistic investment portfolio data across 2025 and 2026.
     */
    suspend fun seedSamplePortfolio() {
        clearAllData()

        val apple = AssetEntity(symbol = "AAPL", name = "Apple Inc.", category = "Stock", colorHex = "#38BDF8")
        val nvidia = AssetEntity(symbol = "NVDA", name = "Nvidia Corp.", category = "Stock", colorHex = "#22C55E")
        val voo = AssetEntity(symbol = "VOO", name = "Vanguard S&P 500 ETF", category = "ETF", colorHex = "#A855F7")
        val btc = AssetEntity(symbol = "BTC", name = "Bitcoin", category = "Crypto", colorHex = "#F59E0B")

        val appleId = assetDao.insertAsset(apple)
        val nvidiaId = assetDao.insertAsset(nvidia)
        val vooId = assetDao.insertAsset(voo)
        val btcId = assetDao.insertAsset(btc)

        // Transactions: Purchases made across 2025 and 2026
        val txs = listOf(
            // 2025 Q1
            TransactionEntity(assetId = vooId, type = "BUY", date = "2025-01-15", quantity = 10.0, pricePerUnit = 440.0, fees = 0.0, notes = "Initial index investment"),
            TransactionEntity(assetId = appleId, type = "BUY", date = "2025-01-20", quantity = 25.0, pricePerUnit = 180.0, fees = 1.5, notes = "Core tech holding"),
            TransactionEntity(assetId = btcId, type = "BUY", date = "2025-02-10", quantity = 0.25, pricePerUnit = 52000.0, fees = 12.0, notes = "DCA into Bitcoin"),
            // 2025 Q2
            TransactionEntity(assetId = nvidiaId, type = "BUY", date = "2025-04-12", quantity = 30.0, pricePerUnit = 85.0, fees = 2.0, notes = "AI computing growth"),
            TransactionEntity(assetId = vooId, type = "BUY", date = "2025-05-18", quantity = 5.0, pricePerUnit = 460.0, fees = 0.0, notes = "Monthly DCA"),
            // 2025 Q3
            TransactionEntity(assetId = appleId, type = "BUY", date = "2025-07-22", quantity = 15.0, pricePerUnit = 210.0, fees = 1.0, notes = "Dip add"),
            TransactionEntity(assetId = btcId, type = "BUY", date = "2025-08-14", quantity = 0.15, pricePerUnit = 59000.0, fees = 8.0, notes = "Summer accumulation"),
            // 2025 Q4 - Small profit take on Apple
            TransactionEntity(assetId = appleId, type = "SELL", date = "2025-10-05", quantity = 10.0, pricePerUnit = 230.0, fees = 2.0, notes = "Rebalancing tech trim"),
            TransactionEntity(assetId = nvidiaId, type = "BUY", date = "2025-11-19", quantity = 20.0, pricePerUnit = 110.0, fees = 1.5, notes = "Earnings follow-up"),
            // 2026 Q1
            TransactionEntity(assetId = vooId, type = "BUY", date = "2026-01-10", quantity = 8.0, pricePerUnit = 490.0, fees = 0.0, notes = "New year bonus allocation"),
            TransactionEntity(assetId = btcId, type = "BUY", date = "2026-02-15", quantity = 0.10, pricePerUnit = 68000.0, fees = 10.0, notes = "Pre-halving follow-through"),
            TransactionEntity(assetId = nvidiaId, type = "BUY", date = "2026-03-20", quantity = 15.0, pricePerUnit = 122.0, fees = 1.0, notes = "Data center strength")
        )
        transactionDao.insertTransactions(txs)

        // Monthly closing prices for each month
        val months = listOf(
            "2025-01", "2025-02", "2025-03", "2025-04", "2025-05", "2025-06",
            "2025-07", "2025-08", "2025-09", "2025-10", "2025-11", "2025-12",
            "2026-01", "2026-02", "2026-03"
        )

        val priceData = listOf(
            // Month, AAPL, NVDA, VOO, BTC
            Triple("2025-01", listOf(185.0, 78.0, 445.0, 54000.0), 0),
            Triple("2025-02", listOf(182.0, 82.0, 452.0, 58000.0), 0),
            Triple("2025-03", listOf(178.0, 88.0, 458.0, 64000.0), 0),
            Triple("2025-04", listOf(186.0, 92.0, 464.0, 61000.0), 0),
            Triple("2025-05", listOf(198.0, 105.0, 472.0, 65000.0), 0),
            Triple("2025-06", listOf(210.0, 118.0, 480.0, 63000.0), 0),
            Triple("2025-07", listOf(218.0, 114.0, 490.0, 66000.0), 0),
            Triple("2025-08", listOf(222.0, 120.0, 498.0, 62000.0), 0),
            Triple("2025-09", listOf(225.0, 124.0, 505.0, 63500.0), 0),
            Triple("2025-10", listOf(231.0, 132.0, 510.0, 69000.0), 0),
            Triple("2025-11", listOf(238.0, 140.0, 522.0, 75000.0), 0),
            Triple("2025-12", listOf(242.0, 144.0, 528.0, 78000.0), 0),
            Triple("2026-01", listOf(236.0, 138.0, 524.0, 74000.0), 0),
            Triple("2026-02", listOf(245.0, 146.0, 535.0, 82000.0), 0),
            Triple("2026-03", listOf(252.0, 155.0, 545.0, 86000.0), 0)
        )

        val monthlyEntities = mutableListOf<MonthlyPriceEntity>()
        priceData.forEach { (month, prices, _) ->
            monthlyEntities.add(MonthlyPriceEntity(assetId = appleId, yearMonth = month, closingPrice = prices[0]))
            monthlyEntities.add(MonthlyPriceEntity(assetId = nvidiaId, yearMonth = month, closingPrice = prices[1]))
            monthlyEntities.add(MonthlyPriceEntity(assetId = vooId, yearMonth = month, closingPrice = prices[2]))
            monthlyEntities.add(MonthlyPriceEntity(assetId = btcId, yearMonth = month, closingPrice = prices[3]))
        }
        monthlyPriceDao.insertOrUpdatePrices(monthlyEntities)
    }
}
