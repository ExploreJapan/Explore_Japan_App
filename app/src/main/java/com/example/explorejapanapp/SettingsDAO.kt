package com.example.explorejapanapp

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface SettingsDao {
    @Insert
    suspend fun insert(about: Settings)

    @Query("SELECT * FROM settings LIMIT 1")
    suspend fun getAbout(): Settings?

    @Query("DELETE FROM settings")
    suspend fun deleteAll()
}