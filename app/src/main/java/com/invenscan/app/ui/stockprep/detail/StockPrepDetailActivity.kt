package com.invenscan.app.ui.stockprep.detail

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
import com.invenscan.app.databinding.ActivityStockPrepDetailBinding
import com.invenscan.app.scanner.ScannerContract
import com.invenscan.app.scanner.ScannerManager
import com.invenscan.app.ui.camera.CameraActivity
import com.invenscan.app.ui.stockprep.detail.adapter.StockPrepDetailAdapter
import com.invenscan.app.util.CustomDialog
import com.invenscan.app.util.WorkManagerUtil
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class StockPrepDetailActivity : BaseActivity<ActivityStockPrepDetailBinding>(),
    ScannerContract.ScanListener {

    private val viewModel: StockPrepDetailViewModel by viewModels()
    private val detailAdapter = StockPrepDetailAdapter()
    private var isScanning = false

    @Inject
    lateinit var scannerManager: ScannerManager

    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val code = result.data?.getStringExtra(CameraActivity.EXTRA_SCANNED_CODE)
            if (!code.isNullOrBlank()) viewModel.onScanResult(code, ScannerContract.ScanType.BARCODE)
        }
    }

    override fun inflateBinding() = ActivityStockPrepDetailBinding.inflate(layoutInflater)

    override fun initView() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val prepId = intent.getLongExtra(EXTRA_PREP_ID, 0L)
        val docNumber = intent.getStringExtra(EXTRA_DOC_NUMBER) ?: ""
        supportActionBar?.title = docNumber

        binding.rvPickItems.adapter = detailAdapter

        binding.btnSubmit.setOnClickListener {
            CustomDialog.show(
                context = this,
                title = getString(R.string.dialog_confirm),
                message = getString(R.string.confirm_submit_prep, viewModel.pickedItems),
                onPositive = { viewModel.submit() }
            )
        }

        binding.btnRetry.setOnClickListener { viewModel.loadDetail(prepId) }

        binding.fabCamera.setOnClickListener {
            cameraLauncher.launch(Intent(this, CameraActivity::class.java))
        }

        scannerManager.getScanner().initialize(this, this)
        viewModel.loadDetail(prepId)
    }

    override fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.detailState.collect { state ->
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
                                isScanning = true
                                scannerManager.getScanner().startScan()
                                binding.tvScanHint.text = getString(R.string.hint_ready_to_scan)
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
                    viewModel.pickItems.collect { items ->
                        detailAdapter.submitList(items.toList())
                        val progress = getString(R.string.label_progress, viewModel.pickedItems, viewModel.totalItems)
                        binding.tvProgress.text = progress
                        binding.progressIndicator.max = viewModel.totalItems
                        binding.progressIndicator.progress = viewModel.pickedItems
                        setViewVisibility(binding.layoutEmpty, items.isEmpty())
                        setViewVisibility(binding.rvPickItems, items.isNotEmpty())
                    }
                }
                launch {
                    viewModel.submitState.collect { state ->
                        when (state) {
                            is Resource.Loading -> binding.progressBar.visibility = View.VISIBLE
                            is Resource.Success -> {
                                binding.progressBar.visibility = View.GONE
                                val docNumber = intent.getStringExtra(EXTRA_DOC_NUMBER) ?: ""
                                appLogger.logStockPrepSubmit(docNumber)
                                showMessage(getString(R.string.success_submit_offline))
                                WorkManagerUtil.triggerImmediateSync(this@StockPrepDetailActivity)
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

    override fun onScanResult(code: String, type: ScannerContract.ScanType) {
        viewModel.onScanResult(code, type)
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
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> { onBackPressedDispatcher.onBackPressed(); true }
            R.id.action_simulate_scan -> {
                val scanner = scannerManager.getScanner()
                if (scanner is com.invenscan.app.scanner.MockScanner && isScanning) {
                    val unpickedItem = viewModel.pickItems.value.firstOrNull { it.pickedQty < it.detail.requestedQty }
                    val code = unpickedItem?.detail?.itemCode ?: "UNKNOWN-${System.currentTimeMillis() % 9999}"
                    scanner.simulateScan(code, ScannerContract.ScanType.BARCODE)
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
        const val EXTRA_PREP_ID = "extra_prep_id"
        const val EXTRA_DOC_NUMBER = "extra_doc_number"
    }
}
