package com.jayma.pos.ui.setup

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import com.jayma.pos.databinding.ActivityPosSetupBinding
import com.jayma.pos.ui.MainActivity
import com.jayma.pos.ui.settings.TenantSettingsActivity
import com.jayma.pos.ui.viewmodel.PosSetupViewModel
import com.jayma.pos.util.SharedPreferencesHelper
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class PosSetupActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityPosSetupBinding
    private val viewModel: PosSetupViewModel by viewModels()
    
    @Inject
    lateinit var sharedPreferences: SharedPreferencesHelper
    
    override fun onCreate(savedInstanceState: Bundle?) {
        // Install splash screen (Android 12+)
        val splashScreen = installSplashScreen()
        
        super.onCreate(savedInstanceState)
        
        // Check if tenant domain is configured
        val apiBaseUrl = sharedPreferences.getApiBaseUrl()
        if (apiBaseUrl.isNullOrBlank()) {
            // Navigate to tenant settings first
            val intent = Intent(this, TenantSettingsActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
            return
        }
        
        binding = ActivityPosSetupBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Keep splash screen visible until setup is complete
        var keepSplashScreen = true
        splashScreen.setKeepOnScreenCondition { keepSplashScreen }
        
        observeViewModel { keepSplashScreen = false }
        setupRetryButton()
    }
    
    private fun observeViewModel(onSplashComplete: () -> Unit = {}) {
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
                        // If host resolution error, navigate to settings
                        if (state.shouldNavigateToSettings) {
                            onSplashComplete()
                            navigateToSettings()
                            return@collect
                        }
                        
                        binding.errorMessage.text = state.error
                        binding.errorMessage.visibility = android.view.View.VISIBLE
                        binding.retryButton.visibility = android.view.View.VISIBLE
                        onSplashComplete() // Hide splash on error
                    } else if (state.setupComplete) {
                        onSplashComplete() // Hide splash before navigation
                        // Navigate to main POS screen
                        navigateToMain()
                    }
                }
            }
        }
    }
    
    private fun navigateToSettings() {
        val intent = Intent(this, TenantSettingsActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
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
