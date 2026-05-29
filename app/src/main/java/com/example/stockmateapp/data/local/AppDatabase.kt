package com.example.stockmateapp.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.stockmateapp.data.local.dao.ProductDao
import com.example.stockmateapp.data.local.entities.CachedProduct

@Database(
    entities = [CachedProduct::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun productDao(): ProductDao

    companion object {
        fun create(context: Context): AppDatabase {
            return Room.databaseBuilder(context, AppDatabase::class.java, "stockmate.db")
                .fallbackToDestructiveMigration(false)
                .build()
        }
    }
}
