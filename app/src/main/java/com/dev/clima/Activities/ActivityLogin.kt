package com.dev.clima.Activities

import android.app.AlertDialog
import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.dev.clima.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import dmax.dialog.SpotsDialog
import kotlinx.android.synthetic.main.activity_login.*


class ActivityLogin : AppCompatActivity() {

    private var mAuth: FirebaseAuth? = null

    var alertDialog: AlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        mAuth = FirebaseAuth.getInstance()
//        preferenceManager = PreferenceManager(applicationContext)

        alertDialog = SpotsDialog(this, R.style.loginAlert)

        forgotPassword.setOnClickListener {
            val intent: Intent = Intent(applicationContext, ActivityResetPassword::class.java)
            startActivity(intent)
        }

        createAccount.setOnClickListener {
            val intent: Intent = Intent(applicationContext, ActivityRegister::class.java)
            startActivity(intent)
        }

        loginBtn.setOnClickListener {
            validateFields()
        }

    }

    private fun validateFields() {
        var emailAddress: String? = loginEmail.text.toString()
        var password: String? = loginPassword.text.toString()

        if (emailAddress.isNullOrEmpty()){
            Toast.makeText(
                this@ActivityLogin,
                " Please enter your Email Address",
                Toast.LENGTH_LONG
            ).show()
        }
        else if (!emailAddress.isNullOrEmpty() && !isEmailValid(emailAddress)){
            Toast.makeText(
                this@ActivityLogin,
                " Please Enter a valid Email address ",
                Toast.LENGTH_LONG
            )
                .show()
        }
        else if (password.isNullOrEmpty()){
            Toast.makeText(
                this@ActivityLogin,
                " Please enter your Password",
                Toast.LENGTH_LONG
            ).show()
        }
        else{
            alertDialog!!.setCancelable(false)
            alertDialog!!.show()
            signInUser(emailAddress, password)
        }
    }

    private fun signInUser(emailAddress: String, password: String) {

        mAuth?.signInWithEmailAndPassword(emailAddress, password)?.addOnCompleteListener {
            if (it.isSuccessful){
                val user = mAuth!!.currentUser
                alertDialog!!.cancel()
                toMain(user)
            }
            else{
                // If sign in fails, display a message to the user.
                Log.w(ContentValues.TAG, "signInWithEmail:failure", it.exception)
                Toast.makeText(
                    this@ActivityLogin,
                    "Login failed. A user with that Email and Password does not exist. Please try again.",
                    Toast.LENGTH_LONG
                ).show()

                alertDialog!!.cancel()
            }
        }

    }

    private fun isEmailValid(login_email: String): Boolean {
        return login_email.contains("@")
    }

    private fun toMain(user: FirebaseUser?) {
        val intent: Intent = Intent(applicationContext, MainActivity::class.java)
        startActivity(intent)
    }
}