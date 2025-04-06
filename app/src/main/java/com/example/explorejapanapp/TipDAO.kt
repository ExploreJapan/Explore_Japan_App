package com.example.explorejapanapp

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface TipDao {
    @Insert
    suspend fun insert(tip: Tip)

    @Query("SELECT * FROM tips")
    suspend fun getAllTips(): List<Tip>

    @Query("DELETE FROM tips")
    suspend fun deleteAll()
}