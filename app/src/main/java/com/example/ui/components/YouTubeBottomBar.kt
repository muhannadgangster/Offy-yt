package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Subscriptions
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Subscriptions
import androidx.compose.material.icons.outlined.VideoLibrary
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.YouTubeBlack
import com.example.ui.theme.YouTubeBorder
import com.example.ui.theme.YouTubeTextPrimary
import com.example.ui.theme.YouTubeTextSecondary

@Composable
fun YouTubeBottomBar(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(YouTubeBlack)
            .navigationBarsPadding()
            .testTag("youtube_bottom_bar")
    ) {
        HorizontalDivider(color = YouTubeBorder, thickness = 0.5.dp)

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 1. Home
            BottomNavItem(
                label = "Home",
                selectedIcon = Icons.Filled.Home,
                unselectedIcon = Icons.Outlined.Home,
                isSelected = selectedTab == 0,
                onClick = { onTabSelected(0) },
                tag = "tab_home"
            )

            // 2. Shorts
            BottomNavItem(
                label = "Shorts",
                selectedIcon = Icons.Filled.PlayArrow,
                unselectedIcon = Icons.Filled.PlayArrow,
                isSelected = selectedTab == 1,
                onClick = { onTabSelected(1) },
                tag = "tab_shorts"
            )

            // 3. Center Plus (+)
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(Color.Transparent)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onTabSelected(2) }
                    .testTag("tab_add_media"),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(YouTubeTextPrimary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Media",
                        tint = YouTubeBlack,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            // 4. Subscriptions
            BottomNavItem(
                label = "Subscriptions",
                selectedIcon = Icons.Filled.Subscriptions,
                unselectedIcon = Icons.Outlined.Subscriptions,
                isSelected = selectedTab == 3,
                onClick = { onTabSelected(3) },
                tag = "tab_subscriptions"
            )

            // 5. You (Library)
            BottomNavItem(
                label = "You",
                selectedIcon = Icons.Filled.VideoLibrary,
                unselectedIcon = Icons.Outlined.VideoLibrary,
                isSelected = selectedTab == 4,
                onClick = { onTabSelected(4) },
                tag = "tab_library"
            )
        }
    }
}

@Composable
private fun BottomNavItem(
    label: String,
    selectedIcon: ImageVector,
    unselectedIcon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    tag: String
) {
    val interactionSource = remember { MutableInteractionSource() }

    Column(
        modifier = Modifier
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .testTag(tag),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = if (isSelected) selectedIcon else unselectedIcon,
            contentDescription = label,
            tint = if (isSelected) YouTubeTextPrimary else YouTubeTextSecondary,
            modifier = Modifier.size(22.dp)
        )
        Text(
            text = label,
            color = if (isSelected) YouTubeTextPrimary else YouTubeTextSecondary,
            fontSize = 10.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}
