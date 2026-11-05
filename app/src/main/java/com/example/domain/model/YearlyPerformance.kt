package com.example.domain.model

data class YearlyPerformance(
    val year: String, // e.g. "2025", "2026"
    val yearEndValue: Double,
    val investedCapital: Double,
    val yearlyPnL: Double,
    val yearlyPnLPercent: Double,
    val totalPnL: Double,
    val totalPnLPercent: Double,
    val isProfitable: Boolean = yearlyPnL >= 0.0
)
