package com.example.moneytracker
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.getField
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*


class Category: AppCompatActivity(){
    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_category)
        CoroutineScope(Dispatchers.IO).launch {  }

        val userId = intent.getStringExtra("userID").toString()
        val month = intent.getStringExtra("month").toString()
        val monthRef = FirebaseFirestore.getInstance().document("Users/$userId/Months/$month")
        val catName = intent.getStringExtra("name").toString()
        val catNum = intent.getStringExtra("catNum").toString()
        Log.d("TAG","\"Users/$userId/Months/$month/Categories")

        val addButton = findViewById<Button>(R.id.add)
        val categoryTitle = findViewById<TextView>(R.id.Category)
        val setBudget = findViewById<TextView>(R.id.setBudget)
        val amount = findViewById<TextView>(R.id.amount)
        val budget = findViewById<TextView>(R.id.budget)

        val expenses = ArrayList<String>()

        val layout = findViewById(R.id.layout) as LinearLayout
//        val btnList: MutableList<Button> = ArrayList()
//        val button = Button(this)
//        button.layoutParams = LinearLayout.LayoutParams(
//            LinearLayout.LayoutParams.WRAP_CONTENT,
//            ViewGroup.LayoutParams.WRAP_CONTENT)
//        layout.addView(button)
//        btnList.add(button)

        addButton.setOnClickListener {
            val i = Intent(this@Category, Expense::class.java)
            i.putExtra("userID", userId)
            i.putExtra("month", month)
            i.putExtra("category", catName)
            i.putExtra("catNum", catNum)
            i.putExtra("expenseId", "null")
            startActivity(i)
        }

        fun setExpenses(listString: String) {
            val bString =listString.removePrefix("{").removeSuffix("}")
            val map = bString.split(", ").associate {
                val (left, right) = it.split("=")
                left to right.toString()
            }
            for ((id, expense) in map) {
                val button = Button(this)
                button.layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT)
                layout.addView(button)
                button.text = expense
                button.setOnClickListener {
                    val i = Intent(this@Category, Expense::class.java)
                    i.putExtra("userID", userId)
                    i.putExtra("month", month)
                    i.putExtra("category", catName)
                    i.putExtra("catNum", catNum)
                    i.putExtra("expenseId", id)
                    startActivity(i)
                }
            }
        }

        fun setInfo(ref: DocumentSnapshot){
            val data = ref.data!!
            // set category name
            categoryTitle.text = data["name"].toString()
            // set budget
            if (data["budget"].toString() != "0") {
                setBudget.visibility = View.GONE
                amount.visibility = View.VISIBLE
                budget.visibility = View.VISIBLE
                budget.text = data["budget"].toString() + "$"
                amount.text = data["amount"].toString() + "$"
            }
            // set progress bar
            updateProgressBar(
                data["amount"].toString().toDouble(),
                data["budget"].toString().toDouble()
            )
            // set expenses list
//            setExpenses(data["expenses"].toString())
        }

        fun createNewCategory(catBudget: String, name: String) {
            val category = hashMapOf(
                "name" to name,
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
                    val cats = documentReference.getField<Object>("categories")!!
                    var s = cats.toString().removePrefix("{").removeSuffix("}")
                    val nameMap = s.split(", ").associate {
                        val (left, right) = it.split("=")
                        left to right.toString()
                    }
                    val name = nameMap[catNum].toString()
                    if (budget.toString() == "0") {
                        createNewCategory("0", name)
                    }
                    else {
                        val s = budget.toString().removePrefix("{").removeSuffix("}")
                        val budgets = s.split(", ").associate {
                            val (left, right) = it.split("=")
                            left to right.toString()
                        }
                        createNewCategory(budgets[catNum].toString(), name)
                    }
                }
            }
            createNewCategory("0", catName)
        }

        fun getDoc() {
            Log.d("TAG", "get doc category ~~~~~~~~~~~~")

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

        setBudget.setOnClickListener {
            val i = Intent(this@Category, SettingsActivity::class.java)
            i.putExtra("userID", userId)
            i.putExtra("month", month)
            startActivity(i)
        }

    }

    fun updateProgressBar(amount: Double, budget: Double) {
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