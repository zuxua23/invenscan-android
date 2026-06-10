package com.invenscan.app.ui.stocktaking

import androidx.lifecycle.viewModelScope
import com.invenscan.app.base.BaseViewModel
import com.invenscan.app.base.Resource
import com.invenscan.app.data.model.StockTakingModel
import com.invenscan.app.data.repository.StockTakingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StockTakingViewModel @Inject constructor(
    private val stockTakingRepository: StockTakingRepository
) : BaseViewModel() {

    private val _sessionState = MutableStateFlow<Resource<StockTakingModel>>(Resource.Loading)
    val sessionState: StateFlow<Resource<StockTakingModel>> = _sessionState

    init {
        loadActiveSession()
    }

    fun loadActiveSession() {
        viewModelScope.launch {
            _sessionState.value = Resource.Loading
            _sessionState.value = stockTakingRepository.getActiveSession()
        }
    }
}
