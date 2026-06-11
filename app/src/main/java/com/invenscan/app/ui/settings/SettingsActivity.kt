package com.invenscan.app.ui.settings

import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatDelegate
import com.invenscan.app.BuildConfig
import com.invenscan.app.R
import com.invenscan.app.base.BaseActivity
import com.invenscan.app.base.BaseViewModel
import com.invenscan.app.databinding.ActivitySettingsBinding
import com.invenscan.app.util.CustomToast
import com.invenscan.app.util.PrefManager
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    val prefManager: PrefManager
) : BaseViewModel() {

    fun save(serverUrl: String, deviceId: String) {
        prefManager.serverUrl = serverUrl.trim()
        prefManager.deviceId = deviceId.trim()
    }
}

@AndroidEntryPoint
class SettingsActivity : BaseActivity<ActivitySettingsBinding>() {

    private val viewModel: SettingsViewModel by viewModels()

    override fun inflateBinding() = ActivitySettingsBinding.inflate(layoutInflater)

    override fun initView() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.title_settings)

        binding.etServerUrl.setText(viewModel.prefManager.serverUrl ?: "")
        binding.etDeviceId.setText(viewModel.prefManager.deviceId)
        binding.tvAppVersion.text = BuildConfig.VERSION_NAME
        binding.tvScannerNote.text = getString(R.string.label_scanner_note)
        binding.switchDarkMode.isChecked = viewModel.prefManager.isDarkTheme

        binding.btnSave.setOnClickListener {
            val serverUrl = binding.etServerUrl.text.toString().trim()
            if (serverUrl.isBlank()) {
                binding.tilServerUrl.error = getString(R.string.error_server_url_empty)
                return@setOnClickListener
            }
            binding.tilServerUrl.error = null
            val deviceId = binding.etDeviceId.text.toString().trim()
                .ifBlank { viewModel.prefManager.deviceId }
            viewModel.save(serverUrl, deviceId)
            CustomToast.show(this, getString(R.string.success_settings_saved), CustomToast.Type.SUCCESS)
        }

        binding.switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
            viewModel.prefManager.isDarkTheme = isChecked
            AppCompatDelegate.setDefaultNightMode(
                if (isChecked) AppCompatDelegate.MODE_NIGHT_YES
                else AppCompatDelegate.MODE_NIGHT_NO
            )
        }
    }

    override fun observeViewModel() {}

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressedDispatcher.onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
