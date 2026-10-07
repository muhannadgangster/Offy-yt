package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.VideoItem
import com.example.ui.theme.YouTubeBadgeBg
import com.example.ui.theme.YouTubeBlack
import com.example.ui.theme.YouTubeCardSurface
import com.example.ui.theme.YouTubePillBg
import com.example.ui.theme.YouTubeRed
import com.example.ui.theme.YouTubeTextPrimary
import com.example.ui.theme.YouTubeTextSecondary

@Composable
fun VideoCard(
    video: VideoItem,
    thumbnailBitmap: Bitmap?,
    onClick: () -> Unit,
    onEditTitle: () -> Unit,
    onToggleSubscribe: () -> Unit,
    onToggleSave: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var menuExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(YouTubeBlack)
            .clickable { onClick() }
            .padding(bottom = 16.dp)
            .testTag("video_card_${video.id}")
    ) {
        // 16:9 Thumbnail Container
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
                .background(YouTubeCardSurface)
        ) {
            if (thumbnailBitmap != null) {
                Image(
                    bitmap = thumbnailBitmap.asImageBitmap(),
                    contentDescription = video.displayTitle,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                // Fallback elegant thumbnail
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(YouTubeCardSurface),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.5f),
                        modifier = Modifier.size(48.dp)
                    )
                }
            }

            // Duration badge at bottom-right
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(YouTubeBadgeBg)
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = video.formattedDuration,
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // Details Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 12.dp, end = 4.dp, top = 12.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Channel Avatar
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(Color(video.channelAvatarColor or 0xFF000000)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = video.channelName.take(1).uppercase(),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Title and Metadata
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = video.displayTitle,
                        color = YouTubeTextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        lineHeight = 18.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    // MANDATORY: Small '+' or Edit icon next to title to rename
                    Box(
                        modifier = Modifier
                            .padding(start = 6.dp)
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(YouTubePillBg)
                            .clickable { onEditTitle() }
                            .testTag("edit_title_button_${video.id}"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Set Custom Title",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(3.dp))

                val subtitle = buildString {
                    append(video.channelName)
                    append(" • ")
                    append(video.formattedSize)
                    if (video.isSaved) append(" • Saved")
                }

                Text(
                    text = subtitle,
                    color = YouTubeTextSecondary,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // 3-dot Menu
            Box {
                IconButton(
                    onClick = { menuExpanded = true },
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("menu_button_${video.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Options",
                        tint = YouTubeTextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false },
                    modifier = Modifier.background(YouTubeCardSurface)
                ) {
                    DropdownMenuItem(
                        text = { Text("Set Custom Title", color = YouTubeTextPrimary) },
                        leadingIcon = {
                            Icon(Icons.Default.Edit, contentDescription = null, tint = YouTubeTextPrimary)
                        },
                        onClick = {
                            menuExpanded = false
                            onEditTitle()
                        }
                    )
                    DropdownMenuItem(
                        text = {
                            Text(
                                if (video.isSubscribed) "Unsubscribe" else "Subscribe to Channel",
                                color = YouTubeTextPrimary
                            )
                        },
                        onClick = {
                            menuExpanded = false
                            onToggleSubscribe()
                        }
                    )
                    DropdownMenuItem(
                        text = {
                            Text(
                                if (video.isSaved) "Remove from Saved" else "Save to Library",
                                color = YouTubeTextPrimary
                            )
                        },
                        onClick = {
                            menuExpanded = false
                            onToggleSave()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Share Video", color = YouTubeTextPrimary) },
                        onClick = {
                            menuExpanded = false
                            shareVideo(context, video)
                        }
                    )
                }
            }
        }
    }
}

private fun shareVideo(context: Context, video: VideoItem) {
    try {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "video/*"
            putExtra(Intent.EXTRA_STREAM, video.uri)
            putExtra(Intent.EXTRA_TEXT, "Watch '${video.displayTitle}' via MyTube!")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(shareIntent, "Share Video"))
    } catch (_: Exception) {
        val textIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, "Playing '${video.displayTitle}' on MyTube Media Player")
        }
        context.startActivity(Intent.createChooser(textIntent, "Share Video"))
    }
}
