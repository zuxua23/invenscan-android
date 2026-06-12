package com.invenscan.app.ui.tagreg

import android.app.Activity
import android.content.Intent
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewModelScope
import com.invenscan.app.R
import com.invenscan.app.base.BaseActivity
import com.invenscan.app.base.BaseViewModel
import com.invenscan.app.base.Resource
import com.invenscan.app.data.model.TagModel
import com.invenscan.app.data.remote.ApiService
import com.invenscan.app.databinding.ActivityTagRegistrationBinding
import com.invenscan.app.scanner.ScannerContract
import com.invenscan.app.scanner.ScannerManager
import com.invenscan.app.ui.camera.CameraActivity
import com.invenscan.app.util.CustomDialog
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TagRegistrationViewModel @Inject constructor(
    private val apiService: ApiService
) : BaseViewModel() {

    private val _tagState = MutableStateFlow<Resource<TagModel>?>(null)
    val tagState: StateFlow<Resource<TagModel>?> = _tagState.asStateFlow()

    private val _reregisterState = MutableStateFlow<Resource<Unit>?>(null)
    val reregisterState: StateFlow<Resource<Unit>?> = _reregisterState.asStateFlow()

    fun lookupTag(code: String) {
        viewModelScope.launch {
            _tagState.value = Resource.Loading
            runCatching {
                apiService.getTagById(code)
            }.onSuccess { response ->
                when {
                    response.isSuccessful -> {
                        val tag = response.body()?.data
                        if (tag != null) _tagState.value = Resource.Success(tag)
                        else _tagState.value = Resource.Error("Tag not found", 404)
                    }
                    response.code() == 404 -> _tagState.value = Resource.Error("Tag not found in system", 404)
                    else -> _tagState.value = Resource.Error("Lookup failed (${response.code()})", response.code())
                }
            }.onFailure { e ->
                _tagState.value = Resource.Error(e.message ?: "Network error", 0)
            }
        }
    }

    fun reregisterTag(tagId: String) {
        viewModelScope.launch {
            _reregisterState.value = Resource.Loading
            runCatching {
                apiService.reregisterTag(mapOf("tagId" to tagId))
            }.onSuccess { response ->
                if (response.isSuccessful) _reregisterState.value = Resource.Success(Unit)
                else _reregisterState.value = Resource.Error("Re-register failed (${response.code()})", response.code())
            }.onFailure { e ->
                _reregisterState.value = Resource.Error(e.message ?: "Network error", 0)
            }
        }
    }

    fun resetTagState() { _tagState.value = null }
    fun resetReregisterState() { _reregisterState.value = null }
}

