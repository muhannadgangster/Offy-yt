package com.example.data.model

import android.net.Uri

data class VideoItem(
    val id: Long,
    val uri: Uri,
    val path: String,
    val originalTitle: String,
    val customTitle: String? = null,
    val durationMs: Long = 0L,
    val sizeBytes: Long = 0L,
    val width: Int = 1920,
    val height: Int = 1080,
    val isVertical: Boolean = false,
    val channelName: String = "MyTube Creator",
    val channelHandle: String = "@creator",
    val channelAvatarColor: Long = 0xFFE53935,
    val category: String = "All",
    val isSubscribed: Boolean = false,
    val isLiked: Boolean = false,
    val isDisliked: Boolean = false,
    val isSaved: Boolean = false,
    val likeCount: Int = 124,
    val notes: String = "",
    val lastPositionMs: Long = 0L,
    val lastWatchedTimestamp: Long = 0L
) {
    val displayTitle: String
        get() = if (!customTitle.isNullOrBlank()) customTitle else originalTitle

    val formattedDuration: String
        get() {
            val totalSeconds = (durationMs / 1000).coerceAtLeast(0)
            val minutes = totalSeconds / 60
            val seconds = totalSeconds % 60
            val hours = minutes / 60
            return if (hours > 0) {
                String.format("%d:%02d:%02d", hours, minutes % 60, seconds)
            } else {
                String.format("%d:%02d", minutes, seconds)
            }
        }

    val formattedSize: String
        get() {
            if (sizeBytes <= 0) return "Unknown size"
            val mb = sizeBytes / (1024.0 * 1024.0)
            return if (mb >= 1024) {
                String.format("%.1f GB", mb / 1024.0)
            } else {
                String.format("%.1f MB", mb)
            }
        }
}
