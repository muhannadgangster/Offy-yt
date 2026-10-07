package com.example.ui.library

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SwitchAccount
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserProfile
import com.example.data.model.VideoItem
import com.example.ui.theme.YouTubeBadgeBg
import com.example.ui.theme.YouTubeBlack
import com.example.ui.theme.YouTubeBorder
import com.example.ui.theme.YouTubeCardSurface
import com.example.ui.theme.YouTubePillBg
import com.example.ui.theme.YouTubeRed
import com.example.ui.theme.YouTubeTextPrimary
import com.example.ui.theme.YouTubeTextSecondary

@Composable
fun LibraryScreen(
    userProfile: UserProfile,
    historyVideos: List<VideoItem>,
    savedVideos: List<VideoItem>,
    likedVideos: List<VideoItem>,
    thumbnailBitmaps: Map<String, Bitmap>,
    onVideoClick: (VideoItem) -> Unit,
    onOpenBackupDialog: () -> Unit,
    onOpenProfileDialog: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(YouTubeBlack)
            .testTag("library_screen")
    ) {
        // Profile Header Section
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Profile Avatar
                    Box(
                        modifier = Modifier
                            .size(68.dp)
                            .clip(CircleShape)
                            .background(Color(userProfile.avatarColor or 0xFF000000)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = userProfile.avatarInitial,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 28.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = userProfile.name,
                            color = YouTubeTextPrimary,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${userProfile.handle} • View channel",
                            color = YouTubeTextSecondary,
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Cloud Sync Active • Device Encrypted",
                            color = Color(0xFF4CAF50),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Action Pills (Switch Account, Google Sync, Backup/Restore)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onOpenProfileDialog,
                        shape = RoundedCornerShape(18.dp),
                        modifier = Modifier.testTag("switch_account_button")
                    ) {
                        Icon(Icons.Default.SwitchAccount, contentDescription = null, tint = YouTubeTextPrimary, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Switch Account", color = YouTubeTextPrimary, fontSize = 12.sp)
                    }

                    OutlinedButton(
                        onClick = onOpenBackupDialog,
                        shape = RoundedCornerShape(18.dp),
                        modifier = Modifier.testTag("backup_sync_button")
                    ) {
                        Icon(Icons.Default.Backup, contentDescription = null, tint = YouTubeTextPrimary, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Backup & Sync", color = YouTubeTextPrimary, fontSize = 12.sp)
                    }
                }
            }
        }

        // Watch History Section
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "History",
                        color = YouTubeTextPrimary,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "View all",
                        color = YouTubeTextSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                if (historyVideos.isEmpty()) {
                    Text(
                        text = "No watch history yet. Videos you play will appear here.",
                        color = YouTubeTextSecondary,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                } else {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        historyVideos.take(8).forEach { item ->
                            val thumb = thumbnailBitmaps[item.uri.toString()]
                            Column(
                                modifier = Modifier
                                    .width(140.dp)
                                    .clickable { onVideoClick(item) }
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .aspectRatio(16f / 9f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(YouTubeCardSurface)
                                ) {
                                    if (thumb != null) {
                                        Image(
                                            bitmap = thumb.asImageBitmap(),
                                            contentDescription = null,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    } else {
                                        Box(
                                            modifier = Modifier.fillMaxSize(),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.PlayArrow,
                                                contentDescription = null,
                                                tint = Color.White.copy(alpha = 0.5f),
                                                modifier = Modifier.size(28.dp)
                                            )
                                        }
                                    }

                                    // Duration badge
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.BottomEnd)
                                            .padding(4.dp)
                                            .clip(RoundedCornerShape(3.dp))
                                            .background(YouTubeBadgeBg)
                                            .padding(horizontal = 4.dp, vertical = 1.dp)
                                    ) {
                                        Text(
                                            text = item.formattedDuration,
                                            color = Color.White,
                                            fontSize = 9.sp
                                        )
                                    }

                                    // Progress bar at bottom
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.BottomStart)
                                            .fillMaxWidth(0.65f)
                                            .height(3.dp)
                                            .background(YouTubeRed)
                                    )
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                Text(
                                    text = item.displayTitle,
                                    color = YouTubeTextPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )

                                Text(
                                    text = item.channelName,
                                    color = YouTubeTextSecondary,
                                    fontSize = 10.sp,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            HorizontalDivider(color = YouTubeBorder, thickness = 0.5.dp, modifier = Modifier.padding(vertical = 8.dp))
        }

        // Playlists & Quick Sections
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                LibraryRowItem(
                    icon = Icons.Default.Bookmark,
                    title = "Saved / Downloads",
                    subtitle = "${savedVideos.size} videos available offline",
                    onClick = { /* navigate to saved */ }
                )
                LibraryRowItem(
                    icon = Icons.Default.ThumbUp,
                    title = "Liked videos",
                    subtitle = "${likedVideos.size} videos",
                    onClick = { /* navigate to liked */ }
                )
                LibraryRowItem(
                    icon = Icons.Default.VideoLibrary,
                    title = "Your offline clips",
                    subtitle = "Organized by custom titles",
                    onClick = { /* navigate to clips */ }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun LibraryRowItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = YouTubeTextPrimary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = YouTubeTextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = subtitle,
                color = YouTubeTextSecondary,
                fontSize = 12.sp
            )
        }
    }
}
