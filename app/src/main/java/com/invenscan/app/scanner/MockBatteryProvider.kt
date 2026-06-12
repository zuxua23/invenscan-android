package com.invenscan.app.scanner

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager

class MockBatteryProvider(private val context: Context) : BatteryProvider {

    override fun getHtBattery(): Int {
        val intent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
            ?: return 0
        val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
        if (level < 0 || scale <= 0) return 0
        return (level * 100 / scale)
    }

    override fun getRfidBattery(): Int = 72
}
