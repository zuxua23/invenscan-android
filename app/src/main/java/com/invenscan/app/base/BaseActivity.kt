package com.invenscan.app.base

import android.os.Bundle
import android.view.Menu
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.viewbinding.ViewBinding
import com.invenscan.app.R
import com.invenscan.app.scanner.BatteryProvider
import com.invenscan.app.util.AppLogger
import com.invenscan.app.util.CustomDialog
import com.invenscan.app.util.CustomToast
import com.invenscan.app.util.PrefManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

abstract class BaseActivity<VB : ViewBinding> : AppCompatActivity() {

    private var _binding: VB? = null
    protected val binding get() = _binding!!

    @Inject
    lateinit var prefManager: PrefManager

    @Inject
    lateinit var batteryProvider: BatteryProvider

    @Inject
    lateinit var appLogger: AppLogger

    abstract fun inflateBinding(): VB
    abstract fun initView()
    abstract fun observeViewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        applyTheme()
        super.onCreate(savedInstanceState)
        _binding = inflateBinding()
        setContentView(binding.root)
        initView()
        observeViewModel()
    }

    private fun applyTheme() {
        val isDark = try {
            val prefs = androidx.security.crypto.EncryptedSharedPreferences.create(
                this,
                PrefManager.PREFS_FILE_NAME,
                androidx.security.crypto.MasterKey.Builder(this)
                    .setKeyScheme(androidx.security.crypto.MasterKey.KeyScheme.AES256_GCM)
                    .build(),
                androidx.security.crypto.EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                androidx.security.crypto.EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
            prefs.getBoolean(PrefManager.KEY_DARK_THEME, false)
        } catch (e: Exception) {
            false
        }

        AppCompatDelegate.setDefaultNightMode(
            if (isDark) AppCompatDelegate.MODE_NIGHT_YES
            else AppCompatDelegate.MODE_NIGHT_NO
        )
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_battery, menu)
        val actionView = menu.findItem(R.id.action_battery)?.actionView
        if (actionView != null) {
            val tvHt = actionView.findViewById<TextView>(R.id.tvBatteryHt)
            val tvRfid = actionView.findViewById<TextView>(R.id.tvBatteryRfid)
            tvHt?.text = getString(R.string.label_battery_ht) + ": ${batteryProvider.getHtBattery()}%"
            if (prefManager.batteryDisplayMode == PrefManager.BATTERY_MODE_DUAL) {
                tvRfid?.visibility = View.VISIBLE
                tvRfid?.text = "  " + getString(R.string.label_battery_rfid) + ": ${batteryProvider.getRfidBattery() ?: 0}%"
            }
        }
        return super.onCreateOptionsMenu(menu)
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    protected fun collectError(viewModel: BaseViewModel) {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.errorMessage.collect { message ->
                    if (message != null) {
                        showError(message)
                        viewModel.clearError()
                    }
                }
            }
        }
    }

    protected fun showError(message: String) {
        CustomToast.show(this, message, CustomToast.Type.ERROR)
    }

    protected fun showMessage(message: String) {
        CustomToast.show(this, message, CustomToast.Type.SUCCESS)
    }

    protected fun showWarning(message: String) {
        CustomToast.show(this, message, CustomToast.Type.WARNING)
    }

    protected fun showConfirmDialog(
        title: String,
        message: String,
        positiveText: String = getString(com.invenscan.app.R.string.dialog_yes),
        negativeText: String = getString(com.invenscan.app.R.string.dialog_no),
        onConfirm: () -> Unit
    ) {
        CustomDialog.show(
            context = this,
            title = title,
            message = message,
            positiveText = positiveText,
            negativeText = negativeText,
            onPositive = onConfirm
        )
    }

    protected fun setViewVisibility(view: View, isVisible: Boolean) {
        view.visibility = if (isVisible) View.VISIBLE else View.GONE
    }
}
