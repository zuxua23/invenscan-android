package com.invenscan.app.ui.stocktaking

import androidx.lifecycle.viewModelScope
import com.invenscan.app.base.BaseViewModel
import com.invenscan.app.base.Resource
import com.invenscan.app.data.model.LocationModel
import com.invenscan.app.data.model.StockTakingModel
import com.invenscan.app.data.repository.LocationRepository
import com.invenscan.app.data.repository.StockTakingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StockTakingViewModel @Inject constructor(
    private val stockTakingRepository: StockTakingRepository,
    private val locationRepository: LocationRepository
) : BaseViewModel() {

    private val _locationListState = MutableStateFlow<Resource<List<LocationModel>>>(Resource.Loading)
    val locationListState: StateFlow<Resource<List<LocationModel>>> = _locationListState

    private val _sessionState = MutableStateFlow<Resource<StockTakingModel>?>(null)
    val sessionState: StateFlow<Resource<StockTakingModel>?> = _sessionState

    private val _createSessionState = MutableStateFlow<Resource<StockTakingModel>?>(null)
    val createSessionState: StateFlow<Resource<StockTakingModel>?> = _createSessionState

    private val _selectedLocation = MutableStateFlow<LocationModel?>(null)
    val selectedLocation: StateFlow<LocationModel?> = _selectedLocation

    init {
        loadLocations()
    }

    fun loadLocations() {
        viewModelScope.launch {
            _locationListState.value = Resource.Loading
            _locationListState.value = locationRepository.getLocations()
        }
    }

    fun onLocationSelected(location: LocationModel) {
        _selectedLocation.value = location
        _sessionState.value = null
        loadActiveSession(location.id)
    }

    fun loadActiveSession(locationId: Long) {
        viewModelScope.launch {
            _sessionState.value = Resource.Loading
            _sessionState.value = stockTakingRepository.getActiveSession(locationId)
        }
    }

    fun createSession(locationId: Long, remark: String) {
        viewModelScope.launch {
            _createSessionState.value = Resource.Loading
            _createSessionState.value = stockTakingRepository.createSession(locationId, remark)
        }
    }

    fun resetCreateState() {
        _createSessionState.value = null
    }
}
