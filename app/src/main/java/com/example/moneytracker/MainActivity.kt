package com.example.moneytracker

import android.annotation.SuppressLint
import android.content.Intent
import android.nfc.Tag
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private var progr = 60.0

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        CoroutineScope(Dispatchers.IO).launch {  }

        val button = findViewById<FloatingActionButton>(R.id.settings)

        button.setOnClickListener {
            val i = Intent(this@MainActivity, SettingsActivity::class.java)
            startActivity(i)
        }

        //updateProgressBar()
        //FirebaseApp.initializeApp();
        val db = FirebaseFirestore.getInstance().document("sampleData/try")
        // Create a new user with a first and last name
        val user = mapOf(
            "first" to "Adi",
            "last" to "Lovelace",
            "born" to 1815
        )
        db.set(user).addOnSuccessListener { _ ->
            Log.d("TAG", "DocumentSnapshot added with ID")
        }
            .addOnFailureListener { e ->
                Log.w("TAG", "Error adding document", e)
            }

        db.get().addOnSuccessListener { documentReference ->
            Log.d("TAG", "DocumentSnapshot recieved")
            val s = documentReference.getString("first").toString()
            Log.d("TAG", s)
        }
            .addOnFailureListener { e ->
                Log.w("TAG", "Error getting document", e)
            }
/*
        // Add a new document with a generated ID
        db.collection("users")
            .add(user)
            .addOnSuccessListener { documentReference ->
                Log.d("TAG", "DocumentSnapshot added with ID: ${documentReference.id}")
            }
            .addOnFailureListener { e ->
                Log.w("TAG", "Error adding document", e)
            }
//        val c = db.collection("users").get()
//        val b = c
*/

    }

    fun logIn(username: String, password: String): Int {
        val db: AppDB = AppDB.getInstance(this)
        val id = db.userDAO().getId(username, password)
        return id
    }

    private fun updateProgressBar() {
        val pb = findViewById<ProgressBar>(R.id.progressBar)
        val b = findViewById<EditText>(R.id.budget)
        var budget = b.text.toString().toDouble()
        var percent = 0
        if (budget > 0) {
             percent = (progr / budget * 100).toInt()
        }
        if (percent > 100) {
            percent = 100
        }
        if (percent < 0) {
            percent = 0
        }
        pb.progress = percent
    }

    fun updateAmount(newAmount: Double) {
        val amount = findViewById<TextView>(R.id.amount)
        progr += newAmount
        amount.text = "$progr$"
        updateProgressBar()
    }
}