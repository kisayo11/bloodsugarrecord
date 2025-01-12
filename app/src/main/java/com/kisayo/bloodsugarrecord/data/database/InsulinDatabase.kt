package com.kisayo.bloodsugarrecord.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.kisayo.bloodsugarrecord.data.dao.InsulinRecordDao
import com.kisayo.bloodsugarrecord.data.model.InsulinInjection
import com.kisayo.bloodsugarrecord.data.model.InsulinStock

@Database(
    entities = [
        InsulinStock::class,
        InsulinInjection::class
    ],
    version = 1
)
abstract class InsulinDatabase : RoomDatabase(){
  abstract fun insulinRecordDao() : InsulinRecordDao

    companion object{
        @Volatile
        private var INSTANCE: InsulinDatabase? = null

        fun getDatabase(context: Context): InsulinDatabase{
            return INSTANCE ?: synchronized(this){
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    InsulinDatabase::class.java,
                    "insulin_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}