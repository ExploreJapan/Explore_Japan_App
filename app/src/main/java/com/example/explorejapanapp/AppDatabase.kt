package com.example.explorejapanapp

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [HomeContent::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun homeDao(): HomeDao
}