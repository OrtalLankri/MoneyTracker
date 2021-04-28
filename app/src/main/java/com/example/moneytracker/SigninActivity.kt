package com.example.moneytracker

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SigninActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signin)
        CoroutineScope(Dispatchers.IO).launch {  }

        val button = findViewById<Button>(R.id.signin)

        button.setOnClickListener {
            val i = Intent(this@SigninActivity, MainActivity::class.java)
            startActivity(i)
        }
    }
}