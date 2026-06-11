package com.invenscan.app.ui.search

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
import com.invenscan.app.data.model.SearchItemModel
import com.invenscan.app.databinding.ActivitySearchItemBinding
import com.invenscan.app.scanner.ScannerContract
import com.invenscan.app.scanner.ScannerManager
import com.invenscan.app.ui.camera.CameraActivity
import com.invenscan.app.ui.search.adapter.SearchHistoryAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SearchItemActivity : BaseActivity<ActivitySearchItemBinding>(), ScannerContract.ScanListener {

    private val viewModel: SearchItemViewModel by viewModels()
    private val historyAdapter = SearchHistoryAdapter()

    @Inject
    lateinit var scannerManager: ScannerManager

    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val code = result.data?.getStringExtra(CameraActivity.EXTRA_SCANNED_CODE)
            if (!code.isNullOrBlank()) viewModel.onScanResult(code)
        }
    }

    override fun inflateBinding() = ActivitySearchItemBinding.inflate(layoutInflater)

    override fun initView() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.title_search_item)

        binding.rvHistory.adapter = historyAdapter

        binding.fabCamera.setOnClickListener {
            cameraLauncher.launch(Intent(this, CameraActivity::class.java))
        }

        scannerManager.getScanner().initialize(this, this)
        scannerManager.getScanner().startScan()
    }

    override fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.searchState.collect { state ->
                        when (state) {
                            is Resource.Loading -> {
                                binding.progressBar.visibility = View.VISIBLE
                                binding.layoutResult.visibility = View.GONE
                                binding.layoutEmpty.visibility = View.GONE
                                binding.tvError.visibility = View.GONE
                            }
                            is Resource.Success -> {
                                binding.progressBar.visibility = View.GONE
                                binding.layoutEmpty.visibility = View.GONE
                                binding.tvError.visibility = View.GONE
                                binding.layoutResult.visibility = View.VISIBLE
                                bindItemResult(state.data)
                            }
                            is Resource.Error -> {
                                binding.progressBar.visibility = View.GONE
                                binding.layoutResult.visibility = View.GONE
                                binding.layoutEmpty.visibility = View.GONE
                                binding.tvError.visibility = View.VISIBLE
                                binding.tvError.text = state.message
                            }
                            null -> {
                                binding.progressBar.visibility = View.GONE
                                binding.layoutResult.visibility = View.GONE
                                binding.tvError.visibility = View.GONE
                                binding.layoutEmpty.visibility = View.VISIBLE
                            }
                        }
                    }
                }
                launch {
                    viewModel.scanHistory.collect { history ->
                        historyAdapter.submitList(history)
                        setViewVisibility(binding.layoutHistorySection, history.isNotEmpty())
                    }
                }
            }
        }
    }

    private fun bindItemResult(item: SearchItemModel) {
        binding.tvItemCode.text = item.itemCode
        binding.tvItemName.text = item.itemName
        binding.tvUnit.text = item.unit ?: "-"
        binding.tvLocation.text = item.locationName ?: "-"
        binding.tvItemStatus.text = item.status ?: "-"
    }

    override fun onScanResult(code: String, type: ScannerContract.ScanType) {
        viewModel.onScanResult(code)
    }

    override fun onScanError(message: String) {
        showError(message)
    }

    override fun onScannerDisconnected() {
        showError("Scanner disconnected")
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
                if (scanner is com.invenscan.app.scanner.MockScanner) {
                    scanner.simulateScan("ITEM-DEMO-001", ScannerContract.ScanType.BARCODE)
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
