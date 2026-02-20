package com.jayma.pos.ui.settings

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.jayma.pos.data.local.entities.ClientEntity
import com.jayma.pos.data.local.entities.WarehouseEntity
import com.jayma.pos.data.repository.PosDataRepository
import com.jayma.pos.databinding.ActivityTenantSettingsBinding
import com.jayma.pos.sync.SyncManager
import com.jayma.pos.ui.setup.PosSetupActivity
import com.jayma.pos.util.SharedPreferencesHelper
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class TenantSettingsActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityTenantSettingsBinding
    
    @Inject
    lateinit var sharedPreferences: SharedPreferencesHelper
    
    @Inject
    lateinit var syncManager: SyncManager
    
    @Inject
    lateinit var posDataRepository: PosDataRepository
    
    private var warehouses: List<WarehouseEntity> = emptyList()
    private var clients: List<ClientEntity> = emptyList()
    private var warehouseAdapter: ArrayAdapter<String>? = null
    private var clientAdapter: ArrayAdapter<String>? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTenantSettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupViews()
        loadWarehousesAndClients()
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
        
        binding.syncButton.setOnClickListener {
            syncData()
        }
        
        // Setup warehouse spinner
        binding.warehouseSpinner.setOnItemClickListener { _, _, position, _ ->
            val selectedWarehouse = warehouses[position]
            sharedPreferences.saveDefaultWarehouse(selectedWarehouse.id)
            Toast.makeText(this, "Warehouse set to: ${selectedWarehouse.name}", Toast.LENGTH_SHORT).show()
        }
        
        // Setup client spinner
        binding.clientSpinner.setOnItemClickListener { _, _, position, _ ->
            val selectedClient = clients[position]
            sharedPreferences.saveDefaultClient(selectedClient.id)
            Toast.makeText(this, "Default client set to: ${selectedClient.name}", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun loadWarehousesAndClients() {
        lifecycleScope.launch {
            try {
                // Load warehouses
                warehouses = posDataRepository.getAllWarehouses().first()
                if (warehouses.size > 1) {
                    // Show warehouse dropdown if more than one warehouse
                    binding.warehouseInputLayout.visibility = View.VISIBLE
                    binding.dividerView.visibility = View.VISIBLE
                    val warehouseNames = warehouses.map { it.name }
                    warehouseAdapter = ArrayAdapter(
                        this@TenantSettingsActivity,
                        android.R.layout.simple_dropdown_item_1line,
                        warehouseNames
                    )
                    binding.warehouseSpinner.setAdapter(warehouseAdapter)
                    
                    // Set current selection
                    val currentWarehouseId = sharedPreferences.getDefaultWarehouse()
                    currentWarehouseId?.let { id ->
                        val index = warehouses.indexOfFirst { it.id == id }
                        if (index >= 0) {
                            binding.warehouseSpinner.setText(warehouses[index].name, false)
                        }
                    }
                } else {
                    binding.warehouseInputLayout.visibility = View.GONE
                    binding.dividerView.visibility = View.GONE
                }
                
                // Load clients
                clients = posDataRepository.getAllClients().first()
                val clientNames = clients.map { it.name }
                clientAdapter = ArrayAdapter(
                    this@TenantSettingsActivity,
                    android.R.layout.simple_dropdown_item_1line,
                    clientNames
                )
                binding.clientSpinner.setAdapter(clientAdapter)
                
                // Set current selection - default to "Walk-in" or first client
                val currentClientId = sharedPreferences.getDefaultClient()
                val defaultClient = clients.find { it.name.equals("Walk-in", ignoreCase = true) }
                    ?: clients.firstOrNull()
                
                if (currentClientId != null) {
                    val index = clients.indexOfFirst { it.id == currentClientId }
                    if (index >= 0) {
                        binding.clientSpinner.setText(clients[index].name, false)
                    } else if (defaultClient != null) {
                        binding.clientSpinner.setText(defaultClient.name, false)
                        sharedPreferences.saveDefaultClient(defaultClient.id)
                    }
                } else if (defaultClient != null) {
                    binding.clientSpinner.setText(defaultClient.name, false)
                    sharedPreferences.saveDefaultClient(defaultClient.id)
                }
            } catch (e: Exception) {
                // If data not loaded yet, try to fetch it
                Toast.makeText(this@TenantSettingsActivity, "Loading warehouses and clients...", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun syncData() {
        binding.syncButton.isEnabled = false
        binding.syncButton.text = "Syncing..."
        
        lifecycleScope.launch {
            try {
                val warehouseId = sharedPreferences.getDefaultWarehouse()
                if (warehouseId == null) {
                    Toast.makeText(this@TenantSettingsActivity, "No warehouse configured", Toast.LENGTH_SHORT).show()
                    binding.syncButton.isEnabled = true
                    binding.syncButton.text = "Sync Data Now"
                    return@launch
                }
                
                // Trigger product sync and sale upload
                syncManager.triggerProductSync()
                syncManager.triggerSaleUpload()
                
                // Show initial feedback
                Toast.makeText(
                    this@TenantSettingsActivity,
                    "Sync started. Products and sales will be updated shortly.",
                    Toast.LENGTH_SHORT
                ).show()
                
                // Wait a bit and then show completion message
                delay(2000)
                
                Toast.makeText(
                    this@TenantSettingsActivity,
                    "Sync operations have been queued. Data will be synchronized in the background.",
                    Toast.LENGTH_LONG
                ).show()
                
                binding.syncButton.isEnabled = true
                binding.syncButton.text = "Sync Data Now"
            } catch (e: Exception) {
                Toast.makeText(
                    this@TenantSettingsActivity,
                    "Sync failed to start: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
                binding.syncButton.isEnabled = true
                binding.syncButton.text = "Sync Data Now"
            }
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
