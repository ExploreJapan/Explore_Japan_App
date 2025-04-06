package com.example.explorejapanapp

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface MapDao {
    @Insert
    suspend fun insert(location: MapLocation)

    @Query("SELECT * FROM map_locations")
    suspend fun getAllLocations(): List<MapLocation>

    @Query("DELETE FROM map_locations")
    suspend fun deleteAll()
}