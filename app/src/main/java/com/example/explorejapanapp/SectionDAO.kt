package com.example.explorejapanapp

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface SectionDao {
    @Insert
    suspend fun insert(section: Section)

    @Query("SELECT * FROM sections")
    suspend fun getAllSections(): List<Section>

    @Query("DELETE FROM sections")
    suspend fun deleteAll()
}