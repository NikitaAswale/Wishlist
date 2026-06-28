package com.example.wishlist.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "wishes")
data class Wish(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String,
    val targetDate: Long,
    val createdAt: Long = System.currentTimeMillis(),
    val isFulfilled: Boolean = false,
    val priority: Priority = Priority.MEDIUM,
    val category: String = "General"
)

enum class Priority(val label: String) {
    LOW("Low"),
    MEDIUM("Medium"),
    HIGH("High"),
    DREAM("Dream")
}
