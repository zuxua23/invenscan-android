package com.invenscan.app.ui.stocktaking.detail

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
import com.invenscan.app.R
import com.invenscan.app.base.BaseActivity
import com.invenscan.app.base.Resource
import com.invenscan.app.databinding.ActivityStockTakingDetailBinding
import com.invenscan.app.scanner.ScannerContract
import com.invenscan.app.scanner.ScannerManager
import com.invenscan.app.ui.camera.CameraActivity
import com.invenscan.app.ui.stocktaking.detail.adapter.ScanResultAdapter
import com.invenscan.app.util.CustomDialog
import com.invenscan.app.util.WorkManagerUtil
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class StockTakingDetailActivity : BaseActivity<ActivityStockTakingDetailBinding>(),
    ScannerContract.ScanListener {

    private val viewModel: StockTakingDetailViewModel by viewModels()
    private val scanResultAdapter = ScanResultAdapter()
    private var isScanning = false

    @Inject
    lateinit var scannerManager: ScannerManager

    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val code = result.data?.getStringExtra(CameraActivity.EXTRA_SCANNED_CODE)
            if (!code.isNullOrBlank()) viewModel.onScanResult(code)
        }
    }

    override fun inflateBinding() = ActivityStockTakingDetailBinding.inflate(layoutInflater)

    override fun initView() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val sttId = intent.getLongExtra(EXTRA_STT_ID, 0L)
        val sessionCode = intent.getStringExtra(EXTRA_SESSION_CODE) ?: ""
        supportActionBar?.title = sessionCode

        binding.rvScanResults.adapter = scanResultAdapter

        binding.btnSubmitResult.setOnClickListener {
            CustomDialog.show(
                context = this,
                title = getString(R.string.dialog_confirm),
                message = getString(R.string.confirm_submit_taking),
                onPositive = { viewModel.submitResults() }
            )
        }

        binding.btnRetry.setOnClickListener { viewModel.loadSessionTags(sttId) }

        binding.fabCamera.setOnClickListener {
            cameraLauncher.launch(Intent(this, CameraActivity::class.java))
        }

        scannerManager.getScanner().initialize(this, this)
        viewModel.loadSessionTags(sttId)
    }

    override fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.sessionTagsState.collect { state ->
                        when (state) {
                            is Resource.Loading -> {
                                binding.progressBar.visibility = View.VISIBLE
                                binding.layoutContent.visibility = View.GONE
                                binding.layoutError.visibility = View.GONE
                            }
                            is Resource.Success -> {
                                binding.progressBar.visibility = View.GONE
                                binding.layoutContent.visibility = View.VISIBLE
                                binding.layoutError.visibility = View.GONE
                                toggleScanning(true)
                            }
                            is Resource.Error -> {
                                binding.progressBar.visibility = View.GONE
                                binding.layoutContent.visibility = View.GONE
                                binding.layoutError.visibility = View.VISIBLE
                                binding.tvError.text = state.message
                            }
                        }
                    }
                }
                launch {
                    viewModel.scanResults.collect { results ->
                        scanResultAdapter.submitList(results.toList())
                        binding.tvFoundCount.text = viewModel.foundCount.toString()
                        binding.tvMissingCount.text = viewModel.missingCount.toString()
                        binding.tvUnknownCount.text = viewModel.unknownCount.toString()
                        setViewVisibility(binding.layoutEmpty, results.isEmpty())
                        setViewVisibility(binding.rvScanResults, results.isNotEmpty())
                    }
                }
                launch {
                    viewModel.submitState.collect { state ->
                        when (state) {
                            is Resource.Loading -> binding.progressBar.visibility = View.VISIBLE
                            is Resource.Success -> {
                                binding.progressBar.visibility = View.GONE
                                showMessage(getString(R.string.success_submit_offline))
                                WorkManagerUtil.triggerImmediateSync(this@StockTakingDetailActivity)
                                viewModel.resetSubmitState()
                                finish()
                            }
                            is Resource.Error -> {
                                binding.progressBar.visibility = View.GONE
                                showError(state.message)
                                viewModel.resetSubmitState()
                            }
                            null -> binding.progressBar.visibility = View.GONE
                        }
                    }
                }
            }
        }
    }

    private fun toggleScanning(start: Boolean) {
        if (start) {
            scannerManager.getScanner().startScan()
            isScanning = true
            binding.tvScanHint.text = getString(R.string.hint_ready_to_scan)
        } else {
            scannerManager.getScanner().stopScan()
            isScanning = false
            binding.tvScanHint.text = getString(R.string.status_scan_stopped)
        }
    }

    override fun onScanResult(code: String, type: ScannerContract.ScanType) {
        viewModel.onScanResult(code)
    }

    override fun onScanError(message: String) {
        showError(message)
    }

    override fun onScannerDisconnected() {
        showError("Scanner disconnected")
        isScanning = false
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_scan, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> { onBackPressedDispatcher.onBackPressed(); true }
            R.id.action_simulate_scan -> {
                val scanner = scannerManager.getScanner()
                if (scanner is com.invenscan.app.scanner.MockScanner && isScanning) {
                    val results = viewModel.scanResults.value
                    val nextMissing = results.firstOrNull { it.status == ScanResultStatus.MISSING }
                    val code = nextMissing?.tagId ?: "UNKNOWN-${System.currentTimeMillis() % 9999}"
                    scanner.simulateScan(code, ScannerContract.ScanType.RFID)
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

    companion object {
        const val EXTRA_STT_ID = "extra_stt_id"
        const val EXTRA_SESSION_CODE = "extra_session_code"
    }
}
