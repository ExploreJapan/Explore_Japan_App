package com.example.explorejapanapp

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface AboutDao {
    @Insert
    suspend fun insert(about: About)

    @Query("SELECT * FROM about LIMIT 1")
    suspend fun getAbout(): About?

    @Query("DELETE FROM about")
    suspend fun deleteAll()
}