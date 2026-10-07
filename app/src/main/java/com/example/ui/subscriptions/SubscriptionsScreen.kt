package com.example.ui.subscriptions

import android.graphics.Bitmap
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Subscriptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.VideoItem
import com.example.ui.components.VideoCard
import com.example.ui.theme.YouTubeBlack
import com.example.ui.theme.YouTubeBorder
import com.example.ui.theme.YouTubeCardSurface
import com.example.ui.theme.YouTubeRed
import com.example.ui.theme.YouTubeTextPrimary
import com.example.ui.theme.YouTubeTextSecondary

@Composable
fun SubscriptionsScreen(
    subscribedVideos: List<VideoItem>,
    thumbnailBitmaps: Map<String, Bitmap>,
    onVideoClick: (VideoItem) -> Unit,
    onEditTitle: (VideoItem) -> Unit,
    onToggleSubscribe: (VideoItem) -> Unit,
    onToggleSave: (VideoItem) -> Unit,
    onExploreHome: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Unique channels derived from subscribed videos
    val channels = subscribedVideos.distinctBy { it.channelName }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(YouTubeBlack)
            .testTag("subscriptions_screen")
    ) {
        if (subscribedVideos.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
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
                            .clip(CircleShape)
                            .background(YouTubeCardSurface),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Subscriptions,
                            contentDescription = null,
                            tint = YouTubeTextSecondary,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "No Subscriptions Yet",
                        color = YouTubeTextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Tap the 'Subscribe' button on any video or channel to bookmark it here for quick access.",
                        color = YouTubeTextSecondary,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = onExploreHome,
                        colors = ButtonDefaults.buttonColors(containerColor = YouTubeRed),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.testTag("explore_videos_button")
                    ) {
                        Text("Explore Videos", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            // Horizontal Channel Avatars Row
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(YouTubeBlack)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    channels.forEach { item ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clickable { /* channel filter */ }
                                .width(60.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(CircleShape)
                                    .border(2.dp, YouTubeRed, CircleShape)
                                    .background(Color(item.channelAvatarColor or 0xFF000000)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = item.channelName.take(1).uppercase(),
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = item.channelName,
                                color = YouTubeTextPrimary,
                                fontSize = 11.sp,
                                maxLines = 1,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                HorizontalDivider(color = YouTubeBorder, thickness = 0.5.dp)
            }

            // Subscribed Videos Feed
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(subscribedVideos, key = { it.id }) { video ->
                    val thumb = thumbnailBitmaps[video.uri.toString()]
                    VideoCard(
                        video = video,
                        thumbnailBitmap = thumb,
                        onClick = { onVideoClick(video) },
                        onEditTitle = { onEditTitle(video) },
                        onToggleSubscribe = { onToggleSubscribe(video) },
                        onToggleSave = { onToggleSave(video) }
                    )
                }
            }
        }
    }
}
