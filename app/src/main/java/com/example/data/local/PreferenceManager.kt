package com.example.data.local

import android.content.Context
import android.content.SharedPreferences
import java.security.MessageDigest

class PreferenceManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREFS_NAME = "myspend_prefs"
        private const val KEY_PIN_HASH = "key_pin_hash"
        private const val KEY_PIN_ENABLED = "key_pin_enabled"
        private const val KEY_MONTHLY_BALANCE = "key_monthly_balance"
        private const val KEY_ALARM_ENABLED = "key_alarm_enabled"
        private const val KEY_ALARM_HOUR = "key_alarm_hour"
        private const val KEY_ALARM_MINUTE = "key_alarm_minute"
    }

    var isPinEnabled: Boolean
        get() = prefs.getBoolean(KEY_PIN_ENABLED, false)
        set(value) = prefs.edit().putBoolean(KEY_PIN_ENABLED, value).apply()

    var monthlyBalance: Double
        get() = prefs.getFloat(KEY_MONTHLY_BALANCE, 0f).toDouble()
        set(value) = prefs.edit().putFloat(KEY_MONTHLY_BALANCE, value.toFloat()).apply()

    var isAlarmEnabled: Boolean
        get() = prefs.getBoolean(KEY_ALARM_ENABLED, false)
        set(value) = prefs.edit().putBoolean(KEY_ALARM_ENABLED, value).apply()

    var alarmHour: Int
        get() = prefs.getInt(KEY_ALARM_HOUR, 20) // default 8 PM
        set(value) = prefs.edit().putInt(KEY_ALARM_HOUR, value).apply()

    var alarmMinute: Int
        get() = prefs.getInt(KEY_ALARM_MINUTE, 0)
        set(value) = prefs.edit().putInt(KEY_ALARM_MINUTE, value).apply()

    fun setPin(pin: String) {
        val hash = hashPin(pin)
        prefs.edit()
            .putString(KEY_PIN_HASH, hash)
            .putBoolean(KEY_PIN_ENABLED, true)
            .apply()
    }

    fun verifyPin(pin: String): Boolean {
        val storedHash = prefs.getString(KEY_PIN_HASH, null) ?: return false
        return storedHash == hashPin(pin)
    }

    fun disablePin() {
        prefs.edit()
            .remove(KEY_PIN_HASH)
            .putBoolean(KEY_PIN_ENABLED, false)
            .apply()
    }

    private fun hashPin(pin: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        val bytes = md.digest("myspend_salt_$pin".toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
