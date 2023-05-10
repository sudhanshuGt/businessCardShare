package com.app.businesscardshare.auth

import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.widget.TextView
import android.widget.Toast
import com.app.businesscardshare.activity.MainActivity
import com.app.businesscardshare.activity.ProfileSetup
import com.app.businesscardshare.R
import com.app.businesscardshare.databinding.ActivityLoginBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class Login : AppCompatActivity() {


    lateinit var binding: ActivityLoginBinding;
    private lateinit var mAuth: FirebaseAuth;
    private var RC_SIGN_IN : Int = 100;


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater);
        setContentView(binding.root);

        listeners()

    }
    private fun listeners() {

        // Configuring Google Sign-In options
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        val googleSignInClient = GoogleSignIn.getClient(this, gso)

        binding.continueWithGoogle.setOnClickListener {
            val signInIntent = googleSignInClient.signInIntent
            startActivityForResult(signInIntent, RC_SIGN_IN)
            binding.bottomProgress.visibility = android.view.View.VISIBLE
        }

        binding.navigateToSignUp.setOnClickListener {
            val intent = Intent(this, SignUp::class.java)
            startActivity(intent)
        }

        binding.login.setOnClickListener {
            val email = binding.email.text.toString().trim()
            val password = binding.password.text.toString().trim()

            binding.loginWithEmailPassword.visibility = android.view.View.INVISIBLE
            binding.loginProgress.visibility = android.view.View.VISIBLE

            if (email.isEmpty()) {
                binding.email.error = "Email is required"
                binding.email.requestFocus()
                binding.loginProgress.visibility = android.view.View.INVISIBLE
                binding.loginWithEmailPassword.visibility = android.view.View.VISIBLE
                return@setOnClickListener
            }

            if (password.isEmpty()) {
                binding.password.error = "Password is required"
                binding.password.requestFocus()
                binding.loginProgress.visibility = android.view.View.INVISIBLE
                binding.loginWithEmailPassword.visibility = android.view.View.VISIBLE
                return@setOnClickListener
            }

            mAuth = FirebaseAuth.getInstance()
            mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        setProfile()
                    } else {
                        Log.w(TAG, "signInWithEmail:failure", task.exception)
                        if (task.exception?.message?.contains("no user record") == true) {
                            binding.loginProgress.visibility = android.view.View.INVISIBLE
                            binding.loginWithEmailPassword.visibility = android.view.View.VISIBLE
                           showToast("Email is not registered !")
                        } else {
                            binding.loginProgress.visibility = android.view.View.INVISIBLE
                            binding.loginWithEmailPassword.visibility = android.view.View.VISIBLE
                            showToast("Check your password or continue with google !")
                        }
                    }
                }
        }
    }

    // Handling the sign-in response in onActivityResult()
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)!!
                firebaseAuthWithGoogle(account.idToken!!)

            } catch (e: ApiException) {
                Log.i("--Auth--", "Google sign in failed", e)
                binding.bottomProgress.visibility = android.view.View.GONE
            }
        }
    }

    // Authenticating with Firebase using the Google ID token
    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        val firebaseAuth = FirebaseAuth.getInstance()
        firebaseAuth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    setProfile()
                } else {
                    // if Sign in failed, displaying a message to the user.
                    binding.bottomProgress.visibility = android.view.View.GONE
                    Log.i("--Auth--", "signInWithCredential:failure", task.exception)
                    showToast("something went wrong !")
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
                if (dataSnapshot.exists()) {
                    // Profile data exists, get the data
                    val intent = Intent(this@Login, MainActivity::class.java)
                    startActivity(intent)
                } else {
                   goToProfileSetup()
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })

    }

    private fun goToProfileSetup() {
        val intent = Intent(this, ProfileSetup::class.java)
        startActivity(intent)
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


}