package com.example.moneytracker

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

class Expense: AppCompatActivity(){
    @SuppressLint("SetTextI18n")



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_expense)
        CoroutineScope(Dispatchers.IO).launch { }

        val btnDatePicker = Button(this)

        var txtDate: EditText? = null
        var mYear = 0
        var mMonth: Int = 0
        var mDay: Int = 0

        val category = intent.getStringExtra("catNum")
        val userId = intent.getStringExtra("userID").toString()
        val month = intent.getStringExtra("month").toString()
        val expenseId = intent.getStringExtra("expenseId").toString()
        val catRef = FirebaseFirestore.getInstance()
            .document("Users/$userId/Months/$month/Categories/$category")
        val save = findViewById<Button>(R.id.save_button)
        val date = findViewById<Button>(R.id.date)
        val remark = findViewById<EditText>(R.id.remark)
        val product_name = findViewById<EditText>(R.id.product_name)
        val price = findViewById<EditText>(R.id.price)
        val newExpense = findViewById<TextView>(R.id.title1)
        txtDate = findViewById<EditText>(R.id.in_date);
        var dataChanged = false


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
                        "${calendar.get(Calendar.MONTH) + 1}-${calendar.get(Calendar.YEAR)}"

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

        save.setOnClickListener {
            // if it's a new expense
            if (expenseId == "null") {
                val expense = hashMapOf(
                    "date" to date.text.toString(),
                    "description" to remark.text.toString(),
                    "title" to product_name.text.toString(),
                    "price" to price.text.toString()
                )
                catRef.collection("Expenses")
                    .add(expense)
                    .addOnSuccessListener { documentReference ->
                        Log.d(
                            "TAG",
                            "DocumentSnapshot written with ID-------------------------------------: ${documentReference.id}"
                        )
                        val name = product_name.text.toString()
                        showMessage("Saved Successfully!")
                        // ADD EXPENSE TO CATEGORY
                        val i = Intent(this@Expense, Category::class.java)
                        i.putExtra("userID", userId)
                        i.putExtra("month", month)
                        i.putExtra("category", category)
                        i.putExtra("catNum", intent.getStringExtra("catNum").toString())
                        startActivity(i)
                    }
                    .addOnFailureListener { e ->
                        Log.w("TAG", "Error adding document", e)
                        showMessage("Error: Please try again")
                    }
            } else if (dataChanged) {
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
                expRef.update("price", price.text.toString())
                    .addOnSuccessListener { Log.d("TAG", "DocumentSnapshot successfully updated!") }
                    .addOnFailureListener { e -> Log.w("TAG", "Error updating document", e) }
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
    }

    private fun showMessage(s: String) {
        Toast.makeText(applicationContext, s, Toast.LENGTH_LONG).show()
    }

}