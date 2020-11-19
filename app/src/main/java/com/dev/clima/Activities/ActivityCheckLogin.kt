package com.dev.clima.Activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class ActivityCheckLogin : AppCompatActivity() {
    private val mAuth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val userLoggedIn = mAuth.currentUser

        if (userLoggedIn == null) {
            val Login = Intent(applicationContext, ActivityLogin::class.java)
            startActivity(Login)
        } else {
            val MainPage = Intent(applicationContext, MainActivity::class.java)
            startActivity(MainPage)
        }

        finish()
    }
}