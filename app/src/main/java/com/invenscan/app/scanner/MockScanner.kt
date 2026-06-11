package com.invenscan.app.scanner

import android.content.Context

class MockScanner : ScannerContract {

    private var listener: ScannerContract.ScanListener? = null
    private var isRunning = false

    override fun initialize(context: Context, listener: ScannerContract.ScanListener) {
        this.listener = listener
    }

    override fun startScan() {
        isRunning = true
    }

    override fun stopScan() {
        isRunning = false
    }

    override fun release() {
        isRunning = false
        listener = null
    }

    override fun isReady(): Boolean = true

    /**
     * Simulates an incoming scan result — use in tests and demos without hardware.
     */
    fun simulateScan(code: String, type: ScannerContract.ScanType) {
        if (isRunning) {
            listener?.onScanResult(code, type)
        }
    }

    fun simulateError(message: String) {
        listener?.onScanError(message)
    }

    fun simulateDisconnect() {
        listener?.onScannerDisconnected()
    }
}
