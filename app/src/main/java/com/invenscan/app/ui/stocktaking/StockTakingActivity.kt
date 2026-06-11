package com.invenscan.app.ui.stocktaking

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
import com.invenscan.app.data.model.StockTakingModel
import com.invenscan.app.databinding.ActivityStockTakingBinding
import com.invenscan.app.ui.stocktaking.detail.StockTakingDetailActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class StockTakingActivity : BaseActivity<ActivityStockTakingBinding>() {

    private val viewModel: StockTakingViewModel by viewModels()

    override fun inflateBinding() = ActivityStockTakingBinding.inflate(layoutInflater)

    override fun initView() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.title_stock_taking)

        binding.btnRetry.setOnClickListener { viewModel.loadActiveSession() }
        binding.btnStartSession.setOnClickListener {
            val session = (viewModel.sessionState.value as? Resource.Success)?.data
            session?.let { navigateToDetail(it) }
        }
    }

    override fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.sessionState.collect { state ->
                    when (state) {
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
                            if (state.message.contains("aktif", ignoreCase = true) ||
                                state.code == 404
                            ) {
                                binding.layoutSession.visibility = View.GONE
                                binding.layoutEmpty.visibility = View.VISIBLE
                                binding.layoutError.visibility = View.GONE
                            } else {
                                binding.layoutSession.visibility = View.GONE
                                binding.layoutEmpty.visibility = View.GONE
                                binding.layoutError.visibility = View.VISIBLE
                                binding.tvError.text = state.message
                            }
                        }
                    }
                }
            }
        }
    }

    private fun bindSessionData(session: StockTakingModel) {
        binding.tvSessionCode.text = session.sessionCode
        binding.tvSessionDate.text = session.createdAt?.take(10) ?: "-"
        binding.tvSessionRemark.text = session.remark ?: "-"
    }

    private fun navigateToDetail(session: StockTakingModel) {
        val intent = Intent(this, StockTakingDetailActivity::class.java).apply {
            putExtra(StockTakingDetailActivity.EXTRA_STT_ID, session.id)
            putExtra(StockTakingDetailActivity.EXTRA_SESSION_CODE, session.sessionCode)
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
