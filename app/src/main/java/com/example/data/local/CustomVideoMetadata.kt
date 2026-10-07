package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "video_metadata")
data class CustomVideoMetadata(
    @PrimaryKey
    val videoUri: String,
    val customTitle: String? = null,
    val category: String = "All",
    val isSubscribed: Boolean = false,
    val isLiked: Boolean = false,
    val isDisliked: Boolean = false,
    val isSavedToLibrary: Boolean = false,
    val likeCountOffset: Int = 0,
    val notes: String = "",
    val lastPositionMs: Long = 0L,
    val watchCount: Int = 0,
    val lastWatchedTimestamp: Long = 0L
)
