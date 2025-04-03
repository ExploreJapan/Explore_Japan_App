package com.example.explorejapanapp

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface HomeDao {
    @Insert
    suspend fun insert(content: HomeContent)

    @Query("SELECT * FROM home_content LIMIT 1")
    suspend fun getHomeContent(): HomeContent?

    @Query("DELETE FROM home_content")
    suspend fun deleteAll()
}