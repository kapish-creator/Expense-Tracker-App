package com.example.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.data.local.PreferenceManager
import com.example.util.AlarmScheduler

class ExpenseAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val prefManager = PreferenceManager(context)

        // Show the reminder notification with sound
        AlarmScheduler.showReminderNotification(context)

        // Automatically reschedule next day's alarm if still enabled
        if (prefManager.isAlarmEnabled) {
            AlarmScheduler.scheduleDailyAlarm(
                context,
                prefManager.alarmHour,
                prefManager.alarmMinute
            )
        }
    }
}
