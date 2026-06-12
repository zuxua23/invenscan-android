package com.invenscan.app.scanner

import android.content.Context

interface ScannerContract {

    fun initialize(context: Context, listener: ScanListener)
    fun startScan()
    fun stopScan()
    fun release()
    fun isReady(): Boolean

    interface ScanListener {
        fun onScanResult(code: String, type: ScanType)
        fun onScanError(message: String)
        fun onScannerDisconnected()
    }

    enum class ScanType { RFID, BARCODE }
}
