package com.dev.clima.Activities

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.dev.clima.R
import com.google.firebase.auth.FirebaseAuth
import dmax.dialog.SpotsDialog
import kotlinx.android.synthetic.main.activity_reset_password.*

class ActivityResetPassword : AppCompatActivity() {

    var enterEmail: String? = null
    var resetDialog: AlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reset_password)

        resetDialog = SpotsDialog(this, R.style.resetPasswordAlert)

        backHome.setOnClickListener(View.OnClickListener {
            val back = Intent(this@ActivityResetPassword, ActivityLogin::class.java)
            startActivity(back)
            finish()
        })

        buttonResetPassword.setOnClickListener {
            enterEmail = yourEmail.text.toString()

            if (enterEmail.isNullOrEmpty()){

                Toast.makeText(
                    applicationContext,
                    "Please Enter Your Email Address",
                    Toast.LENGTH_LONG
                ).show()


            } else {
                resetDialog?.setCancelable(false)
                resetDialog?.show()
                resetPassword(enterEmail!!)
            }
        }
    }

    private fun resetPassword(enterEmail: String) {
        val auth = FirebaseAuth.getInstance()

        auth.sendPasswordResetEmail(enterEmail)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(
                        this@ActivityResetPassword,
                        "Please check your email. We have sent you instructions to reset your password!",
                        Toast.LENGTH_SHORT
                    ).show()
                    yourEmail.setText("")
                } else {
                    Toast.makeText(
                        this@ActivityResetPassword,
                        "That email address does not exist. Please try again",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                resetDialog!!.dismiss()
            }
    }
}