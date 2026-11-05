package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.dao.AssetDao
import com.example.data.dao.MonthlyPriceDao
import com.example.data.dao.TransactionDao
import com.example.data.entity.AssetEntity
import com.example.data.entity.MonthlyPriceEntity
import com.example.data.entity.TransactionEntity

@Database(
    entities = [
        AssetEntity::class,
        TransactionEntity::class,
        MonthlyPriceEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun assetDao(): AssetDao
    abstract fun transactionDao(): TransactionDao
    abstract fun monthlyPriceDao(): MonthlyPriceDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "portfolia_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
