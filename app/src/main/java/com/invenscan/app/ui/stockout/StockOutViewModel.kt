package com.invenscan.app.ui.stockout

import androidx.lifecycle.viewModelScope
import com.invenscan.app.base.BaseViewModel
import com.invenscan.app.base.Resource
import com.invenscan.app.data.model.LocationModel
import com.invenscan.app.data.model.StockOutDetailRequest
import com.invenscan.app.data.model.StockOutSubmitRequest
import com.invenscan.app.data.repository.LocationRepository
import com.invenscan.app.data.repository.StockOutRepository
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

data class ScannedOutItem(
    val scannedCode: String,
    val scanType: ScannerContract.ScanType,
    val itemId: Long?,
    val itemCode: String?,
    val itemName: String?,
    val tagId: String?,
    val locationId: Long?,
    val locationName: String?,
    val isResolved: Boolean
)

@HiltViewModel
class StockOutViewModel @Inject constructor(
    private val locationRepository: LocationRepository,
    private val stockOutRepository: StockOutRepository,
    private val prefManager: PrefManager
) : BaseViewModel() {

    private val _locationsState = MutableStateFlow<Resource<List<LocationModel>>>(Resource.Loading)
    val locationsState: StateFlow<Resource<List<LocationModel>>> = _locationsState

    private val _scannedItems = MutableStateFlow<List<ScannedOutItem>>(emptyList())
    val scannedItems: StateFlow<List<ScannedOutItem>> = _scannedItems

    private val _submitState = MutableStateFlow<Resource<Unit>?>(null)
    val submitState: StateFlow<Resource<Unit>?> = _submitState

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning

    var selectedLocation: LocationModel? = null
    var scanMode: ScannerContract.ScanType = ScannerContract.ScanType.RFID

    private val sessionDocNumber: String by lazy {
        "SO-${SimpleDateFormat("yyyyMMdd-HHmmss", Locale.getDefault()).format(Date())}"
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

    fun startScan() {
        _isScanning.value = true
    }

    fun stopScan() {
        _isScanning.value = false
    }

    fun onScanResult(code: String, type: ScannerContract.ScanType) {
        if (_scannedItems.value.any { it.scannedCode == code }) return

        val placeholder = ScannedOutItem(
            scannedCode = code,
            scanType = type,
            itemId = null,
            itemCode = null,
            itemName = null,
            tagId = null,
            locationId = null,
            locationName = null,
            isResolved = false
        )
        _scannedItems.value = _scannedItems.value + placeholder

        viewModelScope.launch {
            val result = stockOutRepository.resolveTag(code, type)
            val resolved = when (result) {
                is Resource.Success -> placeholder.copy(
                    itemId = result.data.itemId,
                    itemCode = result.data.itemCode,
                    itemName = result.data.itemName,
                    tagId = result.data.tagId,
                    locationId = result.data.locationId,
                    locationName = result.data.locationName,
                    isResolved = true
                )
                else -> placeholder.copy(isResolved = true)
            }
            _scannedItems.value = _scannedItems.value.map {
                if (it.scannedCode == code) resolved else it
            }
        }
    }

    fun removeItem(position: Int) {
        val current = _scannedItems.value.toMutableList()
        if (position in current.indices) {
            current.removeAt(position)
            _scannedItems.value = current
        }
    }

    fun submitStockOut(locationId: Long) {
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
            val items = _scannedItems.value

            val details = items.map { item ->
                StockOutDetailRequest(
                    scannedCode = item.scannedCode,
                    scanType = item.scanType.name,
                    itemId = item.itemId
                )
            }

            val result = stockOutRepository.submitDirectly(
                StockOutSubmitRequest(
                    locationId = location.id,
                    notes = null,
                    details = details
                )
            )

            if (result is Resource.Error) {
                items.forEach { item ->
                    stockOutRepository.saveToQueue(
                        docNumber = sessionDocNumber,
                        locationId = location.id,
                        locationName = location.locationName,
                        tagId = item.tagId,
                        itemId = item.itemId,
                        itemName = item.itemName,
                        itemCode = item.itemCode,
                        scannedCode = item.scannedCode,
                        scanType = item.scanType
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
