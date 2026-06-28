package com.example.wishlist.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.wishlist.data.Wish
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Calendar
import javax.inject.Inject

class ReminderScheduler @Inject constructor(@param:ApplicationContext private val context: Context) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun scheduleReminder(wish: Wish) {
        if (wish.isFulfilled) return

        val reminderTime = calculateReminderTime(wish.targetDate)
        if (reminderTime <= System.currentTimeMillis()) return

        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra(EXTRA_WISH_ID, wish.id)
            putExtra(EXTRA_WISH_TITLE, wish.title)
            putExtra(EXTRA_WISH_DESCRIPTION, wish.description)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            wish.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    reminderTime,
                    pendingIntent
                )
            } else {
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    reminderTime,
                    pendingIntent
                )
            }
        } else {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                reminderTime,
                pendingIntent
            )
        }
    }

    fun cancelReminder(wishId: Long) {
        val intent = Intent(context, ReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            wishId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }

    private fun calculateReminderTime(targetDate: Long): Long {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = targetDate
        calendar.add(Calendar.DAY_OF_YEAR, -1)
        calendar.set(Calendar.HOUR_OF_DAY, 9)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)

        val reminderTime = calendar.timeInMillis
        if (reminderTime <= System.currentTimeMillis()) {
            calendar.timeInMillis = targetDate
            calendar.set(Calendar.HOUR_OF_DAY, 9)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            if (calendar.timeInMillis <= System.currentTimeMillis()) {
                return System.currentTimeMillis() + 60_000
            }
            return calendar.timeInMillis
        }
        return reminderTime
    }

    companion object {
        const val EXTRA_WISH_ID = "extra_wish_id"
        const val EXTRA_WISH_TITLE = "extra_wish_title"
        const val EXTRA_WISH_DESCRIPTION = "extra_wish_description"
        const val CHANNEL_ID = "wish_reminders"
    }
}
