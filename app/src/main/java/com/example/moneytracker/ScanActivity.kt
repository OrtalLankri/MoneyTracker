package com.example.moneytracker

import android.app.Activity
import android.content.ContentValues.TAG
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Point
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.text.FirebaseVisionText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

private const val FILE_NAME = "photo.jpg"
private const val REQUEST_CODE = 8
private lateinit var photoFile : File
private lateinit var bitmapImage : Bitmap

@Suppress("DEPRECATION")
class ScanActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scan)
        CoroutineScope(Dispatchers.IO).launch { }

        val userId = intent.getStringExtra("userID").toString()
        val month = intent.getStringExtra("month").toString()
        val button = findViewById<Button>(R.id.button)
        val save  = findViewById<Button>(R.id.save)
        val back = findViewById<Button>(R.id.back)
        val progressBar = findViewById<ProgressBar>(R.id.progressBar)
        val image = findViewById<ImageView>(R.id.image)

        button.setOnClickListener {
            val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            photoFile = getPhotoFile(FILE_NAME)
            val fileProvider = FileProvider.getUriForFile(this, "com.example.moneytracker.fileprovider", photoFile)
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, fileProvider)
            if (takePictureIntent.resolveActivity(this.packageManager) != null) {
                startActivityForResult(takePictureIntent, REQUEST_CODE)
            }
        }

        image.setOnClickListener {
            val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            photoFile = getPhotoFile(FILE_NAME)
            val fileProvider = FileProvider.getUriForFile(this, "com.example.moneytracker.fileprovider", photoFile)
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, fileProvider)
            if (takePictureIntent.resolveActivity(this.packageManager) != null) {
                startActivityForResult(takePictureIntent, REQUEST_CODE)
            }
        }

        save.setOnClickListener {
            save.isEnabled = false
            progressBar.visibility = View.VISIBLE
            runTextRecognition()
        }

        back.setOnClickListener {
            val i = Intent(this@ScanActivity, MainActivity::class.java)
            i.putExtra("userID", userId)
            i.putExtra("month", month)
            startActivity(i)
        }
    }

    private fun getPhotoFile(fileName: String): File {
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(fileName, ".jpg", storageDir)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val image = findViewById<ImageView>(R.id.image)
        val button = findViewById<Button>(R.id.button)
        val save  = findViewById<Button>(R.id.save)
        if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            bitmapImage = BitmapFactory.decodeFile(photoFile.absolutePath)
            image.setImageBitmap(bitmapImage)
            image.visibility = View.VISIBLE
            image.isClickable = false
            button.text = "retake picture"
            button.setBackgroundColor(Color.WHITE)
            button.setTextColor(Color.rgb(98, 0, 238))
            save.visibility = View.VISIBLE
            save.isEnabled = true
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun runTextRecognition() {
        val firebaseImage = FirebaseVisionImage.fromBitmap(bitmapImage)
        val detector = FirebaseVision.getInstance().onDeviceTextRecognizer
        detector.processImage(firebaseImage)
                .addOnSuccessListener{ firebaseVisionText ->
                    processTextRecognitionResult(firebaseVisionText)
                }
                .addOnFailureListener { e ->
                    Log.w(TAG, "Failed to process image to text", e)
                    showMessage("Error: Please try again")
                }

    }

    private fun processTextRecognitionResult(text: FirebaseVisionText) {
        var price = ""
        lateinit var pricePoints :  Array<Point>
        var isPriceInitialize = false
        var date = ""
        lateinit var datePoints :  Array<Point>
        var isDateInitialize = false
        // get price and date
        for (block in text.textBlocks) {
            for (line in block.lines) {
                for (word in line.elements) {
                    // get price
                    if (price == "") {
                        if (!isPriceInitialize && (word.text == "TOTAL" || word.text == "Total" || word.text == "total")) {
                            pricePoints = word.cornerPoints as Array<Point>
                            isPriceInitialize = true
                        }
                        else if (isPriceInitialize) {
                            val wordPoints = word.cornerPoints as Array<Point>
                            if (word.text.contains('.') && inRange(pricePoints, wordPoints)) {
                                price = word.text
                            }
                        }
                    }
                    // get date
                    if (date == "") {
                        if (!isDateInitialize && (word.text == "DATE" || word.text == "Date" || word.text == "date")) {
                            datePoints = word.cornerPoints as Array<Point>
                            isDateInitialize = true
                        } else if (isDateInitialize) {
                            val wordPoints = word.cornerPoints as Array<Point>
                            if ((word.text.contains('.') || word.text.contains('/') || word.text.contains('-'))
                                    && inRange(datePoints, wordPoints)) {
                                date = word.text
                            }
                        }
                    }
                }
            }
        }
        // get price and date
        if (date == ""  || price == "") {
            for (block in text.textBlocks) {
                for (line in block.lines) {
                    for (word in line.elements) {
                        // get price
                        if (price == "") {
                            if (!isPriceInitialize && (word.text == "TOTAL" || word.text == "Total" || word.text == "total")) {
                                pricePoints = word.cornerPoints as Array<Point>
                                isPriceInitialize = true
                            } else if (isPriceInitialize) {
                                val wordPoints = word.cornerPoints as Array<Point>
                                if (word.text.contains('.') && inRange(pricePoints, wordPoints)) {
                                    price = word.text
                                }
                            }
                        }
                        // get date
                        if (date == "") {
                            if (!isDateInitialize && (word.text == "DATE" || word.text == "Date" || word.text == "date")) {
                                datePoints = word.cornerPoints as Array<Point>
                                isDateInitialize = true
                            } else if (isDateInitialize) {
                                val wordPoints = word.cornerPoints as Array<Point>
                                if ((word.text.contains('.') || word.text.contains('/') || word.text.contains(
                                        '-'
                                    ))
                                    && inRange(datePoints, wordPoints)
                                ) {
                                    date = word.text
                                }
                            }
                        }
                    }
                }
            }
        }
        // if could not find information
        if (date == ""  || price == "") {
            showMessage("Could not process picture. Please try again.")
            val progressBar = findViewById<ProgressBar>(R.id.progressBar)
            progressBar.visibility = View.GONE
        }
        // else add to expense
        else {
            var title = text.textBlocks[0].lines[0].text
            Log.d("result title:", title)
            Log.d("result price:", price)
            Log.d("result date:", date)
            val userId = intent.getStringExtra("userID").toString()
            val month = intent.getStringExtra("month").toString()
            val i = Intent(this@ScanActivity, Expense::class.java)
            i.putExtra("userID", userId)
            i.putExtra("month", month)
            i.putExtra("expenseId", "scan")
            i.putExtra("catNum", "null")
            i.putExtra("scanTitle", title)
            i.putExtra("scanPrice", price)
            i.putExtra("scanDate", date)
            startActivity(i)
            finish()
        }
    }

    private fun showMessage(s: String) {
        Toast.makeText(applicationContext, s, Toast.LENGTH_LONG).show()
    }

    private fun inRange(checkPoints : Array<Point>, wordPoints: Array<Point>) : Boolean {
        for (i in 0..3) {
            if (checkPoints[i].y - wordPoints[i].y > 20 || checkPoints[i].y - wordPoints[i].y < -20) {
                return false
            }
        }
        return true
    }
}


