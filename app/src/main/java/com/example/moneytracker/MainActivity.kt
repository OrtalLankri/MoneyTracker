package com.example.moneytracker

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
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
        val monthName = intent.getStringExtra("month").toString()
        val month = findViewById<TextView>(R.id.month)
        val cat = arrayListOf<Button>()
        cat.add(findViewById<Button>(R.id.c1))
        cat.add(findViewById<Button>(R.id.c2))
        cat.add(findViewById<Button>(R.id.c3))
        cat.add(findViewById<Button>(R.id.c4))
        cat.add(findViewById<Button>(R.id.c5))
        cat.add(findViewById<Button>(R.id.c6))
        val setBudget = findViewById<TextView>(R.id.setBudget)
        val amount = findViewById<TextView>(R.id.amount)
        val budget = findViewById<TextView>(R.id.budget)
        val addExpense = findViewById<Button>(R.id.add)
        val analysis = findViewById<Button>(R.id.analysis)
        val settings = findViewById<FloatingActionButton>(R.id.settings)
        val prev = findViewById<ImageButton>(R.id.prev)
        val next = findViewById<ImageButton>(R.id.next)


        val currentDate = if (monthName == "null") {
            SimpleDateFormat("MMyy").format(Date())
        } else {
            monthName
        }

        if (currentDate == SimpleDateFormat("MMyy").format(Date())) {
            next.visibility = View.GONE
        }

        val monthIndex= currentDate.substring(0, 2).toInt()

        fun setInfo(ref: DocumentSnapshot){
            val data = ref.data!!
            // set categories names
            val s = data["categories"].toString().removePrefix("{").removeSuffix("}")
            val categories = s.split(", ").associate {
                val (left, right) = it.split("=")
                left to right.toString()
            }
            for (i in 1..6) {
                cat[i-1].text = categories["c$i"]
            }
            // set budget
            if (data["budget"].toString() != "0") {
                setBudget.visibility = View.GONE
                amount.visibility = View.VISIBLE
                budget.visibility = View.VISIBLE
                budget.text = data["budget"].toString()+ "$"
                amount.text = data["amount"].toString()+ "$"
            }
            // set progress bar
            updateProgressBar(data["amount"].toString().toDouble(), data["budget"].toString().toDouble())
            // set month name
            month.text = data["name"].toString()
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

        addExpense.setOnClickListener {
            val i = Intent(this@MainActivity, Expense::class.java)
            i.putExtra("userID", userId)
            i.putExtra("month", currentDate)
            i.putExtra("expenseId", "null")
            i.putExtra("catNum", "null")
            startActivity(i)
        }

        settings.setOnClickListener {
            val i = Intent(this@MainActivity, SettingsActivity::class.java)
            i.putExtra("userID", userId)
            i.putExtra("month", currentDate)
            startActivity(i)
        }

        analysis.setOnClickListener {
            val i = Intent(this@MainActivity, AnalysisActivity::class.java)
            i.putExtra("userID", userId)
            i.putExtra("month", currentDate)
            startActivity(i)
        }

        for (j in 1..6) {
            cat[j-1].setOnClickListener {
                val i = Intent(this@MainActivity, Category::class.java)
                i.putExtra("userID", userId)
                i.putExtra("month", currentDate)
                i.putExtra("name", cat[j-1].text.toString())
                i.putExtra("catNum", "c$j")
                startActivity(i)
            }
        }

        setBudget.setOnClickListener {
            val i = Intent(this@MainActivity, SettingsActivity::class.java)
            i.putExtra("userID", userId)
            i.putExtra("month", currentDate)
            startActivity(i)
        }

        next.setOnClickListener {
            val m = AnalysisActivity().next(currentDate)
            val i = Intent(this@MainActivity, MainActivity::class.java)
            i.putExtra("userID", userId)
            i.putExtra("month", m)
            startActivity(i)
            finish()
        }

        prev.setOnClickListener {
            val m = AnalysisActivity().prev(currentDate)
            val i = Intent(this@MainActivity, MainActivity::class.java)
            i.putExtra("userID", userId)
            i.putExtra("month", m)
            startActivity(i)
            finish()
        }
    }


    private fun updateProgressBar(amount: Double, budget: Double) {
        val pb = findViewById<ProgressBar>(R.id.progressBar)
        val pbRed = findViewById<ProgressBar>(R.id.progressBarRed)
        pb.visibility = View.VISIBLE
        pbRed.visibility = View.INVISIBLE
        var percent = 0
        if (budget > 0) {
            percent = (amount / budget * 100).toInt()
        }
        if (percent >= 100) {
            percent = 100
            pb.visibility = View.INVISIBLE
            pbRed.visibility = View.VISIBLE
        }
        if (percent < 0) {
            percent = 0
        }
        pb.progress = percent
    }

}