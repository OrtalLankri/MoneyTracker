package com.example.moneytracker

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.moneytracker.ui.login.afterTextChanged
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class SettingsActivity : AppCompatActivity() {
    private lateinit var settingsViewModel: SettingsViewModel
    private lateinit var budget : TextView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        CoroutineScope(Dispatchers.IO).launch { }

        val button = findViewById<Button>(R.id.save)
        budget = findViewById<EditText>(R.id.budget)
        budget.setText(settingsViewModel.budget)
        //settingsViewModel.updateBudget(50.0)

        button.setOnClickListener {
            val i = Intent(this@SettingsActivity, MainActivity::class.java)
            startActivity(i)
        }

//        budget.afterTextChanged {
//            settingsViewModel.updateBudget(budget.text.toString().toDouble())
//        }
    }

}
