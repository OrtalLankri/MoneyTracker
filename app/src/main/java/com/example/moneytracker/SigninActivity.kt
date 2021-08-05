package com.example.moneytracker

import android.content.Intent
import android.os.Bundle
import android.os.Message
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.*
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
        val passwordLength = findViewById<TextView>(R.id.passwordLength)

        val db = FirebaseFirestore.getInstance()

        fun createNewUser(){
            val categories = hashMapOf(
                    "c1" to "Home",
                    "c2" to "Groceries",
                    "c3" to "Clothes",
                    "c4" to "Car",
                    "c5" to "Health",
                    "c6" to "fun"
            )
            val user = hashMapOf(
                    "firstName" to fName.text.toString(),
                    "lastName" to lName.text.toString(),
                    "email" to email.text.toString(),
                    "password" to password1.text.toString().hashCode().toString(),
                    "defaultCategories" to categories,
            )
            db.collection("Users")
                    .add(user)
                    .addOnSuccessListener { documentReference ->
                        Log.d("TAG", "DocumentSnapshot written with ID: ${documentReference.id}")
                        val name = fName.text.toString()
                        showMessage("Welcome $name!")
                        val i = Intent(this@SigninActivity, MainActivity::class.java)
                        i.putExtra("userID", documentReference.id)
                        i.putExtra("month", "null")
                        startActivity(i)
                        finish()
                    }
                    .addOnFailureListener { e ->
                        Log.w("TAG", "Error adding document", e)
                        showMessage("Error: Please try again")
                    }
        }

        button.setOnClickListener {
            button.isEnabled = false
            findViewById<ProgressBar>(R.id.spin).visibility = View.VISIBLE
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
                                showMessage("Error: User's email already exists")
                            }
                            else {
                                // create new user
                                Log.d("TAG", "Empty")
                                createNewUser()
                            }
                        }
                    }
        }

        //                            for (document in result.documents) {
//                                val e = document.get("email").toString()
//                                Log.d("TAG", e)
//                            }

        login.setOnClickListener {
            val i = Intent(this@SigninActivity, LoginActivity::class.java)
            startActivity(i)
        }

        fun confirmPassword(): Boolean {
            passwordLength.visibility = View.INVISIBLE
            confirm.visibility = View.INVISIBLE
            val p1 = password1.text.toString()
            val p2 = password2.text.toString()
            if ((p1.isNotEmpty() && p1.length < 5) || (p2.length > 0 && p2.length < 5)) {
                passwordLength.visibility = View.VISIBLE
                return false
            }
            else if (p1.isNotEmpty() && p2.isNotEmpty() && p1 != p2) {
                confirm.visibility = View.VISIBLE
                return false
            }
            return true
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
            if (confirmPassword()) {
                textChanged()
            }
        }
        password2.afterTextChanged {
            if (confirmPassword()) {
                textChanged()
            }
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