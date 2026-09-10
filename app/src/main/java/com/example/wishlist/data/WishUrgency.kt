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
