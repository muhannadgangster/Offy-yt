package com.example.ui.shorts

import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.widget.FrameLayout
import android.widget.VideoView
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ThumbDown
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.outlined.ThumbDown
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
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
import com.example.ui.theme.YouTubeBlack
import com.example.ui.theme.YouTubeCardSurface
import com.example.ui.theme.YouTubePillActiveBg
import com.example.ui.theme.YouTubePillActiveText
import com.example.ui.theme.YouTubePillBg
import com.example.ui.theme.YouTubeRed
import com.example.ui.theme.YouTubeTextPrimary
import com.example.ui.theme.YouTubeTextSecondary
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShortsScreen(
    shortsList: List<VideoItem>,
    onToggleLike: (VideoItem) -> Unit,
    onToggleDislike: (VideoItem) -> Unit,
    onToggleSubscribe: (VideoItem) -> Unit,
    onEditTitle: (VideoItem) -> Unit,
    onSaveNotes: (VideoItem, String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    if (shortsList.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(YouTubeBlack),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No Shorts available. Add or import portrait videos!",
                color = YouTubeTextSecondary,
                fontSize = 14.sp
            )
        }
        return
    }

    val pagerState = rememberPagerState(pageCount = { shortsList.size })
    var commentsVideoTarget by remember { mutableStateOf<VideoItem?>(null) }
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()

    VerticalPager(
        state = pagerState,
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .testTag("shorts_pager")
    ) { page ->
        val currentShort = shortsList[page]
        val isCurrentPage = pagerState.currentPage == page

        ShortPageItem(
            video = currentShort,
            isActive = isCurrentPage,
            onToggleLike = { onToggleLike(currentShort) },
            onToggleDislike = { onToggleDislike(currentShort) },
            onToggleSubscribe = { onToggleSubscribe(currentShort) },
            onEditTitle = { onEditTitle(currentShort) },
            onOpenComments = { commentsVideoTarget = currentShort },
            onShare = { shareShortVideo(context, currentShort) }
        )
    }

    // Offline Comments & Notes Bottom Sheet
    if (commentsVideoTarget != null) {
        val target = commentsVideoTarget!!
        var noteInput by remember(target.notes) { mutableStateOf(target.notes) }

        ModalBottomSheet(
            onDismissRequest = { commentsVideoTarget = null },
            sheetState = sheetState,
            containerColor = YouTubeCardSurface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .navigationBarsPadding()
            ) {
                Text(
                    text = "Comments & Notes (Offline)",
                    color = YouTubeTextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Write your private notes or commentary for this short:",
                    color = YouTubeTextSecondary,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = noteInput,
                    onValueChange = { noteInput = it },
                    placeholder = { Text("Add a comment or personal note...", color = YouTubeTextSecondary) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = YouTubeTextPrimary,
                        unfocusedTextColor = YouTubeTextPrimary,
                        focusedBorderColor = YouTubeRed,
                        unfocusedBorderColor = YouTubePillBg
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("shorts_comment_input")
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = {
                            onSaveNotes(target, noteInput)
                            scope.launch {
                                sheetState.hide()
                                commentsVideoTarget = null
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = YouTubeRed),
                        modifier = Modifier.testTag("save_comment_button")
                    ) {
                        Text("Save Note", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun ShortPageItem(
    video: VideoItem,
    isActive: Boolean,
    onToggleLike: () -> Unit,
    onToggleDislike: () -> Unit,
    onToggleSubscribe: () -> Unit,
    onEditTitle: () -> Unit,
    onOpenComments: () -> Unit,
    onShare: () -> Unit
) {
    var isPlaying by remember { mutableStateOf(true) }
    var isPrepared by remember { mutableStateOf(false) }
    var hasError by remember { mutableStateOf(false) }
    var videoViewRef by remember { mutableStateOf<VideoView?>(null) }

    // Spinning record disc infinite animation
    val infiniteTransition = rememberInfiniteTransition(label = "disc")
    val discRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .pointerInput(isPrepared) {
                detectTapGestures(
                    onTap = {
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
                )
            }
    ) {
        // Fullscreen Video View
        AndroidView(
            factory = { ctx ->
                VideoView(ctx).apply {
                    layoutParams = FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT
                    )
                    setVideoURI(video.uri)
                    setOnPreparedListener { mp ->
                        mp.isLooping = true
                        isPrepared = true
                        hasError = false
                        if (isActive) {
                            start()
                            isPlaying = true
                        }
                    }
                    setOnErrorListener { _, _, _ ->
                        hasError = true
                        isPrepared = false
                        isPlaying = false
                        true
                    }
                    videoViewRef = this
                }
            },
            update = { vv ->
                videoViewRef = vv
                if (isPrepared) {
                    if (isActive) {
                        if (!vv.isPlaying && isPlaying) vv.start()
                    } else {
                        if (vv.isPlaying) vv.pause()
                    }
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // Pause Indicator in Center
        if (!isPlaying) {
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.6f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Play",
                    tint = Color.White,
                    modifier = Modifier.size(36.dp)
                )
            }
        }

        // Bottom Gradient Shadow for readability
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(280.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.9f))
                    )
                )
        )

        // Right Side Action Buttons Overlay
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 12.dp, bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Like
            ShortsActionButton(
                icon = if (video.isLiked) Icons.Filled.ThumbUp else Icons.Outlined.ThumbUp,
                label = "${video.likeCount}",
                tint = if (video.isLiked) YouTubeRed else Color.White,
                onClick = onToggleLike,
                tag = "shorts_like_button"
            )

            // Dislike
            ShortsActionButton(
                icon = if (video.isDisliked) Icons.Filled.ThumbDown else Icons.Outlined.ThumbDown,
                label = "Dislike",
                tint = if (video.isDisliked) YouTubeRed else Color.White,
                onClick = onToggleDislike,
                tag = "shorts_dislike_button"
            )

            // Comments / Notes
            ShortsActionButton(
                icon = Icons.Default.ChatBubbleOutline,
                label = if (video.notes.isNotBlank()) "Notes (1)" else "Comments",
                tint = Color.White,
                onClick = onOpenComments,
                tag = "shorts_comments_button"
            )

            // Share
            ShortsActionButton(
                icon = Icons.Default.Share,
                label = "Share",
                tint = Color.White,
                onClick = onShare,
                tag = "shorts_share_button"
            )

            // Spinning Vinyl Record Disc
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(YouTubeCardSurface)
                    .rotate(if (isPlaying) discRotation else 0f),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(YouTubeRed),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = "Sound",
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }

        // Bottom Left Info Overlay
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth(0.78f)
                .padding(start = 14.dp, bottom = 24.dp)
        ) {
            // Channel Row + Subscribe Button
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(Color(video.channelAvatarColor or 0xFF000000)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = video.channelName.take(1).uppercase(),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = video.channelHandle,
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.width(10.dp))

                // Subscribe Pill
                Button(
                    onClick = onToggleSubscribe,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (video.isSubscribed) YouTubePillBg else YouTubeRed
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .height(30.dp)
                        .testTag("shorts_subscribe_button")
                ) {
                    Text(
                        text = if (video.isSubscribed) "Subscribed" else "Subscribe",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Title with small '+' Edit button
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = video.displayTitle,
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                Box(
                    modifier = Modifier
                        .padding(start = 6.dp)
                        .size(26.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.25f))
                        .clickable { onEditTitle() }
                        .testTag("shorts_edit_title_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Edit Title",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Audio track ticker
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.GraphicEq,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.8f),
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "♫ ${video.channelName} • Original Sound",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 11.sp
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
            videoViewRef = null
        }
    }
}

@Composable
private fun ShortsActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    tint: Color,
    onClick: () -> Unit,
    tag: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable { onClick() }
            .testTag(tag)
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.45f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = tint,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = label,
            color = Color.White,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

private fun shareShortVideo(context: Context, video: VideoItem) {
    try {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "video/*"
            putExtra(Intent.EXTRA_STREAM, video.uri)
            putExtra(Intent.EXTRA_TEXT, "Watch this Short on MyTube: '${video.displayTitle}'")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(shareIntent, "Share Short"))
    } catch (_: Exception) {
        val textIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, "Watching '${video.displayTitle}' Short on MyTube!")
        }
        context.startActivity(Intent.createChooser(textIntent, "Share Short"))
    }
}
