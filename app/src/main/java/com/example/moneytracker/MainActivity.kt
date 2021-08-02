package com.example.moneytracker

import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.Layout
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.moneytracker.ui.login.LoginActivity
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.getField
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*


enum class Months {
    January, February, March, April, May, June, July, August, September, October, November, December
}

class MainActivity : AppCompatActivity() {

    val CHANNEL_ID = "channel"

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        CoroutineScope(Dispatchers.IO).launch {  }

        createNotificationChannel()

        val userId = intent.getStringExtra("userID").toString()
        val userRef = FirebaseFirestore.getInstance().collection("Users").document(userId)
        val monthName = intent.getStringExtra("month").toString()
        val month = findViewById<TextView>(R.id.month)
        val cat = arrayListOf<Button>()
        cat.add(findViewById<Button>(R.id.c1))
        cat.add(findViewById<Button>(R.id.c2))
        cat.add(findViewById<Button>(R.id.c3))
        cat.add(findViewById<Button>(R.id.c4))
        cat.add(findViewById<Button>(R.id.c5))
        cat.add(findViewById<Button>(R.id.c6))
        val setBudget = findViewById<TextView>(R.id.setBudget)
        val amount = findViewById<TextView>(R.id.amount)
        val budget = findViewById<TextView>(R.id.budget)
        val addExpense = findViewById<Button>(R.id.add)
        val scan = findViewById<Button>(R.id.scan)
        val menu = findViewById<LinearLayout>(R.id.menu)
        val expanded_menu = findViewById<Button>(R.id.expanded_menu)
        val analysis = findViewById<Button>(R.id.analysis)
        val graph = findViewById<Button>(R.id.graph)
        val settings = findViewById<Button>(R.id.settings)
        val prev = findViewById<ImageButton>(R.id.prev)
        val next = findViewById<ImageButton>(R.id.next)
        val layout = findViewById<ConstraintLayout>(R.id.layout)
        val logout = findViewById<Button>(R.id.logout)

        val currentDate = if (monthName == "null") {
            SimpleDateFormat("MMyy").format(Date())
        } else {
            monthName
        }

        if (currentDate == SimpleDateFormat("MMyy").format(Date())) {
            next.visibility = View.GONE
        }

        val monthIndex= currentDate.substring(0, 2).toInt()

        menu.visibility = View.GONE

        fun setInfo(ref: DocumentSnapshot){
            val data = ref.data!!
            // set categories names
            val s = data["categories"].toString().removePrefix("{").removeSuffix("}")
            val categories = s.split(", ").associate {
                val (left, right) = it.split("=")
                left to right.toString()
            }
            for (i in 1..6) {
                cat[i - 1].text = categories["c$i"]
                if (categories["c$i"]!!.length > 8) {
                    cat[i - 1].textSize = 10.toFloat()
                } else if (categories["c$i"]!!.length > 6) {
                    cat[i - 1].textSize = 12.toFloat()
                } else {
                    cat[i - 1].textSize = 14.toFloat()
                }
            }
            // set budget
            if (data["budget"].toString() != "0") {
                setBudget.visibility = View.GONE
                amount.visibility = View.VISIBLE
                budget.visibility = View.VISIBLE
                budget.text = data["budget"].toString()+ "$"
                amount.text = data["amount"].toString()+ "$"
            }
            // set progress bar
            updateProgressBar(data["amount"].toString().toDouble(), data["budget"].toString().toDouble())
            // set month name
            month.text = data["name"].toString()
        }

        fun createNewMonth(categories: Object) {
            val name = Months.values()[monthIndex - 1].name
            val newMonth = hashMapOf(
                    "name" to name,
                    "budget" to 0,
                    "amount" to 0,
                    "categories" to categories,
                    "categoriesBudget" to 0
            )
            userRef.collection("Months").document(currentDate)
                    .set(newMonth).addOnSuccessListener {
                        Log.d("TAG", "Month document added successfully")
                    }
                    .addOnFailureListener { e ->
                        Log.w("TAG", "Error adding month document", e)
                    }
        }

        fun setDefaultInfo() {
            userRef.get().addOnCompleteListener {
                if (it.isSuccessful) {
                    val documentReference = it.result!!
                    val categories = documentReference.getField<Object>("defaultCategories")!!
                    createNewMonth(categories)
                }
            }
        }

