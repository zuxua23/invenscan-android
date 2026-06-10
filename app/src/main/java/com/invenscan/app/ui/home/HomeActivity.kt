package com.invenscan.app.ui.home

import android.content.Intent
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import com.invenscan.app.R
import com.invenscan.app.base.BaseActivity
import com.invenscan.app.databinding.ActivityHomeBinding
import com.invenscan.app.ui.login.LoginActivity
import com.invenscan.app.ui.search.SearchItemActivity
import com.invenscan.app.ui.settings.SettingsActivity
import com.invenscan.app.ui.stockin.StockInActivity
import com.invenscan.app.ui.stockprep.StockPrepActivity
import com.invenscan.app.ui.stocktaking.StockTakingActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeActivity : BaseActivity<ActivityHomeBinding>() {

    private val viewModel: HomeViewModel by viewModels()

    override fun inflateBinding() = ActivityHomeBinding.inflate(layoutInflater)

    override fun initView() {
        setSupportActionBar(binding.toolbar)
        binding.tvWelcome.text = getString(R.string.label_welcome, viewModel.fullName)

        binding.cardStockIn.setOnClickListener {
            startActivity(Intent(this, StockInActivity::class.java))
        }
        binding.cardStockTaking.setOnClickListener {
            startActivity(Intent(this, StockTakingActivity::class.java))
        }
        binding.cardStockPrep.setOnClickListener {
            startActivity(Intent(this, StockPrepActivity::class.java))
        }
        binding.cardSearchItem.setOnClickListener {
            startActivity(Intent(this, SearchItemActivity::class.java))
        }
    }

    override fun observeViewModel() {}

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_home, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> {
                startActivity(Intent(this, SettingsActivity::class.java))
                true
            }
            R.id.action_logout -> {
                viewModel.logout()
                startActivity(Intent(this, LoginActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                })
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
