package com.invenscan.app.base

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.viewbinding.ViewBinding
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

abstract class BaseActivity<VB : ViewBinding> : AppCompatActivity() {

    private var _binding: VB? = null
    protected val binding get() = _binding!!

    abstract fun inflateBinding(): VB
    abstract fun initView()
    abstract fun observeViewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = inflateBinding()
        setContentView(binding.root)
        initView()
        observeViewModel()
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
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }

    protected fun showMessage(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
    }

    protected fun setViewVisibility(view: View, isVisible: Boolean) {
        view.visibility = if (isVisible) View.VISIBLE else View.GONE
    }
}
