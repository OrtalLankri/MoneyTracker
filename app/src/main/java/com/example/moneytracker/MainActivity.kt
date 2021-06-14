package com.example.moneytracker

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.getField
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap

enum class Months {
    January, February, March, April, May, June, July, August, September, October, November, December
}

class MainActivity : AppCompatActivity() {

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        CoroutineScope(Dispatchers.IO).launch {  }

        val userId = intent.getStringExtra("userID").toString()
        val userRef = FirebaseFirestore.getInstance().collection("Users").document(userId)
        val settings = findViewById<FloatingActionButton>(R.id.settings)
        val month = findViewById<TextView>(R.id.month)
        val c1 = findViewById<Button>(R.id.c1)
        val c2 = findViewById<Button>(R.id.c2)
        val c3 = findViewById<Button>(R.id.c3)
        val c4 = findViewById<Button>(R.id.c4)
        val c5 = findViewById<Button>(R.id.c5)
        val c6 = findViewById<Button>(R.id.c6)
        val setBudget = findViewById<TextView>(R.id.setBudget)
        val amount = findViewById<TextView>(R.id.amount)
        val budget = findViewById<TextView>(R.id.budget)


        val currentDate = SimpleDateFormat("MMyy").format(Date())
        val monthIndex = currentDate.substring(0, 2).toInt()

        fun setInfo(ref: DocumentSnapshot){
            val data = ref.data!!
            // set month name
            month.text = data["name"].toString()
            // set categories names
            val s = data["categories"].toString().removePrefix("{").removeSuffix("}")
            val categories = s.split(", ").associate {
                val (left, right) = it.split("=")
                left to right.toString()
            }
            c1.text = categories["c1"]
            c2.text = categories["c2"]
            c3.text = categories["c3"]
            c4.text = categories["c4"]
            c5.text = categories["c5"]
            c6.text = categories["c6"]
            // set budget
            if (data["budget"].toString() != "0") {
                setBudget.visibility = View.GONE
                amount.visibility = View.VISIBLE
                budget.visibility = View.VISIBLE
            }
            // set progress bar
            updateProgressBar(data["amount"].toString().toDouble(), data["budget"].toString().toDouble())
        }

        fun createNewMonth(categories: Object) {
            val name = Months.values()[monthIndex-1].name
            val month = hashMapOf(
                    "name" to name,
                    "budget" to 0,
                    "amount" to 0,
                    "categories" to categories,
                    "categoriesBudget" to 0
            )
            userRef.collection("Months").document(currentDate)
                    .set(month).addOnSuccessListener {
                        Log.d("TAG", "Month document added successfully")
                    }
                    .addOnFailureListener { e ->
                        Log.w("TAG", "Error adding month document", e)
                    }
        }

        fun setDefaultInfo() {
            userRef.get().addOnCompleteListener {
                if (it.isSuccessful) {
                    val documentReference = it.result!!
                    val categories = documentReference.getField<Object>("defaultCategories")!!
                    createNewMonth(categories)
                }
            }
        }

        fun getDoc() {
            // check if the current month has data already
            userRef.collection("Months").document(currentDate)
                    .get()
                    .addOnCompleteListener {
                        if (it.isSuccessful) {
                            Log.d("TAG", "successful")
                            val result = it.result!!
                            // if current month exists already
                            if (result.exists()) {
                                Log.d("TAG", "Doc Exists")
                                setInfo(result)
                            } else {
                                Log.d("TAG", "Doc Does Not Exist")
                                setDefaultInfo()
                                getDoc()
                            }
                        }
                    }
        }
        getDoc()

        settings.setOnClickListener {
            val i = Intent(this@MainActivity, SettingsActivity::class.java)
            i.putExtra("userID", userId)
            i.putExtra("month", currentDate)
            startActivity(i)
        }
        c1.setOnClickListener {
            val i = Intent(this@MainActivity, Category::class.java)
            i.putExtra("userID", userId)
            i.putExtra("month", currentDate)
            i.putExtra("name", c1.text.toString())
            i.putExtra("catNum", "c1")
            startActivity(i)
        }
        c2.setOnClickListener {
            val i = Intent(this@MainActivity, Category::class.java)
            i.putExtra("userID", userId)
            i.putExtra("month", currentDate)
            i.putExtra("name", c2.text.toString())
            i.putExtra("catNum", "c2")
            startActivity(i)
        }
        c3.setOnClickListener {
            val i = Intent(this@MainActivity, Category::class.java)
            i.putExtra("userID", userId)
            i.putExtra("month", currentDate)
            i.putExtra("name", c3.text.toString())
            i.putExtra("catNum", "c3")
            startActivity(i)
        }
        c4.setOnClickListener {
            val i = Intent(this@MainActivity, Category::class.java)
            i.putExtra("userID", userId)
            i.putExtra("month", currentDate)
            i.putExtra("name", c4.text.toString())
            i.putExtra("catNum", "c4")
            startActivity(i)
        }
        c5.setOnClickListener {
            val i = Intent(this@MainActivity, Category::class.java)
            i.putExtra("userID", userId)
            i.putExtra("month", currentDate)
            i.putExtra("name", c5.text.toString())
            i.putExtra("catNum", "c5")
            startActivity(i)
        }
        c6.setOnClickListener {
            val i = Intent(this@MainActivity, Category::class.java)
            i.putExtra("userID", userId)
            i.putExtra("month", currentDate)
            i.putExtra("name", c6.text.toString())
            i.putExtra("catNum", "c6")
            startActivity(i)
        }

        setBudget.setOnClickListener {
            val i = Intent(this@MainActivity, SettingsActivity::class.java)
            i.putExtra("userID", userId)
            i.putExtra("month", currentDate)
            startActivity(i)
        }
        //updateProgressBar()
        //FirebaseApp.initializeApp();
//        // Create a new user with a first and last name
//        val user = mapOf(
//            "first" to "Adi",
//            "last" to "Lovelace",
//            "born" to 1815
//        )
//        db.set(user).addOnSuccessListener { _ ->
//            Log.d("TAG", "DocumentSnapshot added with ID")
//        }
//            .addOnFailureListener { e ->
//                Log.w("TAG", "Error adding document", e)
//            }
//
//        db.get().addOnSuccessListener { documentReference ->
//            Log.d("TAG", "DocumentSnapshot recieved")
//            val s = documentReference.getString("expensses").toString()
//            Log.d("TAG", s)
//        }
//            .addOnFailureListener { e ->
//                Log.w("TAG", "Error getting document", e)
//            }


    }


    private fun updateProgressBar(amount: Double, budget:Double) {
        val pb = findViewById<ProgressBar>(R.id.progressBar)
        var percent = 0
        if (budget > 0) {
             percent = (amount / budget * 100).toInt()
        }
        if (percent > 100) {
            percent = 100
        }
        if (percent < 0) {
            percent = 0
        }
        pb.progress = percent
    }

}