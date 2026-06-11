package com.invenscan.app.ui.stockin

import androidx.lifecycle.viewModelScope
import com.invenscan.app.base.BaseViewModel
import com.invenscan.app.base.Resource
import com.invenscan.app.data.model.LocationModel
import com.invenscan.app.data.model.StockInDetailRequest
import com.invenscan.app.data.model.StockInSubmitRequest
import com.invenscan.app.data.repository.LocationRepository
import com.invenscan.app.data.repository.StockInRepository
import com.invenscan.app.scanner.ScannerContract
import com.invenscan.app.util.PrefManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

data class ScannedItem(
    val scannedCode: String,
    val scanType: ScannerContract.ScanType,
    val itemId: Long?,
    val itemCode: String?,
    val itemName: String?,
    val isResolved: Boolean
)

@HiltViewModel
class StockInViewModel @Inject constructor(
    private val locationRepository: LocationRepository,
    private val stockInRepository: StockInRepository,
    private val prefManager: PrefManager
) : BaseViewModel() {

    private val _locationsState = MutableStateFlow<Resource<List<LocationModel>>>(Resource.Loading)
    val locationsState: StateFlow<Resource<List<LocationModel>>> = _locationsState

    private val _scannedItems = MutableStateFlow<List<ScannedItem>>(emptyList())
    val scannedItems: StateFlow<List<ScannedItem>> = _scannedItems

    private val _submitState = MutableStateFlow<Resource<Unit>?>(null)
    val submitState: StateFlow<Resource<Unit>?> = _submitState

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning

    var selectedLocation: LocationModel? = null
    var scanMode: ScannerContract.ScanType = ScannerContract.ScanType.RFID

    private val sessionDocNumber: String by lazy {
        "SI-${SimpleDateFormat("yyyyMMdd-HHmmss", Locale.getDefault()).format(Date())}"
    }

    init {
        loadLocations()
    }

    fun loadLocations() {
        viewModelScope.launch {
            _locationsState.value = Resource.Loading
            _locationsState.value = locationRepository.getLocations()
        }
    }

    fun setScanningState(active: Boolean) {
        _isScanning.value = active
    }

    fun onScanResult(code: String, type: ScannerContract.ScanType) {
        if (_scannedItems.value.any { it.scannedCode == code }) return

        val placeholder = ScannedItem(
            scannedCode = code,
            scanType = type,
            itemId = null,
            itemCode = null,
            itemName = null,
            isResolved = false
        )
        _scannedItems.value = _scannedItems.value + placeholder

        viewModelScope.launch {
            val result = stockInRepository.resolveTag(code, type)
            val resolved = when (result) {
                is Resource.Success -> placeholder.copy(
                    itemId = result.data.itemId,
                    itemCode = result.data.itemCode,
                    itemName = result.data.itemName,
                    isResolved = true
                )
                else -> placeholder.copy(isResolved = true)
            }
            _scannedItems.value = _scannedItems.value.map {
                if (it.scannedCode == code) resolved else it
            }
        }
    }

    fun submit() {
        val location = selectedLocation ?: run {
            postError("Pilih lokasi terlebih dahulu")
            return
        }
        if (_scannedItems.value.isEmpty()) {
            postError("Tidak ada item untuk disubmit")
            return
        }

        viewModelScope.launch {
            _submitState.value = Resource.Loading
            val userId = prefManager.userId ?: ""
            val items = _scannedItems.value

            val details = items.map { item ->
                StockInDetailRequest(
                    scannedCode = item.scannedCode,
                    scanType = item.scanType.name,
                    itemId = item.itemId
                )
            }

            val result = stockInRepository.submitDirectly(
                StockInSubmitRequest(
                    locationId = location.id,
                    notes = null,
                    details = details
                )
            )

            if (result is Resource.Error) {
                items.forEach { item ->
                    stockInRepository.saveToQueue(
                        docNumber = sessionDocNumber,
                        locationId = location.id,
                        scannedCode = item.scannedCode,
                        scanType = item.scanType,
                        itemId = item.itemId,
                        itemName = item.itemName,
                        notes = null,
                        userId = userId
                    )
                }
                _submitState.value = Resource.Success(Unit)
            } else {
                _submitState.value = result
            }

            if (_submitState.value is Resource.Success) {
                _scannedItems.value = emptyList()
            }
        }
    }

    fun resetSubmitState() {
        _submitState.value = null
    }

    fun clearScans() {
        _scannedItems.value = emptyList()
    }
}
