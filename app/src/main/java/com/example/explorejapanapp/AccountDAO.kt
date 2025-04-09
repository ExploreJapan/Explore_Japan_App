package com.example.explorejapanapp

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface AccountDao {
    @Insert
    suspend fun insert(account: Account)

    @Query("SELECT * FROM account LIMIT 1")
    suspend fun getAccount(): Account?

    @Query("DELETE FROM account")
    suspend fun deleteAll()

    @Query("SELECT * FROM account WHERE (username = :usernameOrEmail OR email = :usernameOrEmail) AND password = :password LIMIT 1")
    suspend fun login(usernameOrEmail: String, password: String): Account?

    @Query("SELECT * FROM account WHERE username = :username OR email = :email LIMIT 1")
    suspend fun findByUsernameOrEmail(username: String, email: String): Account?
}