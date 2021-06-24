package com.example.moneytracker

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.ArrayList

class Expense: AppCompatActivity(){
    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_expense)
        CoroutineScope(Dispatchers.IO).launch { }

        val category = intent.getStringExtra("category")
        val userId = intent.getStringExtra("userID").toString()
        val month = intent.getStringExtra("month").toString()
        val catRef = FirebaseFirestore.getInstance().document("Users/$userId/Months/$month/Categories/$category")

        val save = findViewById<Button>(R.id.save_button)
        val date = findViewById<EditText>(R.id.date)
        val remark = findViewById<EditText>(R.id.remark)
        val product_name = findViewById<EditText>(R.id.product_name)
        val price = findViewById<EditText>(R.id.price)

        save.setOnClickListener {
            val expense = hashMapOf(
                    "date" to date.text.toString(),
                    "description" to remark.text.toString(),
                    "title" to product_name.text.toString(),
                    "price" to price.text.toString().hashCode().toString()
            )
            catRef.collection("Users")
                    .add(expense)
                    .addOnSuccessListener { documentReference ->
                        Log.d("TAG", "DocumentSnapshot written with ID: ${documentReference.id}")
                        val name = product_name.text.toString()
                        showMessage("Saved Successfully!")
                        val i = Intent(this@Expense, Category::class.java)
                        i.putExtra("userID",userId)
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