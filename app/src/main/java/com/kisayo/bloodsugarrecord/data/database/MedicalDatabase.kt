package com.kisayo.bloodsugarrecord.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.kisayo.bloodsugarrecord.data.dao.MedicalRecordDao
import com.kisayo.bloodsugarrecord.data.model.MedicalRecord

@Database(entities = [MedicalRecord::class], version = 1)
abstract class MedicalDatabase : RoomDatabase() {
    abstract fun medicalRecordDao(): MedicalRecordDao

    companion object {
        @Volatile
        private var INSTANCE: MedicalDatabase? = null

        fun getDatabase(context: Context): MedicalDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MedicalDatabase::class.java,
                    "medical_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}