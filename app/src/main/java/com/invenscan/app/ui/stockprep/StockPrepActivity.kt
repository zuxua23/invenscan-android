package com.invenscan.app.ui.stockprep

import android.content.Intent
import android.view.MenuItem
import android.view.View
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.invenscan.app.R
import com.invenscan.app.base.BaseActivity
import com.invenscan.app.base.Resource
import com.invenscan.app.data.model.StockPrepModel
import com.invenscan.app.databinding.ActivityStockPrepBinding
import com.invenscan.app.ui.stockprep.adapter.StockPrepAdapter
import com.invenscan.app.ui.stockprep.detail.StockPrepDetailActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class StockPrepActivity : BaseActivity<ActivityStockPrepBinding>() {

    private val viewModel: StockPrepViewModel by viewModels()
    private val prepAdapter = StockPrepAdapter { item -> navigateToDetail(item) }

    override fun inflateBinding() = ActivityStockPrepBinding.inflate(layoutInflater)

    override fun initView() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.title_stock_prep)

        binding.rvPrepList.adapter = prepAdapter
        binding.btnRetry.setOnClickListener { viewModel.loadPrepList() }
    }

    override fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.prepListState.collect { state ->
                    when (state) {
                        is Resource.Loading -> {
                            binding.progressBar.visibility = View.VISIBLE
                            binding.rvPrepList.visibility = View.GONE
                            binding.layoutEmpty.visibility = View.GONE
                            binding.layoutError.visibility = View.GONE
                        }
                        is Resource.Success -> {
                            binding.progressBar.visibility = View.GONE
                            binding.layoutError.visibility = View.GONE
                            if (state.data.isEmpty()) {
                                binding.rvPrepList.visibility = View.GONE
                                binding.layoutEmpty.visibility = View.VISIBLE
                            } else {
                                binding.rvPrepList.visibility = View.VISIBLE
                                binding.layoutEmpty.visibility = View.GONE
                                prepAdapter.submitList(state.data)
                            }
                        }
                        is Resource.Error -> {
                            binding.progressBar.visibility = View.GONE
                            binding.rvPrepList.visibility = View.GONE
                            binding.layoutEmpty.visibility = View.GONE
                            binding.layoutError.visibility = View.VISIBLE
                            binding.tvError.text = state.message
                        }
                    }
                }
            }
        }
    }

    private fun navigateToDetail(item: StockPrepModel) {
        val intent = Intent(this, StockPrepDetailActivity::class.java).apply {
            putExtra(StockPrepDetailActivity.EXTRA_PREP_ID, item.id)
            putExtra(StockPrepDetailActivity.EXTRA_DOC_NUMBER, item.docNumber)
        }
        startActivity(intent)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressedDispatcher.onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
