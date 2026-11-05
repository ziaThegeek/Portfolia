package com.example.domain.model

data class PortfolioSummary(
    val totalCurrentValue: Double = 0.0,
    val totalInvestedCapital: Double = 0.0,
    val totalPnL: Double = 0.0,
    val totalPnLPercent: Double = 0.0,
    val totalRealizedPnL: Double = 0.0,
    val bestAsset: HoldingsItem? = null,
    val worstAsset: HoldingsItem? = null,
    val latestMonthlyPnL: Double = 0.0,
    val latestMonthlyPnLPercent: Double = 0.0,
    val currentYearPnL: Double = 0.0,
    val currentYearPnLPercent: Double = 0.0,
    val assetCount: Int = 0,
    val transactionCount: Int = 0,
    val lastValuationMonth: String = ""
)
