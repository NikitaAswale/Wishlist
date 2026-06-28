package com.example.wishlist.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import com.example.wishlist.ui.components.formatDate
import com.example.wishlist.ui.theme.GradientEnd
import com.example.wishlist.ui.theme.GradientMid
import com.example.wishlist.ui.theme.GradientStart
import com.example.wishlist.viewmodel.WishViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun CalendarScreen(
    onWishClick: (Long) -> Unit,
    onBack: () -> Unit,
    viewModel: WishViewModel = hiltViewModel()
) {
    val allWishes by viewModel.allWishes.collectAsState()
    var currentMonth by remember { mutableStateOf(Calendar.getInstance()) }
    var selectedDate by remember { mutableStateOf(Calendar.getInstance()) }

    val wishesForSelectedDate = allWishes.filter { wish ->
        val wishCal = Calendar.getInstance().apply { timeInMillis = wish.targetDate }
        wishCal.get(Calendar.YEAR) == selectedDate.get(Calendar.YEAR) &&
        wishCal.get(Calendar.DAY_OF_YEAR) == selectedDate.get(Calendar.DAY_OF_YEAR)
    }

    val wishDatesMap = remember(allWishes) {
        allWishes.groupBy { wish ->
            val cal = Calendar.getInstance().apply { timeInMillis = wish.targetDate }
            Triple(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH))
        }
    }

    GradientBackground(
        darkTheme = MaterialTheme.colorScheme.background.luminance() < 0.5f,
        modifier = Modifier.fillMaxSize()
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        // Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
                Icon(
                    Icons.Filled.CalendarMonth,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Calendar",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Month navigation
        item {
            MonthCalendar(
                currentMonth = currentMonth,
                selectedDate = selectedDate,
                wishDatesMap = wishDatesMap,
                onMonthChange = { currentMonth = it },
                onDateSelected = { selectedDate = it }
            )
        }

        // Selected date header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
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
                    text = formatDate(selectedDate.timeInMillis),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "(${wishesForSelectedDate.size})",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                )
            }
        }

        // Wishes for selected date
        if (wishesForSelectedDate.isEmpty()) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 40.dp, bottom = 40.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Filled.AutoAwesome,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No wishes for this date",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                    )
                }
            }
        } else {
            items(
                items = wishesForSelectedDate,
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

        item { Spacer(modifier = Modifier.height(80.dp)) }
    }
}

