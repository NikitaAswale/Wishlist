package com.example.wishlist.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.wishlist.data.Priority
import com.example.wishlist.data.Wish
import com.example.wishlist.data.isActive
import com.example.wishlist.ui.components.GradientBackground
import com.example.wishlist.ui.components.getDaysRemaining
import com.example.wishlist.ui.theme.GradientEnd
import com.example.wishlist.ui.theme.GradientMid
import com.example.wishlist.ui.theme.GradientStart
import com.example.wishlist.ui.theme.PriorityDream
import com.example.wishlist.ui.theme.PriorityHigh
import com.example.wishlist.ui.theme.PriorityLow
import com.example.wishlist.ui.theme.PriorityMedium
import com.example.wishlist.viewmodel.WishViewModel

@Composable
fun StatsScreen(
    viewModel: WishViewModel = hiltViewModel()
) {
    val allWishes by viewModel.allWishes.collectAsState()

    val activeWishes = allWishes.filter { it.isActive }
    val fulfilledWishes = allWishes.filter { it.isFulfilled }
    val completionRate = if (allWishes.isEmpty()) 0f
    else fulfilledWishes.size.toFloat() / allWishes.size

    val categoryStats = allWishes
        .groupBy { it.category.ifBlank { "General" } }
        .map { (category, wishes) -> category to wishes.size }
        .sortedByDescending { it.second }

    val priorityStats = Priority.entries.map { priority ->
        priority to allWishes.count { it.priority == priority }
    }.filter { it.second > 0 }

    val nextUpcomingWish = activeWishes
        .filter { getDaysRemaining(it.targetDate) >= 0 }
        .minByOrNull { it.targetDate }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        GradientBackground(
            darkTheme = MaterialTheme.colorScheme.background.luminance() < 0.50f,
            modifier = Modifier.fillMaxSize()
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(paddingValues),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            item {
                Text(
                    text = "Insights",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)
                )
            }

            if (allWishes.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 120.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.AutoAwesome,
                            contentDescription = "No insights yet",
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                            modifier = Modifier.size(80.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Add some wishes to see\nyour insights here!",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                // Overview cards
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OverviewCard(
                            icon = Icons.Filled.AutoAwesome,
                            count = allWishes.size,
                            label = "Total",
                            gradientColors = listOf(GradientStart, GradientMid),
                            modifier = Modifier.weight(1f)
                        )
                        OverviewCard(
                            icon = Icons.Filled.Schedule,
                            count = activeWishes.size,
                            label = "Active",
                            gradientColors = listOf(PriorityMedium, GradientStart),
                            modifier = Modifier.weight(1f)
                        )
                        OverviewCard(
                            icon = Icons.Filled.CheckCircle,
                            count = fulfilledWishes.size,
                            label = "Fulfilled",
                            gradientColors = listOf(PriorityLow, GradientEnd),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Completion progress card
                item {
                    CompletionCard(
                        completionRate = completionRate,
                        fulfilledCount = fulfilledWishes.size,
                        totalCount = allWishes.size
                    )
                }

                // Next upcoming wish
                if (nextUpcomingWish != null) {
                    item {
                        SectionHeader(title = "Next Up")
                        UpcomingWishCard(wish = nextUpcomingWish)
                    }
                }

                // Category breakdown
                if (categoryStats.isNotEmpty()) {
                    item {
                        SectionHeader(title = "By Category")
                        BreakdownCard {
                            categoryStats.forEachIndexed { index, (category, count) ->
                                if (index > 0) Spacer(modifier = Modifier.height(12.dp))
                                StatBar(
                                    label = category,
                                    count = count,
                                    fraction = count.toFloat() / allWishes.size,
                                    color = listOf(
                                        GradientStart, GradientMid, GradientEnd,
                                        PriorityHigh, PriorityDream, PriorityLow
                                    )[index % 6]
                                )
                            }
                        }
                    }
                }

                // Priority breakdown
                if (priorityStats.isNotEmpty()) {
                    item {
                        SectionHeader(title = "By Priority")
                        BreakdownCard {
                            priorityStats.forEachIndexed { index, (priority, count) ->
                                if (index > 0) Spacer(modifier = Modifier.height(12.dp))
                                StatBar(
                                    label = priority.label,
                                    count = count,
                                    fraction = count.toFloat() / allWishes.size,
                                    color = when (priority) {
                                        Priority.LOW -> PriorityLow
                                        Priority.MEDIUM -> PriorityMedium
                                        Priority.HIGH -> PriorityHigh
                                        Priority.DREAM -> PriorityDream
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OverviewCard(
    icon: ImageVector,
    count: Int,
    label: String,
    gradientColors: List<Color>,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.linearGradient(gradientColors.map { it.copy(alpha = 0.15f) })
            )
            .padding(14.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = icon,
                contentDescription = "$label insights",
                tint = gradientColors[0],
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
private fun CompletionCard(
    completionRate: Float,
    fulfilledCount: Int,
    totalCount: Int
) {
    val animatedProgress by animateFloatAsState(
        targetValue = completionRate.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 800),
        label = "completionProgress"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp)
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Dreams Come True",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "${(completionRate * 100).toInt()}%",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(5.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(animatedProgress)
                        .height(10.dp)
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(GradientStart, GradientMid, GradientEnd)
                            )
                        )
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "$fulfilledCount of $totalCount wishes fulfilled",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun UpcomingWishCard(wish: Wish) {
    val daysRemaining = getDaysRemaining(wish.targetDate)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
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
                    .clip(CircleShape)
                    .background(GradientEnd.copy(alpha = 0.25f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (daysRemaining == 0) "★" else "${daysRemaining}d",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = GradientMid
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = wish.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = when {
                        daysRemaining == 0 -> "Target date is today!"
                        daysRemaining == 1 -> "1 day to go"
                        else -> "$daysRemaining days to go"
                    },
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun BreakdownCard(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp)
    ) {
        Column {
            content()
        }
    }
}

@Composable
private fun StatBar(
    label: String,
    count: Int,
    fraction: Float,
    color: Color
) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction.coerceIn(0.02f, 1f))
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(color)
            )
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(width = 4.dp, height = 20.dp)
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

private fun Color.luminance(): Float {
    val r = this.red
    val g = this.green
    val b = this.blue
    return 0.299f * r + 0.587f * g + 0.114f * b
}
