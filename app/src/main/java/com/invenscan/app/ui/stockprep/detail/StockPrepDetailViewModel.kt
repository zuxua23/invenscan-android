package com.invenscan.app.ui.stockprep.detail

import androidx.lifecycle.viewModelScope
import com.invenscan.app.base.BaseViewModel
import com.invenscan.app.base.Resource
import com.invenscan.app.data.model.StockPrepDetailItem
import com.invenscan.app.data.model.StockPrepDetailModel
import com.invenscan.app.data.model.StockPrepPickedItem
import com.invenscan.app.data.model.StockPrepSubmitRequest
import com.invenscan.app.data.repository.StockPrepRepository
import com.invenscan.app.scanner.ScannerContract
import com.invenscan.app.util.PrefManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PickItem(
    val detail: StockPrepDetailItem,
    val pickedQty: Int,
    val lastScannedCode: String?
)

@HiltViewModel
class StockPrepDetailViewModel @Inject constructor(
    private val stockPrepRepository: StockPrepRepository,
    private val prefManager: PrefManager
) : BaseViewModel() {

    private val _detailState = MutableStateFlow<Resource<StockPrepDetailModel>>(Resource.Loading)
    val detailState: StateFlow<Resource<StockPrepDetailModel>> = _detailState

    private val _pickItems = MutableStateFlow<List<PickItem>>(emptyList())
    val pickItems: StateFlow<List<PickItem>> = _pickItems

    private val _submitState = MutableStateFlow<Resource<Unit>?>(null)
    val submitState: StateFlow<Resource<Unit>?> = _submitState

    var currentPrepId: Long = 0L

    val totalItems get() = _pickItems.value.size
    val pickedItems get() = _pickItems.value.count { it.pickedQty >= it.detail.requestedQty }

    fun loadDetail(prepId: Long) {
        currentPrepId = prepId
        viewModelScope.launch {
            _detailState.value = Resource.Loading
            val result = stockPrepRepository.getPrepDetail(prepId)
            if (result is Resource.Success) {
                _pickItems.value = result.data.details.map { PickItem(it, it.pickedQty, null) }
            }
            _detailState.value = result
        }
    }

    fun onScanResult(code: String, type: ScannerContract.ScanType) {
        val itemCode = code.substringAfter("-").ifBlank { code }

        val matchIndex = _pickItems.value.indexOfFirst { pick ->
            pick.detail.itemCode.contains(itemCode, ignoreCase = true) ||
            pick.detail.scannedCode == code ||
            pick.pickedQty < pick.detail.requestedQty
        }

        if (matchIndex < 0) {
            postError("Kode $code tidak cocok dengan item manapun")
            return
        }

        val updated = _pickItems.value.toMutableList()
        val current = updated[matchIndex]
        if (current.pickedQty < current.detail.requestedQty) {
            updated[matchIndex] = current.copy(
                pickedQty = current.pickedQty + 1,
                lastScannedCode = code
            )
            _pickItems.value = updated
        } else {
            postError("Item ${current.detail.itemName} sudah terpenuhi")
        }
    }

    fun submit() {
        val pickedList = _pickItems.value.filter { it.pickedQty > 0 }
        if (pickedList.isEmpty()) {
            postError("Belum ada item yang dipick")
            return
        }

        viewModelScope.launch {
            _submitState.value = Resource.Loading
            val userId = prefManager.userId ?: ""

            val pickedItems = pickedList.map { pick ->
                StockPrepPickedItem(
                    detailId = pick.detail.id,
                    scannedCode = pick.lastScannedCode ?: pick.detail.scannedCode ?: "",
                    scanType = ScannerContract.ScanType.BARCODE.name,
                    pickedQty = pick.pickedQty
                )
            }

            val result = stockPrepRepository.submitBulk(
                StockPrepSubmitRequest(
                    stockPrepId = currentPrepId,
                    items = pickedItems
                )
            )

            if (result is Resource.Error) {
                pickedList.forEach { pick ->
                    stockPrepRepository.queuePendingSubmit(
                        stockPrepId = currentPrepId,
                        detailId = pick.detail.id,
                        tagId = pick.detail.scannedCode ?: "",
                        itemId = pick.detail.itemId,
                        scannedCode = pick.lastScannedCode ?: "",
                        scanType = ScannerContract.ScanType.BARCODE.name,
                        pickedQty = pick.pickedQty,
                        userId = userId
                    )
                }
                _submitState.value = Resource.Success(Unit)
            } else {
                _submitState.value = result
            }
        }
    }

    fun resetSubmitState() {
        _submitState.value = null
    }
}
