package com.app.businesscardshare.activity

import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.Gravity
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.app.businesscardshare.R
import com.app.businesscardshare.databinding.ActivityProfileSetupBinding
import com.app.businesscardshare.model.UserProfileData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import java.lang.Exception

class ProfileSetup : AppCompatActivity() {


    private lateinit var binding: ActivityProfileSetupBinding
    private val PICK_IMAGE_REQUEST = 1
    private lateinit var imageUri: Uri
    private lateinit var UserMail: String
    private var isProfileUploaded: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityProfileSetupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val sharedPreferences = getSharedPreferences("UserData", Context.MODE_PRIVATE)
        UserMail = sharedPreferences.getString("mail", "").toString()


        listener()
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.data != null) {
            imageUri = data.data!!
            binding.profile.setImageURI(imageUri)
            binding.setUpProfile.setBackgroundResource(R.drawable.button_bg)
            binding.setUpProfile.backgroundTintList =
                ColorStateList.valueOf(ContextCompat.getColor(this, R.color.main_color))
            binding.continueHome.setTextColor(resources.getColor(R.color.white))
            isProfileUploaded = true
        }
    }

    private fun listener() {

        binding.email.setText(UserMail)

        binding.uploadImage.setOnClickListener {
            openGallery()
        }

        binding.setUpProfile.setOnClickListener {

            Log.i("--ProfileSetup--", "get clicked")

            binding.profileProgress.visibility = View.VISIBLE
            binding.continueHome.visibility = View.GONE

            val name: String = binding.name.text.toString().trim()
            val phone: String = binding.phone.text.toString().trim()
            val git: String = binding.git.text.toString().trim()
            val country: String = binding.country.text.toString().trim()

            if (name.isEmpty()) {
                binding.name.error = "name is required"
                binding.name.requestFocus()
                binding.profileProgress.visibility = View.GONE
                binding.continueHome.visibility = View.VISIBLE
                return@setOnClickListener
            }
            if (phone.isEmpty()) {
                binding.phone.error = "phone is required"
                binding.phone.requestFocus()
                binding.profileProgress.visibility = View.GONE
                binding.continueHome.visibility = View.VISIBLE
                return@setOnClickListener
            }
            if (git.isEmpty()) {
                binding.git.error = "add your git profile"
                binding.git.requestFocus()
                binding.profileProgress.visibility = View.GONE
                binding.continueHome.visibility = View.VISIBLE
                return@setOnClickListener
            } else if (!Patterns.WEB_URL.matcher(git).matches()) {
                binding.git.error = "invalid profile url"
                binding.git.requestFocus()
                binding.profileProgress.visibility = View.GONE
                binding.continueHome.visibility = View.VISIBLE
                return@setOnClickListener
            }
            if(country.isEmpty()){
                binding.country.error = "country is required"
                binding.country.requestFocus()
                binding.profileProgress.visibility = View.GONE
                binding.continueHome.visibility = View.VISIBLE
                return@setOnClickListener
            }


            if (isProfileUploaded) {
                try {
                    Log.i("--ProfileSetup--", "inside try")
                    uploadImageAndSaveProfileData(imageUri)
                } catch (e: Exception) {
                    e.toString()
                }
            } else {
                showToast("Please upload profile first !")
                binding.profileProgress.visibility = View.GONE
                binding.continueHome.visibility = View.VISIBLE
            }
        }
    }

    private fun uploadImageAndSaveProfileData(
        imageUri: Uri,
        name: String = binding.name.text.toString(),
        email: String = binding.email.text.toString(),
        phone: String = binding.phone.text.toString(),
        githubProfileUrl: String = binding.git.text.toString(),
        country: String = binding.country.text.toString()
    ) {
        Log.i("--ProfileSetup--", "inside upload function")
        // Getting reference to the Firebase Storage location where the image will be saved
        val storageRef =
            Firebase.storage.reference.child("profile_images/${FirebaseAuth.getInstance().currentUser?.uid}")
        // Uploading the image to Firebase Storage
        storageRef.putFile(imageUri)
            .addOnSuccessListener { taskSnapshot ->
                storageRef.downloadUrl
                    .addOnSuccessListener { uri ->
                        // Saving the user profile data (including the image URL) to Firebase Realtime Database
                        val userProfileData = UserProfileData(
                            name = name,
                            email = email,
                            phone = phone,
                            githubProfileUrl = githubProfileUrl,
                            country = country,
                            profileImageUrl = uri.toString()
                        )
                        val userProfileDataRef =
                            FirebaseDatabase.getInstance().reference.child("user_profiles/${FirebaseAuth.getInstance().currentUser?.uid}")
                        userProfileDataRef.setValue(userProfileData)
                            .addOnSuccessListener {
                                val sharedPrefs = getSharedPreferences("UserData", Context.MODE_PRIVATE)
                                sharedPrefs.edit().apply {
                                    putBoolean("isProfileSet", true)
                                    apply()
                                }
                                showToast("Profile is created !")
                                val intent = Intent(this, MainActivity::class.java)
                                startActivity(intent)
                                finish()
                            }
                            .addOnFailureListener { exception ->
                                binding.profileProgress.visibility = View.GONE
                                binding.continueHome.visibility = View.VISIBLE
                                showToast("Profile not created !")
                            }
                    }
                    .addOnFailureListener { exception ->
                        binding.profileProgress.visibility = View.GONE
                        binding.continueHome.visibility = View.VISIBLE
                        showToast("Profile not created !")
                    }
            }
            .addOnFailureListener { exception ->
                // Failed to upload image
                showToast("Profile not created !")
                binding.profileProgress.visibility = View.GONE
                binding.continueHome.visibility = View.VISIBLE
            }
    }

    private fun showToast(message: String) {
        Log.i("--ProfileSetup--", "toast called")
        val inflater = layoutInflater
        val layout = inflater.inflate(R.layout.custom_toast, null)
        val text = layout.findViewById<TextView>(R.id.toastMessage)
        text.text = message
        val toast = Toast(applicationContext)
        toast.setGravity(Gravity.CENTER_HORIZONTAL or Gravity.CENTER_VERTICAL, 0, 32)
        toast.duration = Toast.LENGTH_LONG
        toast.view = layout
        toast.show()
    }

}