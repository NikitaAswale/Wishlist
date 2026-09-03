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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.wishlist.data.Priority
import com.example.wishlist.data.Wish
import com.example.wishlist.ui.components.GradientBackground
import com.example.wishlist.ui.components.WishCard
import com.example.wishlist.ui.components.formatDate
import com.example.wishlist.ui.components.getDaysRemaining
import com.example.wishlist.ui.components.shareWish
import com.example.wishlist.ui.theme.GradientEnd
import com.example.wishlist.ui.theme.GradientMid
import com.example.wishlist.ui.theme.GradientStart
import com.example.wishlist.ui.theme.PriorityDream
import com.example.wishlist.ui.theme.PriorityHigh
import com.example.wishlist.ui.theme.PriorityLow
import com.example.wishlist.ui.theme.PriorityMedium
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
    val context = LocalContext.current
    var showFulfilled by remember {
        mutableStateOf(false)
    }
    var searchQuery by remember { mutableStateOf("") }
    var selectedPriority by remember { mutableStateOf<Priority?>(null) }
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    var surpriseWish by remember { mutableStateOf<Wish?>(null) }

    fun pickSurpriseWish() {
        if (activeWishes.isEmpty()) {
            surpriseWish = null
        } else {
            // Avoid re-showing the same wish when there are plenty to choose from.
            val pool = if (activeWishes.size > 1) {
                activeWishes.filter { it.id != surpriseWish?.id }
            } else {
                activeWishes
            }
            surpriseWish = pool.random()
        }
    }

    val allWishes = activeWishes + fulfilledWishes
    val availableCategories = allWishes
        .map { it.category }
        .filter { it.isNotBlank() }
        .distinct()
        .sorted()

    val filteredActiveWishes = remember(activeWishes, searchQuery, selectedPriority, selectedCategory) {
        filterWishes(activeWishes, searchQuery, selectedPriority, selectedCategory)
    }
    val filteredFulfilledWishes = remember(fulfilledWishes, searchQuery, selectedPriority, selectedCategory) {
        filterWishes(fulfilledWishes, searchQuery, selectedPriority, selectedCategory)
    }
    val isFilterActive = searchQuery.isNotBlank() || selectedPriority != null || selectedCategory != null

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
            darkTheme = MaterialTheme.colorScheme.background.luminance() < 0.50f,
            modifier = Modifier.fillMaxSize()
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(bottom = 96.dp)
        ) {
            // Header
            item {
                HomeHeader(
                    totalWishes = activeWishes.size + fulfilledWishes.size,
                    activeCount = activeWishes.size,
                    fulfilledCount = fulfilledWishes.size
                )
            }

            // Surprise me - random wish picker
            if (!showFulfilled && activeWishes.isNotEmpty()) {
                item {
                    SurpriseButton(onClick = { pickSurpriseWish() })
                }
            }

            // Tab toggle
            item {
                TabToggle(
                    showFulfilled = showFulfilled,
                    onToggle = {
                        showFulfilled = it
                    },
                    activeCount = activeWishes.size,
                    fulfilledCount = fulfilledWishes.size
                )
            }

            // Search bar
            if (allWishes.isNotEmpty()) {
                item {
                    WishSearchBar(
                        query = searchQuery,
                        onQueryChange = { searchQuery = it }
                    )
                }

                // Filter chips
                item {
                    FilterChipsRow(
                        selectedPriority = selectedPriority,
                        onPrioritySelect = { priority ->
                            selectedPriority = if (selectedPriority == priority) null else priority
                        },
                        categories = availableCategories,
                        selectedCategory = selectedCategory,
                        onCategorySelect = { category ->
                            selectedCategory = if (selectedCategory == category) null else category
                        },
                        onClearAll = {
                            searchQuery = ""
                            selectedPriority = null
                            selectedCategory = null
                        },
                        isFilterActive = isFilterActive
                    )
                }
            }

            if (showFulfilled) {
                if (fulfilledWishes.isEmpty()) {
                    item {
                        EmptyState(message = "No fulfilled wishes yet.\nMake your dreams come true!")
                    }
                } else if (filteredFulfilledWishes.isEmpty()) {
                    item {
                        EmptyState(message = "No fulfilled wishes match\nyour search or filters.")
                    }
                } else {
                    items(
                        items = filteredFulfilledWishes,
                        key = {
                            it.id
                        }
                    ) { wish ->
                        WishCard(
                            wish = wish,
                            onClick = {
                                onWishClick(wish.id)
                            },
                            onToggleFulfilled = { viewModel.toggleFulfilled(wish) },
                            onShare = { shareWish(context, wish) },
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)
                        )
                    }
                }
            } else {
                if (activeWishes.isEmpty()) {
                    item {
                        EmptyState(message = "Your wishlist is empty.\nTap + to add your first wish!")
                    }
                } else if (filteredActiveWishes.isEmpty()) {
                    item {
                        EmptyState(message = "No active wishes match\nyour search or filters.")
                    }
                } else {
                    // Group wishes by timeline
                    val grouped = groupWishesByTimeline(filteredActiveWishes)
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
                                onShare = { shareWish(context, wish) },
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    surpriseWish?.let { wish ->
        SurpriseWishDialog(
            wish = wish,
            onOpen = {
                surpriseWish = null
                onWishClick(wish.id)
            },
            onPickAnother = { pickSurpriseWish() },
            onDismiss = { surpriseWish = null }
        )
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
            .padding(24.dp)
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
                        text = "Dream it. Then Plan it. Achieve it.",
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
                    label = "Totals",
                    gradientColors = listOf(GradientEnd, GradientStart)
                )
            }
        }
    }
}

