package com.app.businesscardshare.activity

import android.animation.ObjectAnimator
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.app.businesscardshare.R
import com.app.businesscardshare.auth.Login
import com.app.businesscardshare.databinding.ActivitySplashBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class Splash : AppCompatActivity() {
    private lateinit var uid : String
    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val animator = ObjectAnimator.ofFloat(binding.logo, "alpha", 0.1f, 1f)
        animator.duration = 1000 // animation duration in milliseconds
        animator.start()

        Handler(Looper.getMainLooper()).postDelayed({
            // check condition if user is already logged in...
            checkIfUserIsLoggedIn()
        }, 2000)

    }

    private fun checkIfProfileIsSet(uid : String) {
        val ref = FirebaseDatabase.getInstance().getReference("user_profiles/$uid")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    Log.i("--splash--", "profile exist")
                    val intent = Intent(this@Splash, MainActivity::class.java)
                    startActivity(intent)
                }else{
                    Log.i("--splash--", "profile not exist")
                    val intent = Intent(this@Splash, ProfileSetup::class.java)
                    startActivity(intent)
                }
            }
            override fun onCancelled(error: DatabaseError) {
                Log.i("--splash--", "error occurred")
            }
        })

    }

    private fun checkIfUserIsLoggedIn() {
        val sharedPreferences = getSharedPreferences("UserData", Context.MODE_PRIVATE)
        uid = sharedPreferences.getString("UID", "").toString()
        val isLoggedIn = sharedPreferences.getBoolean("IS_LOGGED_IN", false)
        if(isLoggedIn){
            Log.i("--splash--", "logged in checked")
            checkIfProfileIsSet(uid)
        }else{
            val intent = Intent(this@Splash, Login::class.java)
            startActivity(intent)
        }
    }

}