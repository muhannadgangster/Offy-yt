package com.example.ui.home

import android.graphics.Bitmap
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.VideoItem
import com.example.ui.components.FilterChipRow
import com.example.ui.components.ShortsShelf
import com.example.ui.components.VideoCard
import com.example.ui.theme.YouTubeBlack
import com.example.ui.theme.YouTubeCardSurface
import com.example.ui.theme.YouTubeRed
import com.example.ui.theme.YouTubeTextPrimary
import com.example.ui.theme.YouTubeTextSecondary

@Composable
fun HomeScreen(
    videos: List<VideoItem>,
    shortsVideos: List<VideoItem>,
    thumbnailBitmaps: Map<String, Bitmap>,
    selectedCategory: String,
    onCategorySelected: (String) -> Unit,
    isLoading: Boolean,
    onRefresh: () -> Unit,
    onVideoClick: (VideoItem) -> Unit,
    onShortClick: (VideoItem) -> Unit,
    onEditTitle: (VideoItem) -> Unit,
    onToggleSubscribe: (VideoItem) -> Unit,
    onToggleSave: (VideoItem) -> Unit,
    onOpenAddDialog: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(YouTubeBlack)
            .testTag("home_screen")
    ) {
        // Category Chips Row
        FilterChipRow(
            selectedCategory = selectedCategory,
            onCategorySelected = onCategorySelected
        )

        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = YouTubeRed)
            }
        } else if (videos.isEmpty()) {
            // Empty state with friendly call to action
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.widthIn(max = 400.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .background(YouTubeCardSurface, RoundedCornerShape(36.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.VideoLibrary,
                            contentDescription = null,
                            tint = YouTubeTextSecondary,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "No Videos in Gallery",
                        color = YouTubeTextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Scan your device or import your favorite movies, clips, and music into MyTube.",
                        color = YouTubeTextSecondary,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = onOpenAddDialog,
                        colors = ButtonDefaults.buttonColors(containerColor = YouTubeRed),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.testTag("empty_add_video_button")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Add Video / Song", color = Color.White, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedButton(
                        onClick = onRefresh,
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, tint = YouTubeTextPrimary)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Rescan Device Media", color = YouTubeTextPrimary)
                    }
                }
            }
        } else {
            // Main Feed LazyColumn
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
                    .testTag("home_feed_list")
            ) {
                itemsIndexed(videos, key = { _, item -> item.id }) { index, video ->
                    val thumb = thumbnailBitmaps[video.uri.toString()]
                    VideoCard(
                        video = video,
                        thumbnailBitmap = thumb,
                        onClick = { onVideoClick(video) },
                        onEditTitle = { onEditTitle(video) },
                        onToggleSubscribe = { onToggleSubscribe(video) },
                        onToggleSave = { onToggleSave(video) }
                    )

                    // Insert Shorts Shelf after 1st video
                    if (index == 0 && shortsVideos.isNotEmpty()) {
                        ShortsShelf(
                            shorts = shortsVideos,
                            thumbnailBitmaps = thumbnailBitmaps,
                            onShortClick = onShortClick
                        )
                    }
                }
            }
        }
    }
}
