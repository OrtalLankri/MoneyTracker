package com.example.moneytracker.data

import com.example.moneytracker.data.model.LoggedInUser
import java.io.IOException
import com.example.moneytracker.UserDAO
import com.example.moneytracker.MainActivity

/**
 * Class that handles authentication w/ login credentials and retrieves user information.
 */
class LoginDataSource {

    fun login(username: String, password: String): Result<LoggedInUser> {
//        try {
//            // TODO: handle loggedInUser authentication
//            val m = MainActivity()
//            val id = m.logIn(username, password)
//            val fakeUser = LoggedInUser(java.util.UUID.randomUUID().toString(), "Jane Doe")
//            return Result.Success(fakeUser)
//        } catch (e: Throwable) {
//            return Result.Error(IOException("Error logging in", e))
//        }
        val fakeUser = LoggedInUser(java.util.UUID.randomUUID().toString(), "Jane Doe")
        return Result.Success(fakeUser)
    }

    fun logout() {
        // TODO: revoke authentication
    }
}