package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.entity.MonthlyPriceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MonthlyPriceDao {
    @Query("SELECT * FROM monthly_prices ORDER BY yearMonth ASC")
    fun getAllMonthlyPrices(): Flow<List<MonthlyPriceEntity>>

    @Query("SELECT * FROM monthly_prices WHERE assetId = :assetId ORDER BY yearMonth ASC")
    fun getPricesForAsset(assetId: Long): Flow<List<MonthlyPriceEntity>>

    @Query("SELECT * FROM monthly_prices WHERE yearMonth = :yearMonth")
    fun getPricesForMonth(yearMonth: String): Flow<List<MonthlyPriceEntity>>

    @Query("SELECT * FROM monthly_prices WHERE assetId = :assetId AND yearMonth = :yearMonth LIMIT 1")
    suspend fun getPrice(assetId: Long, yearMonth: String): MonthlyPriceEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdatePrice(price: MonthlyPriceEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdatePrices(prices: List<MonthlyPriceEntity>): List<Long>

    @Delete
    suspend fun deletePrice(price: MonthlyPriceEntity)

    @Query("DELETE FROM monthly_prices WHERE id = :id")
    suspend fun deletePriceById(id: Long)

    @Query("DELETE FROM monthly_prices")
    suspend fun deleteAllPrices()
}
