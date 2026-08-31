package com.hurkledurkle.app.data.local.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.hurkledurkle.app.data.local.dao.SleepEventDao
import com.hurkledurkle.app.data.local.dao.SleepSessionDao
import com.hurkledurkle.app.data.local.entity.SleepEventEntity
import com.hurkledurkle.app.data.local.entity.SleepSessionEntity

@Database(
    entities = [SleepSessionEntity::class, SleepEventEntity::class],
    version = 1,
    exportSchema = false
)
abstract class HurkleDatabase : RoomDatabase() {
    abstract fun sleepSessionDao(): SleepSessionDao
    abstract fun sleepEventDao(): SleepEventDao

    companion object {
        @Volatile
        private var INSTANCE: HurkleDatabase? = null

        fun getInstance(context: Context): HurkleDatabase =
            INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    HurkleDatabase::class.java,
                    "hurkledurkle.db"
                ).build().also { INSTANCE = it }
            }
    }
}