@Composable
private fun MonthCalendar(
    currentMonth: Calendar,
    selectedDate: Calendar,
    wishDatesMap: Map<Triple<Int, Int, Int>, List<Wish>>,
    onMonthChange: (Calendar) -> Unit,
    onDateSelected: (Calendar) -> Unit
) {
    val monthFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
    val today = Calendar.getInstance()
    today.set(Calendar.HOUR_OF_DAY, 0)
    today.set(Calendar.MINUTE, 0)
    today.set(Calendar.SECOND, 0)
    today.set(Calendar.MILLISECOND, 0)

    val daysInMonth = currentMonth.getActualMaximum(Calendar.DAY_OF_MONTH)
    val firstDayOfMonth = currentMonth.clone() as Calendar
    firstDayOfMonth.set(Calendar.DAY_OF_MONTH, 1)
    val firstDayOfWeek = firstDayOfMonth.get(Calendar.DAY_OF_WEEK) - 1 // 0-indexed (Sunday = 0)

    val dayLabels = listOf("S", "M", "T", "W", "T", "F", "S")

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp)
    ) {
        Column {
            // Month header with navigation
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = {
                        val newMonth = currentMonth.clone() as Calendar
                        newMonth.add(Calendar.MONTH, -1)
                        onMonthChange(newMonth)
                    }
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                        contentDescription = "Previous month",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
                Text(
                    text = monthFormat.format(currentMonth.time),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold
                )
                IconButton(
                    onClick = {
                        val newMonth = currentMonth.clone() as Calendar
                        newMonth.add(Calendar.MONTH, 1)
                        onMonthChange(newMonth)
                    }
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = "Next month",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Day labels
            Row(modifier = Modifier.fillMaxWidth()) {
                dayLabels.forEach { label ->
                    Text(
                        text = label,
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Calendar grid
            val totalCells = firstDayOfWeek + daysInMonth
            val rows = (totalCells + 6) / 7

            for (row in 0 until rows) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    for (col in 0 until 7) {
                        val dayIndex = row * 7 + col - firstDayOfWeek + 1
                        DayCell(
                            day = dayIndex,
                            isCurrentMonth = dayIndex in 1..daysInMonth,
                            isToday = isSameDay(currentMonth, dayIndex, today),
                            isSelected = isSameDay(currentMonth, dayIndex, selectedDate),
                            wishCount = if (dayIndex in 1..daysInMonth) {
                                val key = Triple(
                                    currentMonth.get(Calendar.YEAR),
                                    currentMonth.get(Calendar.MONTH),
                                    dayIndex
                                )
                                wishDatesMap[key]?.size ?: 0
                            } else 0,
                            onDateSelected = { day ->
                                val newDate = currentMonth.clone() as Calendar
                                newDate.set(Calendar.DAY_OF_MONTH, day)
                                newDate.set(Calendar.HOUR_OF_DAY, 0)
                                newDate.set(Calendar.MINUTE, 0)
                                newDate.set(Calendar.SECOND, 0)
                                newDate.set(Calendar.MILLISECOND, 0)
                                onDateSelected(newDate)
                            },
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DayCell(
    day: Int,
    isCurrentMonth: Boolean,
    isToday: Boolean,
    isSelected: Boolean,
    wishCount: Int,
    onDateSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val hasWishes = wishCount > 0

    Box(
        modifier = modifier
            .padding(2.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(
                when {
                    isSelected -> Brush.linearGradient(
                        listOf(GradientStart, GradientMid)
                    )
                    isToday -> Brush.linearGradient(
                        listOf(GradientStart.copy(alpha = 0.15f), GradientMid.copy(alpha = 0.15f))
                    )
                    hasWishes -> Brush.linearGradient(
                        listOf(GradientEnd.copy(alpha = 0.1f), GradientStart.copy(alpha = 0.1f))
                    )
                    else -> Brush.linearGradient(
                        listOf(Color.Transparent, Color.Transparent)
                    )
                }
            )
            .clickable(enabled = isCurrentMonth) { onDateSelected(day) },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = if (isCurrentMonth) day.toString() else "",
                style = MaterialTheme.typography.bodyMedium,
                color = when {
                    isSelected -> Color.White
                    isToday -> MaterialTheme.colorScheme.primary
                    hasWishes -> MaterialTheme.colorScheme.onSurface
                    else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                },
                fontWeight = if (isToday || isSelected) FontWeight.Bold else FontWeight.Normal
            )
            if (hasWishes && !isSelected) {
                Spacer(modifier = Modifier.height(2.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    repeat(minOf(wishCount, 3)) {
                        Box(
                            modifier = Modifier
                                .size(4.dp)
                                .clip(CircleShape)
                                .background(GradientMid)
                        )
                    }
                }
            }
            if (hasWishes && isSelected) {
                Spacer(modifier = Modifier.height(2.dp))
                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                )
            }
        }
    }
}

private fun isSameDay(cal: Calendar, day: Int, other: Calendar): Boolean {
    if (day < 1 || day > cal.getActualMaximum(Calendar.DAY_OF_MONTH)) return false
    return cal.get(Calendar.YEAR) == other.get(Calendar.YEAR) &&
           cal.get(Calendar.MONTH) == other.get(Calendar.MONTH) &&
           day == other.get(Calendar.DAY_OF_MONTH)
}

private fun Color.luminance(): Float {
    val r = this.red
    val g = this.green
    val b = this.blue
    return 0.299f * r + 0.587f * g + 0.114f * b
}
