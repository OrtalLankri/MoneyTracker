package com.example.moneytracker
import android.annotation.SuppressLint
import android.content.ContentValues.TAG
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.moneytracker.ui.login.LoginActivity
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.getField
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class Category: AppCompatActivity(){
    @SuppressLint("SetTextI18n")

    val CHANNEL_ID = "channel"
    var categoryName = "null"
    lateinit var month : String
    lateinit var userId : String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_category)
        CoroutineScope(Dispatchers.IO).launch {  }

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        userId = intent.getStringExtra("userID").toString()
        month = intent.getStringExtra("month").toString()
        val monthRef = FirebaseFirestore.getInstance().document("Users/$userId/Months/$month")
        var catName = intent.getStringExtra("name").toString()
        categoryName = catName
        val catNum = intent.getStringExtra("catNum").toString()
        // set background color
        val colors = hashMapOf<String, String>(
                "c1" to "#FBC1D5",
                "c2" to "#F4B5FF",
                "c3" to "#A4E3FF",
                "c4" to "#B9F6CA",
                "c5" to "#F6FF97",
                "c6" to "#FFE57F"
        )
        val root = findViewById<View>(R.id.Category).rootView
        root.setBackgroundColor(Color.parseColor(colors[catNum]))
        val addButton = findViewById<Button>(R.id.add)
        val categoryTitle = findViewById<TextView>(R.id.Category)
        val setBudget = findViewById<TextView>(R.id.setBudget)
        val amount = findViewById<TextView>(R.id.amount)
        val budget = findViewById<TextView>(R.id.budget)

        val expenses = HashMap<String, String>()

        val layout = findViewById<LinearLayout>(R.id.layout)

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
            val bString = listString.removePrefix("{").removeSuffix("}")
            val map = bString.split(", ").associate {
                val (left, right) = it.split("=")
                left to right.toString()
            }
            for ((id, expense) in map) {
                val button = Button(this)
                button.layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT)
                layout.addView(button)
                button.text = expense
                button.isAllCaps = false
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

        fun setInfo(ref: DocumentSnapshot) {
            val data = ref.data!!
            // set category name
            categoryTitle.text = data["name"].toString()
            catName = data["name"].toString()
            categoryName = catName
            // set budget and amount
            if (data["budget"].toString() != "0" && data["budget"].toString() != "") {
                setBudget.visibility = View.GONE
                amount.visibility = View.VISIBLE
                budget.visibility = View.VISIBLE
                budget.text = data["budget"].toString() + "$"
                amount.text = data["amount"].toString() + "$"
                // set progress bar
                updateProgressBar(
                        data["amount"].toString().toDouble(),
                        data["budget"].toString().toDouble()
                )
            }
            // set expenses list
            if (data["expenses"].toString() != "{}") {
                setExpenses(data["expenses"].toString())
            }
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

    private fun updateProgressBar(amount: Double, budget: Double) {
        val pb = findViewById<ProgressBar>(R.id.progressBar)
        val pbRed = findViewById<ProgressBar>(R.id.progressBarRed)
        pb.visibility = View.VISIBLE
        pbRed.visibility = View.INVISIBLE
        var percent = 0
        if (budget > 0) {
            percent = (amount / budget * 100).toInt()
        }
        if (percent >= 100) {
            percent = 100
            pb.visibility = View.INVISIBLE
            pbRed.visibility = View.VISIBLE
        }
        if (percent < 0) {
            percent = 0
        }
        pb.progress = percent
        if(intent.getStringExtra("notify").toString() == "true" && percent > 97) {
            notify(percent)
        }
    }

    private fun notify(percent: Int) {
        if (percent >= 100) {
            notification("You Have Reached The \"$categoryName\" Budget", "Please note that you have " +
                    "reached the budget limit you have set for the \"$categoryName\" category")
        } else {
            notification("Getting Close To \"$categoryName\"'s Budget", "The amount you have spent on \"$categoryName\"" +
                    " is getting close to the budget limit.\nwatch your spending carefully")
        }
    }

    private fun notification(title: String, text: String) {
        var builder = NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.notification_icon)
                .setContentTitle(title)
                .setContentText(text)
                .setStyle(NotificationCompat.BigTextStyle()
                        .bigText(text))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
        val notificationManager = NotificationManagerCompat.from(this)
        notificationManager.notify(0, builder.build())

    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        // back
        16908332 -> {
            val i = Intent(this@Category, MainActivity::class.java)
            i.putExtra("userID", userId)
            i.putExtra("month", month)
            i.putExtra("notify", intent.getStringExtra("notify").toString())
            startActivity(i)
            finish()
            true
        }
        else -> {
            Log.d(TAG, item.itemId.toString())
            super.onOptionsItemSelected(item)
        }
    }

}
