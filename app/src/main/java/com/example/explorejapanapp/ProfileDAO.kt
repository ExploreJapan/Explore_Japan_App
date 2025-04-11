package com.example.explorejapanapp

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface ProfileDao {
    @Insert
    suspend fun insert(profile: Profile)

    @Query("SELECT * FROM profile LIMIT 1")
    suspend fun getProfile(): Profile?

    @Query("DELETE FROM profile")
    suspend fun deleteAll()

    @Query("SELECT * FROM profile WHERE (username = :usernameOrEmail OR email = :usernameOrEmail) AND password = :password LIMIT 1")
    suspend fun login(usernameOrEmail: String, password: String): Profile?

    @Query("SELECT * FROM profile WHERE username = :username OR email = :email LIMIT 1")
    suspend fun findByUsernameOrEmail(username: String, email: String): Profile?
}