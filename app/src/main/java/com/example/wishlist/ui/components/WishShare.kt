package com.example.wishlist.ui.components

import android.content.Context
import android.content.Intent
import com.example.wishlist.data.Wish

/**
 * Builds a friendly, formatted message from a [Wish] and opens the Android
 * share sheet so the wish can be shared with friends and family.
 */
fun shareWish(context: Context, wish: Wish) {
    val message = buildString {
        append("✨ ")
        append(wish.title)
        if (wish.description.isNotBlank()) {
            append("\n\n")
            append(wish.description)
        }
        append("\n\n🗓️ Target: ")
        append(formatDate(wish.targetDate))
        append("\n⭐ Priority: ")
        append(wish.priority.label)
        if (wish.category.isNotBlank() && wish.category != "General") {
            append("\n🏷️ Category: ")
            append(wish.category)
        }
        append("\n✅ Status: ")
        append(if (wish.isFulfilled) "Fulfilled" else "In Progress")
    }

    val sendIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, "My wish: ${wish.title}")
        putExtra(Intent.EXTRA_TEXT, message)
    }
    context.startActivity(Intent.createChooser(sendIntent, "Share wish"))
}
