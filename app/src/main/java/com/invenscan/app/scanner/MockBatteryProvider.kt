package com.invenscan.app.scanner

class MockBatteryProvider : BatteryProvider {
    override fun getHtBattery(): Int = 85
    override fun getRfidBattery(): Int = 72
}
