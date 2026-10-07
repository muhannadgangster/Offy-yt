package com.example.ui.library

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.YouTubeCardSurface
import com.example.ui.theme.YouTubePillBg
import com.example.ui.theme.YouTubeRed
import com.example.ui.theme.YouTubeTextPrimary
import com.example.ui.theme.YouTubeTextSecondary
import kotlinx.coroutines.launch

@Composable
fun BackupSyncDialog(
    onDismiss: () -> Unit,
    onExportBackup: suspend () -> String,
    onImportBackup: (String, (Int) -> Unit) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var backupJson by remember { mutableStateOf("") }
    var importInput by remember { mutableStateOf("") }
    var statusMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        backupJson = onExportBackup()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = YouTubeCardSurface,
        shape = RoundedCornerShape(16.dp),
        title = {
            Text(
                text = "Cloud Sync & Device Backup",
                color = YouTubeTextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Backup all custom video titles, subscriptions, watch history, and private notes across multiple devices.",
                    color = YouTubeTextSecondary,
                    fontSize = 12.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Export Options
                Text(
                    text = "1. Export Backup (Multi-Device Sync)",
                    color = YouTubeTextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("MyTube Backup", backupJson))
                            Toast.makeText(context, "Backup copied to clipboard!", Toast.LENGTH_SHORT).show()
                            statusMessage = "Backup copied to clipboard!"
                        },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("copy_backup_button")
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, tint = YouTubeTextPrimary)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Copy Data", color = YouTubeTextPrimary, fontSize = 11.sp)
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    OutlinedButton(
                        onClick = {
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, backupJson)
                                putExtra(Intent.EXTRA_SUBJECT, "MyTube Cloud Backup")
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Share Backup"))
                        },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("share_backup_button")
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, tint = YouTubeTextPrimary)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Share File", color = YouTubeTextPrimary, fontSize = 11.sp)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Import Options
                Text(
                    text = "2. Restore / Sync from Other Device",
                    color = YouTubeTextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = importInput,
                    onValueChange = { importInput = it },
                    placeholder = { Text("Paste backup JSON here to restore...", color = YouTubeTextSecondary) },
                    maxLines = 3,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = YouTubeTextPrimary,
                        unfocusedTextColor = YouTubeTextPrimary,
                        focusedBorderColor = YouTubeRed,
                        unfocusedBorderColor = YouTubePillBg
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("import_backup_input")
                )

                if (statusMessage != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = statusMessage!!,
                        color = Color(0xFF4CAF50),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (importInput.isNotBlank()) {
                        onImportBackup(importInput.trim()) { count ->
                            statusMessage = "Successfully restored $count items!"
                            Toast.makeText(context, "Restored $count records!", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                enabled = importInput.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = YouTubeRed),
                modifier = Modifier.testTag("confirm_restore_button")
            ) {
                Text("Restore Data", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = YouTubeTextSecondary)
            }
        }
    )
}
