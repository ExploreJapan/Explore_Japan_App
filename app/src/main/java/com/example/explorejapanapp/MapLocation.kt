package com.example.explorejapanapp

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "map_locations")
data class MapLocation(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val description: String? = null
)