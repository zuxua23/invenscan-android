package com.invenscan.app.util

import android.util.Log
import com.invenscan.app.data.local.AppDatabase
import com.invenscan.app.data.local.entity.AppLogEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppLogger @Inject constructor(
    private val database: AppDatabase,
    private val prefManager: PrefManager
) {
    private val scope = CoroutineScope(Dispatchers.IO)
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

    fun log(action: String, module: String, description: String) {
        val userId = prefManager.userId ?: "unknown"
        val timestamp = dateFormat.format(Date())
        Log.d("AppLogger", "[$module] $action: $description (user=$userId)")

        scope.launch {
            runCatching {
                database.appDao().insertLog(
                    AppLogEntity(
                        level = "INFO",
                        tag = module,
                        message = "[$action] $description",
                        userId = userId,
                        deviceId = prefManager.deviceId,
                        timestamp = System.currentTimeMillis()
                    )
                )
            }
        }
    }

    fun logLogin(userId: String) = log("LOGIN", "AUTH", "User $userId logged in")
    fun logLogout(userId: String) = log("LOGOUT", "AUTH", "User $userId logged out")
    fun logStockInSubmit(itemCount: Int) = log("SUBMIT", "STOCK_IN", "Submitted $itemCount items")
    fun logStockOutSubmit(itemCount: Int) = log("SUBMIT", "STOCK_OUT", "Submitted $itemCount items")
    fun logStockTakingSubmit(sessionCode: String) = log("SUBMIT", "STOCK_TAKING", "Session $sessionCode submitted")
    fun logStockPrepSubmit(docNumber: String) = log("SUBMIT", "STOCK_PREP", "Doc $docNumber submitted")
    fun logSettingsChange(field: String) = log("SETTINGS_CHANGE", "SETTINGS", "Changed: $field")
}