        fun getDoc() {
            // check if the current month has data already
            userRef.collection("Months").document(currentDate)
                    .get()
                    .addOnCompleteListener {
                        if (it.isSuccessful) {
                            Log.d("TAG", "successful")
                            val result = it.result!!
                            // if current month exists already
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

        addExpense.setOnClickListener {
            val i = Intent(this@MainActivity, Expense::class.java)
            i.putExtra("userID", userId)
            i.putExtra("month", currentDate)
            i.putExtra("expenseId", "null")
            i.putExtra("catNum", "null")
            startActivity(i)
        }

        scan.setOnClickListener {
            val i = Intent(this@MainActivity, ScanActivity::class.java)
            i.putExtra("userID", userId)
            i.putExtra("month", currentDate)
            startActivity(i)
        }

        expanded_menu.setOnClickListener {
            expanded_menu.visibility = View.GONE
            menu.visibility = View.VISIBLE
            menu.bringToFront()
            layout.setOnClickListener {
                menu.visibility = View.GONE
                expanded_menu.visibility = View.VISIBLE
            }
        }

        settings.setOnClickListener {
            val i = Intent(this@MainActivity, SettingsActivity::class.java)
            i.putExtra("userID", userId)
            i.putExtra("month", currentDate)
            startActivity(i)
        }

        analysis.setOnClickListener {
            val i = Intent(this@MainActivity, AnalysisActivity::class.java)
            i.putExtra("userID", userId)
            i.putExtra("month", currentDate)
            startActivity(i)
        }

        graph.setOnClickListener {
            val i = Intent(this@MainActivity, GraphActivity::class.java)
            i.putExtra("userID", userId)
            i.putExtra("month", currentDate)
            startActivity(i)
        }

        for (j in 1..6) {
            cat[j - 1].setOnClickListener {
                val i = Intent(this@MainActivity, Category::class.java)
                i.putExtra("userID", userId)
                i.putExtra("month", currentDate)
                i.putExtra("name", cat[j - 1].text.toString())
                i.putExtra("catNum", "c$j")
                startActivity(i)
            }
        }

        setBudget.setOnClickListener {
            val i = Intent(this@MainActivity, SettingsActivity::class.java)
            i.putExtra("userID", userId)
            i.putExtra("month", currentDate)
            startActivity(i)
        }

        next.setOnClickListener {
            val m = AnalysisActivity().next(currentDate)
            val i = Intent(this@MainActivity, MainActivity::class.java)
            i.putExtra("userID", userId)
            i.putExtra("month", m)
            startActivity(i)
            finish()
        }

        prev.setOnClickListener {
            val m = AnalysisActivity().prev(currentDate)
            val i = Intent(this@MainActivity, MainActivity::class.java)
            i.putExtra("userID", userId)
            i.putExtra("month", m)
            startActivity(i)
            finish()
        }


        logout.setOnClickListener {
            val i = Intent(this@MainActivity, LoginActivity::class.java)
            startActivity(i)
            finish()
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
        notify(percent)
    }


    private fun notify(percent: Int) {
        if (percent >= 100) {
            notification("You Have Reached The Budget Limit", "Please note that you have " +
                    "reached the budget limit you have set for this month.\nYou can always try again next month")
        } else if (percent >= 97) {
            notification("Getting Close To Budget", "The amount you have spent this month" +
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

    private fun createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            val name = getString(R.string.channel_name)
            val name = "Notification"
//            val descriptionText = getString(R.string.channel_description)
            val descriptionText = "This is the notification for the MoneyTracker App"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                    getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

//    private fun scheduleNotification(title: String, text: String, delay: Long) {
//        Log.d("Timer", "called")
//        var builder = NotificationCompat.Builder(this, CHANNEL_ID)
//                .setContentTitle(title)
//                .setContentText(text)
//                .setAutoCancel(true)
//                .setSmallIcon(R.drawable.notification_icon)
//        val intent = Intent(this, MainActivity::class.java)
//        val activity = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT)
//        builder.setContentIntent(activity)
//        val notification = builder.build()
//        val notificationIntent = Intent(this, MainActivity::class.java)
//        notificationIntent.putExtra(MyNotificationPublisher.NOTIFICATION_ID, 0)
//        notificationIntent.putExtra(MyNotificationPublisher.NOTIFICATION, notification)
//        val pendingIntent = PendingIntent.getBroadcast(this, 0, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT)
//        val futureInMillis = SystemClock.elapsedRealtime() + delay
//        val alarmManager = this.getSystemService(ALARM_SERVICE) as AlarmManager
//        alarmManager[AlarmManager.ELAPSED_REALTIME_WAKEUP, futureInMillis] = pendingIntent
//        Log.d("Timer", "finished")
//
//    }


}