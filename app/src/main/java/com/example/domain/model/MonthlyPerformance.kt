package com.example.domain.model

data class MonthlyPerformance(
    val yearMonth: String, // e.g. "2026-03"
    val displayMonth: String, // e.g. "Mar 2026"
    val endingPortfolioValue: Double,
    val investedCapital: Double,
    val cumulativeNetInflows: Double,
    val netInflowThisMonth: Double,
    val monthlyPnL: Double,
    val monthlyPnLPercent: Double,
    val totalPnL: Double,
    val totalPnLPercent: Double,
    val isProfitable: Boolean = monthlyPnL >= 0.0
)
