package com.example.moneytracker

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface UserDAO {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun saveUser(user:UserEntity)

    @Query("UPDATE Users SET Password = :password WHERE Username = :name")
    fun updatePassword(name:String, password:String)

    @Query("SELECT Distinct user_id FROM Users WHERE Username = :name AND Password = :password")
    fun getId(name:String, password:String): Int

    @Query("DELETE FROM Users")
    fun deleteAll()
}