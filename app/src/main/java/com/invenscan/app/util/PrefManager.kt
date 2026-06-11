package com.invenscan.app.util

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PrefManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val prefs: SharedPreferences by lazy { buildEncryptedPrefs() }

    private fun buildEncryptedPrefs(): SharedPreferences {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        return EncryptedSharedPreferences.create(
            context,
            PREFS_FILE_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    var token: String?
        get() = prefs.getString(KEY_TOKEN, null)
        set(value) = prefs.edit().putString(KEY_TOKEN, value).apply()

    var refreshToken: String?
        get() = prefs.getString(KEY_REFRESH_TOKEN, null)
        set(value) = prefs.edit().putString(KEY_REFRESH_TOKEN, value).apply()

    var userId: String?
        get() = prefs.getString(KEY_USER_ID, null)
        set(value) = prefs.edit().putString(KEY_USER_ID, value).apply()

    var fullName: String?
        get() = prefs.getString(KEY_FULL_NAME, null)
        set(value) = prefs.edit().putString(KEY_FULL_NAME, value).apply()

    var userRole: String?
        get() = prefs.getString(KEY_USER_ROLE, null)
        set(value) = prefs.edit().putString(KEY_USER_ROLE, value).apply()

    var serverUrl: String?
        get() = prefs.getString(KEY_SERVER_URL, null)
        set(value) = prefs.edit().putString(KEY_SERVER_URL, value).apply()

    var deviceId: String
        get() {
            val stored = prefs.getString(KEY_DEVICE_ID, null)
            if (!stored.isNullOrBlank()) return stored
            val generated = UUID.randomUUID().toString()
            prefs.edit().putString(KEY_DEVICE_ID, generated).apply()
            return generated
        }
        set(value) = prefs.edit().putString(KEY_DEVICE_ID, value).apply()

    var isDarkTheme: Boolean
        get() = prefs.getBoolean(KEY_DARK_THEME, false)
        set(value) = prefs.edit().putBoolean(KEY_DARK_THEME, value).apply()

    var rfidPower: Int
        get() = prefs.getInt(KEY_RFID_POWER, DEFAULT_RFID_POWER)
        set(value) = prefs.edit().putInt(KEY_RFID_POWER, value).apply()

    var rfidTriggerMode: String
        get() = prefs.getString(KEY_RFID_TRIGGER_MODE, DEFAULT_RFID_TRIGGER_MODE) ?: DEFAULT_RFID_TRIGGER_MODE
        set(value) = prefs.edit().putString(KEY_RFID_TRIGGER_MODE, value).apply()

    var rfidSensitivity: Int
        get() = prefs.getInt(KEY_RFID_SENSITIVITY, DEFAULT_RFID_SENSITIVITY)
        set(value) = prefs.edit().putInt(KEY_RFID_SENSITIVITY, value).apply()

    var rfidSession: String
        get() = prefs.getString(KEY_RFID_SESSION, DEFAULT_RFID_SESSION) ?: DEFAULT_RFID_SESSION
        set(value) = prefs.edit().putString(KEY_RFID_SESSION, value).apply()

    var rfidQFactor: Int
        get() = prefs.getInt(KEY_RFID_Q_FACTOR, DEFAULT_RFID_Q_FACTOR)
        set(value) = prefs.edit().putInt(KEY_RFID_Q_FACTOR, value).apply()

    var batteryDisplayMode: String
        get() = prefs.getString(KEY_BATTERY_DISPLAY_MODE, BATTERY_MODE_SINGLE) ?: BATTERY_MODE_SINGLE
        set(value) = prefs.edit().putString(KEY_BATTERY_DISPLAY_MODE, value).apply()

    val isLoggedIn: Boolean
        get() = !token.isNullOrBlank() && !serverUrl.isNullOrBlank()

    fun clearSession() {
        prefs.edit()
            .remove(KEY_TOKEN)
            .remove(KEY_REFRESH_TOKEN)
            .remove(KEY_USER_ID)
            .remove(KEY_FULL_NAME)
            .remove(KEY_USER_ROLE)
            .apply()
    }

    companion object {
        const val PREFS_FILE_NAME = "invenscan_secure_prefs"
        const val KEY_TOKEN = "token"
        const val KEY_REFRESH_TOKEN = "refresh_token"
        const val KEY_USER_ID = "user_id"
        const val KEY_FULL_NAME = "full_name"
        const val KEY_USER_ROLE = "user_role"
        const val KEY_SERVER_URL = "server_url"
        const val KEY_DEVICE_ID = "device_id"
        const val KEY_DARK_THEME = "dark_theme"
        const val KEY_RFID_POWER = "rfid_power"
        const val KEY_RFID_TRIGGER_MODE = "rfid_trigger_mode"
        const val KEY_RFID_SENSITIVITY = "rfid_sensitivity"
        const val KEY_RFID_SESSION = "rfid_session"
        const val KEY_RFID_Q_FACTOR = "rfid_q_factor"
        const val KEY_BATTERY_DISPLAY_MODE = "battery_display_mode"

        const val DEFAULT_RFID_POWER = 27
        const val DEFAULT_RFID_TRIGGER_MODE = "Continuous"
        const val DEFAULT_RFID_SENSITIVITY = 5
        const val DEFAULT_RFID_SESSION = "S1"
        const val DEFAULT_RFID_Q_FACTOR = 4

        const val BATTERY_MODE_SINGLE = "SINGLE"
        const val BATTERY_MODE_DUAL = "DUAL"
    }
}
