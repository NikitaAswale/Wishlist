package com.example.wishlist.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.wishlist.data.WishDao
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {

    @Inject
    lateinit var wishDao: WishDao

    @Inject
    lateinit var reminderScheduler: ReminderScheduler

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED ||
            intent.action == "android.intent.action.QUICKBOOT_POWERON" ||
            intent.action == "android.intent.action.MY_PACKAGE_REPLACED"
        ) {
            val pendingResult = goAsync()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    wishDao.getActiveWishesList().forEach { wish ->
                        reminderScheduler.scheduleReminder(wish)
                    }
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}
