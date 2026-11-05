package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.PortfolioRepository
import com.example.data.entity.AssetEntity
import com.example.data.entity.MonthlyPriceEntity
import com.example.data.entity.TransactionEntity
import com.example.domain.PortfolioCalculator
import com.example.domain.model.HoldingsItem
import com.example.domain.model.MonthlyPerformance
import com.example.domain.model.PortfolioSummary
import com.example.domain.model.YearlyPerformance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed class ActiveDialog {
    data object None : ActiveDialog()
    data class AddTransaction(val preselectedAssetId: Long? = null) : ActiveDialog()
    data object AddAsset : ActiveDialog()
    data object UpdateMonthlyPrice : ActiveDialog()
}

data class PortfolioUiState(
    val assets: List<AssetEntity> = emptyList(),
    val transactions: List<TransactionEntity> = emptyList(),
    val monthlyPrices: List<MonthlyPriceEntity> = emptyList(),
    val holdings: List<HoldingsItem> = emptyList(),
    val monthlyPerformances: List<MonthlyPerformance> = emptyList(),
    val yearlyPerformances: List<YearlyPerformance> = emptyList(),
    val summary: PortfolioSummary = PortfolioSummary(),
    val selectedTimeframe: String = "All", // "All", "1Y", "6M", "YTD"
    val selectedChartType: String = "VALUE", // "VALUE", "MOM_PNL", "ALLOCATION"
    val isLoading: Boolean = false,
    val userMessage: String? = null
)

class PortfolioViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: PortfolioRepository

    init {
        val database = AppDatabase.getDatabase(application)
        repository = PortfolioRepository(
            assetDao = database.assetDao(),
            transactionDao = database.transactionDao(),
            monthlyPriceDao = database.monthlyPriceDao()
        )
    }

    private val _selectedTimeframe = MutableStateFlow("All")
    private val _selectedChartType = MutableStateFlow("VALUE")
    private val _userMessage = MutableStateFlow<String?>(null)

    // Dedicated state flow for dialogs ensures immediate reactivity
    private val _activeDialog = MutableStateFlow<ActiveDialog>(ActiveDialog.None)
    val activeDialog: StateFlow<ActiveDialog> = _activeDialog.asStateFlow()

    val uiState: StateFlow<PortfolioUiState> = combine(
        repository.allAssets,
        repository.allTransactions,
        repository.allMonthlyPrices,
        _selectedTimeframe,
        _selectedChartType
    ) { assets, transactions, prices, timeframe, chartType ->
        val holdings = PortfolioCalculator.calculateHoldings(assets, transactions, prices)
        val allMonthly = PortfolioCalculator.calculateMonthlyPerformance(assets, transactions, prices)
        val allYearly = PortfolioCalculator.calculateYearlyPerformance(allMonthly)

        val filteredMonthly = when (timeframe) {
            "6M" -> allMonthly.takeLast(6)
            "1Y" -> allMonthly.takeLast(12)
            "YTD" -> {
                val currentYear = if (allMonthly.isNotEmpty()) allMonthly.last().yearMonth.substring(0, 4) else "2026"
                allMonthly.filter { it.yearMonth.startsWith(currentYear) }
            }
            else -> allMonthly
        }

        val summary = PortfolioCalculator.calculateSummary(
            holdings = holdings,
            monthlyPerformances = allMonthly,
            yearlyPerformances = allYearly,
            transactionCount = transactions.size
        )

        PortfolioUiState(
            assets = assets,
            transactions = transactions,
            monthlyPrices = prices,
            holdings = holdings,
            monthlyPerformances = filteredMonthly,
            yearlyPerformances = allYearly,
            summary = summary,
            selectedTimeframe = timeframe,
            selectedChartType = chartType,
            userMessage = _userMessage.value
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = PortfolioUiState(isLoading = true)
    )

    fun setTimeframe(timeframe: String) {
        _selectedTimeframe.value = timeframe
    }

    fun setChartType(chartType: String) {
        _selectedChartType.value = chartType
    }

    fun openAddTransactionDialog(assetId: Long? = null) {
        _activeDialog.value = ActiveDialog.AddTransaction(assetId)
    }

    fun openAddAssetDialog() {
        _activeDialog.value = ActiveDialog.AddAsset
    }

    fun openUpdatePriceDialog() {
        _activeDialog.value = ActiveDialog.UpdateMonthlyPrice
    }

    fun closeDialog() {
        _activeDialog.value = ActiveDialog.None
    }

    fun clearUserMessage() {
        _userMessage.value = null
    }

    fun addAsset(asset: AssetEntity) {
        viewModelScope.launch {
            repository.insertAsset(asset)
            _activeDialog.value = ActiveDialog.None
            _userMessage.value = "Asset ${asset.symbol} added successfully"
        }
    }

    fun addTransaction(transaction: TransactionEntity) {
        viewModelScope.launch {
            repository.insertTransaction(transaction)
            _activeDialog.value = ActiveDialog.None
            _userMessage.value = "Transaction recorded"
        }
    }

    fun addTransactionWithNewAsset(asset: AssetEntity, transactionBuilder: (Long) -> TransactionEntity) {
        viewModelScope.launch {
            val assetId = repository.insertAsset(asset)
            repository.insertTransaction(transactionBuilder(assetId))
            _activeDialog.value = ActiveDialog.None
            _userMessage.value = "Asset ${asset.symbol} created and trade recorded"
        }
    }

    fun deleteTransaction(transaction: TransactionEntity) {
        viewModelScope.launch {
            repository.deleteTransaction(transaction)
            _userMessage.value = "Transaction deleted"
        }
    }

    fun saveMonthlyPrices(yearMonth: String, prices: Map<Long, Double>) {
        viewModelScope.launch {
            val entities = prices.map { (assetId, price) ->
                MonthlyPriceEntity(
                    assetId = assetId,
                    yearMonth = yearMonth,
                    closingPrice = price,
                    updatedAt = System.currentTimeMillis()
                )
            }
            repository.setMonthlyPricesBatch(entities)
            _activeDialog.value = ActiveDialog.None
            _userMessage.value = "Prices updated for $yearMonth"
        }
    }

    fun seedSampleData() {
        viewModelScope.launch {
            repository.seedSamplePortfolio()
            _activeDialog.value = ActiveDialog.None
            _userMessage.value = "Sample portfolio loaded with historical months & trades"
        }
    }

    fun clearAllData() {
        viewModelScope.launch {
            repository.clearAllData()
            _activeDialog.value = ActiveDialog.None
            _userMessage.value = "Portfolio cleared"
        }
    }
}
