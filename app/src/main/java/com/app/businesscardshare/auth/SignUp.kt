package com.app.businesscardshare.auth

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.Gravity
import android.widget.TextView
import android.widget.Toast
import com.app.businesscardshare.activity.ProfileSetup
import com.app.businesscardshare.R
import com.app.businesscardshare.databinding.ActivitySignUpBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class SignUp : AppCompatActivity() {

    private lateinit var binding: ActivitySignUpBinding;
    private lateinit var mAuth: FirebaseAuth;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)


        binding.signUpWithEmailPassword.setOnClickListener {
            val email = binding.email.text.toString().trim()
            val password = binding.password.text.toString().trim()
            val confirmPassword = binding.confirmPassword.text.toString().trim()

            binding.signUpWithEmailPassword.visibility = android.view.View.INVISIBLE
            binding.signUpProgress.visibility = android.view.View.VISIBLE

            if (email.isEmpty()) {
                binding.email.error = "Email is required"
                binding.email.requestFocus()
                binding.signUpProgress.visibility = android.view.View.INVISIBLE
                binding.signUpWithEmailPassword.visibility = android.view.View.VISIBLE
                return@setOnClickListener
            } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                binding.email.error = "Please enter a valid email address"
                binding.email.requestFocus()
                binding.signUpProgress.visibility = android.view.View.INVISIBLE
                binding.signUpWithEmailPassword.visibility = android.view.View.VISIBLE
                return@setOnClickListener
            }



            if (password.isEmpty()) {
                binding.password.error = "Password is required"
                binding.password.requestFocus()
                binding.signUpProgress.visibility = android.view.View.INVISIBLE
                binding.signUpWithEmailPassword.visibility = android.view.View.VISIBLE
                return@setOnClickListener
            }

            if(confirmPassword.isEmpty()){
                binding.confirmPassword.error = "input password again"
                binding.signUpProgress.visibility = android.view.View.INVISIBLE
                binding.signUpWithEmailPassword.visibility = android.view.View.VISIBLE
                return@setOnClickListener
            }

            if(confirmPassword != password){
                showToast("password is not same")
                binding.signUpProgress.visibility = android.view.View.INVISIBLE
                binding.signUpWithEmailPassword.visibility = android.view.View.VISIBLE
                return@setOnClickListener
            }

            mAuth = FirebaseAuth.getInstance()
            mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        setProfile()
                    } else {
                        Log.i("--SignUp--", "createUserWithEmail:failure", task.exception)
                        if (task.exception?.message?.contains("email address is already in use") == true) {
                            showToast("Email is already registered")
                            binding.signUpProgress.visibility = android.view.View.INVISIBLE
                            binding.signUpWithEmailPassword.visibility = android.view.View.VISIBLE
                        } else {
                            showToast("something went wrong !")
                            binding.signUpProgress.visibility = android.view.View.INVISIBLE
                            binding.signUpWithEmailPassword.visibility = android.view.View.VISIBLE
                        }
                    }
                }
        }

    }

    private fun setProfile() {
        // Saving user ID and login status to SharedPreferences
        val sharedPrefs = getSharedPreferences("UserData", Context.MODE_PRIVATE)
        sharedPrefs.edit().apply {
            putString("UID", FirebaseAuth.getInstance().currentUser?.uid)
            putString("mail", FirebaseAuth.getInstance().currentUser?.email)
            putBoolean("IS_LOGGED_IN", true)
            apply()
        }

        val ref = FirebaseDatabase.getInstance().getReference("user_profiles/${FirebaseAuth.getInstance().currentUser?.uid}")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (!dataSnapshot.exists()) {
                    goToProfileSetup()
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

    private fun goToProfileSetup() {
        binding.signUpProgress.visibility = android.view.View.INVISIBLE
        binding.signUpWithEmailPassword.visibility = android.view.View.VISIBLE
        val intent = Intent(this, ProfileSetup::class.java)
        startActivity(intent)
    }


}