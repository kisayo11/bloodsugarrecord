package com.kisayo.bloodsugarrecord.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.kisayo.bloodsugarrecord.data.dao.DailyRecordDao
import com.kisayo.bloodsugarrecord.data.model.DailyRecord

@Database(
    entities = [DailyRecord::class],
    version = 4
)
abstract class GlucoseDatabase : RoomDatabase() {
    abstract fun dailyRecordDao(): DailyRecordDao

    companion object {
        @Volatile
        private var INSTANCE: GlucoseDatabase? = null

        fun getDatabase(context: Context): GlucoseDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    GlucoseDatabase::class.java,
                    "glucose_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}