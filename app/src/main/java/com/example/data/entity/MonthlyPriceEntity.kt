package com.example.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "monthly_prices",
    foreignKeys = [
        ForeignKey(
            entity = AssetEntity::class,
            parentColumns = ["id"],
            childColumns = ["assetId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["assetId", "yearMonth"], unique = true),
        Index(value = ["yearMonth"])
    ]
)
data class MonthlyPriceEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val assetId: Long,
    val yearMonth: String, // Format: "YYYY-MM" e.g. "2026-01", "2026-02"
    val closingPrice: Double,
    val updatedAt: Long = System.currentTimeMillis()
)
