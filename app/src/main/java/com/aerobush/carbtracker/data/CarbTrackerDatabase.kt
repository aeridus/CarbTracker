package com.aerobush.carbtracker.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [CarbTimeItem::class], version = 2, exportSchema = false)
abstract class CarbTrackerDatabase : RoomDatabase() {
    abstract fun carbTimeItemDao(): CarbTimeItemDao

    companion object {
        @Volatile
        private var Instance: CarbTrackerDatabase? = null

        fun getDatabase(context: Context): CarbTrackerDatabase {
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(context, CarbTrackerDatabase::class.java, "carb_tracker_database")
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { Instance = it }
            }
        }
    }
}