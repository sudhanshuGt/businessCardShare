package com.app.businesscardshare.model

data class UserProfileData(
    val name: String,
    val email: String,
    val phone: String,
    val githubProfileUrl: String,
    val country: String,
    val profileImageUrl: String
)
