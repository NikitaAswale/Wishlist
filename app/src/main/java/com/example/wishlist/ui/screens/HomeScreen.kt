package com.example.wishlist.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.wishlist.data.Wish
import com.example.wishlist.ui.components.GradientBackground
import com.example.wishlist.ui.components.WishCard
import com.example.wishlist.ui.components.getDaysRemaining
import com.example.wishlist.ui.theme.GradientEnd
import com.example.wishlist.ui.theme.GradientMid
import com.example.wishlist.ui.theme.GradientStart
import com.example.wishlist.viewmodel.WishViewModel
import java.util.Calendar

@Composable
fun HomeScreen(
    onWishClick: (Long) -> Unit,
    onAddWishClick: () -> Unit,
    onCalendarClick: () -> Unit,
    viewModel: WishViewModel = hiltViewModel()
) {
    val activeWishes by viewModel.activeWishes.collectAsState()
    val fulfilledWishes by viewModel.fulfilledWishes.collectAsState()
    var showFulfilled by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddWishClick,
                containerColor = MaterialTheme.colorScheme.primary,
                elevation = FloatingActionButtonDefaults.elevation(8.dp),
                shape = CircleShape
            ) {
                Icon(
                    Icons.Filled.Add,
                    contentDescription = "Add Wish",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    ) { paddingValues ->
        GradientBackground(
            darkTheme = MaterialTheme.colorScheme.background.luminance() < 0.5f,
            modifier = Modifier.fillMaxSize()
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            // Header
            item {
                HomeHeader(
                    totalWishes = activeWishes.size + fulfilledWishes.size,
                    activeCount = activeWishes.size,
                    fulfilledCount = fulfilledWishes.size
                )
            }

            // Tab toggle
            item {
                TabToggle(
                    showFulfilled = showFulfilled,
                    onToggle = { showFulfilled = it },
                    activeCount = activeWishes.size,
                    fulfilledCount = fulfilledWishes.size
                )
            }

            if (showFulfilled) {
                if (fulfilledWishes.isEmpty()) {
                    item { EmptyState(message = "No fulfilled wishes yet.\nMake your dreams come true!") }
                } else {
                    items(
                        items = fulfilledWishes,
                        key = { it.id }
                    ) { wish ->
                        WishCard(
                            wish = wish,
                            onClick = { onWishClick(wish.id) },
                            onToggleFulfilled = { viewModel.toggleFulfilled(wish) },
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                        )
                    }
                }
            } else {
                if (activeWishes.isEmpty()) {
                    item {
                        EmptyState(message = "Your wishlist is empty.\nTap + to add your first wish!")
                    }
                } else {
                    // Group wishes by timeline
                    val grouped = groupWishesByTimeline(activeWishes)
                    grouped.forEach { (sectionTitle, wishes) ->
                        item {
                            SectionHeader(title = sectionTitle, count = wishes.size)
                        }
                        items(
                            items = wishes,
                            key = { it.id }
                        ) { wish ->
                            WishCard(
                                wish = wish,
                                onClick = { onWishClick(wish.id) },
                                onToggleFulfilled = { viewModel.toggleFulfilled(wish) },
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeHeader(
    totalWishes: Int,
    activeCount: Int,
    fulfilledCount: Int
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(20.dp)
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Filled.AutoAwesome,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "My Wishes",
                        style = MaterialTheme.typography.displayMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Dream it. Plan it. Achieve it.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Stats row
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                StatCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Outlined.AutoAwesome,
                    count = activeCount,
                    label = "Active",
                    gradientColors = listOf(GradientStart, GradientMid)
                )
                StatCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Filled.CheckCircle,
                    count = fulfilledCount,
                    label = "Fulfilled",
                    gradientColors = listOf(GradientMid, GradientEnd)
                )
                StatCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Filled.Star,
                    count = totalWishes,
                    label = "Total",
                    gradientColors = listOf(GradientEnd, GradientStart)
                )
            }
        }
    }
}

@Composable
private fun StatCard(
    modifier: Modifier = Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    count: Int,
    label: String,
    gradientColors: List<Color>
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.linearGradient(gradientColors.map { it.copy(alpha = 0.15f) })
            )
            .padding(16.dp)
    ) {
        Column {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = gradientColors[0],
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
private fun TabToggle(
    showFulfilled: Boolean,
    onToggle: (Boolean) -> Unit,
    activeCount: Int,
    fulfilledCount: Int
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        TabButton(
            text = "Active",
            count = activeCount,
            isSelected = !showFulfilled,
            onClick = { onToggle(false) },
            modifier = Modifier.weight(1f)
        )
        TabButton(
            text = "Fulfilled",
            count = fulfilledCount,
            isSelected = showFulfilled,
            onClick = { onToggle(true) },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun TabButton(
    text: String,
    count: Int,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(
                if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            )
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.titleSmall,
                color = if (isSelected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
            )
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.labelSmall,
                color = if (isSelected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun SectionHeader(title: String, count: Int) {
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
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "($count)",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
        )
    }
}

@Composable
private fun EmptyState(message: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 80.dp, bottom = 80.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Outlined.AutoAwesome,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
            modifier = Modifier.size(80.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
            textAlign = TextAlign.Center
        )
    }
}

private fun groupWishesByTimeline(wishes: List<Wish>): List<Pair<String, List<Wish>>> {
    val today = mutableListOf<Wish>()
    val thisWeek = mutableListOf<Wish>()
    val upcoming = mutableListOf<Wish>()
    val overdue = mutableListOf<Wish>()

    val now = Calendar.getInstance()
    now.set(Calendar.HOUR_OF_DAY, 0)
    now.set(Calendar.MINUTE, 0)
    now.set(Calendar.SECOND, 0)
    now.set(Calendar.MILLISECOND, 0)

    val weekEnd = Calendar.getInstance()
    weekEnd.set(Calendar.HOUR_OF_DAY, 0)
    weekEnd.set(Calendar.MINUTE, 0)
    weekEnd.set(Calendar.SECOND, 0)
    weekEnd.set(Calendar.MILLISECOND, 0)
    weekEnd.add(Calendar.DAY_OF_YEAR, 7)

    wishes.forEach { wish ->
        val days = getDaysRemaining(wish.targetDate)
        when {
            days < 0 -> overdue.add(wish)
            days == 0 -> today.add(wish)
            days <= 7 -> thisWeek.add(wish)
            else -> upcoming.add(wish)
        }
    }

    val result = mutableListOf<Pair<String, List<Wish>>>()
    if (overdue.isNotEmpty()) result.add("Overdue" to overdue)
    if (today.isNotEmpty()) result.add("Today" to today)
    if (thisWeek.isNotEmpty()) result.add("This Week" to thisWeek)
    if (upcoming.isNotEmpty()) result.add("Upcoming" to upcoming)
    return result
}

private fun Color.luminance(): Float {
    val r = this.red
    val g = this.green
    val b = this.blue
    return 0.299f * r + 0.587f * g + 0.114f * b
}
