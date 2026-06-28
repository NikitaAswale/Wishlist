package com.example.wishlist.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.wishlist.ui.theme.DarkBackground
import com.example.wishlist.ui.theme.GradientEnd
import com.example.wishlist.ui.theme.GradientMid
import com.example.wishlist.ui.theme.GradientStart
import com.example.wishlist.ui.theme.LightBackground

@Composable
fun GradientBackground(
    darkTheme: Boolean,
    modifier: Modifier = Modifier
) {
    val baseColor = if (darkTheme) DarkBackground else LightBackground
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        GradientStart.copy(alpha = if (darkTheme) 0.15f else 0.08f),
                        GradientMid.copy(alpha = if (darkTheme) 0.08f else 0.04f),
                        baseColor
                    )
                )
            )
    )
}

@Composable
fun HeaderGradient(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(280.dp)
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        GradientStart,
                        GradientMid,
                        GradientEnd
                    )
                )
            )
    )
}

@Composable
fun StarryOverlay(
    modifier: Modifier = Modifier,
    starColor: Color = Color.White.copy(alpha = 0.3f)
) {
    Box(
        modifier = modifier.background(
            Brush.radialGradient(
                colors = listOf(
                    starColor.copy(alpha = 0.15f),
                    Color.Transparent
                )
            )
        )
    )
}
