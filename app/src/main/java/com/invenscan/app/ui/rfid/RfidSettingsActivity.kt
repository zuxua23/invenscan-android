package com.invenscan.app.ui.rfid

import android.view.MenuItem
import android.widget.ArrayAdapter
import android.widget.SeekBar
import androidx.activity.viewModels
import com.invenscan.app.R
import com.invenscan.app.base.BaseActivity
import com.invenscan.app.base.BaseViewModel
import com.invenscan.app.databinding.ActivityRfidSettingsBinding
import com.invenscan.app.util.CustomToast
import com.invenscan.app.util.PrefManager
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class RfidSettingsViewModel @Inject constructor(
    val prefManager: PrefManager
) : BaseViewModel() {

    fun save(triggerMode: String, power: Int, sensitivity: Int, session: String, qFactor: Int) {
        prefManager.rfidTriggerMode = triggerMode
        prefManager.rfidPower = power
        prefManager.rfidSensitivity = sensitivity
        prefManager.rfidSession = session
        prefManager.rfidQFactor = qFactor
    }

    fun resetDefaults() {
        prefManager.rfidTriggerMode = PrefManager.DEFAULT_RFID_TRIGGER_MODE
        prefManager.rfidPower = PrefManager.DEFAULT_RFID_POWER
        prefManager.rfidSensitivity = PrefManager.DEFAULT_RFID_SENSITIVITY
        prefManager.rfidSession = PrefManager.DEFAULT_RFID_SESSION
        prefManager.rfidQFactor = PrefManager.DEFAULT_RFID_Q_FACTOR
    }
}

@AndroidEntryPoint
class RfidSettingsActivity : BaseActivity<ActivityRfidSettingsBinding>() {

    private val viewModel: RfidSettingsViewModel by viewModels()

    private val triggerModes = listOf("Continuous", "Key Press", "Auto")
    private val powerValues = listOf(5, 10, 15, 20, 25, 27, 30)
    private val sessions = listOf("S0", "S1", "S2", "S3")
    private val qFactors = (0..15).toList()

    override fun inflateBinding() = ActivityRfidSettingsBinding.inflate(layoutInflater)

    override fun initView() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.title_rfid_settings)

        setupSpinners()
        loadCurrentValues()
        setupSensitivitySlider()

        binding.btnSaveRfid.setOnClickListener { saveSettings() }
        binding.btnResetDefaults.setOnClickListener { resetToDefaults() }
    }

    override fun observeViewModel() {}

    private fun setupSpinners() {
        binding.spinnerTriggerMode.adapter =
            ArrayAdapter(this, android.R.layout.simple_spinner_item, triggerModes).apply {
                setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            }

        val powerLabels = powerValues.map { "$it dBm" }
        binding.spinnerDefaultPower.adapter =
            ArrayAdapter(this, android.R.layout.simple_spinner_item, powerLabels).apply {
                setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            }

        binding.spinnerSession.adapter =
            ArrayAdapter(this, android.R.layout.simple_spinner_item, sessions).apply {
                setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            }

        val qFactorLabels = qFactors.map { it.toString() }
        binding.spinnerQFactor.adapter =
            ArrayAdapter(this, android.R.layout.simple_spinner_item, qFactorLabels).apply {
                setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            }
    }

    private fun loadCurrentValues() {
        val triggerIdx = triggerModes.indexOf(viewModel.prefManager.rfidTriggerMode).coerceAtLeast(0)
        binding.spinnerTriggerMode.setSelection(triggerIdx)

        val powerIdx = powerValues.indexOf(viewModel.prefManager.rfidPower).coerceAtLeast(0)
        binding.spinnerDefaultPower.setSelection(powerIdx)

        val sensitivity = viewModel.prefManager.rfidSensitivity
        binding.seekSensitivity.progress = (sensitivity - 1).coerceIn(0, 9)
        binding.tvSensitivityValue.text = sensitivity.toString()

        val sessionIdx = sessions.indexOf(viewModel.prefManager.rfidSession).coerceAtLeast(0)
        binding.spinnerSession.setSelection(sessionIdx)

        val qFactorIdx = qFactors.indexOf(viewModel.prefManager.rfidQFactor).coerceAtLeast(0)
        binding.spinnerQFactor.setSelection(qFactorIdx)
    }

    private fun setupSensitivitySlider() {
        binding.seekSensitivity.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                binding.tvSensitivityValue.text = (progress + 1).toString()
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    private fun saveSettings() {
        val triggerMode = triggerModes[binding.spinnerTriggerMode.selectedItemPosition]
        val power = powerValues[binding.spinnerDefaultPower.selectedItemPosition]
        val sensitivity = binding.seekSensitivity.progress + 1
        val session = sessions[binding.spinnerSession.selectedItemPosition]
        val qFactor = qFactors[binding.spinnerQFactor.selectedItemPosition]

        viewModel.save(triggerMode, power, sensitivity, session, qFactor)
        CustomToast.show(this, getString(R.string.success_rfid_settings_saved), CustomToast.Type.SUCCESS)
    }

    private fun resetToDefaults() {
        viewModel.resetDefaults()
        loadCurrentValues()
        CustomToast.show(this, getString(R.string.success_rfid_settings_reset), CustomToast.Type.INFO)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressedDispatcher.onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
