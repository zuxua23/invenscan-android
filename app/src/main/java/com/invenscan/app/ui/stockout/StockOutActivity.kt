package com.invenscan.app.ui.stockout

import android.app.Activity
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.invenscan.app.R
import com.invenscan.app.base.BaseActivity
import com.invenscan.app.base.Resource
import com.invenscan.app.data.model.LocationModel
import com.invenscan.app.databinding.ActivityStockOutBinding
import com.invenscan.app.scanner.ScannerContract
import com.invenscan.app.scanner.ScannerManager
import com.invenscan.app.ui.camera.CameraActivity
import com.invenscan.app.ui.stockout.adapter.StockOutAdapter
import com.invenscan.app.util.CustomDialog
import com.invenscan.app.util.WorkManagerUtil
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class StockOutActivity : BaseActivity<ActivityStockOutBinding>(), ScannerContract.ScanListener {

    private val viewModel: StockOutViewModel by viewModels()
    private val stockOutAdapter = StockOutAdapter()
    private var locations: List<LocationModel> = emptyList()

    private val powerValues = listOf(5, 10, 15, 20, 25, 27, 30)

    @Inject
    lateinit var scannerManager: ScannerManager

    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val code = result.data?.getStringExtra(CameraActivity.EXTRA_SCANNED_CODE)
            if (!code.isNullOrBlank()) {
                viewModel.onScanResult(code, ScannerContract.ScanType.BARCODE)
            }
        }
    }

    override fun inflateBinding() = ActivityStockOutBinding.inflate(layoutInflater)

    override fun initView() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.title_stock_out)

        binding.rvScannedItems.adapter = stockOutAdapter
        setupSwipeToDelete()
        setupPowerSpinner()
        setupScanModeToggle()

        binding.btnStartScan.setOnClickListener {
            if (viewModel.isScanning.value) stopScanning() else startScanning()
        }

        binding.btnSubmit.setOnClickListener {
            if (viewModel.scannedItems.value.isEmpty()) {
                showError(getString(R.string.error_empty_scan))
                return@setOnClickListener
            }
            confirmAndSubmit()
        }

        binding.btnRetry.setOnClickListener { viewModel.loadLocations() }

        binding.fabCamera.setOnClickListener {
            cameraLauncher.launch(Intent(this, CameraActivity::class.java))
        }

        scannerManager.getScanner().initialize(this, this)
    }

    private fun setupScanModeToggle() {
        binding.toggleScanMode.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                val isRfid = checkedId == R.id.btnModeRfid
                viewModel.scanMode = if (isRfid) ScannerContract.ScanType.RFID
                else ScannerContract.ScanType.BARCODE
                binding.spinnerPower.visibility = if (isRfid) View.VISIBLE else View.GONE
            }
        }
        binding.toggleScanMode.check(R.id.btnModeRfid)
    }

    private fun setupPowerSpinner() {
        val powerLabels = powerValues.map { "$it dBm" }
        binding.spinnerPower.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, powerLabels).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        val idx = powerValues.indexOf(prefManager.rfidPower).coerceAtLeast(0)
        binding.spinnerPower.setSelection(idx)

        binding.spinnerPower.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                prefManager.rfidPower = powerValues[position]
            }
            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
        }
    }

    override fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { viewModel.locationsState.collect { handleLocationsState(it) } }
                launch { viewModel.scannedItems.collect { handleScannedItems(it) } }
                launch { viewModel.isScanning.collect { handleScanningState(it) } }
                launch { viewModel.submitState.collect { handleSubmitState(it) } }
            }
        }
    }

    private fun handleLocationsState(state: Resource<List<LocationModel>>) {
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
                locations = state.data
                setupLocationSpinner(state.data)
                startScanning()
            }
            is Resource.Error -> {
                binding.progressBar.visibility = View.GONE
                binding.layoutContent.visibility = View.GONE
                binding.layoutError.visibility = View.VISIBLE
                binding.tvError.text = state.message
            }
        }
    }

    private fun handleScannedItems(items: List<ScannedOutItem>) {
        stockOutAdapter.submitList(items.toList())
        binding.tvItemCount.text = getString(R.string.label_item_count, items.size)
        setViewVisibility(binding.layoutEmpty, items.isEmpty())
        setViewVisibility(binding.rvScannedItems, items.isNotEmpty())
        binding.btnSubmit.isEnabled = items.isNotEmpty()
    }

    private fun handleScanningState(isScanning: Boolean) {
        binding.btnStartScan.text = if (isScanning) getString(R.string.action_stop_scan)
        else getString(R.string.action_start_scan)
        binding.tvScanHint.text = if (isScanning) getString(R.string.hint_ready_to_scan)
        else getString(R.string.status_scan_stopped)
    }

    private fun handleSubmitState(state: Resource<Unit>?) {
        when (state) {
            is Resource.Loading -> binding.progressBar.visibility = View.VISIBLE
            is Resource.Success -> {
                binding.progressBar.visibility = View.GONE
                showMessage(getString(R.string.success_submit_stock_out_offline))
                WorkManagerUtil.triggerImmediateSync(this)
                viewModel.resetSubmitState()
            }
            is Resource.Error -> {
                binding.progressBar.visibility = View.GONE
                showError(state.message)
                viewModel.resetSubmitState()
            }
            null -> binding.progressBar.visibility = View.GONE
        }
    }

    private fun setupLocationSpinner(locs: List<LocationModel>) {
        val names = locs.map { it.locationName }
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, names).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        binding.spinnerLocation.adapter = adapter
        binding.spinnerLocation.setSelection(0)
        viewModel.selectedLocation = locs.firstOrNull()

        binding.spinnerLocation.onItemSelectedListener =
            object : android.widget.AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                    viewModel.selectedLocation = locs.getOrNull(position)
                }
                override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
            }
    }

    private fun setupSwipeToDelete() {
        val swipeBackground = ColorDrawable(Color.parseColor("#FCEBEB"))
        val callback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            override fun onMove(rv: RecyclerView, vh: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder) = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                viewModel.removeItem(viewHolder.adapterPosition)
            }

            override fun onChildDraw(c: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean) {
                val itemView = viewHolder.itemView
                swipeBackground.setBounds(itemView.right + dX.toInt(), itemView.top, itemView.right, itemView.bottom)
                swipeBackground.draw(c)
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
            }
        }
        ItemTouchHelper(callback).attachToRecyclerView(binding.rvScannedItems)
    }

    private fun startScanning() {
        scannerManager.getScanner().startScan()
        viewModel.startScan()
    }

    private fun stopScanning() {
        scannerManager.getScanner().stopScan()
        viewModel.stopScan()
    }

    private fun confirmAndSubmit() {
        val locationId = viewModel.selectedLocation?.id ?: run {
            showError(getString(R.string.error_select_location))
            return
        }
        CustomDialog.show(
            context = this,
            title = getString(R.string.dialog_confirm),
            message = getString(R.string.confirm_submit_stock_out, viewModel.scannedItems.value.size),
            onPositive = { viewModel.submitStockOut(locationId) }
        )
    }

    override fun onScanResult(code: String, type: ScannerContract.ScanType) {
        viewModel.onScanResult(code, type)
    }

    override fun onScanError(message: String) {
        showError(message)
    }

    override fun onScannerDisconnected() {
        showError("Scanner disconnected")
        viewModel.stopScan()
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
                    val mockCode = "MOCK-OUT-${System.currentTimeMillis() % 10000}"
                    scanner.simulateScan(mockCode, viewModel.scanMode)
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onBackPressed() {
        if (viewModel.scannedItems.value.isNotEmpty()) {
            CustomDialog.show(
                context = this,
                title = getString(R.string.dialog_confirm),
                message = getString(R.string.confirm_leave_screen),
                positiveText = getString(R.string.dialog_leave),
                negativeText = getString(R.string.dialog_stay),
                onPositive = { super.onBackPressed() }
            )
        } else {
            super.onBackPressed()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        scannerManager.getScanner().release()
    }
}
