package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "assets")
data class AssetEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val symbol: String, // e.g. "AAPL", "BTC", "VOO"
    val name: String, // e.g. "Apple Inc.", "Bitcoin"
    val category: String = "Stock", // "Stock", "Crypto", "ETF", "Commodity", "Real Estate", "Other"
    val colorHex: String = "#38BDF8", // For charts and visual tags
    val createdAt: Long = System.currentTimeMillis()
)
