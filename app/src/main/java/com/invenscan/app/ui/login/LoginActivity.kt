package com.invenscan.app.ui.login

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.invenscan.app.base.BaseActivity
import com.invenscan.app.base.Resource
import com.invenscan.app.databinding.ActivityLoginBinding
import com.invenscan.app.ui.home.HomeActivity
import com.invenscan.app.util.PrefManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class LoginActivity : BaseActivity<ActivityLoginBinding>() {

    private val viewModel: LoginViewModel by viewModels()

    @Inject
    lateinit var prefManager: PrefManager

    override fun inflateBinding() = ActivityLoginBinding.inflate(layoutInflater)

    override fun initView() {
        if (prefManager.isLoggedIn) {
            navigateToHome()
            return
        }

        prefManager.serverUrl?.let { binding.etServerUrl.setText(it) }

        binding.btnLogin.setOnClickListener {
            val serverUrl = binding.etServerUrl.text.toString().trim()
            val userId = binding.etUsername.text.toString().trim()
            val password = binding.etPassword.text.toString()

            if (!validateInputs(serverUrl, userId, password)) return@setOnClickListener
            viewModel.login(serverUrl, userId, password)
        }
    }

    override fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.loginState.collect { state ->
                    when (state) {
                        is Resource.Loading -> showLoading(true)
                        is Resource.Success -> {
                            showLoading(false)
                            navigateToHome()
                        }
                        is Resource.Error -> {
                            showLoading(false)
                            showError(state.message)
                            viewModel.resetState()
                        }
                        null -> showLoading(false)
                    }
                }
            }
        }
    }

    private fun validateInputs(serverUrl: String, userId: String, password: String): Boolean {
        var valid = true

        if (serverUrl.isBlank()) {
            binding.tilServerUrl.error = getString(com.invenscan.app.R.string.error_server_url_required)
            valid = false
        } else if (!serverUrl.startsWith("http://") && !serverUrl.startsWith("https://")) {
            binding.tilServerUrl.error = getString(com.invenscan.app.R.string.error_invalid_url)
            valid = false
        } else {
            binding.tilServerUrl.error = null
        }

        if (userId.isBlank()) {
            binding.tilUsername.error = getString(com.invenscan.app.R.string.error_username_required)
            valid = false
        } else {
            binding.tilUsername.error = null
        }

        if (password.isBlank()) {
            binding.tilPassword.error = getString(com.invenscan.app.R.string.error_password_required)
            valid = false
        } else {
            binding.tilPassword.error = null
        }

        return valid
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.btnLogin.isEnabled = !isLoading
        binding.etServerUrl.isEnabled = !isLoading
        binding.etUsername.isEnabled = !isLoading
        binding.etPassword.isEnabled = !isLoading
    }

    private fun navigateToHome() {
        startActivity(Intent(this, HomeActivity::class.java))
        finish()
    }
}
