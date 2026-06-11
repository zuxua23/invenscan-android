package com.invenscan.app.scanner

interface BatteryProvider {
    fun getHtBattery(): Int
    fun getRfidBattery(): Int?
}
