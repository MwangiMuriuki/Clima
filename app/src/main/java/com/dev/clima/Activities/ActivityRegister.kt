package com.dev.clima.Activities

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.dev.clima.DataClasses.UserDetailsDataClass
import com.dev.clima.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import com.theartofdev.edmodo.cropper.CropImage
import dmax.dialog.SpotsDialog
import kotlinx.android.synthetic.main.activity_register.*

class ActivityRegister : AppCompatActivity() {
    var resultUri: Uri? = null
    var mAuth: FirebaseAuth? = null
    var myFirestore = FirebaseFirestore.getInstance()
    var storageReference: StorageReference? = null
    var myUploadTask: UploadTask? = null

    var full_Name: String? = null
    var email: String? = null
    var password: String? = null
    var display_picture: String? = null
    var downloadUri: String? = null

    var alertDialog: AlertDialog? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        mAuth = FirebaseAuth.getInstance()
        storageReference = FirebaseStorage.getInstance().reference

        alertDialog = SpotsDialog(this, R.style.registerAlert)

        buttonCreateAccount.setOnClickListener {
            validateFields()
        }

       registerProfilePicture.setOnClickListener {
            CropImage.activity()
                .setAspectRatio(1, 1)
                .start(this@ActivityRegister)
        }

       backHome.setOnClickListener {
            val intent: Intent = Intent(applicationContext, ActivityLogin::class.java)
            startActivity(intent)
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            val result = CropImage.getActivityResult(data)
            if (resultCode == RESULT_OK) {
                resultUri = result.uri
                registerProfilePicture.setImageURI(resultUri)
                downloadUri = resultUri.toString()
                Toast.makeText(this@ActivityRegister, downloadUri, Toast.LENGTH_LONG).show()
            }
        }
    }

     fun validateFields() {
        val fullName: String? = fullName.text.toString()
        val emailAddress: String? = regEmail.text.toString()
        val password: String? = regPassword.text.toString()

       if(downloadUri.isNullOrEmpty()){
           Toast.makeText(
                   this@ActivityRegister,
                   " Please select a profile picture ",
                   Toast.LENGTH_LONG
           ).show()
        }
        else if(fullName.isNullOrEmpty()){
            Toast.makeText(
                this@ActivityRegister,
                " Please enter your Full Name ",
                Toast.LENGTH_LONG
            ).show()
        }
        else if (emailAddress.isNullOrEmpty()){
            Toast.makeText(
                this@ActivityRegister,
                " Please enter your Email Address",
                Toast.LENGTH_LONG
            ).show()
        }
        else if (!emailAddress.isNullOrEmpty() && !isEmailValid(emailAddress)){
            Toast.makeText(
                this@ActivityRegister,
                " Please Enter a valid Email address ",
                Toast.LENGTH_LONG
            )
                .show()
        }
        else if (password.isNullOrEmpty()){
            Toast.makeText(this@ActivityRegister, " Please Enter your password ", Toast.LENGTH_LONG)
                .show()
        } else if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            Toast.makeText(
                this@ActivityRegister,
                " Please Enter a password with 6 or more characters ",
                Toast.LENGTH_LONG
            )
                .show()
        }
        else{
           alertDialog!!.setCancelable(false)
           alertDialog!!.show()
            registerNewUser(fullName, emailAddress, password)
        }
    }

    fun registerNewUser(fullName: String, emailAddress: String, password: String) {

        mAuth?.createUserWithEmailAndPassword(emailAddress, password)?.addOnCompleteListener {
            if (it.isSuccessful){
                alertDialog!!.cancel()
                // Sign in success, update UI with the signed-in user's information
                Log.d("SUCCESS_TAG", "createUserWithEmail:success")
                val user = mAuth!!.currentUser
                val userID = mAuth!!.currentUser!!.uid

                if (resultUri != null){
                    val mStorageRef = storageReference!!.child("Profile_Images").child(fullName)
                    myUploadTask = mStorageRef.putFile(resultUri!!)

                    val urlTask = myUploadTask!!.continueWithTask { task ->
                        if (!task.isSuccessful) {
                            throw task.exception!!
                        }
                        // Continue with the task to get the download URL
                        mStorageRef.downloadUrl
                    }.addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val downloadUri = task.result!!
                            display_picture = downloadUri.toString()
                            val modelClassUsers = UserDetailsDataClass(
                                fullName,
                                emailAddress,
                                userID,
                                display_picture
                            )
                            myFirestore.collection("Users").document(userID).set(modelClassUsers)
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        Toast.makeText(
                                            this@ActivityRegister,
                                            "Registration Successful!!!",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    } else {
                                        Toast.makeText(
                                            applicationContext,
                                            "Account Creation failed. Please try again Later. ",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }
                                }.addOnFailureListener {
                                        var error: String? = task.exception.toString()

                                        Toast.makeText(
                                                applicationContext,
                                                error,
                                                Toast.LENGTH_LONG
                                        ).show()
                            }
                        }
                        else {
                            Toast.makeText(
                                applicationContext,
                                "Error Creating Account. Please try again Later. ",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                }

                if (user != null) {
                    updateUI(user)
                }
            }
            else{
                // If sign in fails, display a message to the user.
                Log.w("FAILURE_TAG", "createUserWithEmail:failure", it.exception)
                Toast.makeText(applicationContext, "Authentication failed.", Toast.LENGTH_LONG)
                    .show()
            }
        }
    }

    private fun isPasswordValid(password: String): Boolean {
        return password.length >= 6
    }

    private fun isEmailValid(login_email: String): Boolean {
        return login_email.contains("@")
    }

    private fun updateUI(user: FirebaseUser) {
        val nextPage = Intent(applicationContext, MainActivity::class.java)
        startActivity(nextPage)
        finish()
    }

}