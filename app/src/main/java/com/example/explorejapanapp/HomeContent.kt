package com.example.explorejapanapp

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "home_content")
data class HomeContent(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val description: String
)