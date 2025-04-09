package com.example.explorejapanapp

import android.content.Context
import androidx.room.Room

object DatabaseProvider {
    private var database: AppDatabase? = null

    fun getDatabase(context: Context): AppDatabase {
        if (database == null || database?.isOpen == false) {
            database = Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "explore_japan_db"
            )
                .fallbackToDestructiveMigration()
                .build()
        }
        return database!!
    }
}