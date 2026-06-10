package com.invenscan.app.util

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.WorkManager
import com.invenscan.app.worker.SyncWorker

object WorkManagerUtil {

    fun schedulePeriodSync(context: Context) {
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            SyncWorker.TAG,
            ExistingPeriodicWorkPolicy.KEEP,
            SyncWorker.buildPeriodicRequest()
        )
    }

    fun cancelSync(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(SyncWorker.TAG)
    }

    fun triggerImmediateSync(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(SyncWorker.TAG)
        schedulePeriodSync(context)
    }
}
