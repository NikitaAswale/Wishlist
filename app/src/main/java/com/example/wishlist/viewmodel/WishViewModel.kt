package com.example.wishlist.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wishlist.data.Priority
import com.example.wishlist.data.Wish
import com.example.wishlist.data.WishRepository
import com.example.wishlist.notification.ReminderScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WishViewModel @Inject constructor(
    private val repository: WishRepository,
    private val reminderScheduler: ReminderScheduler
) : ViewModel() {

    val allWishes: StateFlow<List<Wish>> = repository.allWishes
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val activeWishes: StateFlow<List<Wish>> = repository.activeWishes
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val fulfilledWishes: StateFlow<List<Wish>> = repository.fulfilledWishes
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    suspend fun getWishById(id: Long): Wish? = repository.getWishById(id)

    fun addWish(title: String, description: String, targetDate: Long, priority: Priority, category: String) {
        viewModelScope.launch {
            val wish = Wish(
                title = title,
                description = description,
                targetDate = targetDate,
                priority = priority,
                category = category
            )
            val id = repository.insert(wish)
            reminderScheduler.scheduleReminder(wish.copy(id = id))
        }
    }

    fun updateWish(wish: Wish) {
        viewModelScope.launch {
            repository.update(wish)
            reminderScheduler.scheduleReminder(wish)
        }
    }

    fun deleteWish(wish: Wish) {
        viewModelScope.launch {
            repository.delete(wish)
            reminderScheduler.cancelReminder(wish.id)
        }
    }

    fun toggleFulfilled(wish: Wish) {
        viewModelScope.launch {
            repository.setFulfilled(wish.id, !wish.isFulfilled)
            if (!wish.isFulfilled) {
                reminderScheduler.cancelReminder(wish.id)
            } else {
                reminderScheduler.scheduleReminder(wish)
            }
        }
    }

    fun rescheduleAllReminders() {
        viewModelScope.launch {
            activeWishes.value.forEach { wish ->
                if (!wish.isFulfilled) {
                    reminderScheduler.scheduleReminder(wish)
                }
            }
        }
    }
}
