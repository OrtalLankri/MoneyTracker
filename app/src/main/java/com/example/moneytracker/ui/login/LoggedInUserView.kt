package com.example.moneytracker.ui.login

import com.google.firebase.firestore.DocumentSnapshot

/**
 * User details post authentication that is exposed to the UI
 */
data class LoggedInUserView(
        val displayName: DocumentSnapshot


        //... other data fields that may be accessible to the UI
)