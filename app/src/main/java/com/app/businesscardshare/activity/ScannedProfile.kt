package com.app.businesscardshare.activity

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.Gravity
import android.view.View
import android.widget.TextView
import android.widget.Toast
import com.app.businesscardshare.R
import com.app.businesscardshare.databinding.ActivityScannedProfileBinding
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.lang.Exception

class ScannedProfile : AppCompatActivity() {

    private lateinit var binding: ActivityScannedProfileBinding

    private lateinit var userProfile: String
    private lateinit var name: String
    private lateinit var mail: String
    private lateinit var phone: String
    private lateinit var githubProfileUrl: String
    private lateinit var country: String

    private lateinit var uid: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityScannedProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // Get the intent object
        val intent = intent


        val extras = intent.extras
        uid = extras?.getString("UID").toString()
        val isSavedContact = extras?.getBoolean("isSavedContact")

            if(isSavedContact == true){
                binding.saveContact.visibility = View.GONE
                val name = extras?.getString("name")
                val phone = extras?.getString("phone")
                val mail = extras?.getString("mail")
                githubProfileUrl = extras?.getString("gitHubProfile").toString()
                val profile = extras?.getString("profile")
                val country = extras?.getString("country")

                if (profile != null && name != null && mail != null && githubProfileUrl != null && country != null &&  phone != null) {
                    setUpData(name, phone, mail, githubProfileUrl, country, profile)
                }

            }else{
                if (uid != null) {
                    getUserData(uid)
                }
            }

        listener()

    }

    private fun listener() {

        binding.back.setOnClickListener {
            super.onBackPressed()
        }

        binding.git.setOnClickListener {
            if (Patterns.WEB_URL.matcher(githubProfileUrl).matches()) {
                try {
                    val url = if (!githubProfileUrl.startsWith("http://") && !githubProfileUrl.startsWith("https://")) {
                        "https://$githubProfileUrl"
                    } else {
                        githubProfileUrl
                    }
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                    startActivity(intent)
                }catch (e : Exception){
                    e.toString()
                }
            } else {
                showToast("Invalid profile url")
            }

        }

        binding.saveContact.setOnClickListener {

            val sharedPreferences = getSharedPreferences("UserData", Context.MODE_PRIVATE)
            val myUid = sharedPreferences.getString("UID", "")

            binding.saveContactText.visibility = View.GONE
            binding.saveContactProgress.visibility = View.VISIBLE

            if (myUid.equals(uid)){
                showToast("Your profile can't be save as contact !")
                binding.saveContactProgress.visibility = View.GONE
                binding.saveContactText.visibility = View.VISIBLE
            }else{
                // Getting a reference to the user profile node for the current user
                val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return@setOnClickListener
                val profileRef = FirebaseDatabase.getInstance().getReference("user_profiles/$uid")

                val savedContactRef = profileRef.child("savedContact")
                savedContactRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            // Checking if a contact with the same email already exists
                            for (contactSnapshot in snapshot.children) {
                                if (name == (contactSnapshot.child(name).value as? String ?: ""))
                                    return
                            }
                            binding.saveContactProgress.visibility = View.GONE
                            binding.saveContactText.visibility = View.VISIBLE
                            finish()
                        }

                        // Saving data to a new contact node
                        val contactData = mapOf(
                            "profileUrl" to userProfile,
                            "name" to name,
                            "mail" to mail,
                            "phone" to phone,
                            "gitProfileUrl" to githubProfileUrl,
                            "country" to country
                        )
                        savedContactRef.child(name).setValue(contactData).addOnSuccessListener {
                            Log.i("--savedContact--", "saved");
                            binding.saveContactProgress.visibility = View.GONE
                            binding.saveContactText.visibility = View.VISIBLE
                            showToast("Contact saved !")
                            finish()
                        }.addOnFailureListener { error ->
                            binding.saveContactProgress.visibility = View.GONE
                            binding.saveContactText.visibility = View.VISIBLE
                            Log.i("--savedContact--", "failed");
                            showToast("something went wrong !")
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.i("--savedContact--", "cancel");
                        binding.saveContactProgress.visibility = View.GONE
                        binding.saveContactText.visibility = View.VISIBLE
                        showToast("something went wrong !")

                    }
                })
            }




        }
    }

    private fun getUserData(uid: String) {

        val ref = FirebaseDatabase.getInstance().getReference("user_profiles/$uid")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    // if Profile data exists, getting the data
                    name = dataSnapshot.child("name").value.toString()
                    mail = dataSnapshot.child("email").value.toString()
                    phone = dataSnapshot.child("phone").value.toString()
                    githubProfileUrl = dataSnapshot.child("githubProfileUrl").value.toString()
                    country = dataSnapshot.child("country").value.toString()
                    userProfile = dataSnapshot.child("profileImageUrl").value.toString()
                    setUpData(name, mail, phone, githubProfileUrl, country, userProfile)
                } else {

                }
            }
            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    private fun showToast(message : String) {
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

    private fun setUpData(
        name: String,
        email: String,
        phone: String,
        githubProfileUrl: String,
        country: String,
        profileImageUrl: String
    ) {
        Glide.with(this).load(profileImageUrl).error(R.drawable.error).into(binding.userProfile)
        binding.userName.text = name
        binding.mail.text = email
        binding.phone.text = phone
        binding.country.text = country
    }
}