@Composable
private fun StatCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
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
            onClick = {
                onToggle(false)
            },
            modifier = Modifier.weight(1f)
        )
        TabButton(
            text = "Fulfilled",
            count = fulfilledCount,
            isSelected = showFulfilled,
            onClick = {
                onToggle(true)
            },
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

@Composable
private fun WishSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        placeholder = {
            Text(
                text = "Search wishes...",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Filled.Search,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Clear search",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        },
        singleLine = true,
        shape = RoundedCornerShape(20.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface
        )
    )
}

@Composable
private fun FilterChipsRow(
    selectedPriority: Priority?,
    onPrioritySelect: (Priority) -> Unit,
    categories: List<String>,
    selectedCategory: String?,
    onCategorySelect: (String) -> Unit,
    onClearAll: () -> Unit,
    isFilterActive: Boolean,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(Priority.entries.toList()) { priority ->
            val isSelected = selectedPriority == priority
            FilterChip(
                selected = isSelected,
                onClick = { onPrioritySelect(priority) },
                shape = RoundedCornerShape(12.dp),
                label = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(
                                    when (priority) {
                                        Priority.LOW -> PriorityLow
                                        Priority.MEDIUM -> PriorityMedium
                                        Priority.HIGH -> PriorityHigh
                                        Priority.DREAM -> PriorityDream
                                    }
                                )
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = priority.label)
                    }
                }
            )
        }

        items(categories) { category ->
            val isSelected = selectedCategory == category
            FilterChip(
                selected = isSelected,
                onClick = { onCategorySelect(category) },
                shape = RoundedCornerShape(12.dp),
                label = { Text(text = category) }
            )
        }

        if (isFilterActive) {
            item {
                FilterChip(
                    selected = false,
                    onClick = onClearAll,
                    shape = RoundedCornerShape(12.dp),
                    label = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "Clear")
                        }
                    }
                )
            }
        }
    }
}

private fun filterWishes(
    wishes: List<Wish>,
    query: String,
    priority: Priority?,
    category: String?
): List<Wish> {
    val normalizedQuery = query.trim().lowercase()
    return wishes.filter { wish ->
        val matchesQuery = normalizedQuery.isEmpty() ||
            wish.title.lowercase().contains(normalizedQuery) ||
            wish.description.lowercase().contains(normalizedQuery) ||
            wish.category.lowercase().contains(normalizedQuery)
        val matchesPriority = priority == null || wish.priority == priority
        val matchesCategory = category == null || wish.category == category
        matchesQuery && matchesPriority && matchesCategory
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
@Composable
private fun SurpriseButton(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.horizontalGradient(
                    listOf(GradientStart, GradientMid)
                )
            )
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Casino,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Surprise me",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = "Pick a random wish from your list",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.85f)
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Icon(
            imageVector = Icons.Filled.ChevronRight,
            contentDescription = null,
            tint = Color.White
        )
    }
}
@Composable
private fun SurpriseWishDialog(
    wish: Wish,
    onOpen: () -> Unit,
    onPickAnother: () -> Unit,
    onDismiss: () -> Unit
) {
    val daysRemaining = getDaysRemaining(wish.targetDate)
    val priorityColor = when (wish.priority) {
        Priority.LOW -> PriorityLow
        Priority.MEDIUM -> PriorityMedium
        Priority.HIGH -> PriorityHigh
        Priority.DREAM -> PriorityDream
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Filled.AutoAwesome,
                contentDescription = null,
                tint = GradientMid
            )
        },
        title = { Text("Your surprise wish!") },
        text = {
            Column {
                Text(
                    text = wish.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (wish.description.isNotBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = wish.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 4,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(modifier = Modifier.height(14.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Schedule,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = formatDate(wish.targetDate),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(priorityColor.copy(alpha = 0.15f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = when {
                                daysRemaining < 0 -> "${-daysRemaining}d overdue"
                                daysRemaining == 0 -> "Today!"
                                daysRemaining == 1 -> "1 day left"
                                else -> "$daysRemaining days left"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = priorityColor
                        )
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(priorityColor)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = wish.priority.label,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (wish.category.isNotBlank() && wish.category != "General") {
                        Spacer(modifier = Modifier.width(12.dp))
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = wish.category,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onOpen) {
                Text("Open Wish")
            }
        },
        dismissButton = {
            TextButton(onClick = onPickAnother) {
                Text("Pick Another")
            }
        }
    )
}
