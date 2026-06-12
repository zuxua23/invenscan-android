package com.invenscan.app.ui.home

import com.invenscan.app.base.BaseViewModel
import com.invenscan.app.util.PrefManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val prefManager: PrefManager
) : BaseViewModel() {

    val fullName: String get() = prefManager.fullName ?: "User"
    val userId: String get() = prefManager.userId ?: ""

    fun logout() {
        prefManager.clearSession()
    }
}
