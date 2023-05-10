package com.app.businesscardshare.activity

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
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
import com.app.businesscardshare.auth.Login
import com.app.businesscardshare.databinding.ActivityMainBinding
import com.app.businesscardshare.util.Constants
import com.bumptech.glide.Glide
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import java.lang.Exception

class MainActivity : AppCompatActivity() {


    private lateinit var binding: ActivityMainBinding;
    private lateinit var githubProfileUrl : String
    private lateinit var name : String
    private lateinit var mail : String
    private lateinit var phone: String
    private lateinit var profileImageUrl: String
    private lateinit var country : String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater);
        setContentView(binding.root);

        binding.progressBar.visibility = View.VISIBLE

        getUserData()
        listener()
    }

    private fun listener() {

        binding.edit.setOnClickListener {
            val intent = Intent(this, Edit::class.java)
            intent.putExtra("name", name)
            intent.putExtra("phone", phone)
            intent.putExtra("mail", mail)
            intent.putExtra("country", country)
            intent.putExtra("profile", profileImageUrl)
            intent.putExtra("git", githubProfileUrl)
            startActivity(intent)
        }

        binding.logOut.setOnClickListener {
            val sharedPrefs = getSharedPreferences("UserData", Context.MODE_PRIVATE)
            sharedPrefs.edit().apply {
                clear()
                apply()
            }
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()
            val mGoogleSignInClient = GoogleSignIn.getClient(this, gso)
            mGoogleSignInClient.signOut()
                .addOnCompleteListener(this) {
                    // After sign out is complete, clearing the back stack and navigating to the logout screen
                    val intent = Intent(this@MainActivity, Login::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                }


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

        binding.scanner.setOnClickListener {
            val intent = Intent(this, Scanner::class.java)
            startActivity(intent)
        }
        binding.savedContact.setOnClickListener {
            val intent = Intent(this, SavedContact::class.java)
            startActivity(intent)
        }
    }

    private fun getUserData() {
        val sharedPreferences = getSharedPreferences("UserData", Context.MODE_PRIVATE)
        val uid = sharedPreferences.getString("UID", "")

        if (uid != null) {
          try {
              binding.UserInfoQR.setImageBitmap(generateQRCode(uid, 300))
          }catch (e : Exception){
              Log.i("--QR--", e.toString())
          }
        }

        val ref = FirebaseDatabase.getInstance().getReference("user_profiles/$uid")
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Profile data exists, get the data
                    name = dataSnapshot.child("name").value.toString()
                    mail = dataSnapshot.child("email").value.toString()
                    phone = dataSnapshot.child("phone").value.toString()
                    githubProfileUrl = dataSnapshot.child("githubProfileUrl").value.toString()
                    country = dataSnapshot.child("country").value.toString()
                    profileImageUrl = dataSnapshot.child("profileImageUrl").value.toString()
                    setUpData(name, mail, phone, githubProfileUrl, country, profileImageUrl)
                } else {

                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle cancelled event
            }
        })

    }

    private fun setUpData(name: String, email: String, phone: String, githubProfileUrl: String, country: String, profileImageUrl: String) {
        binding.progressBar.visibility = View.GONE
        Glide.with(this).load(profileImageUrl).error(R.drawable.error).placeholder(R.drawable.profile).into(binding.userProfile)
        binding.userName.text = name
        binding.mail.text = email
        binding.phone.text = phone
        binding.country.text = country
    }

    fun generateQRCode(content: String, size: Int): Bitmap {
        val bitMatrix = MultiFormatWriter().encode(content, BarcodeFormat.QR_CODE, size, size)
        val width = bitMatrix.width
        val height = bitMatrix.height
        val pixels = IntArray(width * height)

        for (y in 0 until height) {
            val offset = y * width
            for (x in 0 until width) {
                pixels[offset + x] = if (bitMatrix[x, y]) Color.BLACK else Color.WHITE
            }
        }

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height)
        return bitmap
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

    override fun onResume() {
        super.onResume()
    }

}