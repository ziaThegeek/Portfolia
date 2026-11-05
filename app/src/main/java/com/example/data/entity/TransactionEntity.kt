package com.example.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "transactions",
    foreignKeys = [
        ForeignKey(
            entity = AssetEntity::class,
            parentColumns = ["id"],
            childColumns = ["assetId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["assetId"]), Index(value = ["date"])]
)
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val assetId: Long,
    val type: String, // "BUY" or "SELL"
    val date: String, // Format: "YYYY-MM-DD" e.g. "2026-03-15"
    val quantity: Double,
    val pricePerUnit: Double,
    val fees: Double = 0.0,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
) {
    val totalAmount: Double
        get() = if (type == "BUY") {
            (quantity * pricePerUnit) + fees
        } else {
            (quantity * pricePerUnit) - fees
        }
}
