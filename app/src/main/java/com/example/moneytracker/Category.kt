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

class Category: AppCompatActivity(){
    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        CoroutineScope(Dispatchers.IO).launch {  }

        val userId = intent.getStringExtra("userID").toString()
        val month = intent.getStringExtra("month").toString()
        val monthRef = FirebaseFirestore.getInstance().document("Users/$userId/Months/$month")
        val catName = intent.getStringExtra("name").toString()
        val catNum = intent.getStringExtra("catNum").toString()
        val add = findViewById<Button>(R.id.add)
        val Category = findViewById<TextView>(R.id.Category)
        val setBudget = findViewById<TextView>(R.id.setBudget)
        val amount = findViewById<TextView>(R.id.amount)
        val budget = findViewById<TextView>(R.id.budget)
        val expenses = ArrayList<String>()


        add.setOnClickListener {
            val i = Intent(this@Category, Expense::class.java)
            i.putExtra("userID",userId)
            i.putExtra("Date", month)
            i.putExtra("category", catName)
            i.putExtra("catNum", catNum)
            startActivity(i)
        }

        fun setInfo(ref: DocumentSnapshot){
            val data = ref.data!!
            // set month name
            Category.text = catName
            // set categories names
            val s = data["categories"].toString().removePrefix("{").removeSuffix("}")
            val categories = s.split(", ").associate {
                val (left, right) = it.split("=")
                left to right.toString()
            }
            // set budget
            if (data["budget"].toString() != "0") {
                setBudget.visibility = View.GONE
                amount.visibility = View.VISIBLE
                budget.visibility = View.VISIBLE
            }
            // set progress bar
            updateProgressBar(data["amount"].toString().toDouble(), data["budget"].toString().toDouble())
        }

        fun createNewCategory(catBudget: String) {
            val category = hashMapOf(
                    "name" to Category,
                    "budget" to catBudget,
                    "amount" to 0,
                    "expenses" to expenses
            )
            monthRef.collection("Categories").document(catNum)
                    .set(category).addOnSuccessListener {
                        Log.d("TAG", "Month document added successfully")
                    }
                    .addOnFailureListener { e ->
                        Log.w("TAG", "Error adding month document", e)
                    }
        }


        fun setDefaultInfo() {
            monthRef.get().addOnCompleteListener {
                if (it.isSuccessful) {
                    val documentReference = it.result!!
                    val budget = documentReference.getField<Object>("categoriesBudget")!!
                    if (budget.toString() == "0") {
                        createNewCategory("0")
                    }
                    else {
                        val s = budget.toString().removePrefix("{").removeSuffix("}")
                        val budgets = s.split(", ").associate {
                            val (left, right) = it.split("=")
                            left to right.toString()
                        }
                        createNewCategory(budgets[catNum].toString())
                    }
                }
            }
            createNewCategory("0")
        }

        fun getDoc() {
            // check if the current category has data already
            monthRef.collection("Categories").document(catNum)
                    .get()
                    .addOnCompleteListener {
                        if (it.isSuccessful) {
                            Log.d("TAG", "successful")
                            val result = it.result!!
                            // if current category exists already
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

    }
    fun updateProgressBar(amount: Double, budget:Double) {
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