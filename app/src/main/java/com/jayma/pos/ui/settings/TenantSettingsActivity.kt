package com.jayma.pos.ui.settings

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.jayma.pos.databinding.ActivityTenantSettingsBinding
import com.jayma.pos.ui.setup.PosSetupActivity
import com.jayma.pos.util.SharedPreferencesHelper
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class TenantSettingsActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityTenantSettingsBinding
    
    @Inject
    lateinit var sharedPreferences: SharedPreferencesHelper
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTenantSettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupViews()
        loadCurrentSettings()
    }
    
    private fun setupViews() {
        // Load current domain if set, otherwise set default "https://"
        val currentUrl = sharedPreferences.getApiBaseUrl()
        if (currentUrl != null) {
            binding.domainEditText.setText(currentUrl)
            // Position cursor at the end
            binding.domainEditText.setSelection(currentUrl.length)
        } else {
            // Set default "https://" and position cursor after "//"
            binding.domainEditText.setText("https://")
            binding.domainEditText.setSelection(8) // Position after "https://"
        }
        
        // Set hint with example
        binding.domainEditText.hint = "yourdomain.com"
        
        // Request focus and show keyboard
        binding.domainEditText.requestFocus()
        binding.domainEditText.post {
            // Use post to ensure the view is fully laid out before showing keyboard
            val imm = getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
            imm.showSoftInput(binding.domainEditText, android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT)
        }
        
        binding.saveButton.setOnClickListener {
            saveDomain()
        }
        
        binding.clearButton.setOnClickListener {
            clearDomain()
        }
    }
    
    private fun loadCurrentSettings() {
        val currentUrl = sharedPreferences.getApiBaseUrl()
        binding.currentDomainText.text = currentUrl ?: "Not configured"
    }
    
    private fun saveDomain() {
        val domain = binding.domainEditText.text.toString().trim()
        
        if (domain.isEmpty()) {
            Toast.makeText(this, "Please enter a domain", Toast.LENGTH_SHORT).show()
            return
        }
        
        // Validate URL format
        if (!domain.startsWith("http://") && !domain.startsWith("https://")) {
            Toast.makeText(this, "Domain must start with http:// or https://", Toast.LENGTH_SHORT).show()
            return
        }
        
        // Save the domain
        sharedPreferences.saveApiBaseUrl(domain)
        
        Toast.makeText(this, "Domain saved successfully", Toast.LENGTH_SHORT).show()
        
        // If this is the first time setting up, navigate to setup
        val warehouseId = sharedPreferences.getDefaultWarehouse()
        if (warehouseId == null) {
            // Navigate to setup to initialize POS
            val intent = Intent(this, PosSetupActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        } else {
            // Just reload current settings
            loadCurrentSettings()
        }
    }
    
    private fun clearDomain() {
        sharedPreferences.clearApiBaseUrl()
        // Reset to default "https://" with cursor after "//"
        binding.domainEditText.setText("https://")
        binding.domainEditText.setSelection(8) // Position after "https://"
        binding.domainEditText.requestFocus()
        loadCurrentSettings()
        Toast.makeText(this, "Domain cleared. Please configure a new domain.", Toast.LENGTH_LONG).show()
    }
}
