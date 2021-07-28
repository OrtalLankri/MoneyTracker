package com.example.moneytracker

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import android.widget.AdapterView.OnItemSelectedListener
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.getField
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.*
import kotlin.collections.HashMap
import kotlin.concurrent.thread


class Expense: AppCompatActivity() {
    @SuppressLint("SetTextI18n")



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_expense)
        CoroutineScope(Dispatchers.IO).launch { }

        var category = intent.getStringExtra("catNum").toString()
        Log.d("baby", category)
        val userId = intent.getStringExtra("userID").toString()
        val month = intent.getStringExtra("month").toString()
        val expenseId = intent.getStringExtra("expenseId").toString()
        val monthRef = FirebaseFirestore.getInstance().document("Users/$userId/Months/$month")
        var catRef = FirebaseFirestore.getInstance()
            .document("Users/$userId/Months/$month/Categories/$category")
        val save = findViewById<Button>(R.id.save_button)
        val delete = findViewById<Button>(R.id.delete)
        val date = findViewById<Button>(R.id.date)
        val remark = findViewById<EditText>(R.id.remark)
        val product_name = findViewById<EditText>(R.id.product_name)
        val price = findViewById<EditText>(R.id.price)
        val newExpense = findViewById<TextView>(R.id.title1)
        val dropdown = findViewById<Spinner>(R.id.spinner1)
        val spin = findViewById<ProgressBar>(R.id.spin)
        var dataChanged = false
        var isCategoryChosen = false


        val spinnerListener = object : OnItemSelectedListener {
            override fun onItemSelected(av: AdapterView<*>?, v: View, i: Int, l: Long) {
                Log.d("position", i.toString())
                val pos = i + 1
                if (category == "null") {
                    category = "c$pos"
                    catRef = FirebaseFirestore.getInstance()
                        .document("Users/$userId/Months/$month/Categories/$category")
                    isCategoryChosen = true
                }
            }
            override fun onNothingSelected(p0: AdapterView<*>?) {
                TODO("Not yet implemented")
            }
        }
        // dropdown
        monthRef.get().addOnSuccessListener {
            val s = it.get("categories").toString().removePrefix("{").removeSuffix("}")
            val categories = s.split(", ").associate {
                val (left, right) = it.split("=")
                left to right.toString()
            }
            var items = arrayOf("", "", "", "", "", "")
            // if expense is related to category
            if (category != "null") {
                items = arrayOf(categories[category].toString())
                isCategoryChosen = true
            } else {
                for (i in 1..6) {
                    items[i-1] = categories["c$i"].toString()
                }
            }
            val adapter: ArrayAdapter<Any?> = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, items)
            dropdown.adapter = adapter
            dropdown.onItemSelectedListener = spinnerListener
        }

        date.setOnClickListener {
            // Create the date picker builder and set the title
            val builder = MaterialDatePicker.Builder.datePicker()
                .also {
                    title = "Pick Date"
                }
            // create the date picker
            val datePicker = builder.build()
            // set listener when date is selected
            datePicker.addOnPositiveButtonClickListener {
                // Create calendar object and set the date to be that returned from selection
                val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
                calendar.time = Date(it)
                date.text = "${calendar.get(Calendar.DAY_OF_MONTH)}- " +
                        "${calendar.get(Calendar.MONTH) + 1}- "+
                "${calendar.get(Calendar.YEAR)}"

            }
            datePicker.show(supportFragmentManager, "MyTAG")
        }

        fun setInfo(ref: DocumentSnapshot) {
            val data = ref.data!!
            product_name.setText(data["title"].toString())
            price.setText(data["price"].toString())
            remark.setText(data["description"].toString())
            date.text = data["date"].toString()
        }

        // if expense already exist
        if (expenseId != "null") {
            newExpense.visibility = View.INVISIBLE
            delete.visibility = View.VISIBLE
            catRef.collection("Expenses").document(expenseId)
                .get()
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        Log.d("TAG", "successful")
                        val result = it.result!!
                        setInfo(result)
                    }
                }
        }

        fun updateAmount(amountToAdd: Double) {
            Log.d("WWW", "update amount")
            // update category
            catRef.get().addOnSuccessListener {
                val oldAmount = it.get("amount").toString().toDouble()
                var newAmount = oldAmount + amountToAdd
                if (newAmount < 0) {
                    newAmount = 0.0
                }
                newAmount = BigDecimal(newAmount).setScale(1, RoundingMode.HALF_EVEN).toDouble()
                catRef.update("amount", newAmount.toString())
                        .addOnSuccessListener { Log.d("TAG", "DocumentSnapshot successfully updated!") }
                        .addOnFailureListener { e -> Log.w("TAG", "Error updating document", e) }
            }
            // update month
            monthRef.get().addOnSuccessListener {
                val oldAmount = it.get("amount").toString().toDouble()
                var newAmount = oldAmount + amountToAdd
                if (newAmount < 0) {
                    newAmount = 0.0
                }
                newAmount = BigDecimal(newAmount).setScale(1, RoundingMode.HALF_EVEN).toDouble()
                monthRef.update("amount", newAmount.toString())
                        .addOnSuccessListener { Log.d("TAG", "DocumentSnapshot successfully updated!") }
                        .addOnFailureListener { e -> Log.w("TAG", "Error updating document", e) }
            }
        }

        fun updateMap(map: HashMap<String, String>) {
            Log.d("CCC", "update map")
            catRef.update("expenses", map)
                .addOnSuccessListener { Log.d("TAG", "DocumentSnapshot successfully updated!") }
                .addOnFailureListener { e -> Log.w("TAG", "Error updating document", e) }
        }

        fun addExpenseToCategory(string: String, id : String) {
            Log.d("CCC", "add expense")
            catRef.get().addOnCompleteListener {
                if (it.isSuccessful) {
                    val documentReference = it.result!!
                    val newMap = hashMapOf(
                        id to string
                    )
                    // expenses
                    val c = documentReference.getField<Object>("expenses")!!
                    // if the current map is not empty
                    if (c.toString() != "{}") {
                        var cString = c.toString().removePrefix("{").removeSuffix("}")
                        val map = cString.split(", ").associate {
                            val (left, right) = it.split("=")
                            left to right.toString()
                        }
                        for ((oldId, oldString) in map) {
                            newMap.put(oldId, oldString)
                        }
                    }
                    updateMap(newMap)
                }
            }
        }

        save.setOnClickListener {
            save.text = "Saving..."
            spin.visibility = View.VISIBLE
            // if it's a new expense
            if (expenseId == "null") {
                Log.d("CCC", "new expense")
                val expense = hashMapOf(
                    "date" to date.text.toString(),
                    "description" to remark.text.toString(),
                    "title" to product_name.text.toString(),
                    "price" to price.text.toString()
                )
                updateAmount(expense["price"]!!.toDouble())
                catRef.collection("Expenses")
                    .add(expense)
                    .addOnSuccessListener { documentReference ->
                        Log.d(
                            "TAG",
                            "DocumentSnapshot written with ID-------------------------------------: ${documentReference.id}"
                        )
                        // add expense to category
                        val expenseString = expense["title"] + " - " + expense["price"] + "$"
                        val id = documentReference.id
                        Log.d("CCC", "before add to cat id: $id")
                        addExpenseToCategory(expenseString, id)
                        showMessage("Saved Successfully!")
                    }
                    .addOnFailureListener { e ->
                        Log.w("TAG", "Error adding document", e)
                        showMessage("Error: Please try again")
                    }
            }
            // if it's not a new expense
            else if (dataChanged) {
                val expRef = catRef.collection("Expenses").document(expenseId)
                expRef.update("date", date.text.toString())
                    .addOnSuccessListener { Log.d("TAG", "DocumentSnapshot successfully updated!") }
                    .addOnFailureListener { e -> Log.w("TAG", "Error updating document", e) }
                expRef.update("description", remark.text.toString())
                    .addOnSuccessListener { Log.d("TAG", "DocumentSnapshot successfully updated!") }
                    .addOnFailureListener { e -> Log.w("TAG", "Error updating document", e) }
                expRef.update("title", product_name.text.toString())
                    .addOnSuccessListener { Log.d("TAG", "DocumentSnapshot successfully updated!") }
                    .addOnFailureListener { e -> Log.w("TAG", "Error updating document", e) }
                var newPrice = price.text.toString()
                expRef.get().addOnSuccessListener {
                    var oldPrice = it.get("price").toString()
                    if (oldPrice != newPrice) {
                        updateAmount(newPrice.toDouble() - oldPrice.toDouble())
                    }
                }
                expRef.update("price", newPrice)
                    .addOnSuccessListener { Log.d("TAG", "DocumentSnapshot successfully updated!") }
                    .addOnFailureListener { e -> Log.w("TAG", "Error updating document", e) }
            }
            val i = Intent(this@Expense, Category::class.java)
            i.putExtra("userID", userId)
            i.putExtra("month", month)
            i.putExtra("name", intent.getStringExtra("category").toString())
            i.putExtra("catNum", category)
            i.putExtra("fromExpense", true)
            startActivity(i)
        }

        delete.setOnClickListener {
            // if expense already exist
            if (expenseId != "null") {
                delete.text = "deleting..."
                save.isEnabled = false
                spin.visibility = View.VISIBLE
                // delete from category
                catRef.get().addOnCompleteListener {
                    if (it.isSuccessful) {
                        val documentReference = it.result!!
                        val newMap = HashMap<String, String>()
                        // expenses
                        val c = documentReference.getField<Object>("expenses")!!
                        // if the current map is not empty
                        if (c.toString() != "Map is empty" && c.toString() != "[]") {
                            Log.d("CCC", "Map is empty")
                            var cString = c.toString().removePrefix("{").removeSuffix("}")
                            val map = cString.split(", ").associate {
                                val (left, right) = it.split("=")
                                left to right.toString()
                            }
                            for ((oldId, oldString) in map) {
                                if (oldId != expenseId) {
                                    newMap.put(oldId, oldString)
                                }
                            }
                        }
                        updateMap(newMap)
                    }
                }
                // update price
                updateAmount(-price.text.toString().toDouble())
                // delete from expenses
                catRef.collection("Expenses").document(expenseId)
                        .delete()
                        .addOnSuccessListener { Log.d("TAG", "DocumentSnapshot successfully deleted!") }
                        .addOnFailureListener { e -> Log.w("TAG", "Error deleting document", e) }
                val i = Intent(this@Expense, Category::class.java)
                i.putExtra("userID", userId)
                i.putExtra("month", month)
                i.putExtra("name", intent.getStringExtra("category").toString())
                i.putExtra("catNum", category)
                i.putExtra("fromExpense", true)
                startActivity(i)
            }
        }


        product_name.afterTextChanged{
            dataChanged = true
        }

        price.afterTextChanged{
            dataChanged = true
        }

        remark.afterTextChanged{
            dataChanged = true
        }
        date.doAfterTextChanged {
            dataChanged = true
        }
        price.afterTextChanged {
            save.isEnabled = price.text.isNotBlank() && product_name.text.isNotBlank() && isCategoryChosen
        }
        product_name.afterTextChanged {
            save.isEnabled = price.text.isNotBlank() && product_name.text.isNotBlank() && isCategoryChosen
        }

    }

    private fun showMessage(s: String) {
        Toast.makeText(applicationContext, s, Toast.LENGTH_LONG).show()
    }



}
