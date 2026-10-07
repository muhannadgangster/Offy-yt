package com.example.ui.add

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.VideoFile
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.YouTubeCardSurface
import com.example.ui.theme.YouTubePillActiveBg
import com.example.ui.theme.YouTubePillActiveText
import com.example.ui.theme.YouTubePillBg
import com.example.ui.theme.YouTubeRed
import com.example.ui.theme.YouTubeTextPrimary
import com.example.ui.theme.YouTubeTextSecondary

@Composable
fun AddMediaDialog(
    onDismiss: () -> Unit,
    onImportVideo: (uri: Uri, title: String?, category: String) -> Unit
) {
    var selectedUri by remember { mutableStateOf<Uri?>(null) }
    var customTitle by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Shorts") }
    val categories = listOf("Shorts", "Music", "Cinematic", "Gaming", "Tech", "All")

    // Android Modern Zero-Permission Photo/Video Picker
    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedUri = uri
            if (customTitle.isBlank()) {
                customTitle = "My Imported Video"
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = YouTubeCardSurface,
        shape = RoundedCornerShape(16.dp),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(YouTubeRed),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Add to MyTube Catalog",
                    color = YouTubeTextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Select a local video file from your device gallery to play and assign a custom title.",
                    color = YouTubeTextSecondary,
                    fontSize = 12.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Media Source Chooser Button
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(YouTubePillBg)
                        .clickable {
                            videoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
                            )
                        }
                        .padding(14.dp)
                        .testTag("pick_device_video_button"),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.VideoFile,
                        contentDescription = null,
                        tint = YouTubeRed,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = if (selectedUri != null) "Video Selected ✓" else "Choose from Device Gallery",
                            color = YouTubeTextPrimary,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = if (selectedUri != null) selectedUri.toString().takeLast(30) else "Browse .mp4, .mkv, .mov files",
                            color = YouTubeTextSecondary,
                            fontSize = 11.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = customTitle,
                    onValueChange = { customTitle = it },
                    label = { Text("Custom Video Title") },
                    placeholder = { Text("e.g., My Favorite Song / Highlight") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = YouTubeTextPrimary,
                        unfocusedTextColor = YouTubeTextPrimary,
                        focusedBorderColor = YouTubeRed,
                        unfocusedBorderColor = YouTubePillBg
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("add_custom_title_input")
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Select Category / Tab:",
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
                        val isSel = cat.equals(selectedCategory, ignoreCase = true)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSel) YouTubePillActiveBg else YouTubePillBg)
                                .clickable { selectedCategory = cat }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
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
                    val uri = selectedUri
                    if (uri != null) {
                        onImportVideo(uri, customTitle.ifBlank { null }, selectedCategory)
                    }
                },
                enabled = selectedUri != null,
                colors = ButtonDefaults.buttonColors(containerColor = YouTubeRed),
                modifier = Modifier.testTag("confirm_import_button")
            ) {
                Text("Add Video", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = YouTubeTextSecondary)
            }
        }
    )
}
