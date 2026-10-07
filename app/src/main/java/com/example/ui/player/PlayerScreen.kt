package com.example.ui.player

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.media.MediaPlayer
import android.widget.FrameLayout
import android.widget.VideoView
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.ThumbDown
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.outlined.ThumbDown
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.data.model.VideoItem
import com.example.ui.components.VideoCard
import com.example.ui.theme.YouTubeBadgeBg
import com.example.ui.theme.YouTubeBlack
import com.example.ui.theme.YouTubeCardSurface
import com.example.ui.theme.YouTubePillActiveBg
import com.example.ui.theme.YouTubePillActiveText
import com.example.ui.theme.YouTubePillBg
import com.example.ui.theme.YouTubeRed
import com.example.ui.theme.YouTubeTextPrimary
import com.example.ui.theme.YouTubeTextSecondary
import kotlinx.coroutines.delay

@Composable
fun PlayerScreen(
    video: VideoItem,
    upNextVideos: List<VideoItem>,
    thumbnailBitmaps: Map<String, Bitmap>,
    onClose: () -> Unit,
    onEditTitle: () -> Unit,
    onToggleSubscribe: () -> Unit,
    onToggleLike: () -> Unit,
    onToggleDislike: () -> Unit,
    onToggleSave: () -> Unit,
    onSelectUpNext: (VideoItem) -> Unit,
    onEditUpNextTitle: (VideoItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    BackHandler { onClose() }

    var isPlaying by remember { mutableStateOf(true) }
    var showControls by remember { mutableStateOf(true) }
    var currentPositionMs by remember { mutableLongStateOf(0L) }
    var totalDurationMs by remember { mutableLongStateOf(video.durationMs.coerceAtLeast(1000L)) }
    var isSeeking by remember { mutableStateOf(false) }
    var seekProgress by remember { mutableFloatStateOf(0f) }

    var doubleTapFeedback by remember { mutableStateOf<String?>(null) }
    var isPlayerLocked by remember { mutableStateOf(false) }
    var speedMenuOpen by remember { mutableStateOf(false) }
    var playbackSpeed by remember { mutableFloatStateOf(1.0f) }

    var videoViewRef by remember { mutableStateOf<VideoView?>(null) }
    var mediaPlayerRef by remember { mutableStateOf<MediaPlayer?>(null) }
    var isPrepared by remember { mutableStateOf(false) }
    var hasError by remember { mutableStateOf(false) }

    // Auto-hide controls after 4 seconds of inactivity
    LaunchedEffect(showControls, isPlaying) {
        if (showControls && isPlaying) {
            delay(4000)
            showControls = false
        }
    }

    // Periodic time polling
    LaunchedEffect(isPlaying) {
        while (true) {
            videoViewRef?.let { vv ->
                if (vv.isPlaying && !isSeeking) {
                    currentPositionMs = vv.currentPosition.toLong()
                    if (vv.duration > 0) totalDurationMs = vv.duration.toLong()
                }
            }
            delay(500)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(YouTubeBlack)
            .statusBarsPadding()
            .testTag("player_screen")
    ) {
        // TOP HALF: Video Player Box
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
                .background(Color.Black)
                .pointerInput(isPlayerLocked, isPrepared) {
                    detectTapGestures(
                        onDoubleTap = { offset ->
                            if (!isPlayerLocked && isPrepared) {
                                val isRight = offset.x > size.width / 2
                                if (isRight) {
                                    // +10s skip
                                    videoViewRef?.let { vv ->
                                        val newPos = (vv.currentPosition + 10000).coerceAtMost(vv.duration)
                                        vv.seekTo(newPos)
                                        currentPositionMs = newPos.toLong()
                                    }
                                    doubleTapFeedback = "+10s"
                                } else {
                                    // -10s skip
                                    videoViewRef?.let { vv ->
                                        val newPos = (vv.currentPosition - 10000).coerceAtLeast(0)
                                        vv.seekTo(newPos)
                                        currentPositionMs = newPos.toLong()
                                    }
                                    doubleTapFeedback = "-10s"
                                }
                            }
                        },
                        onTap = {
                            showControls = !showControls
                        }
                    )
                }
        ) {
            // Native VideoView
            AndroidView(
                factory = { ctx ->
                    VideoView(ctx).apply {
                        layoutParams = FrameLayout.LayoutParams(
                            FrameLayout.LayoutParams.MATCH_PARENT,
                            FrameLayout.LayoutParams.MATCH_PARENT
                        )
                        setVideoURI(video.uri)
                        setOnPreparedListener { mp ->
                            mediaPlayerRef = mp
                            mp.isLooping = true
                            isPrepared = true
                            hasError = false
                            totalDurationMs = duration.toLong()
                            start()
                            isPlaying = true
                        }
                        setOnErrorListener { _, _, _ ->
                            hasError = true
                            isPrepared = false
                            isPlaying = false
                            mediaPlayerRef = null
                            true // Handle gracefully without crashing
                        }
                        videoViewRef = this
                    }
                },
                update = { vv ->
                    videoViewRef = vv
                },
                modifier = Modifier.fillMaxSize()
            )

            // Playback error overlay if any error occurs
            if (hasError) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.85f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Playback Error",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Unable to decode this video file",
                            color = YouTubeTextSecondary,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            // Double Tap Ripple/Indicator Overlay
            LaunchedEffect(doubleTapFeedback) {
                if (doubleTapFeedback != null) {
                    delay(800)
                    doubleTapFeedback = null
                }
            }
            androidx.compose.animation.AnimatedVisibility(
                visible = doubleTapFeedback != null,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier.align(Alignment.Center)
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color.Black.copy(alpha = 0.7f))
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (doubleTapFeedback == "+10s") Icons.Default.FastForward else Icons.Default.FastRewind,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = doubleTapFeedback ?: "",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Controls Overlay
            androidx.compose.animation.AnimatedVisibility(
                visible = showControls,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier.fillMaxSize()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.55f))
                ) {
                    // Top Bar inside Player
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onClose,
                            modifier = Modifier.testTag("player_close_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Close Player",
                                tint = Color.White
                            )
                        }

                        Spacer(modifier = Modifier.weight(1f))

                        // Player Lock Toggle
                        IconButton(onClick = { isPlayerLocked = !isPlayerLocked }) {
                            Icon(
                                imageVector = if (isPlayerLocked) Icons.Default.Lock else Icons.Default.LockOpen,
                                contentDescription = "Lock Controls",
                                tint = if (isPlayerLocked) YouTubeRed else Color.White
                            )
                        }

                        // Playback Speed
                        Box {
                            IconButton(onClick = { speedMenuOpen = true }) {
                                Icon(
                                    imageVector = Icons.Default.Speed,
                                    contentDescription = "Playback Speed",
                                    tint = Color.White
                                )
                            }
                            DropdownMenu(
                                expanded = speedMenuOpen,
                                onDismissRequest = { speedMenuOpen = false },
                                modifier = Modifier.background(YouTubeCardSurface)
                            ) {
                                listOf(0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 2.0f).forEach { speed ->
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                text = "${speed}x" + if (speed == playbackSpeed) " ✓" else "",
                                                color = YouTubeTextPrimary
                                            )
                                        },
                                        onClick = {
                                            playbackSpeed = speed
                                            speedMenuOpen = false
                                            mediaPlayerRef?.let { mp ->
                                                try {
                                                    mp.playbackParams = mp.playbackParams.setSpeed(speed)
                                                } catch (_: Exception) {}
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }

                    if (!isPlayerLocked) {
                        // Center Play / Pause
                        Box(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.6f))
                                .clickable {
                                    if (isPrepared) {
                                        videoViewRef?.let { vv ->
                                            if (vv.isPlaying) {
                                                vv.pause()
                                                isPlaying = false
                                            } else {
                                                vv.start()
                                                isPlaying = true
                                            }
                                        }
                                    }
                                }
                                .testTag("player_play_pause_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (isPlaying) "Pause" else "Play",
                                tint = Color.White,
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        // Bottom Seekbar & Time Row
                        Column(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = formatDuration(currentPositionMs),
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = formatDuration(totalDurationMs),
                                    color = Color.White.copy(alpha = 0.7f),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            Slider(
                                value = if (isSeeking) seekProgress else (currentPositionMs.toFloat() / totalDurationMs.coerceAtLeast(1L)),
                                onValueChange = {
                                    isSeeking = true
                                    seekProgress = it
                                },
                                onValueChangeFinished = {
                                    isSeeking = false
                                    val targetMs = (seekProgress * totalDurationMs).toLong()
                                    videoViewRef?.seekTo(targetMs.toInt())
                                    currentPositionMs = targetMs
                                },
                                colors = SliderDefaults.colors(
                                    thumbColor = YouTubeRed,
                                    activeTrackColor = YouTubeRed,
                                    inactiveTrackColor = Color.White.copy(alpha = 0.3f)
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(24.dp)
                                    .testTag("player_seekbar")
                            )
                        }
                    }
                }
            }
        }

        // BOTTOM HALF: Details, Actions, and Up Next
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(YouTubeBlack)
        ) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp)
                ) {
                    // Video Title Row with '+' Edit Button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = video.displayTitle,
                            color = YouTubeTextPrimary,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            lineHeight = 22.sp,
                            modifier = Modifier.weight(1f)
                        )

                        // Small '+' / Edit icon right next to title
                        Box(
                            modifier = Modifier
                                .padding(start = 8.dp)
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(YouTubePillBg)
                                .clickable { onEditTitle() }
                                .testTag("player_edit_title_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Edit Title",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Video stats
                    Text(
                        text = "${video.likeCount * 8} views • Local Storage • ${video.formattedSize}",
                        color = YouTubeTextSecondary,
                        fontSize = 12.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Channel Row with Subscribe Button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
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

                        Spacer(modifier = Modifier.width(10.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = video.channelName,
                                color = YouTubeTextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = video.channelHandle,
                                color = YouTubeTextSecondary,
                                fontSize = 11.sp
                            )
                        }

                        // YouTube Subscribe Pill Button
                        Button(
                            onClick = onToggleSubscribe,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (video.isSubscribed) YouTubePillBg else YouTubePillActiveBg
                            ),
                            shape = RoundedCornerShape(18.dp),
                            modifier = Modifier
                                .height(36.dp)
                                .testTag("player_subscribe_button")
                        ) {
                            Text(
                                text = if (video.isSubscribed) "Subscribed" else "Subscribe",
                                color = if (video.isSubscribed) YouTubeTextPrimary else YouTubePillActiveText,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Modern Pill Action Buttons Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Like / Dislike segmented pill
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(18.dp))
                                .background(YouTubePillBg)
                                .height(36.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier
                                    .clickable { onToggleLike() }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                                    .testTag("player_like_button"),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (video.isLiked) Icons.Filled.ThumbUp else Icons.Outlined.ThumbUp,
                                    contentDescription = "Like",
                                    tint = if (video.isLiked) YouTubeRed else YouTubeTextPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "${video.likeCount}",
                                    color = YouTubeTextPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .width(1.dp)
                                    .height(20.dp)
                                    .background(Color.White.copy(alpha = 0.2f))
                            )

                            IconButton(
                                onClick = onToggleDislike,
                                modifier = Modifier
                                    .size(36.dp)
                                    .testTag("player_dislike_button")
                            ) {
                                Icon(
                                    imageVector = if (video.isDisliked) Icons.Filled.ThumbDown else Icons.Outlined.ThumbDown,
                                    contentDescription = "Dislike",
                                    tint = if (video.isDisliked) YouTubeRed else YouTubeTextPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        // Share Pill
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(18.dp))
                                .background(YouTubePillBg)
                                .clickable { shareVideo(context, video) }
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                                .height(20.dp)
                                .testTag("player_share_button"),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Share",
                                tint = YouTubeTextPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Share",
                                color = YouTubeTextPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        // Save / Download Pill
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(18.dp))
                                .background(if (video.isSaved) YouTubePillActiveBg else YouTubePillBg)
                                .clickable { onToggleSave() }
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                                .height(20.dp)
                                .testTag("player_save_button"),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (video.isSaved) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
                                contentDescription = "Save",
                                tint = if (video.isSaved) YouTubePillActiveText else YouTubeTextPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (video.isSaved) "Saved" else "Save",
                                color = if (video.isSaved) YouTubePillActiveText else YouTubeTextPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))
                    HorizontalDivider(color = YouTubeCardSurface, thickness = 1.dp)
                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Up next",
                        color = YouTubeTextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Up Next Recommendations List
            items(upNextVideos.filter { it.uri != video.uri }, key = { it.id }) { nextVideo ->
                val thumb = thumbnailBitmaps[nextVideo.uri.toString()]
                VideoCard(
                    video = nextVideo,
                    thumbnailBitmap = thumb,
                    onClick = { onSelectUpNext(nextVideo) },
                    onEditTitle = { onEditUpNextTitle(nextVideo) },
                    onToggleSubscribe = {},
                    onToggleSave = {},
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            }
        }
    }

    DisposableEffect(video.uri) {
        onDispose {
            try {
                if (isPrepared) {
                    videoViewRef?.stopPlayback()
                }
            } catch (_: Exception) {}
            isPrepared = false
            mediaPlayerRef = null
            videoViewRef = null
        }
    }
}

private fun formatDuration(millis: Long): String {
    val totalSeconds = (millis / 1000).coerceAtLeast(0)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    val hours = minutes / 60
    return if (hours > 0) {
        String.format("%d:%02d:%02d", hours, minutes % 60, seconds)
    } else {
        String.format("%d:%02d", minutes, seconds)
    }
}

private fun shareVideo(context: Context, video: VideoItem) {
    try {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "video/*"
            putExtra(Intent.EXTRA_STREAM, video.uri)
            putExtra(Intent.EXTRA_TEXT, "Watching '${video.displayTitle}' via MyTube Offline Player")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(shareIntent, "Share Video"))
    } catch (_: Exception) {
        val textIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, "Watching '${video.displayTitle}' on MyTube")
        }
        context.startActivity(Intent.createChooser(textIntent, "Share Video"))
    }
}
