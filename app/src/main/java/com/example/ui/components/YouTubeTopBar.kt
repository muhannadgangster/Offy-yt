package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Cast
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserProfile
import com.example.ui.theme.YouTubeBlack
import com.example.ui.theme.YouTubeCardSurface
import com.example.ui.theme.YouTubeRed
import com.example.ui.theme.YouTubeTextPrimary
import com.example.ui.theme.YouTubeTextSecondary

@Composable
fun YouTubeTopBar(
    isSearchActive: Boolean,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onSearchActiveToggle: (Boolean) -> Unit,
    userProfile: UserProfile,
    onProfileClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(YouTubeBlack)
            .statusBarsPadding()
            .padding(horizontal = 8.dp, vertical = 6.dp)
            .height(48.dp)
            .testTag("youtube_top_bar")
    ) {
        if (isSearchActive) {
            // Interactive Search Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(YouTubeCardSurface)
                    .padding(horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { onSearchActiveToggle(false) },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = YouTubeTextPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 6.dp)
                ) {
                    if (searchQuery.isEmpty()) {
                        Text(
                            text = "Search local videos...",
                            color = YouTubeTextSecondary,
                            fontSize = 15.sp
                        )
                    }
                    BasicTextField(
                        value = searchQuery,
                        onValueChange = onSearchQueryChange,
                        singleLine = true,
                        cursorBrush = SolidColor(YouTubeRed),
                        textStyle = TextStyle(
                            color = YouTubeTextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Normal
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("search_input")
                    )
                }

                if (searchQuery.isNotEmpty()) {
                    IconButton(
                        onClick = { onSearchQueryChange("") },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Clear search",
                            tint = YouTubeTextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        } else {
            // Standard YouTube Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // YouTube Logo + "MyTube"
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { /* Home click */ }
                        .padding(horizontal = 6.dp, vertical = 4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(width = 30.dp, height = 21.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(YouTubeRed),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "MyTube",
                        color = YouTubeTextPrimary,
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.5).sp
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                // Cast Icon
                IconButton(
                    onClick = { /* Cast indicator */ },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Cast,
                        contentDescription = "Cast",
                        tint = YouTubeTextPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Notification Bell with offline alert dot
                Box(contentAlignment = Alignment.TopEnd) {
                    IconButton(
                        onClick = { /* Notifications */ },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Notifications",
                            tint = YouTubeTextPrimary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Box(
                        modifier = Modifier
                            .padding(top = 8.dp, end = 8.dp)
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(YouTubeRed)
                    )
                }

                // Search Icon
                IconButton(
                    onClick = { onSearchActiveToggle(true) },
                    modifier = Modifier
                        .size(40.dp)
                        .testTag("search_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = YouTubeTextPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                }

                // Profile Avatar (Top-Right)
                Box(
                    modifier = Modifier
                        .padding(start = 4.dp, end = 4.dp)
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(Color(userProfile.avatarColor or 0xFF000000))
                        .clickable { onProfileClick() }
                        .testTag("profile_avatar_top"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = userProfile.avatarInitial,
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
