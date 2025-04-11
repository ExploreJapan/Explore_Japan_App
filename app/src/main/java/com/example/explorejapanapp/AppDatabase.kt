package com.example.explorejapanapp

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [HomeContent::class, Profile::class], version = 2, exportSchema = true)
abstract class AppDatabase : RoomDatabase() {
    abstract fun homeDao(): HomeDao
    abstract fun profileDao(): ProfileDao
}