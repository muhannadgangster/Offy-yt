package com.example.ui.library

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Shield
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
import com.example.data.model.UserProfile
import com.example.ui.theme.YouTubeCardSurface
import com.example.ui.theme.YouTubePillBg
import com.example.ui.theme.YouTubeRed
import com.example.ui.theme.YouTubeTextPrimary
import com.example.ui.theme.YouTubeTextSecondary

@Composable
fun ProfileDialog(
    userProfile: UserProfile,
    onDismiss: () -> Unit,
    onSaveProfile: (name: String, handle: String) -> Unit
) {
    var nameInput by remember { mutableStateOf(userProfile.name) }
    var handleInput by remember { mutableStateOf(userProfile.handle) }
    var selectedProfileType by remember { mutableStateOf("Personal") }

    val presetAccounts = listOf(
        Triple("Alex Developer", "@alex_dev", 0xFF1E88E5),
        Triple("Studio Creator", "@studiocreator", 0xFFE53935),
        Triple("Offline Guest", "@guest_user", 0xFF43A047)
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = YouTubeCardSurface,
        shape = RoundedCornerShape(16.dp),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Shield, contentDescription = null, tint = YouTubeRed, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "MyTube Account & Security",
                    color = YouTubeTextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Switch between profiles or update your channel branding:",
                    color = YouTubeTextSecondary,
                    fontSize = 12.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Quick Switch Row
                presetAccounts.forEach { (name, handle, color) ->
                    val isCurrent = nameInput.equals(name, ignoreCase = true)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isCurrent) YouTubePillBg else Color.Transparent)
                            .clickable {
                                nameInput = name
                                handleInput = handle
                            }
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color(color or 0xFF000000)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(name.take(1), color = Color.White, fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(name, color = YouTubeTextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            Text(handle, color = YouTubeTextSecondary, fontSize = 11.sp)
                        }

                        if (isCurrent) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = YouTubeRed, modifier = Modifier.size(18.dp))
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = nameInput,
                    onValueChange = { nameInput = it },
                    label = { Text("Display Name") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = YouTubeTextPrimary,
                        unfocusedTextColor = YouTubeTextPrimary,
                        focusedBorderColor = YouTubeRed,
                        unfocusedBorderColor = YouTubePillBg
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("profile_name_input")
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = handleInput,
                    onValueChange = { handleInput = it },
                    label = { Text("Channel Handle") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = YouTubeTextPrimary,
                        unfocusedTextColor = YouTubeTextPrimary,
                        focusedBorderColor = YouTubeRed,
                        unfocusedBorderColor = YouTubePillBg
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("profile_handle_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (nameInput.isNotBlank()) {
                        onSaveProfile(nameInput.trim(), handleInput.trim())
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = YouTubeRed),
                modifier = Modifier.testTag("save_profile_button")
            ) {
                Text("Save Profile", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = YouTubeTextSecondary)
            }
        }
    )
}
