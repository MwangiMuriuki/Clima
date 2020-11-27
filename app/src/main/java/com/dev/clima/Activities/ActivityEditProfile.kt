package com.dev.clima.Activities

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.dev.clima.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import com.theartofdev.edmodo.cropper.CropImage
import dmax.dialog.SpotsDialog
import kotlinx.android.synthetic.main.activity_all_videos.*
import kotlinx.android.synthetic.main.activity_edit_profile.*
import kotlinx.android.synthetic.main.activity_register.*
import kotlinx.android.synthetic.main.activity_single_article.*

class ActivityEditProfile : AppCompatActivity() {
    var resultUri: Uri? = null
    var mAuth: FirebaseAuth? = null
    var firebaseUser: FirebaseUser? = null
    var myFirestore = FirebaseFirestore.getInstance()
    var storageReference: StorageReference? = null
    var myUploadTask: UploadTask? = null

    var display_picture: String? = null
    var downloadUri: String? = null
    var userID: String? = null

    var alertDialog: AlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)
        setSupportActionBar(editProfileToolBar)
        editProfileToolBar.setNavigationIcon(R.drawable.ic_back)
        title = "Back"

        mAuth = FirebaseAuth.getInstance()
        storageReference = FirebaseStorage.getInstance().reference

        alertDialog = SpotsDialog(this, R.style.registerAlert)

        mAuth = FirebaseAuth.getInstance()
        firebaseUser = mAuth!!.currentUser

        userID = firebaseUser!!.uid

        val passedFullName: String? = intent.getStringExtra("fullName")
        val passedProfileImage: String? = intent.getStringExtra("profileImage")

        val imgUri: Uri? = Uri.parse(passedProfileImage)

        Glide.with(applicationContext).load(imgUri).into(editProfilePicture)

        profilePageIcon.setOnClickListener {
            CropImage.activity()
                .setAspectRatio(1, 1)
                .start(this@ActivityEditProfile)
        }

        buttonSaveChanges.setOnClickListener {
            validateFields(imgUri)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            val result = CropImage.getActivityResult(data)
            if (resultCode == RESULT_OK) {
                resultUri = result.uri
                editProfilePicture.setImageURI(resultUri)
                downloadUri = resultUri.toString()
                Toast.makeText(this@ActivityEditProfile, downloadUri, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun validateFields(imgUri: Uri?) {
        val fullName: String = changeFullName.text.toString()

        if(downloadUri.isNullOrEmpty()){
            resultUri = imgUri
        }

        if(fullName.isNullOrEmpty()){
            Toast.makeText(
                this@ActivityEditProfile,
                " Please enter a new name ",
                Toast.LENGTH_LONG
            ).show()
        }
        else{
            alertDialog!!.setCancelable(false)
            alertDialog!!.show()
            updateUserDetails(fullName, downloadUri)
        }
    }

    private fun updateUserDetails(fullName: String, downloadUri: String?) {

        if (downloadUri.isNullOrEmpty()){
            val docRef: DocumentReference = myFirestore.collection("Users").document(userID!!)

            docRef.update("full_Name", fullName).addOnSuccessListener {
                alertDialog!!.dismiss()
                updateUI()
            }
        }
        else{
            val mStorageRef = storageReference!!.child("Profile_Images").child(fullName)
            myUploadTask = mStorageRef.putFile(resultUri!!)

            val urlTask = myUploadTask!!.continueWithTask { task ->
                if (!task.isSuccessful) {
                    throw task.exception!!
                }
                // Continue with the task to get the download URL
                mStorageRef.downloadUrl
            }

            urlTask.addOnCompleteListener {
                if (it.isSuccessful){
                    val downloadUri = it.result!!
                    var dp: String? = downloadUri.toString()

                    val docRef: DocumentReference = myFirestore.collection("Users").document(userID!!)
                    docRef.update("display_picture", dp).addOnSuccessListener {
                        // Toast.makeText(this@ActivityEditProfile, "You have Successfully changed your Profile Picture", Toast.LENGTH_LONG).show()
                    }

                    //  val docRef2: DocumentReference = myFirestore.collection("Users").document(userID!!)
                    docRef.update("full_Name", fullName).addOnSuccessListener {
                        alertDialog!!.dismiss()
                        updateUI()
                    }
                }
            }
        }
    }

    private fun updateUI() {
        finish()
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {

            finish()
        }

        return super.onOptionsItemSelected(item)
    }


}