package com.invenscan.app.ui.home

import android.content.Intent
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import com.invenscan.app.util.SessionManager
import com.invenscan.app.R
import com.invenscan.app.base.BaseActivity
import com.invenscan.app.databinding.ActivityHomeBinding
import com.invenscan.app.ui.login.LoginActivity
import com.invenscan.app.ui.rfid.RfidSettingsActivity
import com.invenscan.app.ui.search.SearchItemActivity
import com.invenscan.app.ui.settings.SettingsActivity
import com.invenscan.app.ui.stockin.StockInActivity
import com.invenscan.app.ui.stockout.StockOutActivity
import com.invenscan.app.ui.stockprep.StockPrepActivity
import com.invenscan.app.ui.stocktaking.StockTakingActivity
import com.invenscan.app.ui.tagreg.TagRegistrationActivity
import com.invenscan.app.util.CustomDialog
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
        binding.cardStockOut.setOnClickListener {
            startActivity(Intent(this, StockOutActivity::class.java))
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
        binding.cardSettings.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
        binding.cardRfidSettings.setOnClickListener {
            startActivity(Intent(this, RfidSettingsActivity::class.java))
        }
        binding.cardTagRegistration.setOnClickListener {
            startActivity(Intent(this, TagRegistrationActivity::class.java))
        }
    }

    override fun observeViewModel() {}

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_home, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> {
                startActivity(Intent(this, SettingsActivity::class.java))
                true
            }
            R.id.action_logout -> {
                CustomDialog.show(
                    context = this,
                    title = getString(R.string.action_logout),
                    message = getString(R.string.confirm_logout),
                    positiveText = getString(R.string.action_logout),
                    negativeText = getString(R.string.dialog_cancel),
                    onPositive = {
                        appLogger.logLogout(prefManager.userId ?: "")
                        SessionManager.isActive = false
                        viewModel.logout()
                        startActivity(Intent(this, LoginActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        })
                    }
                )
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
