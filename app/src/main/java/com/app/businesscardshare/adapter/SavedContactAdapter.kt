package com.app.businesscardshare.adapter


import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.app.businesscardshare.R
import com.app.businesscardshare.activity.ScannedProfile
import com.app.businesscardshare.model.UserProfileData
import com.bumptech.glide.Glide
import com.google.android.material.imageview.ShapeableImageView

class SavedContactAdapter(private val context : Context, private val contacts: List<UserProfileData>) :
    RecyclerView.Adapter<SavedContactAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.saved_contact_layout, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val contact = contacts[position]
        holder.nameTextView.text = contact.name
        holder.emailTextView.text = contact.email
        Glide.with(context).load(contact.profileImageUrl).into(holder.profile)
        holder.container.setOnClickListener {
            val intent = Intent(context, ScannedProfile::class.java)
            intent.putExtra("isSavedContact", true)
            intent.putExtra("name", contact.name)
            intent.putExtra("mail", contact.email)
            intent.putExtra("phone", contact.phone)
            intent.putExtra("gitHubProfile", contact.githubProfileUrl)
            intent.putExtra("country", contact.country)
            intent.putExtra("profile", contact.profileImageUrl)
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return contacts.size
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val profile : ShapeableImageView = view.findViewById(R.id.profile)
        val nameTextView: TextView = view.findViewById(R.id.name)
        val emailTextView: TextView = view.findViewById(R.id.mail)
        val container : ConstraintLayout = view.findViewById(R.id.container)
    }
}
