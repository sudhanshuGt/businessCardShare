package com.app.businesscardshare.activity

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import com.app.businesscardshare.adapter.SavedContactAdapter
import com.app.businesscardshare.databinding.ActivitySavedContactBinding
import com.app.businesscardshare.model.UserProfileData
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class SavedContact : AppCompatActivity() {

    private lateinit var binding: ActivitySavedContactBinding
    private lateinit var uid: String
    val personList = ArrayList<UserProfileData>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySavedContactBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.progressBar.visibility = View.VISIBLE

        val sharedPreferences = getSharedPreferences("UserData", Context.MODE_PRIVATE)
        val uid = sharedPreferences.getString("UID", "")
        val ref = FirebaseDatabase.getInstance().getReference("user_profiles/$uid/savedContact")

        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (childSnapshot in snapshot.children) {
                    // Get the key and value of each child node
                    val name = childSnapshot.child("name").value.toString()
                    val email = childSnapshot.child("mail").value.toString()
                    val phone = childSnapshot.child("phone").value.toString()
                    val githubProfileUrl = childSnapshot.child("gitProfileUrl").value.toString()
                    val country = childSnapshot.child("country").value.toString()
                    val profileImageUrl = childSnapshot.child("profileUrl").value.toString()

                    personList.add(UserProfileData(name, email, phone, githubProfileUrl, country, profileImageUrl))

                }
                val adapter : SavedContactAdapter = SavedContactAdapter(this@SavedContact, personList)
                binding.savedContactRecycler.adapter = adapter
                if(personList.isEmpty()){
                    binding.noDataTxt.visibility = View.VISIBLE
                }
                binding.progressBar.visibility = View.GONE
            }

            override fun onCancelled(error: DatabaseError) {
                if(personList.isEmpty()){
                    binding.noDataTxt.visibility = View.VISIBLE
                }
                binding.progressBar.visibility = View.GONE
                Log.i("--Firebase--", "Error reading data: ${error.message}")
            }
        })


    }
}