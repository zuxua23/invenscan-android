package com.invenscan.app.scanner

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ScannerManager @Inject constructor() {

    private var scanner: ScannerContract = MockScanner()

    fun setScanner(scanner: ScannerContract) {
        this.scanner = scanner
    }

    fun getScanner(): ScannerContract = scanner

    fun isReady(): Boolean = scanner.isReady()
}
