package com.example.domain.model

import com.example.data.entity.AssetEntity

data class HoldingsItem(
    val asset: AssetEntity,
    val unitsHeld: Double,
    val totalBoughtQty: Double,
    val totalBoughtCost: Double,
    val totalSoldQty: Double,
    val totalSoldProceeds: Double,
    val avgBuyPrice: Double,
    val costBasis: Double, // unitsHeld * avgBuyPrice
    val currentPrice: Double,
    val currentValue: Double, // unitsHeld * currentPrice
    val unrealizedPnL: Double, // currentValue - costBasis
    val unrealizedPnLPercent: Double,
    val realizedPnL: Double,
    val allocationPercent: Double = 0.0
)
