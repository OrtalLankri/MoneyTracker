package com.example.moneytracker

import android.widget.TextView
import androidx.lifecycle.ViewModel

class SettingsViewModel : ViewModel() {
    var budget = "500.0"
    private var categories = arrayOf("Home", "Car", "Food", "Clothes", "Fun", "Other")

//    override fun onCreateView() {
//
//    }

    fun updateCategory(newName: String, index: Int) {
        if (index >= 0 && index < categories.size) {
            categories[index] = newName
        }
    }

    fun updateBudget(newBudget: Double) {
        if (newBudget >= 0) {
            budget = newBudget.toString()
        }
    }
}