package com.invenscan.app.ui.stockprep

import androidx.lifecycle.viewModelScope
import com.invenscan.app.base.BaseViewModel
import com.invenscan.app.base.Resource
import com.invenscan.app.data.model.StockPrepModel
import com.invenscan.app.data.repository.StockPrepRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StockPrepViewModel @Inject constructor(
    private val stockPrepRepository: StockPrepRepository
) : BaseViewModel() {

    private val _prepListState = MutableStateFlow<Resource<List<StockPrepModel>>>(Resource.Loading)
    val prepListState: StateFlow<Resource<List<StockPrepModel>>> = _prepListState

    init {
        loadPrepList()
    }

    fun loadPrepList() {
        viewModelScope.launch {
            _prepListState.value = Resource.Loading
            _prepListState.value = stockPrepRepository.getPrepList()
        }
    }
}
