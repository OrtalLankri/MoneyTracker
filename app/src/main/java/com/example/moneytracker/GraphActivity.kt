package com.example.moneytracker

import android.content.ContentValues
import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.jjoe64.graphview.DefaultLabelFormatter
import com.jjoe64.graphview.GraphView
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.LineGraphSeries
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*


class GraphActivity : AppCompatActivity() {

    lateinit var month : String
    lateinit var userId : String

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.activity_graph)
            CoroutineScope(Dispatchers.IO).launch { }

            supportActionBar?.setDisplayHomeAsUpEnabled(true)

            userId = intent.getStringExtra("userID").toString()
            month = intent.getStringExtra("month").toString()
            val userRef = FirebaseFirestore.getInstance().collection("Users").document(userId)
            val graph = findViewById<GraphView>(R.id.graph)
            val average = findViewById<TextView>(R.id.average)
            graph.title = "Monthly Expenses Distribution Graph"
            graph.titleTextSize = 50F
            val format = SimpleDateFormat("MM/yy")
            graph.gridLabelRenderer.labelFormatter = object : DefaultLabelFormatter() {
                override fun formatLabel(value: Double, isValueX: Boolean): String {
                    return if (isValueX) {
                        format.format(Date(value.toLong()))
                    } else {
                        super.formatLabel(value, isValueX)
                    }
                }
            }
            //retrieve all month documents
            userRef.collection("Months")
                .get()
                .addOnSuccessListener { result ->
                    var avg = 0.0
                    val arr = arrayOfNulls<DataPoint>(result.size())
                    graph.gridLabelRenderer.numHorizontalLabels = result.size()
                    var i = 0
                    for (document in result) {
                        var y = document.data["amount"].toString()
                        @Suppress("DEPRECATION")
                        var x = Date(document.id.toString().substring(2).toInt(), document.id.toString().substring(0, 2).toInt(), 1)
                        arr[i] = DataPoint(x, y.toDouble())
                        i += 1
                        Log.d(TAG, "${document.id} => ${document.data}")
                        avg += y.toDouble()
                    }
                    val series = LineGraphSeries(arr)
                    graph.addSeries(series)
                    if (i > 0) {
                        avg /= i.toDouble()
                        average.text = "AVERAGE: $avg"
                    }
                }
                .addOnFailureListener { exception ->
                    Log.d(TAG, "Error getting documents: ", exception)
                }
        }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        // back
        16908332 -> {
            val i = Intent(this@GraphActivity, MainActivity::class.java)
            i.putExtra("userID", userId)
            i.putExtra("month", month)
            startActivity(i)
            finish()
            true
        }
        else -> {
            Log.d(ContentValues.TAG, item.itemId.toString())
            super.onOptionsItemSelected(item)
        }
    }
}