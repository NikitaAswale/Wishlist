package com.example.wishlist.ui.screens

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.BrightnessAuto
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.wishlist.ui.components.GradientBackground
import com.example.wishlist.ui.theme.GradientMid
import com.example.wishlist.ui.theme.ThemeMode

@Composable
fun SettingsScreen(
    themeMode: ThemeMode,
    onThemeModeChange: (ThemeMode) -> Unit
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        GradientBackground(
            darkTheme = MaterialTheme.colorScheme.background.luminance() < 0.50f,
            modifier = Modifier.fillMaxSize()
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Text(
                text = "Settings",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Personalize your wishlist experience.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )

            Spacer(modifier = Modifier.height(24.dp))

            SettingsSectionHeader(title = "Appearance")

            ThemeMode.entries.forEach { mode ->
                ThemeOptionRow(
                    mode = mode,
                    isSelected = themeMode == mode,
                    onClick = { onThemeModeChange(mode) }
                )
                Spacer(modifier = Modifier.height(10.dp))
            }

            Spacer(modifier = Modifier.height(16.dp))

            AboutCard()
        }
    }
}

@Composable
private fun ThemeOptionRow(
    mode: ThemeMode,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val icon = when (mode) {
        ThemeMode.SYSTEM -> Icons.Outlined.BrightnessAuto
        ThemeMode.LIGHT -> Icons.Filled.LightMode
        ThemeMode.DARK -> Icons.Filled.DarkMode
    }
    val subtitle = when (mode) {
        ThemeMode.SYSTEM -> "Follow your device's dark/light setting"
        ThemeMode.LIGHT -> "Always use the light theme"
        ThemeMode.DARK -> "Always use the dark theme"
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surface)
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(
                    if (isSelected) GradientMid.copy(alpha = 0.2f)
                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) GradientMid else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = mode.label,
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
        if (isSelected) {
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .clip(CircleShape)
                    .background(GradientMid),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = "Selected",
                    tint = Color.White,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}

@Composable
private fun SettingsSectionHeader(title: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(width = 4.dp, height = 18.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(MaterialTheme.colorScheme.primary)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun AboutCard() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.linearGradient(
                    listOf(GradientMid.copy(alpha = 0.12f), MaterialTheme.colorScheme.surface)
                )
            )
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Filled.Settings,
            contentDescription = null,
            tint = GradientMid,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = "Wishlist",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Keep dreaming. Make wishes come true.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun Color.luminance(): Float {
    val r = this.red
    val g = this.green
    val b = this.blue
    return 0.299f * r + 0.587f * g + 0.114f * b
}