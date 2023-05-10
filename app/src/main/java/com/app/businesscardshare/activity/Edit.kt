package com.app.businesscardshare.activity

import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import android.view.Gravity
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintSet.Constraint
import androidx.core.content.ContextCompat
import com.app.businesscardshare.R
import com.app.businesscardshare.databinding.ActivityEditBinding
import com.app.businesscardshare.util.Constants
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage

class Edit : AppCompatActivity() {

    private lateinit var binding: ActivityEditBinding

    private lateinit var imageUri: Uri
    private var isProfileUploaded: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityEditBinding.inflate(layoutInflater)
        setContentView(binding.root)


        val intent = intent
        val extras = intent.extras

        val name: String = extras?.getString("name").toString()
        val phone: String = extras?.getString("phone").toString()
        val mail: String = extras?.getString("mail").toString()
        val githubProfileUrl: String = extras?.getString("git").toString()
        val profile: String = extras?.getString("profile").toString()
        val country: String = extras?.getString("country").toString()


        setData(name, phone, mail, githubProfileUrl, profile, country)

        listener()
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, 100)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 100 && resultCode == RESULT_OK && data != null && data.data != null) {
            imageUri = data.data!!
            binding.profile.setImageURI(imageUri)
            binding.profile.setBackgroundResource(R.drawable.button_bg)
            isProfileUploaded = true
            binding.imageProgress.visibility = View.VISIBLE
            binding.saveProfile.alpha = 0.5f
            binding.saveProfile.isEnabled = false
            uploadProfileImage(imageUri)
        }
    }

    private fun uploadProfileImage(imageUri: Uri) {
        val storageRef =
            Firebase.storage.reference.child("profile_images/${FirebaseAuth.getInstance().currentUser?.uid}")
        val uploadTask = storageRef.putFile(imageUri)
        uploadTask.addOnSuccessListener {
            showToast("Profile uploaded !")
            binding.imageProgress.visibility  = View.GONE

            storageRef.downloadUrl.addOnSuccessListener { uri ->
                val imageUrl = uri.toString()
                // Update the user's profile image URL in Firebase Database
                val ref = FirebaseDatabase.getInstance().getReference("user_profiles/${FirebaseAuth.getInstance().currentUser?.uid}")
                ref.child("profileImageUrl").setValue(imageUrl)
                    .addOnSuccessListener {
                        binding.saveProfile.alpha = 1f
                        binding.saveProfile.isEnabled = true
                    }
                    .addOnFailureListener {

                    }
            }
        }
            .addOnFailureListener {
                showToast("something went wrong !")
            }
    }

    private fun showToast(message: String) {
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


    private fun listener() {


        binding.uploadImage.setOnClickListener {
            openGallery()
        }

        binding.saveProfile.setOnClickListener {

            binding.profileProgress.visibility = View.VISIBLE
            binding.continueHome.visibility = View.INVISIBLE

            val name: String = binding.name.text.toString().trim()
            val phone: String = binding.phone.text.toString().trim()
            val git: String = binding.git.text.toString().trim()
            val country: String = binding.country.text.toString().trim()
            val mail: String = binding.email.text.toString().trim()


            if (name.isEmpty()) {
                binding.name.error = "name is required"
                binding.name.requestFocus()
                binding.profileProgress.visibility = View.GONE
                binding.continueHome.visibility = View.VISIBLE
                return@setOnClickListener
            }
            if (mail.isEmpty()) {
                binding.email.error = "name is required"
                binding.email.requestFocus()
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
            if (country.isEmpty()) {
                binding.country.error = "country is required"
                binding.country.requestFocus()
                binding.profileProgress.visibility = View.GONE
                binding.continueHome.visibility = View.VISIBLE
                return@setOnClickListener
            }
            updateProfile(name, phone, mail, git, country)
        }
    }

    private fun updateProfile(
        name: String,
        phone: String,
        mail: String,
        git: String,
        country: String
    ) {

        val sharedPreferences = getSharedPreferences("UserData", Context.MODE_PRIVATE)
        val uid = sharedPreferences.getString("UID", "")

        val ref = FirebaseDatabase.getInstance().getReference("user_profiles/$uid")

        // Update the profile data
        ref.child("name").setValue(name)
        ref.child("email").setValue(mail)
        ref.child("phone").setValue(phone)
        ref.child("githubProfileUrl").setValue(git)
        ref.child("country").setValue(country)

        finish()


    }

    private fun setData(
        name: String,
        phone: String,
        mail: String,
        githubProfileUrl: String,
        profile: String,
        country: String
    ) {
        Glide.with(this).load(profile).error(R.drawable.error).placeholder(R.drawable.profile)
            .into(binding.profile)
        binding.name.setText(name)
        binding.email.setText(mail)
        binding.phone.setText(phone)
        binding.country.setText(country)
        binding.git.setText(githubProfileUrl)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        Constants.isProfileUpdated = true
    }
}