@AndroidEntryPoint
class TagRegistrationActivity : BaseActivity<ActivityTagRegistrationBinding>(),
    ScannerContract.ScanListener {

    private val viewModel: TagRegistrationViewModel by viewModels()
    private var lastScannedCode: String? = null

    @Inject
    lateinit var scannerManager: ScannerManager

    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val code = result.data?.getStringExtra(CameraActivity.EXTRA_SCANNED_CODE)
            if (!code.isNullOrBlank()) handleScan(code)
        }
    }

    override fun inflateBinding() = ActivityTagRegistrationBinding.inflate(layoutInflater)

    override fun initView() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.title_tag_registration)

        binding.fabCamera.setOnClickListener {
            cameraLauncher.launch(Intent(this, CameraActivity::class.java))
        }

        binding.btnReregister.setOnClickListener {
            val tag = (viewModel.tagState.value as? Resource.Success)?.data ?: return@setOnClickListener
            CustomDialog.show(
                context = this,
                title = getString(R.string.dialog_reregister_title),
                message = getString(R.string.dialog_reregister_message, tag.itemName ?: tag.tagId),
                positiveText = getString(R.string.action_reregister),
                negativeText = getString(R.string.dialog_cancel),
                onPositive = { viewModel.reregisterTag(tag.tagId) }
            )
        }

        scannerManager.getScanner().initialize(this, this)
        scannerManager.getScanner().startScan()
    }

    override fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.tagState.collect { state ->
                        when (state) {
                            is Resource.Loading -> {
                                binding.progressBar.visibility = View.VISIBLE
                                binding.layoutResult.visibility = View.GONE
                                binding.tvError.visibility = View.GONE
                                binding.btnReregister.visibility = View.GONE
                            }
                            is Resource.Success -> {
                                binding.progressBar.visibility = View.GONE
                                binding.tvError.visibility = View.GONE
                                showTagResult(state.data)
                            }
                            is Resource.Error -> {
                                binding.progressBar.visibility = View.GONE
                                binding.layoutResult.visibility = View.GONE
                                binding.btnReregister.visibility = View.GONE
                                binding.tvError.visibility = View.VISIBLE
                                binding.tvError.text = if (state.code == 404)
                                    getString(R.string.error_tag_not_found)
                                else state.message
                            }
                            null -> {
                                binding.progressBar.visibility = View.GONE
                                binding.layoutResult.visibility = View.GONE
                                binding.tvError.visibility = View.GONE
                                binding.btnReregister.visibility = View.GONE
                            }
                        }
                    }
                }
                launch {
                    viewModel.reregisterState.collect { state ->
                        when (state) {
                            is Resource.Loading -> binding.progressBar.visibility = View.VISIBLE
                            is Resource.Success -> {
                                binding.progressBar.visibility = View.GONE
                                appLogger.log("REREGISTER", "TAG_REGISTRATION", "Tag ${lastScannedCode} re-registered")
                                showMessage(getString(R.string.success_tag_reregistered))
                                viewModel.resetTagState()
                                viewModel.resetReregisterState()
                                binding.layoutResult.visibility = View.GONE
                                binding.btnReregister.visibility = View.GONE
                            }
                            is Resource.Error -> {
                                binding.progressBar.visibility = View.GONE
                                showError(state.message)
                                viewModel.resetReregisterState()
                            }
                            null -> binding.progressBar.visibility = View.GONE
                        }
                    }
                }
            }
        }
    }

    private fun showTagResult(tag: TagModel) {
        binding.layoutResult.visibility = View.VISIBLE
        binding.tvTagId.text = tag.tagId
        binding.tvTagEpc.text = tag.epcTag
        binding.tvTagItem.text = if (!tag.itemName.isNullOrBlank()) "${tag.itemCode} — ${tag.itemName}" else "-"

        when (tag.status.uppercase()) {
            "OUT" -> {
                binding.tvTagStatus.text = tag.status
                binding.tvTagStatus.setTextColor(getColor(R.color.danger))
                binding.btnReregister.visibility = View.VISIBLE
                showWarning(getString(R.string.dialog_reregister_title))
            }
            "IN_STOCK", "AVAILABLE" -> {
                binding.tvTagStatus.text = tag.status
                binding.tvTagStatus.setTextColor(getColor(R.color.teal_primary))
                binding.btnReregister.visibility = View.GONE
                showMessage(getString(R.string.info_tag_already_available))
            }
            else -> {
                binding.tvTagStatus.text = tag.status
                binding.tvTagStatus.setTextColor(getColor(R.color.text_secondary))
                binding.btnReregister.visibility = View.GONE
            }
        }
    }

    private fun handleScan(code: String) {
        lastScannedCode = code
        viewModel.lookupTag(code)
    }

    override fun onScanResult(code: String, type: ScannerContract.ScanType) {
        handleScan(code)
    }

    override fun onScanError(message: String) {
        showError(message)
    }

    override fun onScannerDisconnected() {
        showError("Scanner disconnected")
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_scan, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> { onBackPressedDispatcher.onBackPressed(); true }
            R.id.action_simulate_scan -> {
                val scanner = scannerManager.getScanner()
                if (scanner is com.invenscan.app.scanner.MockScanner) {
                    scanner.simulateScan("TAG-DEMO-OUT-001", ScannerContract.ScanType.RFID)
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        scannerManager.getScanner().release()
    }
}
