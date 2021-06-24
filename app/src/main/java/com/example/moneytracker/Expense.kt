package com.example.moneytracker

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.datepicker.MaterialDatePicker
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
          var mMonth:Int = 0
          var mDay:Int = 0

        val category = intent.getStringExtra("catNum")
        val userId = intent.getStringExtra("userID").toString()
        val month = intent.getStringExtra("month").toString()
        val catRef = FirebaseFirestore.getInstance().document("Users/$userId/Months/$month/Categories/$category")
        Log.d("TAG", "\"Users/$userId/Months/$month/Categories/$category")
        val save = findViewById<Button>(R.id.save_button)
        val date = findViewById<Button>(R.id.date)
        val remark = findViewById<EditText>(R.id.remark)
        val product_name = findViewById<EditText>(R.id.product_name)
        val price = findViewById<EditText>(R.id.price)
        txtDate=findViewById<EditText>(R.id.in_date);


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


//
//        val builder = MaterialDatePicker.Builder.datePicker()
//            .also {
//                title = "Pick Date"
//            }
//
//        // create the date picker
//        val datePicker = builder.build()

        // set listener when date is selected
//        datePicker.addOnPositiveButtonClickListener {
//            datePicker.show(supportFragmentManager, "MyTAG")
//
//            // Create calendar object and set the date to be that returned from selection
//            val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
//            calendar.time = Date(it)
//            date.text = "${calendar.get(Calendar.DAY_OF_MONTH)}- " +
//                    "${calendar.get(Calendar.MONTH) + 1}-${calendar.get(Calendar.YEAR)}"
//
//        }


//        date.setOnClickListener{
//            datePicker.show(supportFragmentManager, "MyTAG")
//            datePicker.addOnPositiveButtonClickListener {
//                //datePicker.show(supportFragmentManager, "MyTAG")
//
//                // Create calendar object and set the date to be that returned from selection
//                val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
//                calendar.time = Date(it)
//                date.text = "${calendar.get(Calendar.DAY_OF_MONTH)}- " +
//                        "${calendar.get(Calendar.MONTH) + 1}-${calendar.get(Calendar.YEAR)}"
//
//            }

            // Create calendar object and set the date to be that returned from selection
//            val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
//            calendar.time = Date(it)
//            date.text = "${calendar.get(Calendar.DAY_OF_MONTH)}- " +
//                    "${calendar.get(Calendar.MONTH) + 1}-${calendar.get(Calendar.YEAR)}"
//            val c: Calendar = Calendar.getInstance()
//            mYear = c.get(Calendar.YEAR)
//            mMonth = c.get(Calendar.MONTH)
//            mDay = c.get(Calendar.DAY_OF_MONTH)
//
//
//            val datePickerDialog = DatePickerDialog(
//                this,
//                { view, year, monthOfYear, dayOfMonth -> txtDate.setText(dayOfMonth.toString() + "-" + (monthOfYear + 1) + "-" + year) },
//                mYear,
//                mMonth,
//                mDay
//            )
//            datePickerDialog.show()
//        }
        save.setOnClickListener {
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
        }

    }

    private fun showMessage(s: String) {
        Toast.makeText(applicationContext, s, Toast.LENGTH_LONG).show()
    }

}