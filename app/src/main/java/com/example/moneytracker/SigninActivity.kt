package com.example.moneytracker

import android.content.Intent
import android.os.Bundle
import android.os.Message
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.moneytracker.ui.login.LoginActivity
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.Thread.sleep


class SigninActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signin)
        CoroutineScope(Dispatchers.IO).launch { }

        val button = findViewById<Button>(R.id.signin)
        val fName = findViewById<EditText>(R.id.firstName)
        val lName = findViewById<EditText>(R.id.lastName)
        val email = findViewById<EditText>(R.id.email)
        val password1 = findViewById<EditText>(R.id.password1)
        val password2 = findViewById<EditText>(R.id.password2)
        val login = findViewById<TextView>(R.id.login)
        val confirm = findViewById<TextView>(R.id.confirmation)

        val db = FirebaseFirestore.getInstance()

        button.setOnClickListener {
            // check that user does not exist
            db.collection("Users")
                    .whereEqualTo("email", email.text.toString()).get()
                    .addOnCompleteListener {
                        if (it.isSuccessful) {
                            Log.d("TAG", "successful")
                            val result = it.result!!
                            // if this email already exist
                            if(!result.isEmpty) {
                                Log.d("TAG", "Not Empty")
                                // error email already exist
                                showMessage("Error: User's email already exists")
                            }
                            else {
                                Log.d("TAG", "Empty")
                                val user = hashMapOf(
                                        "firstName" to fName.text.toString(),
                                        "lastName" to lName.text.toString(),
                                        "email" to email.text.toString(),
                                        "password" to password1.text.toString().hashCode().toString()
                                )
                                db.collection("Users")
                                        .add(user)
                                        .addOnSuccessListener { documentReference ->
                                            Log.d("TAG", "DocumentSnapshot written with ID: ${documentReference.id}")
                                            val name = fName.text.toString()
                                            showMessage("Welcome $name!")
                                            val i = Intent(this@SigninActivity, MainActivity::class.java)
                                            i.putExtra("userID", documentReference.id)
                                            startActivity(i)
                                        }
                                        .addOnFailureListener { e ->
                                            Log.w("TAG", "Error adding document", e)
                                        }
                            }
//                            for (document in result.documents) {
//                                val e = document.get("email").toString()
//                                Log.d("TAG", e)
//                            }
                        }
                    }
        }

        login.setOnClickListener {
            val i = Intent(this@SigninActivity, LoginActivity::class.java)
            startActivity(i)
        }

        fun confirmPassword() {
            confirm.visibility = View.INVISIBLE
            if (password1.text.isNotBlank() && password2.text.isNotBlank()
                    && password2.text.toString() != password1.text.toString()) {
                confirm.visibility = View.VISIBLE
            }
        }

        fun textChanged() {
            button.isEnabled = fName.text.isNotBlank() && lName.text.isNotBlank() && email.text.isNotBlank()
                    && password1.text.isNotBlank() && password2.text.isNotBlank()
                    && password1.text.toString() == password2.text.toString()
        }
        fName.afterTextChanged {
            textChanged()
        }
        lName.afterTextChanged {
            textChanged()
        }
        email.afterTextChanged {
            textChanged()
        }
        password1.afterTextChanged {
            confirmPassword()
            textChanged()
        }
        password2.afterTextChanged {
            confirmPassword()
            textChanged()
        }
    }

    private fun showMessage(message : String) {
        Toast.makeText(applicationContext, message, Toast.LENGTH_LONG).show()
    }
}

/**
 * Extension function to simplify setting an afterTextChanged action to EditText components.
 */
fun EditText.afterTextChanged(afterTextChanged: (String) -> Unit) {
    this.addTextChangedListener(object : TextWatcher {
        override fun afterTextChanged(editable: Editable?) {
            afterTextChanged.invoke(editable.toString())
        }

        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
    })
}