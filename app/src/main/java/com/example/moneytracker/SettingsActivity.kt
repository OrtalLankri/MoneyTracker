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
        val catNames = arrayListOf<EditText>()
        catNames.add(findViewById<EditText>(R.id.cat1))
        catNames.add(findViewById<EditText>(R.id.cat2))
        catNames.add(findViewById<EditText>(R.id.cat3))
        catNames.add(findViewById<EditText>(R.id.cat4))
        catNames.add(findViewById<EditText>(R.id.cat5))
        catNames.add(findViewById<EditText>(R.id.cat6))
        // categories budgets
        val catBudgets = arrayListOf<EditText>()
        catBudgets.add(findViewById<EditText>(R.id.cat1_b))
        catBudgets.add(findViewById<EditText>(R.id.cat2_b))
        catBudgets.add(findViewById<EditText>(R.id.cat3_b))
        catBudgets.add(findViewById<EditText>(R.id.cat4_b))
        catBudgets.add(findViewById<EditText>(R.id.cat5_b))
        catBudgets.add(findViewById<EditText>(R.id.cat6_b))
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
                for (i in 1..6) {
                    catNames[i-1].setText(cMap["c$i"])
                }
                // categories budgets
                val b = documentReference.getField<Object>("categoriesBudget")!!
                if (b.toString() != "0") {
                    val bString = b.toString().removePrefix("{").removeSuffix("}")
                    val bMap = bString.split(", ").associate {
                        val (left, right) = it.split("=")
                        left to right.toString()
                    }
                    for (i in 1..6) {
                        catBudgets[i-1].setText(bMap["c$i"])
                    }
                }
                // budget
                val monthBudget = documentReference.getField<Object>("budget")!!
                budget.setText(monthBudget.toString())
            }
        }

        save.setOnClickListener {
            // new Names
            if (nameChanged) {
                val newNames = hashMapOf<String, String>()
                for (i in 1..6) {
                    newNames.put("c$i", catNames[i-1].text.toString())
                }
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
                val newBudget = hashMapOf<String, String>()
                for (i in 1..6) {
                    newBudget.put("c$i", catBudgets[i-1].text.toString())
                }
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
            i.putExtra("month", month)
            startActivity(i)
            finish()
        }

        fun checkBudget() {
            var cat_b = 0.0
            for (cat in catBudgets) {
                if (cat.text.toString() != "") {
                    cat_b += cat.text.toString().toDouble()
                }
            }
            var b = 0.0
            if (budget.text.toString() != "") {
                b = budget.text.toString().toDouble()
            }
            if (b < cat_b) {
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
            var b = 0.0
            for (cat in catBudgets) {
                if (cat.text.toString() != "") {
                    b += cat.text.toString().toDouble()
                }
            }
//            var cat = c1_b.text.toString().toDouble() +
//                    c2_b.text.toString().toDouble() +
//                    c3_b.text.toString().toDouble() +
//                    c4_b.text.toString().toDouble() +
//                    c5_b.text.toString().toDouble() +
//                    c6_b.text.toString().toDouble()
            if (budget.text.toString() == "" || budget.text.toString().toDouble() < b ) {
                budget.setText(b.toString())
            }
        }

        budget.afterTextChanged {
            checkBudget()
        }
        for (cat in catNames){
            cat.afterTextChanged { nameChanged = true }
        }
        for (cat in catBudgets){
            cat.afterTextChanged { budgetChanged() }
        }
//        c1.afterTextChanged {
//            nameChanged = true
//        }
//        c2.afterTextChanged {
//            nameChanged = true
//        }
//        c3.afterTextChanged {
//            nameChanged = true
//        }
//        c4.afterTextChanged {
//            nameChanged = true
//        }
//        c5.afterTextChanged {
//            nameChanged = true
//        }
//        c6.afterTextChanged {
//            nameChanged = true
//        }
//        c1_b.afterTextChanged { budgetChanged() }
//        c2_b.afterTextChanged { budgetChanged() }
//        c3_b.afterTextChanged { budgetChanged() }
//        c4_b.afterTextChanged { budgetChanged() }
//        c5_b.afterTextChanged { budgetChanged() }
//        c6_b.afterTextChanged { budgetChanged() }

    }

}
