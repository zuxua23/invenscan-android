package com.invenscan.app.ui.stockprep.detail

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
import com.invenscan.app.databinding.ActivityStockPrepDetailBinding
import com.invenscan.app.scanner.ScannerContract
import com.invenscan.app.scanner.ScannerManager
import com.invenscan.app.ui.stockprep.detail.adapter.StockPrepDetailAdapter
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

    override fun inflateBinding() = ActivityStockPrepDetailBinding.inflate(layoutInflater)

    override fun initView() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val prepId = intent.getLongExtra(EXTRA_PREP_ID, 0L)
        val docNumber = intent.getStringExtra(EXTRA_DOC_NUMBER) ?: ""
        supportActionBar?.title = docNumber

        binding.rvPickItems.adapter = detailAdapter

        binding.btnSubmit.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle(getString(R.string.dialog_confirm))
                .setMessage(getString(R.string.confirm_submit_prep, viewModel.pickedItems))
                .setPositiveButton(getString(R.string.dialog_yes)) { _, _ -> viewModel.submit() }
                .setNegativeButton(getString(R.string.dialog_no), null)
                .show()
        }

        binding.btnRetry.setOnClickListener { viewModel.loadDetail(prepId) }

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
                    val unpickedItem = viewModel.pickItems.value.firstOrNull {
                        it.pickedQty < it.detail.requestedQty
                    }
                    val code = unpickedItem?.detail?.itemCode ?: "UNKNOWN-${System.currentTimeMillis() % 9999}"
                    scanner.simulateScan(code, ScannerContract.ScanType.BARCODE)
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
        const val EXTRA_PREP_ID = "extra_prep_id"
        const val EXTRA_DOC_NUMBER = "extra_doc_number"
    }
}
