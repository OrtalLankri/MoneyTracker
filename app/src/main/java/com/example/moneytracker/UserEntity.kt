package com.example.moneytracker

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "Users")

class UserEntity {

    @PrimaryKey(autoGenerate = true)
    var user_id:Int = 0

    @ColumnInfo (name="Username")
    var user_name:String=""

    @ColumnInfo (name="Password")
    var password:String=""
}