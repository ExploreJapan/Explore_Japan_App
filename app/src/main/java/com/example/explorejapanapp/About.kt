package com.example.explorejapanapp

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "about")
data class About(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val description: String
)