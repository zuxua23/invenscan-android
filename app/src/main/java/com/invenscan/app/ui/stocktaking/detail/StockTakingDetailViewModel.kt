package com.invenscan.app.ui.stocktaking.detail

import androidx.lifecycle.viewModelScope
import com.invenscan.app.base.BaseViewModel
import com.invenscan.app.base.Resource
import com.invenscan.app.data.model.StockTakingAction
import com.invenscan.app.data.model.StockTakingSubmitRequest
import com.invenscan.app.data.model.StockTakingScanItem
import com.invenscan.app.data.model.StockTakingTagModel
import com.invenscan.app.data.repository.StockTakingRepository
import com.invenscan.app.util.PrefManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class ScanResultStatus { FOUND, MISSING, UNKNOWN }

data class ScanResultItem(
    val tagId: String,
    val itemCode: String,
    val itemName: String,
    val status: ScanResultStatus,
    val scannedAt: Long = System.currentTimeMillis()
)

@HiltViewModel
class StockTakingDetailViewModel @Inject constructor(
    private val stockTakingRepository: StockTakingRepository,
    private val prefManager: PrefManager
) : BaseViewModel() {

    private val _sessionTagsState = MutableStateFlow<Resource<List<StockTakingTagModel>>>(Resource.Loading)
    val sessionTagsState: StateFlow<Resource<List<StockTakingTagModel>>> = _sessionTagsState

    private val _scanResults = MutableStateFlow<List<ScanResultItem>>(emptyList())
    val scanResults: StateFlow<List<ScanResultItem>> = _scanResults

    private val _submitState = MutableStateFlow<Resource<Unit>?>(null)
    val submitState: StateFlow<Resource<Unit>?> = _submitState

    private var expectedTags: Map<String, StockTakingTagModel> = emptyMap()
    var currentSttId: Long = 0L

    val foundCount get() = _scanResults.value.count { it.status == ScanResultStatus.FOUND }
    val missingCount get() = _scanResults.value.count { it.status == ScanResultStatus.MISSING }
    val unknownCount get() = _scanResults.value.count { it.status == ScanResultStatus.UNKNOWN }

    fun loadSessionTags(sttId: Long) {
        currentSttId = sttId
        viewModelScope.launch {
            _sessionTagsState.value = Resource.Loading
            val result = stockTakingRepository.getSessionTags(sttId)
            if (result is Resource.Success) {
                expectedTags = result.data.associateBy { it.tagId }
                val initialResults = result.data.map { tag ->
                    ScanResultItem(
                        tagId = tag.tagId,
                        itemCode = tag.itemCode,
                        itemName = tag.itemName,
                        status = ScanResultStatus.MISSING
                    )
                }
                _scanResults.value = initialResults
            }
            _sessionTagsState.value = result
        }
    }

    fun onScanResult(code: String) {
        if (_scanResults.value.any { it.tagId == code && it.status == ScanResultStatus.FOUND }) return

        val expectedTag = expectedTags[code]
        val updatedResults = _scanResults.value.toMutableList()

        if (expectedTag != null) {
            val index = updatedResults.indexOfFirst { it.tagId == code }
            if (index >= 0) {
                updatedResults[index] = updatedResults[index].copy(
                    status = ScanResultStatus.FOUND,
                    scannedAt = System.currentTimeMillis()
                )
            }
        } else {
            updatedResults.add(
                ScanResultItem(
                    tagId = code,
                    itemCode = "UNKNOWN",
                    itemName = "Tag tidak dikenal",
                    status = ScanResultStatus.UNKNOWN,
                    scannedAt = System.currentTimeMillis()
                )
            )
        }
        _scanResults.value = updatedResults
    }

    fun submitResults() {
        viewModelScope.launch {
            _submitState.value = Resource.Loading
            val userId = prefManager.userId ?: ""

            val scannedItems = _scanResults.value.map { result ->
                StockTakingScanItem(
                    tagId = result.tagId,
                    itemId = expectedTags[result.tagId]?.itemId ?: 0L,
                    action = when (result.status) {
                        ScanResultStatus.FOUND -> StockTakingAction.SCAN
                        ScanResultStatus.MISSING -> StockTakingAction.MISSING
                        ScanResultStatus.UNKNOWN -> StockTakingAction.SCAN
                    },
                    scannedAt = result.scannedAt
                )
            }

            val request = StockTakingSubmitRequest(
                sttId = currentSttId,
                scannedTags = scannedItems
            )

            val result = stockTakingRepository.submitResults(request)
            if (result is Resource.Error) {
                _scanResults.value
                    .filter { it.status != ScanResultStatus.MISSING }
                    .forEach { item ->
                        stockTakingRepository.queueScanResult(
                            sttId = currentSttId,
                            tagId = item.tagId,
                            itemId = expectedTags[item.tagId]?.itemId ?: 0L,
                            action = StockTakingAction.SCAN,
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
