package com.example.moneytracker

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.moneytracker.ui.login.afterTextChanged
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.getField
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

        val userId = intent.getStringExtra("userID").toString()
        val month = intent.getStringExtra("month").toString()
        val userRef = FirebaseFirestore.getInstance().collection("Users").document(userId)
        val monthRef = FirebaseFirestore.getInstance().document("Users/$userId/Months/$month")
        val save = findViewById<Button>(R.id.save)
        val changeDefault = findViewById<CheckBox>(R.id.checkBox)
        val budget =  findViewById<EditText>(R.id.budget)
        val confirm = findViewById<TextView>(R.id.confirmation)
        // categories names
        val c1 = findViewById<EditText>(R.id.cat1)
        val c2 = findViewById<EditText>(R.id.cat2)
        val c3 = findViewById<EditText>(R.id.cat3)
        val c4 = findViewById<EditText>(R.id.cat4)
        val c5 = findViewById<EditText>(R.id.cat5)
        val c6 = findViewById<EditText>(R.id.cat6)
        // categories budgets
        val c1_b = findViewById<EditText>(R.id.cat1_b)
        val c2_b = findViewById<EditText>(R.id.cat2_b)
        val c3_b = findViewById<EditText>(R.id.cat3_b)
        val c4_b = findViewById<EditText>(R.id.cat4_b)
        val c5_b = findViewById<EditText>(R.id.cat5_b)
        val c6_b = findViewById<EditText>(R.id.cat6_b)
        var nameChanged = false
        var budgetChanged = false

        // set data
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
                c1.setText(cMap["c1"])
                c2.setText(cMap["c2"])
                c3.setText(cMap["c3"])
                c4.setText(cMap["c4"])
                c5.setText(cMap["c5"])
                c6.setText(cMap["c6"])
                // budgets
                val b = documentReference.getField<Object>("categoriesBudget")!!
                if (budget.toString() != "0") {
                    val bString = b.toString().removePrefix("{").removeSuffix("}")
                    val bMap = bString.split(", ").associate {
                        val (left, right) = it.split("=")
                        left to right.toString()
                    }
                    c1_b.setText(bMap["c1"])
                    c2_b.setText(bMap["c2"])
                    c3_b.setText(bMap["c3"])
                    c4_b.setText(bMap["c4"])
                    c5_b.setText(bMap["c5"])
                    c6_b.setText(bMap["c6"])
                }
            }
        }

        save.setOnClickListener {
            // new Names
            if (nameChanged) {
                val newNames = hashMapOf(
                        "c1" to c1.text.toString(),
                        "c2" to c2.text.toString(),
                        "c3" to c3.text.toString(),
                        "c4" to c4.text.toString(),
                        "c5" to c5.text.toString(),
                        "c6" to c6.text.toString()
                )
                // update in month document
                monthRef.update("categories", newNames)
                        .addOnSuccessListener { Log.d("TAG", "DocumentSnapshot successfully updated!") }
                        .addOnFailureListener { e -> Log.w("TAG", "Error updating document", e) }
                // update in user document
                if (changeDefault.isActivated) {
                    userRef.update("defaultCategories", newNames)
                            .addOnSuccessListener { Log.d("TAG", "DocumentSnapshot successfully updated!") }
                            .addOnFailureListener { e -> Log.w("TAG", "Error updating document", e) }
                }
                // update in category document
                for (i in 1..6) {
                    val cat = "c$i"
                    monthRef.collection("Categories").document(cat)
                            .update("name", newNames[cat])
                            .addOnSuccessListener { Log.d("TAG", "DocumentSnapshot successfully updated!") }
                            .addOnFailureListener { e -> Log.w("TAG", "Error updating document", e) }
                }

            }
            // new budgets
            if (budgetChanged) {
                val newBudget = hashMapOf(
                        "c1" to c1_b.text.toString(),
                        "c2" to c2_b.text.toString(),
                        "c3" to c3_b.text.toString(),
                        "c4" to c4_b.text.toString(),
                        "c5" to c5_b.text.toString(),
                        "c6" to c6_b.text.toString()
                )
                // update in month document
                monthRef.update("categoriesBudget", newBudget)
                        .addOnSuccessListener { Log.d("TAG", "DocumentSnapshot successfully updated!") }
                        .addOnFailureListener { e -> Log.w("TAG", "Error updating document", e) }
                // update in category document
                for (i in 1..6) {
                    val cat = "c$i"
                    monthRef.collection("Categories").document(cat)
                            .update("budget", newBudget[cat])
                            .addOnSuccessListener { Log.d("TAG", "DocumentSnapshot successfully updated!") }
                            .addOnFailureListener { e -> Log.w("TAG", "Error updating document", e) }
                }
            }
            // update budget in month document
            monthRef.update("budget", budget.text.toString())
                    .addOnSuccessListener { Log.d("TAG", "DocumentSnapshot successfully updated!") }
                    .addOnFailureListener { e -> Log.w("TAG", "Error updating document", e) }
            // go back to main page
            val i = Intent(this@SettingsActivity, MainActivity::class.java)
            i.putExtra("userID", userId)
            startActivity(i)
        }

        fun checkBudget() {
            var cat = c1_b.text.toString().toDouble() +
                    c2_b.text.toString().toDouble() +
                    c3_b.text.toString().toDouble() +
                    c4_b.text.toString().toDouble() +
                    c5_b.text.toString().toDouble() +
                    c6_b.text.toString().toDouble()
            val b = budget.text.toString().toDouble()
            if (b < cat) {
                save.isEnabled = false
                confirm.visibility = View.VISIBLE
            }
            else {
                save.isEnabled = true
                confirm.visibility = View.INVISIBLE
            }
        }

        fun budgetChanged() {
            budgetChanged = true
            var cat = c1_b.text.toString().toDouble() +
                    c2_b.text.toString().toDouble() +
                    c3_b.text.toString().toDouble() +
                    c4_b.text.toString().toDouble() +
                    c5_b.text.toString().toDouble() +
                    c6_b.text.toString().toDouble()
            val b = budget.text.toString().toDouble() + cat
            budget.setText(b.toString())
        }

        budget.afterTextChanged {
            checkBudget()
        }

        c1.afterTextChanged {
            nameChanged = true
        }
        c2.afterTextChanged {
            nameChanged = true
        }
        c3.afterTextChanged {
            nameChanged = true
        }
        c4.afterTextChanged {
            nameChanged = true
        }
        c5.afterTextChanged {
            nameChanged = true
        }
        c6.afterTextChanged {
            nameChanged = true
        }
        c1_b.afterTextChanged { budgetChanged() }
        c2_b.afterTextChanged { budgetChanged() }
        c3_b.afterTextChanged { budgetChanged() }
        c4_b.afterTextChanged { budgetChanged() }
        c5_b.afterTextChanged { budgetChanged() }
        c6_b.afterTextChanged { budgetChanged() }

    }

}
