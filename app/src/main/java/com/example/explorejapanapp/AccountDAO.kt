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
}