package com.example.data.model

data class UserProfile(
    val id: String = "user_default",
    val name: String = "Alex Developer",
    val handle: String = "@alex_dev",
    val email: String = "alex@mytube.local",
    val avatarInitial: String = "A",
    val avatarColor: Long = 0xFF1E88E5,
    val isCloudSyncEnabled: Boolean = true,
    val lastSyncTimestamp: Long = System.currentTimeMillis()
)
