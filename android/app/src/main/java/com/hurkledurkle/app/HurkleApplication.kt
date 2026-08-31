package com.hurkledurkle.app

import android.app.Application
import com.hurkledurkle.app.data.local.db.HurkleDatabase
import com.hurkledurkle.app.data.preferences.UserPreferences
import com.hurkledurkle.app.data.repository.SleepRepository

class HurkleApplication : Application() {
    val database by lazy { HurkleDatabase.getInstance(this) }
    val repository by lazy {
        SleepRepository(database.sleepSessionDao(), database.sleepEventDao())
    }
    val userPreferences by lazy { UserPreferences(this) }
}
