package com.invenscan.app.ui.search

import androidx.lifecycle.viewModelScope
import com.invenscan.app.base.BaseViewModel
import com.invenscan.app.base.Resource
import com.invenscan.app.data.model.SearchItemModel
import com.invenscan.app.data.repository.SearchItemRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SearchHistoryItem(
    val code: String,
    val result: SearchItemModel?
)

@HiltViewModel
class SearchItemViewModel @Inject constructor(
    private val searchItemRepository: SearchItemRepository
) : BaseViewModel() {

    private val _searchState = MutableStateFlow<Resource<SearchItemModel>?>(null)
    val searchState: StateFlow<Resource<SearchItemModel>?> = _searchState

    private val _scanHistory = MutableStateFlow<List<SearchHistoryItem>>(emptyList())
    val scanHistory: StateFlow<List<SearchHistoryItem>> = _scanHistory

    fun onScanResult(code: String) {
        viewModelScope.launch {
            _searchState.value = Resource.Loading
            val result = searchItemRepository.findByCode(code)
            _searchState.value = result

            val historyItem = SearchHistoryItem(
                code = code,
                result = if (result is Resource.Success) result.data else null
            )
            _scanHistory.value = listOf(historyItem) + _scanHistory.value.take(19)
        }
    }

    fun clearSearch() {
        _searchState.value = null
    }
}
