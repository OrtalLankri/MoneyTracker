package com.example.moneytracker

import android.content.ContentValues
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Color.BLACK
import android.graphics.ColorFilter
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.getField
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AnalysisActivity : AppCompatActivity() {

    lateinit var month : String
    lateinit var userId : String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_analysis)
        CoroutineScope(Dispatchers.IO).launch { }

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        userId = intent.getStringExtra("userID").toString()
        month = intent.getStringExtra("month").toString()

        val title = findViewById<TextView>(R.id.month)
        val prev = findViewById<ImageButton>(R.id.prev)
        val next = findViewById<ImageButton>(R.id.next)
        // categories names
        val catNames = arrayListOf<TextView>()
        catNames.add(findViewById<TextView>(R.id.n_c1))
        catNames.add(findViewById<TextView>(R.id.n_c2))
        catNames.add(findViewById<TextView>(R.id.n_c3))
        catNames.add(findViewById<TextView>(R.id.n_c4))
        catNames.add(findViewById<TextView>(R.id.n_c5))
        catNames.add(findViewById<TextView>(R.id.n_c6))
        // categories budgets
        val catProgress = arrayListOf<ProgressBar>()
        catProgress.add(findViewById<ProgressBar>(R.id.p_c1))
        catProgress.add(findViewById<ProgressBar>(R.id.p_c2))
        catProgress.add(findViewById<ProgressBar>(R.id.p_c3))
        catProgress.add(findViewById<ProgressBar>(R.id.p_c4))
        catProgress.add(findViewById<ProgressBar>(R.id.p_c5))
        catProgress.add(findViewById<ProgressBar>(R.id.p_c6))

        var currentMonth = month
        var previousMonth = month


        fun setMonthInfo(monthRef: DocumentReference){
            monthRef.get().addOnCompleteListener {
                if (it.isSuccessful) {
                    val documentReference = it.result!!
                    if (documentReference.exists()) {
                        // categories names
                        val c = documentReference.getField<Object>("categories")!!
                        var cString = c.toString().removePrefix("{").removeSuffix("}")
                        val cMap = cString.split(", ").associate {
                            val (left, right) = it.split("=")
                            left to right.toString()
                        }
                        for (i in 1..6) {
                            catNames[i - 1].setText(cMap["c$i"])
                        }
                        // categories budgets
                        for (i in 1..6) {
                            val catRef = monthRef.collection("Categories").document("c$i")
                            catRef.get().addOnCompleteListener {
                                if (it.isSuccessful && it.result!!.exists()) {
                                    Log.d("TAG", "successful")
                                    val data = it.result!!.data!!
                                    val budget = data["budget"].toString().toDouble()
                                    val amount = data["amount"].toString().toDouble()
                                    var progress = 0
                                    if (budget > 0) {
                                        progress = (amount / budget * 100).toInt()
                                    }
                                    if (progress > 100) {
                                        progress = 100
                                    }
                                    if (progress < 0) {
                                        progress = 0
                                    }
                                    catProgress[i - 1].progress = progress
                                }
                                else {
                                    catProgress[i - 1].progress = 0
                                }
                            }
                        }
                        // month name
                        val name = documentReference.getField<String>("name").toString()
                        Log.d("Analysis", "name $name")
                        title.text = name
                    } else {
                        currentMonth = previousMonth
                        Log.d("Analysis", "not exist")
                        showMessage("Month Data Does Not Exist")
                    }
                }
            } .addOnFailureListener {
                currentMonth = previousMonth
                Log.d("Analysis", "failure")
                showMessage("Month Data Does Not Exist")
            }
        }

        fun init() {
            val monthRef = FirebaseFirestore.getInstance().document("Users/$userId/Months/$month")
            setMonthInfo(monthRef)
        }
        init()

        prev.setOnClickListener {
            previousMonth = currentMonth
            currentMonth = prev(currentMonth)
            val monthRef = FirebaseFirestore.getInstance().document("Users/$userId/Months/$currentMonth")
            setMonthInfo(monthRef)
        }

        next.setOnClickListener {
            previousMonth = currentMonth
            currentMonth = next(currentMonth)
            val monthRef = FirebaseFirestore.getInstance().document("Users/$userId/Months/$currentMonth")
            setMonthInfo(monthRef)
        }
    }

    fun next(current: String) : String {
        var monthIndex = current.substring(0, 2).toInt()
        var yearIndex = current.substring(2).toInt()
        // if it's the last month of the year
        if (monthIndex == 12) {
            yearIndex += 1
            return if (yearIndex < 10) {
                "010$yearIndex"
            } else if (yearIndex == 99) {
                "0100"
            } else {
                "01$yearIndex"
            }
        }
        else {
            monthIndex += 1
            return if (monthIndex < 10) {
                "0$monthIndex$yearIndex"
            } else {
                "$monthIndex$yearIndex"
            }
        }
    }

    fun prev(current: String) : String {
        var monthIndex = current.substring(0, 2).toInt()
        var yearIndex = current.substring(2).toInt()
        // if it's the first month of the year
        if (monthIndex == 1) {
            yearIndex -= 1
            return if (yearIndex == 0) {
               "1299"
            } else if (yearIndex < 10) {
                "120$yearIndex"
            } else {
                "12$yearIndex"
            }
        }
        // if it's not the first month of the year
        else {
            monthIndex -= 1
            if (monthIndex < 10) {
                return "0$monthIndex$yearIndex"
            }
            return "$monthIndex$yearIndex"
        }
    }

    private fun showMessage(s: String) {
        Toast.makeText(applicationContext, s, Toast.LENGTH_LONG).show()
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        // back
        16908332 -> {
            val i = Intent(this@AnalysisActivity, MainActivity::class.java)
            i.putExtra("userID", userId)
            i.putExtra("month", month)
            startActivity(i)
            finish()
            true
        }
        else -> {
            Log.d(ContentValues.TAG, item.itemId.toString())
            super.onOptionsItemSelected(item)
        }
    }
}