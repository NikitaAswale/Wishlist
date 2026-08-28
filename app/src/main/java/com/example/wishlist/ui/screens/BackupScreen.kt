package com.example.wishlist.ui.screens

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.wishlist.data.Wish
import com.example.wishlist.data.WishBackupSerializer
import com.example.wishlist.ui.components.GradientBackground
import com.example.wishlist.ui.theme.GradientEnd
import com.example.wishlist.ui.theme.GradientMid
import com.example.wishlist.ui.theme.PriorityHigh
import com.example.wishlist.viewmodel.WishViewModel
import java.io.IOException
import kotlinx.coroutines.launch

@Composable
fun BackupScreen(
    viewModel: WishViewModel = hiltViewModel()
) {
    val allWishes by viewModel.allWishes.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var pendingImportUri by remember { mutableStateOf<Uri?>(null) }
    var isWorking by remember { mutableStateOf(false) }

    fun showMessage(message: String) {
        scope.launch { snackbarHostState.showSnackbar(message) }
    }

    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            scope.launch {
                isWorking = true
                try {
                    context.exportWishesTo(uri, allWishes)
                    showMessage("Backup exported (${allWishes.size} wishes)")
                } catch (e: Exception) {
                    showMessage("Export failed: ${e.message ?: "unknown error"}")
                } finally {
                    isWorking = false
                }
            }
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        pendingImportUri = uri
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        GradientBackground(
            darkTheme = MaterialTheme.colorScheme.background.luminance() < 0.50f,
            modifier = Modifier.fillMaxSize()
        )

        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .verticalScroll(rememberScrollState())
                    .padding(paddingValues)
                    .padding(16.dp)
            ) {
                Text(
                    text = "Backup & Restore",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Keep your wishes safe by exporting them to a file on your device, or restore a previous backup anytime.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )

                Spacer(modifier = Modifier.height(24.dp))

                BackupSummaryCard(totalWishes = allWishes.size)

                Spacer(modifier = Modifier.height(16.dp))

                BackupActionCard(
                    icon = Icons.Filled.Upload,
                    title = "Export wishlist",
                    subtitle = if (allWishes.isEmpty()) "Add wishes first to create a backup"
                    else "Save ${allWishes.size} wishes to a JSON file",
                    iconTint = GradientMid,
                    enabled = allWishes.isNotEmpty() && !isWorking,
                    onClick = { exportLauncher.launch("wishlist_backup.json") }
                )

                Spacer(modifier = Modifier.height(12.dp))

                BackupActionCard(
                    icon = Icons.Filled.Download,
                    title = "Import a backup",
                    subtitle = "Restore wishes from a previously exported JSON file",
                    iconTint = PriorityHigh,
                    enabled = !isWorking,
                    onClick = {
                        importLauncher.launch(
                            arrayOf("application/json", "text/plain", "text/json", "application/octet-stream")
                        )
                    }
                )

                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Importing a backup replaces all current wishes with its contents. Exported files use the portable JSON format and can be shared across devices.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                    textAlign = TextAlign.Start
                )
            }

            if (isWorking) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }

    pendingImportUri?.let { uri ->
        AlertDialog(
            onDismissRequest = { pendingImportUri = null },
            title = { Text("Import backup?") },
            text = { Text("This will replace all of your current wishes with the contents of the backup file. This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    enabled = !isWorking,
                    onClick = {
                        pendingImportUri = null
                        scope.launch {
                            isWorking = true
                            try {
                                val wishes = context.readWishesFrom(uri)
                                viewModel.importWishes(wishes)
                                showMessage("Backup restored (${wishes.size} wishes)")
                            } catch (e: Exception) {
                                showMessage("Import failed: ${e.message ?: "invalid backup file"}")
                            } finally {
                                isWorking = false
                            }
                        }
                    }
                ) {
                    Text("Import")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { pendingImportUri = null },
                    enabled = !isWorking
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}
@Composable
private fun BackupSummaryCard(totalWishes: Int) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.linearGradient(
                    listOf(GradientMid.copy(alpha = 0.12f), GradientEnd.copy(alpha = 0.08f))
                )
            )
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(GradientMid.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Backup,
                    contentDescription = null,
                    tint = GradientMid,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "$totalWishes wish${if (totalWishes == 1) "" else "es"}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = if (totalWishes == 0) "Nothing to export yet"
                    else "Ready to back up your wishlist",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun BackupActionCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    iconTint: Color,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surface)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(iconTint.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (enabled) iconTint else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = Icons.Filled.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

private fun Context.exportWishesTo(uri: Uri, wishes: List<Wish>) {
    val json = WishBackupSerializer.wishesToJson(wishes)
    contentResolver.openOutputStream(uri)?.use { out ->
        out.write(json.toByteArray(Charsets.UTF_8))
    } ?: throw IOException("Could not open the selected file for writing")
}

private fun Context.readWishesFrom(uri: Uri): List<Wish> {
    val text = contentResolver.openInputStream(uri)?.use { input ->
        input.readBytes().toString(Charsets.UTF_8)
    } ?: throw IOException("Could not open the selected file for reading")
    return WishBackupSerializer.wishesFromJson(text)
}

private fun Color.luminance(): Float {
    val r = this.red
    val g = this.green
    val b = this.blue
    return 0.299f * r + 0.587f * g + 0.114f * b
}