package com.example.moneytracker

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.common.api.Response
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class SigninActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signin)
        CoroutineScope(Dispatchers.IO).launch { }

        val button = findViewById<Button>(R.id.signin)
        val db_users = FirebaseFirestore.getInstance().collection("Users")

        button.setOnClickListener {
            // check that user does not exist
            val email = findViewById<EditText>(R.id.editTextTextEmailAddress).text.toString()
//            val email = "ortal"
            val db = FirebaseFirestore.getInstance()
//            val db = Firebase.firestore
            db.collection("Users")
                    .whereEqualTo("email", email).get()
                    .addOnCompleteListener {
                        if (it.isSuccessful) {
                            Log.d("TAG", "successful")
                            val res = it.result!!
                            if(res.isEmpty) {
                                Log.d("TAG", "Empty")
                            }
                            for (document in res.documents) {
                                val e = document.get("email").toString()
                                Log.d("TAG", e)
                            }

                        }
                    }
            /*
//            if (snapshot == null) {
//                Log.d("TAG", "NULL")
//            }
//            if (snapshot != null) {
//                Log.d("TAG", "NOT NULL")
//            }
//            val db = FirebaseFirestore.getInstance().collection("Users").document("XSrBh5bZmqPfhJIfMAYl")
//            db.get().addOnSuccessListener { documentReference ->
//                Log.d("TAG", "DocumentSnapshot recieved")
//                val s = documentReference.getString("email").toString()
//                Log.d("TAG", s)
//            }
//                    .addOnFailureListener { e ->
//                        Log.w("TAG", "Error getting document", e)
//                    }

//             db_users.whereEqualTo("email", "ortal")
//                    .get()
//                     .addOnCompleteListener { task ->
//                         if (task.isSuccessful) {
//                             val result = task.result
//                             val a = 0
//                         }
//                     }
//                    .addOnCompleteListener  { documentReference ->
//                        Log.d("TAG", "DocumentSnapshot recieved")
//                        val s = documentReference.get.toString()
//                        Log.d("TAG", s)
//                    }
//                            .addOnFailureListener { e ->
//                                Log.w("TAG", "Error getting document", e)
//                            }
//                    .addOnSuccessListener { document ->
//                        if (document != null) {
//                            Log.d("TAG", "found Document")
//                        } else {
//                            Log.d("TAG", "No such document")
//                        }
//                    }
//                    .addOnFailureListener { exception ->
//                        Log.d("TAG", "get failed with ", exception)
//                    }

             */

                        val b = "abd"

//            val i = Intent(this@SigninActivity, MainActivity::class.java)
//            startActivity(i)
        }
    }
}