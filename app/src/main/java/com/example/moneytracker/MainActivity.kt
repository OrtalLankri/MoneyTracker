package com.example.moneytracker

import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
     fun logIn(username: String, password: String):Int{
        val db: AppDB = AppDB.getInstance(this)
        val id = db.userDAO().getId(username, password)
        return id
    }
}