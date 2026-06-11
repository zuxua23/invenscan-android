package com.invenscan.app.ui.stocktaking.detail

import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.invenscan.app.R
import com.invenscan.app.base.BaseActivity
import com.invenscan.app.base.Resource
import com.invenscan.app.databinding.ActivityStockTakingDetailBinding
import com.invenscan.app.scanner.ScannerContract
import com.invenscan.app.scanner.ScannerManager
import com.invenscan.app.ui.stocktaking.detail.adapter.ScanResultAdapter
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

    override fun inflateBinding() = ActivityStockTakingDetailBinding.inflate(layoutInflater)

    override fun initView() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val sttId = intent.getLongExtra(EXTRA_STT_ID, 0L)
        val sessionCode = intent.getStringExtra(EXTRA_SESSION_CODE) ?: ""
        supportActionBar?.title = sessionCode

        binding.rvScanResults.adapter = scanResultAdapter

        binding.btnSubmitResult.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle(getString(R.string.dialog_confirm))
                .setMessage(getString(R.string.confirm_submit_taking))
                .setPositiveButton(getString(R.string.dialog_yes)) { _, _ ->
                    viewModel.submitResults()
                }
                .setNegativeButton(getString(R.string.dialog_no), null)
                .show()
        }

        binding.btnRetry.setOnClickListener { viewModel.loadSessionTags(sttId) }

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
        } else {
            scannerManager.getScanner().stopScan()
            isScanning = false
        }
    }

    override fun onScanResult(code: String, type: ScannerContract.ScanType) {
        viewModel.onScanResult(code)
    }

    override fun onScanError(message: String) {
        showError(message)
    }

    override fun onScannerDisconnected() {
        showError("Scanner terputus")
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
                    val nextMissing = results.firstOrNull {
                        it.status == ScanResultStatus.MISSING
                    }
                    val code = nextMissing?.tagId ?: "UNKNOWN-${System.currentTimeMillis() % 9999}"
                    scanner.simulateScan(code, ScannerContract.ScanType.RFID)
                } else {
                    showMessage("Tunggu data dimuat")
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
