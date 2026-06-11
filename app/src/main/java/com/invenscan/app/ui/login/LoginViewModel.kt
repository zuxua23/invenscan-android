package com.invenscan.app.ui.login

import androidx.lifecycle.viewModelScope
import com.invenscan.app.base.BaseViewModel
import com.invenscan.app.base.Resource
import com.invenscan.app.data.model.AuthResponse
import com.invenscan.app.data.repository.AuthRepository
import com.invenscan.app.util.PrefManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val prefManager: PrefManager
) : BaseViewModel() {

    private val _loginState = MutableStateFlow<Resource<AuthResponse>?>(null)
    val loginState: StateFlow<Resource<AuthResponse>?> = _loginState

    fun login(serverUrl: String, userId: String, password: String) {
        val trimmedUrl = serverUrl.trim()
        val trimmedUserId = userId.trim()

        prefManager.serverUrl = trimmedUrl

        viewModelScope.launch {
            _loginState.value = Resource.Loading
            _loginState.value = authRepository.login(trimmedUserId, password)
        }
    }

    fun resetState() {
        _loginState.value = null
    }
}
