package com.example.moneytracker

import android.app.Activity
import android.content.ContentValues.TAG
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.Image
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.google.android.gms.tasks.OnSuccessListener
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
class Camera : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.camera)
        CoroutineScope(Dispatchers.IO).launch { }

        val button = findViewById<Button>(R.id.button)
        val button2 = findViewById<Button>(R.id.button2)

        button.setOnClickListener {
            val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            photoFile = getPhotoFile(FILE_NAME)
            val fileProvider = FileProvider.getUriForFile(this, "com.example.moneytracker.fileprovider", photoFile)
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, fileProvider)
            if (takePictureIntent.resolveActivity(this.packageManager) != null) {
                startActivityForResult(takePictureIntent, REQUEST_CODE)
            }
            button2.isEnabled = true
        }

        button2.setOnClickListener {
            button2.isEnabled = false
            runTextRecognition()
        }
    }

    private fun getPhotoFile(fileName: String): File {
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(fileName, ".jpg", storageDir)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val image = findViewById<ImageView>(R.id.image)
        if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            bitmapImage = BitmapFactory.decodeFile(photoFile.absolutePath)
            image.setImageBitmap(bitmapImage)
            //image.rotation = 90F
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
                }

    }

    private fun processTextRecognitionResult(text: FirebaseVisionText) {
        val textView = findViewById<TextView>(R.id.textView)
        for (block in text.textBlocks) {
            textView.text = text.text
            textView.visibility = View.VISIBLE
            for (line in block.lines) {
                for (word in line.elements) {
                    Log.d("lineWord", word.text)
                }
            }
        }
    }
}