import androidx.appcompat.app.AppCompatActivity
import com.example.moneytracker.AppDB

//package com.example.moneytracker
//import android.content.Intent
//import androidx.appcompat.app.AppCompatActivity
//import android.os.Bundle
//import android.text.Editable
//import android.widget.*
//import kotlinx.android.synthetic.main.activity_main.*
//import kotlinx.coroutines.CoroutineScope
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.GlobalScope
//import kotlinx.coroutines.launch
//import Api
//import android.graphics.Bitmap
//import android.graphics.BitmapFactory
//import android.util.Log
//import android.widget.SeekBar
//import android.widget.TextView
//import com.google.gson.GsonBuilder
//import io.github.controlwear.virtual.joystick.android.JoystickView
//import kotlinx.android.synthetic.main.joystick.*
//import kotlinx.coroutines.*
//import kotlinx.coroutines.Dispatchers.IO
//import okhttp3.MediaType
//import okhttp3.RequestBody
//import okhttp3.ResponseBody
//import retrofit2.Call
//import retrofit2.Callback
//import retrofit2.Response
//import retrofit2.Retrofit
//import retrofit2.converter.gson.GsonConverterFactory
//import kotlin.concurrent.fixedRateTimer
//import kotlin.math.abs
//import kotlin.math.cos
//import kotlin.math.sin
//var isLocal= false
//
//
class MainActivity2 : AppCompatActivity() {
    public fun logIn(username: String, password: String):Int{
        val db: AppDB = AppDB.getInstance(this)
        val id = db.userDAO().getId(username, password)
        return id
    }
}
//    companion object {
//        lateinit var bitmap: Bitmap
//    }
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_main)
//
//        //function for the connect button:
//        findViewById<Button>(R.id.connect_button).setOnClickListener(){
//            buttonConnect()
//        }
//        //function LISTENER for the button URL1:
//        findViewById<Button>(R.id.url1).setOnClickListener(){
//            buttonLocalHost1()
//        }
//        //function LISTENER for the button URL2:
//        findViewById<Button>(R.id.url2).setOnClickListener(){
//            buttonLocalHost2()
//        }
//        //function LISTENER for the button URL3:
//        findViewById<Button>(R.id.url3).setOnClickListener(){
//            buttonLocalHost3()
//        }
//        //function LISTENER for the button URL4:
//        findViewById<Button>(R.id.url4).setOnClickListener(){
//            buttonLocalHost4()
//        }
//        //function LISTENER for the button URL5:
//        findViewById<Button>(R.id.url5).setOnClickListener(){
//            buttonLocalHost5()
//        }
//    }
//
//    private fun buttonConnect(){
//        val urlText = findViewById<EditText>(R.id.typeUrl).text.toString()
//        val db:AppDB=AppDB.getInstance(this)
//
//        //if text box is empty: user needs to insert something
//        if(urlText.equals("")){
//            Toast.makeText(
//                this@MainActivity,
//                "please type url, or click one of the local hosts buttons.", Toast.LENGTH_SHORT
//            ).show()
//        } else {//otherwise textbox is not empty:
//
//            if (isLocal) { //if local host is pressed
//                CoroutineScope(IO).launch {
//                    db.urlDAO().updateUrl(urlText, System.currentTimeMillis())
//                }
//                //connect to server
//                tryConnectToServer(urlText)
//
//            } else { //if typed new url
//                val uUrl = typeUrl.text.toString()
//                val url1 = UrlEntity()
//                url1.url_name = uUrl
//                url1.URL_Date = System.currentTimeMillis()
//
//                CoroutineScope(IO).launch {
//                    db.urlDAO().saveUrl(url1)
//                }
//                //connect to server
//                tryConnectToServer(urlText)
//            }
//        }
//    }
//
//    private fun buttonLocalHost1(){
//        val db: AppDB = AppDB.getInstance(this)
//
//        CoroutineScope(IO).launch {
//            val listUrls = db.urlDAO().getRecentUrl()
//
//            //check if there is no url in the database
//            if (listUrls.isEmpty()) {
//                GlobalScope.launch(Dispatchers.Main) {
//                    Toast.makeText(
//                        this@MainActivity,
//                        "No url in local host, please try again", Toast.LENGTH_SHORT
//                    ).show()
//                }
//
//
//            } else { //if there is url in the database at this place:
//                val url1string = listUrls[0]
//
//                //displaying the url in the edit text:
//                val editText = findViewById<EditText>(R.id.typeUrl)
//                editText.text = Editable.Factory.getInstance().newEditable(url1string)
//
//                //updating the url date in the database:
//                db.urlDAO().updateUrl(url1string, System.currentTimeMillis())
//
//            }
//        }
//    }
//
//    private fun buttonLocalHost2(){
//        val db: AppDB = AppDB.getInstance(this)
//
//        CoroutineScope(IO).launch {
//            val listUrls = db.urlDAO().getRecentUrl()
//
//            //check if there is no url in the database at this place:
//            if (listUrls.size < 2) {
//                GlobalScope.launch(Dispatchers.Main) {
//                    Toast.makeText(
//                        this@MainActivity,
//                        "No url in local host, please try again", Toast.LENGTH_SHORT
//                    ).show()
//                }
//
//            }else{ //if there url in the database at thid place:
//                val url1string = listUrls[1]
//                //displaying the url in the edit text:
//                val editText = findViewById<EditText>(R.id.typeUrl)
//                editText.text = Editable.Factory.getInstance().newEditable(url1string)
//
//                //updating the url date in the database:
//                db.urlDAO().updateUrl(url1string, System.currentTimeMillis())
//            }
//        }
//    }
//
//    private fun buttonLocalHost3(){
//        val db: AppDB = AppDB.getInstance(this)
//
//        CoroutineScope(IO).launch {
//            val listUrls = db.urlDAO().getRecentUrl()
//
//            //val url1string = db.urlDAO().getRecentUrl()[2]
//            //check if there is no url in the database
//            if (listUrls.size < 3) {
//                GlobalScope.launch(Dispatchers.Main) {
//                    Toast.makeText(
//                        this@MainActivity,
//                        "No url in local host, please try again", Toast.LENGTH_SHORT
//                    ).show()
//                }
//            }else{ //if there url in the database at third place:
//                val url1string = listUrls[2]
//
//                //displaying the url in the edit text:
//                val editText = findViewById<EditText>(R.id.typeUrl)
//                editText.text = Editable.Factory.getInstance().newEditable(url1string)
//
//                //updating the url date in the database:
//                db.urlDAO().updateUrl(url1string, System.currentTimeMillis())
//            }
//        }
//    }
//
//    private fun buttonLocalHost4(){
//        val db: AppDB = AppDB.getInstance(this)
//
//        CoroutineScope(IO).launch {
//            val listUrls = db.urlDAO().getRecentUrl()
//
//            //val url1string = db.urlDAO().getRecentUrl()[3]
//            //check if there is no url in the database
//            if (listUrls.size < 4) {
//
//                GlobalScope.launch(Dispatchers.Main) {
//                    Toast.makeText(
//                        this@MainActivity,
//                        "No url in local host, please try again", Toast.LENGTH_SHORT
//                    ).show()
//                }
//            }else{ //if there url in the database at third place:
//                val url1string = listUrls[3]
//
//                //displaying the url in the edit text:
//                val editText = findViewById<EditText>(R.id.typeUrl)
//                editText.text = Editable.Factory.getInstance().newEditable(url1string)
//
//                //updating the url date in the database:
//                db.urlDAO().updateUrl(url1string, System.currentTimeMillis())
//
//            }
//        }
//    }
//
//    private fun buttonLocalHost5(){
//        val db: AppDB = AppDB.getInstance(this)
//
//        CoroutineScope(IO).launch {
//            val listUrls = db.urlDAO().getRecentUrl()
//
//            //val url1string = db.urlDAO().getRecentUrl()[4]
//            //check if there is no url in the database
//            if (listUrls.size < 5) {
//
//                GlobalScope.launch(Dispatchers.Main) {
//                    Toast.makeText(
//                        this@MainActivity,
//                        "No url in local host, please try again", Toast.LENGTH_SHORT
//                    ).show()
//                }
//            }else{ //if there url in the database at third place:
//                val url1string = listUrls[4]
//
//                //displaying the url in the edit text:
//                val editText = findViewById<EditText>(R.id.typeUrl)
//                editText.text = Editable.Factory.getInstance().newEditable(url1string)
//
//                //updating the url date in the database:
//                db.urlDAO().updateUrl(url1string, System.currentTimeMillis())
//
//            }
//        }
//    }
//
//    private fun tryConnectToServer(url : String) {
//        try {
//            Toast.makeText(
//                applicationContext,
//                "Logging...",
//                Toast.LENGTH_LONG
//            ).show()
//            val gson = GsonBuilder().setLenient().create()
//            val retrofit = Retrofit.Builder().baseUrl(url)
//                .addConverterFactory(GsonConverterFactory.create(gson)).build()
//            val api = retrofit.create(Api::class.java)
//            api.getImg().enqueue(object : Callback<ResponseBody> {
//                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
//                    println("ERROR:"+t)
//                    Toast.makeText(
//                        applicationContext,
//                        "Can't Connect, try again",
//                        Toast.LENGTH_SHORT
//                    ).show()
//                    return
//                }
//
//                override fun onResponse(
//                    call: Call<ResponseBody>,
//                    response: Response<ResponseBody>
//                ) {
//                    val inputStream = response.body()?.byteStream()
//
//
//                    if (inputStream != null) {
//                        bitmap = BitmapFactory.decodeStream(inputStream)
//                        nextActivity(url)
//                    }else{
//                        Toast.makeText(
//                            applicationContext,
//                            "Can't get an image from the flight gear",
//                            Toast.LENGTH_SHORT
//                        ).show()
//                    }
//                }
//            })
//        } catch (e : Exception) {
//            Toast.makeText(
//                applicationContext,
//                "Can't Connect, try again",
//                Toast.LENGTH_SHORT
//            ).show()
//        }
//    }
//
//    private fun nextActivity(url : String) {
//        // create the second screen
//        val intent = Intent(this, Joystick::class.java)
//        intent.putExtra("url", url)
//        startActivity(intent)
//    }
//
//
//
//}
//
//
//
//
//
//
