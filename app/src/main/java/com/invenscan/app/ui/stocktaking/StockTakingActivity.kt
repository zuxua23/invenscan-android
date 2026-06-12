package com.invenscan.app.ui.stocktaking

import android.content.Intent
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.invenscan.app.R
import com.invenscan.app.base.BaseActivity
import com.invenscan.app.base.Resource
import com.invenscan.app.data.model.LocationModel
import com.invenscan.app.data.model.StockTakingModel
import com.invenscan.app.databinding.ActivityStockTakingBinding
import com.invenscan.app.ui.stocktaking.detail.StockTakingDetailActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class StockTakingActivity : BaseActivity<ActivityStockTakingBinding>() {

    private val viewModel: StockTakingViewModel by viewModels()
    private var locationList: List<LocationModel> = emptyList()

    override fun inflateBinding() = ActivityStockTakingBinding.inflate(layoutInflater)

    override fun initView() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.title_stock_taking)

        binding.btnRetry.setOnClickListener {
            val location = viewModel.selectedLocation.value
            if (location != null) viewModel.loadActiveSession(location.id)
            else viewModel.loadLocations()
        }

        binding.btnJoinSession.setOnClickListener {
            val session = (viewModel.sessionState.value as? Resource.Success)?.data
            val location = viewModel.selectedLocation.value
            if (session != null && location != null) navigateToDetail(session, location)
        }

        binding.btnCreateSession.setOnClickListener {
            val location = viewModel.selectedLocation.value ?: return@setOnClickListener
            viewModel.createSession(location.id, "")
        }
    }

    override fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { viewModel.locationListState.collect { handleLocationState(it) } }
                launch { viewModel.sessionState.collect { handleSessionState(it) } }
                launch { viewModel.createSessionState.collect { handleCreateSessionState(it) } }
            }
        }
    }

    private fun handleLocationState(state: Resource<List<LocationModel>>) {
        when (state) {
            is Resource.Loading -> {
                binding.progressBar.visibility = View.VISIBLE
                binding.layoutSession.visibility = View.GONE
                binding.layoutEmpty.visibility = View.GONE
                binding.layoutError.visibility = View.GONE
            }
            is Resource.Success -> {
                binding.progressBar.visibility = View.GONE
                locationList = state.data
                setupLocationSpinner(state.data)
            }
            is Resource.Error -> {
                binding.progressBar.visibility = View.GONE
                binding.layoutSession.visibility = View.GONE
                binding.layoutEmpty.visibility = View.GONE
                binding.layoutError.visibility = View.VISIBLE
                binding.tvError.text = state.message
            }
        }
    }

    private fun handleSessionState(state: Resource<StockTakingModel>?) {
        when (state) {
            null -> {
                binding.layoutSession.visibility = View.GONE
                binding.layoutEmpty.visibility = View.GONE
                binding.layoutError.visibility = View.GONE
                binding.progressBar.visibility = View.GONE
            }
            is Resource.Loading -> {
                binding.progressBar.visibility = View.VISIBLE
                binding.layoutSession.visibility = View.GONE
                binding.layoutEmpty.visibility = View.GONE
                binding.layoutError.visibility = View.GONE
            }
            is Resource.Success -> {
                binding.progressBar.visibility = View.GONE
                binding.layoutSession.visibility = View.VISIBLE
                binding.layoutEmpty.visibility = View.GONE
                binding.layoutError.visibility = View.GONE
                bindSessionData(state.data)
            }
            is Resource.Error -> {
                binding.progressBar.visibility = View.GONE
                binding.layoutSession.visibility = View.GONE
                if (state.code == 404 || state.message.contains("No active", ignoreCase = true)) {
                    binding.layoutEmpty.visibility = View.VISIBLE
                    binding.layoutError.visibility = View.GONE
                } else {
                    binding.layoutEmpty.visibility = View.GONE
                    binding.layoutError.visibility = View.VISIBLE
                    binding.tvError.text = state.message
                }
            }
        }
    }

    private fun handleCreateSessionState(state: Resource<StockTakingModel>?) {
        when (state) {
            null -> Unit
            is Resource.Loading -> {
                binding.progressBar.visibility = View.VISIBLE
                binding.btnCreateSession.isEnabled = false
            }
            is Resource.Success -> {
                binding.progressBar.visibility = View.GONE
                binding.btnCreateSession.isEnabled = true
                viewModel.resetCreateState()
                val location = viewModel.selectedLocation.value
                if (location != null) navigateToDetail(state.data, location)
            }
            is Resource.Error -> {
                binding.progressBar.visibility = View.GONE
                binding.btnCreateSession.isEnabled = true
                viewModel.resetCreateState()
                showSnackbar(state.message)
            }
        }
    }

    private fun setupLocationSpinner(locations: List<LocationModel>) {
        val labels = listOf(getString(R.string.label_location_hint)) +
                locations.map { "${it.locationCode} — ${it.locationName}" }

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, labels)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerLocation.adapter = adapter

        binding.spinnerLocation.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                if (position == 0) {
                    binding.progressBar.visibility = View.GONE
                    binding.layoutSession.visibility = View.GONE
                    binding.layoutEmpty.visibility = View.GONE
                    binding.layoutError.visibility = View.GONE
                    return
                }
                val location = locations[position - 1]
                viewModel.onLocationSelected(location)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun bindSessionData(session: StockTakingModel) {
        binding.tvSessionCode.text = session.sessionCode
        binding.tvSessionDate.text = session.createdAt?.take(10) ?: "-"
        binding.tvSessionRemark.text = session.remark?.ifBlank { "-" } ?: "-"
    }

    private fun navigateToDetail(session: StockTakingModel, location: LocationModel) {
        val intent = Intent(this, StockTakingDetailActivity::class.java).apply {
            putExtra(StockTakingDetailActivity.EXTRA_STT_ID, session.id)
            putExtra(StockTakingDetailActivity.EXTRA_SESSION_CODE, session.sessionCode)
            putExtra(StockTakingDetailActivity.EXTRA_LOCATION_ID, location.id)
            putExtra(StockTakingDetailActivity.EXTRA_LOCATION_NAME, location.locationName)
        }
        startActivity(intent)
    }

    private fun showSnackbar(message: String) {
        com.google.android.material.snackbar.Snackbar.make(binding.root, message, com.google.android.material.snackbar.Snackbar.LENGTH_LONG).show()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressedDispatcher.onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
