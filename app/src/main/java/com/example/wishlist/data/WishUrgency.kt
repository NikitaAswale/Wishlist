package com.example.wishlist.data

import java.util.Calendar
import java.util.concurrent.TimeUnit

/**
 * Urgency bucket for a [Wish] based on its [Wish.targetDate].
 *
 * Centralises the timeline logic that was previously duplicated in
 * HomeScreen (Overdue / Today / This Week / Upcoming) so it can be
 * reused by widgets, notifications and sorting.
 */
enum class WishUrgency(val label: String) {
    OVERDUE("Overdue"),
    TODAY("Today"),
    THIS_WEEK("This Week"),
    UPCOMING("Upcoming"),
    FULFILLED("Fulfilled")
}

/** Days from today (midnight) to [Wish.targetDate]. Negative = overdue. */
fun Wish.daysUntilDue(nowMillis: Long = System.currentTimeMillis()): Int {
    val startOfToday = Calendar.getInstance().apply {
        timeInMillis = nowMillis
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    val startOfTarget = Calendar.getInstance().apply {
        timeInMillis = targetDate
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    val diff = startOfTarget.timeInMillis - startOfToday.timeInMillis
    return TimeUnit.MILLISECONDS.toDays(diff).toInt()
}

fun Wish.isOverdue(nowMillis: Long = System.currentTimeMillis()): Boolean =
    !isFulfilled && daysUntilDue(nowMillis) < 0

fun Wish.isDueToday(nowMillis: Long = System.currentTimeMillis()): Boolean =
    !isFulfilled && daysUntilDue(nowMillis) == 0

fun Wish.isDueThisWeek(nowMillis: Long = System.currentTimeMillis()): Boolean {
    val days = daysUntilDue(nowMillis)
    return !isFulfilled && days in 1..7
}

fun Wish.isUpcoming(nowMillis: Long = System.currentTimeMillis()): Boolean =
    !isFulfilled && daysUntilDue(nowMillis) > 7

/** True when active and due within [daysAhead] days (today inclusive). */
fun Wish.isDueSoon(
    daysAhead: Int = 3,
    nowMillis: Long = System.currentTimeMillis()
): Boolean {
    val days = daysUntilDue(nowMillis)
    return !isFulfilled && days in 0..daysAhead
}

/** True when the wish is not yet fulfilled. */
val Wish.isActive: Boolean
    get() = !isFulfilled

/**
 * Maps a wish to its [WishUrgency] bucket. Fulfilled wishes always map to FULFILLED.
 *
 * Delegates to [isOverdue], [isDueToday] and [isDueThisWeek] so threshold logic
 * lives in one place.
 */
fun Wish.urgency(nowMillis: Long = System.currentTimeMillis()): WishUrgency {
    return when {
        isFulfilled -> WishUrgency.FULFILLED
        isOverdue(nowMillis) -> WishUrgency.OVERDUE
        isDueToday(nowMillis) -> WishUrgency.TODAY
        isDueThisWeek(nowMillis) -> WishUrgency.THIS_WEEK
        else -> WishUrgency.UPCOMING
    }
}

/**
 * Sorts wishes by urgency: Overdue -> Today -> This Week -> Upcoming -> Fulfilled,
 * then by earliest target date, then by priority (DREAM first).
 */
fun List<Wish>.sortedByUrgency(nowMillis: Long = System.currentTimeMillis()): List<Wish> {
    val urgencyOrder = mapOf(
        WishUrgency.OVERDUE to 0,
        WishUrgency.TODAY to 1,
        WishUrgency.THIS_WEEK to 2,
        WishUrgency.UPCOMING to 3,
        WishUrgency.FULFILLED to 4
    )
    val priorityOrder = mapOf(
        Priority.DREAM to 0,
        Priority.HIGH to 1,
        Priority.MEDIUM to 2,
        Priority.LOW to 3
    )
    return sortedWith(
        compareBy(
            { urgencyOrder.getValue(it.urgency(nowMillis)) },
            { it.targetDate },
            { priorityOrder.getValue(it.priority) }
        )
    )
}

/** Returns only active wishes sorted by urgency (most urgent first). */
fun List<Wish>.activeSortedByUrgency(nowMillis: Long = System.currentTimeMillis()): List<Wish> =
    activeWishes().sortedByUrgency(nowMillis)

/** Groups wishes into urgency buckets, omitting empty buckets. Order is most urgent first. */
fun List<Wish>.groupByUrgency(
    nowMillis: Long = System.currentTimeMillis()
): List<Pair<WishUrgency, List<Wish>>> {
    val orderedBuckets = listOf(
        WishUrgency.OVERDUE,
        WishUrgency.TODAY,
        WishUrgency.THIS_WEEK,
        WishUrgency.UPCOMING,
        WishUrgency.FULFILLED
    )
    val grouped = groupBy { it.urgency(nowMillis) }
    return orderedBuckets.mapNotNull { bucket ->
        grouped[bucket]?.takeIf { it.isNotEmpty() }?.let { bucket to it }
    }
}

/** Groups only active wishes into urgency buckets, omitting empty buckets. */
fun List<Wish>.activeGroupedByUrgency(
    nowMillis: Long = System.currentTimeMillis()
): List<Pair<WishUrgency, List<Wish>>> =
    activeWishes().groupByUrgency(nowMillis)

/** Groups only overdue wishes into their single bucket. */
fun List<Wish>.overdueGrouped(
    nowMillis: Long = System.currentTimeMillis()
): List<Pair<WishUrgency, List<Wish>>> =
    overdueWishes(nowMillis).let { list ->
        if (list.isEmpty()) emptyList() else listOf(WishUrgency.OVERDUE to list)
    }

/** Groups only due-today wishes into their single bucket. */
fun List<Wish>.dueTodayGrouped(
    nowMillis: Long = System.currentTimeMillis()
): List<Pair<WishUrgency, List<Wish>>> =
    dueTodayWishes(nowMillis).let { list ->
        if (list.isEmpty()) emptyList() else listOf(WishUrgency.TODAY to list)
    }

/** Groups only this-week wishes into their single bucket. */
fun List<Wish>.dueThisWeekGrouped(
    nowMillis: Long = System.currentTimeMillis()
): List<Pair<WishUrgency, List<Wish>>> =
    dueThisWeekWishes(nowMillis).let { list ->
        if (list.isEmpty()) emptyList() else listOf(WishUrgency.THIS_WEEK to list)
    }

/** Groups only upcoming wishes into their single bucket. */
fun List<Wish>.upcomingGrouped(
    nowMillis: Long = System.currentTimeMillis()
): List<Pair<WishUrgency, List<Wish>>> =
    upcomingWishes(nowMillis).let { list ->
        if (list.isEmpty()) emptyList() else listOf(WishUrgency.UPCOMING to list)
    }

/** Groups only fulfilled wishes into their single bucket. */
fun List<Wish>.fulfilledGrouped(): List<Pair<WishUrgency, List<Wish>>> =
    fulfilledWishes().let { list ->
        if (list.isEmpty()) emptyList() else listOf(WishUrgency.FULFILLED to list)
    }

/** Counts active wishes past their target date. */
fun List<Wish>.countOverdue(nowMillis: Long = System.currentTimeMillis()): Int =
    count { it.isOverdue(nowMillis) }

/** Counts active wishes due today. */
fun List<Wish>.countDueToday(nowMillis: Long = System.currentTimeMillis()): Int =
    count { it.isDueToday(nowMillis) }

/** Counts active wishes due in the next 1-7 days. */
fun List<Wish>.countDueThisWeek(nowMillis: Long = System.currentTimeMillis()): Int =
    count { it.isDueThisWeek(nowMillis) }

/** Counts active wishes due more than 7 days out. */
fun List<Wish>.countUpcoming(nowMillis: Long = System.currentTimeMillis()): Int =
    count { it.isUpcoming(nowMillis) }

/** Counts fulfilled wishes. */
fun List<Wish>.countFulfilled(): Int =
    count { it.isFulfilled }

/** Counts active (not yet fulfilled) wishes. */
fun List<Wish>.countActive(): Int =
    count { it.isActive }

/** Returns only active (not yet fulfilled) wishes. */
fun List<Wish>.activeWishes(): List<Wish> =
    filter { it.isActive }

/** Returns active wishes sorted by earliest target date first. */
fun List<Wish>.activeSortedByDate(): List<Wish> =
    activeWishes().sortedBy { it.targetDate }

/** Returns true when any wish is still active (not yet fulfilled). */
fun List<Wish>.hasActive(): Boolean =
    any { it.isActive }

/** Returns only fulfilled wishes. */
fun List<Wish>.fulfilledWishes(): List<Wish> =
    filter { it.isFulfilled }

/** Returns fulfilled wishes sorted by earliest target date first. */
fun List<Wish>.fulfilledSortedByDate(): List<Wish> =
    fulfilledWishes().sortedBy { it.targetDate }

/** Returns true when any wish is already fulfilled. */
fun List<Wish>.hasFulfilled(): Boolean =
    any { it.isFulfilled }

/** Returns only active wishes past their target date. */
fun List<Wish>.overdueWishes(nowMillis: Long = System.currentTimeMillis()): List<Wish> =
    filter { it.isOverdue(nowMillis) }

/** Returns overdue wishes sorted by earliest target date first. */
fun List<Wish>.overdueSortedByDate(nowMillis: Long = System.currentTimeMillis()): List<Wish> =
    overdueWishes(nowMillis).sortedBy { it.targetDate }

/** Returns true when any active wish is past its target date. */
fun List<Wish>.hasOverdue(nowMillis: Long = System.currentTimeMillis()): Boolean =
    any { it.isOverdue(nowMillis) }

/** Returns only active wishes due today. */
fun List<Wish>.dueTodayWishes(nowMillis: Long = System.currentTimeMillis()): List<Wish> =
    filter { it.isDueToday(nowMillis) }

/** Returns due-today wishes sorted by earliest target date first. */
fun List<Wish>.dueTodaySortedByDate(nowMillis: Long = System.currentTimeMillis()): List<Wish> =
    dueTodayWishes(nowMillis).sortedBy { it.targetDate }

/** Returns true when any active wish is due today. */
fun List<Wish>.hasDueToday(nowMillis: Long = System.currentTimeMillis()): Boolean =
    any { it.isDueToday(nowMillis) }

/** Returns only active wishes due in the next 1-7 days. */
fun List<Wish>.dueThisWeekWishes(nowMillis: Long = System.currentTimeMillis()): List<Wish> =
    filter { it.isDueThisWeek(nowMillis) }

/** Returns this-week wishes sorted by earliest target date first. */
fun List<Wish>.dueThisWeekSortedByDate(nowMillis: Long = System.currentTimeMillis()): List<Wish> =
    dueThisWeekWishes(nowMillis).sortedBy { it.targetDate }

/** Returns true when any active wish is due in the next 1-7 days. */
fun List<Wish>.hasDueThisWeek(nowMillis: Long = System.currentTimeMillis()): Boolean =
    any { it.isDueThisWeek(nowMillis) }

/** Returns only active wishes due more than 7 days out. */
fun List<Wish>.upcomingWishes(nowMillis: Long = System.currentTimeMillis()): List<Wish> =
    filter { it.isUpcoming(nowMillis) }

/** Returns upcoming wishes sorted by earliest target date first. */
fun List<Wish>.upcomingSortedByDate(nowMillis: Long = System.currentTimeMillis()): List<Wish> =
    upcomingWishes(nowMillis).sortedBy { it.targetDate }

/** Returns true when any active wish is due more than 7 days out. */
fun List<Wish>.hasUpcoming(nowMillis: Long = System.currentTimeMillis()): Boolean =
    any { it.isUpcoming(nowMillis) }

/** Returns only active wishes due within [daysAhead] days (today inclusive). */
fun List<Wish>.dueSoonWishes(
    daysAhead: Int = 3,
    nowMillis: Long = System.currentTimeMillis()
): List<Wish> =
    filter { it.isDueSoon(daysAhead, nowMillis) }

/** Returns due-soon wishes sorted by earliest target date first. */
fun List<Wish>.dueSoonSortedByDate(
    daysAhead: Int = 3,
    nowMillis: Long = System.currentTimeMillis()
): List<Wish> =
    dueSoonWishes(daysAhead, nowMillis).sortedBy { it.targetDate }

/** Counts active wishes due within [daysAhead] days (today inclusive). */
fun List<Wish>.countDueSoon(
    daysAhead: Int = 3,
    nowMillis: Long = System.currentTimeMillis()
): Int =
    count { it.isDueSoon(daysAhead, nowMillis) }

/** Returns true when any active wish is due within [daysAhead] days. */
fun List<Wish>.hasDueSoon(
    daysAhead: Int = 3,
    nowMillis: Long = System.currentTimeMillis()
): Boolean =
    any { it.isDueSoon(daysAhead, nowMillis) }

/** Counts wishes in the given [WishUrgency] bucket. */
fun List<Wish>.countByUrgency(
    urgency: WishUrgency,
    nowMillis: Long = System.currentTimeMillis()
): Int =
    count { it.urgency(nowMillis) == urgency }

/** Returns only wishes in the given [WishUrgency] bucket. */
fun List<Wish>.wishesByUrgency(
    urgency: WishUrgency,
    nowMillis: Long = System.currentTimeMillis()
): List<Wish> =
    filter { it.urgency(nowMillis) == urgency }

/** Returns true when any wish falls in the given [WishUrgency] bucket. */
fun List<Wish>.hasWishesInUrgency(
    urgency: WishUrgency,
    nowMillis: Long = System.currentTimeMillis()
): Boolean =
    any { it.urgency(nowMillis) == urgency }

/** Counts wishes with the given [Priority]. */
fun List<Wish>.countByPriority(priority: Priority): Int =
    count { it.priority == priority }

/** Returns only wishes with the given [Priority]. */
fun List<Wish>.wishesByPriority(priority: Priority): List<Wish> =
    filter { it.priority == priority }

/** Returns true when any wish has the given [Priority]. */
fun List<Wish>.hasWishesWithPriority(priority: Priority): Boolean =
    any { it.priority == priority }

/** Sorts wishes by priority, DREAM first, then HIGH, MEDIUM, LOW. */
fun List<Wish>.sortedByPriority(): List<Wish> {
    val priorityOrder = mapOf(
        Priority.DREAM to 0,
        Priority.HIGH to 1,
        Priority.MEDIUM to 2,
        Priority.LOW to 3
    )
    return sortedWith(compareBy { priorityOrder.getValue(it.priority) })
}
