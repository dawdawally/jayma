package com.jayma.pos.ui.setup

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.jayma.pos.databinding.ActivityPosSetupBinding
import com.jayma.pos.ui.MainActivity
import com.jayma.pos.ui.viewmodel.PosSetupViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class PosSetupActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityPosSetupBinding
    private val viewModel: PosSetupViewModel by viewModels()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPosSetupBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        observeViewModel()
        setupRetryButton()
    }
    
    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                binding.progressMessage.text = state.progressMessage
                
                if (state.isLoading) {
                    binding.progressBar.visibility = android.view.View.VISIBLE
                    binding.errorMessage.visibility = android.view.View.GONE
                    binding.retryButton.visibility = android.view.View.GONE
                } else {
                    binding.progressBar.visibility = android.view.View.GONE
                    
                    if (state.error != null) {
                        binding.errorMessage.text = state.error
                        binding.errorMessage.visibility = android.view.View.VISIBLE
                        binding.retryButton.visibility = android.view.View.VISIBLE
                    } else if (state.setupComplete) {
                        // Navigate to main POS screen
                        navigateToMain()
                    }
                }
            }
        }
    }
    
    private fun setupRetryButton() {
        binding.retryButton.setOnClickListener {
            viewModel.retry()
        }
    }
    
    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
