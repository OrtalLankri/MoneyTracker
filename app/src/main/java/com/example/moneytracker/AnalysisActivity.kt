package com.example.moneytracker

import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.getField
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AnalysisActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_analysis)
        CoroutineScope(Dispatchers.IO).launch { }
        val userId = intent.getStringExtra("userID").toString()
        val month = intent.getStringExtra("month").toString()
        val monthName = intent.getStringExtra("name").toString()
        val monthRef = FirebaseFirestore.getInstance().document("Users/$userId/Months/$month")
        val title = findViewById<TextView>(R.id.month)
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

        title.text = monthName

        fun getCatNames() {
            monthRef.get().addOnCompleteListener {
                if (it.isSuccessful) {
                    val documentReference = it.result!!
                    // categories names
                    val c = documentReference.getField<Object>("categories")!!
                    var cString = c.toString().removePrefix("{").removeSuffix("}")
                    val cMap = cString.split(", ").associate {
                        val (left, right) = it.split("=")
                        left to right.toString()
                    }
                    for (i in 1..6) {
                        catNames[i-1].setText(cMap["c$i"])
                    }
                    // categories budgets
                    for (i in 1..6) {
                        val catRef = monthRef.collection("Categories").document("c$i")
                        catRef.get().addOnCompleteListener {
                            if (it.isSuccessful) {
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
                                catProgress[i-1].progress = progress
                            }
                        }
                    }
                }
            }
        }
    }
}