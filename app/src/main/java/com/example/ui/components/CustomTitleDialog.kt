package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.VideoItem
import com.example.ui.theme.YouTubeCardSurface
import com.example.ui.theme.YouTubePillActiveBg
import com.example.ui.theme.YouTubePillActiveText
import com.example.ui.theme.YouTubePillBg
import com.example.ui.theme.YouTubeRed
import com.example.ui.theme.YouTubeTextPrimary
import com.example.ui.theme.YouTubeTextSecondary

@Composable
fun CustomTitleDialog(
    video: VideoItem,
    onDismiss: () -> Unit,
    onSave: (newTitle: String, category: String) -> Unit
) {
    var titleInput by remember { mutableStateOf(video.customTitle ?: video.originalTitle) }
    var selectedCat by remember { mutableStateOf(video.category) }
    val categories = listOf("All", "Shorts", "Music", "Gaming", "Tech", "Cinematic", "Podcasts")

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = YouTubeCardSurface,
        shape = RoundedCornerShape(16.dp),
        title = {
            Text(
                text = "Set Custom Title",
                color = YouTubeTextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Original filename: ${video.originalTitle}",
                    color = YouTubeTextSecondary,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = titleInput,
                    onValueChange = { titleInput = it },
                    label = { Text("Custom Video Title") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = YouTubeTextPrimary,
                        unfocusedTextColor = YouTubeTextPrimary,
                        focusedBorderColor = YouTubeRed,
                        unfocusedBorderColor = YouTubePillBg,
                        focusedLabelColor = YouTubeRed,
                        unfocusedLabelColor = YouTubeTextSecondary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("custom_title_input")
                )

                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "Category / Tag:",
                    color = YouTubeTextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    categories.forEach { cat ->
                        val isSel = cat.equals(selectedCat, ignoreCase = true)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSel) YouTubePillActiveBg else YouTubePillBg)
                                .clickable { selectedCat = cat }
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = cat,
                                color = if (isSel) YouTubePillActiveText else YouTubeTextPrimary,
                                fontSize = 11.sp,
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (titleInput.isNotBlank()) {
                        onSave(titleInput.trim(), selectedCat)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = YouTubeRed),
                modifier = Modifier.testTag("save_custom_title_button")
            ) {
                Text("Save Title", color = Color.White, fontWeight = FontWeight.SemiBold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = YouTubeTextSecondary)
            }
        }
    )
}
