package com.jayma.pos.ui.settings

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.jayma.pos.data.local.entities.ClientEntity
import com.jayma.pos.data.local.entities.WarehouseEntity
import com.jayma.pos.data.local.entities.PaymentMethodEntity
import com.jayma.pos.data.repository.PosDataRepository
import com.jayma.pos.data.repository.ProductRepository
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
    
    @Inject
    lateinit var productRepository: ProductRepository
    
    private var warehouses: List<WarehouseEntity> = emptyList()
    private var clients: List<ClientEntity> = emptyList()
    private var paymentMethods: List<PaymentMethodEntity> = emptyList()
    private var warehouseAdapter: ArrayAdapter<String>? = null
    private var clientAdapter: ArrayAdapter<String>? = null
    private var paymentMethodAdapter: ArrayAdapter<String>? = null
    
    private var isDomainEditingEnabled = false
    private val SECRET_KEY = "jayma312@jayma"
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTenantSettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupViews()
        loadWarehousesAndClients()
        loadPaymentMethods()
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
        
        // Disable domain editing by default - requires secret key
        // Domain input is already disabled in XML, just ensure state is correct
        updateDomainEditingState(false)
        
        binding.unlockDomainButton.setOnClickListener {
            showSecretKeyDialog()
        }
        
        binding.saveButton.setOnClickListener {
            if (isDomainEditingEnabled) {
                saveDomain()
            } else {
                Toast.makeText(this, "Please unlock domain editing first", Toast.LENGTH_SHORT).show()
            }
        }
        
        binding.clearButton.setOnClickListener {
            if (isDomainEditingEnabled) {
                clearDomain()
            } else {
                Toast.makeText(this, "Please unlock domain editing first", Toast.LENGTH_SHORT).show()
            }
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
        
        // Setup payment method spinner
        binding.paymentMethodSpinner.setOnItemClickListener { _, _, position, _ ->
            val selectedPaymentMethod = paymentMethods[position]
            sharedPreferences.saveDefaultPaymentMethod(selectedPaymentMethod.id)
            Toast.makeText(this, "Default payment method set to: ${selectedPaymentMethod.name}", Toast.LENGTH_SHORT).show()
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
                // Sync all POS data (clients, warehouses, payment methods)
                val posDataResult = posDataRepository.fetchPosData()
                posDataResult.fold(
                    onSuccess = {
                        Toast.makeText(
                            this@TenantSettingsActivity,
                            "POS data (clients, warehouses, payment methods) synced successfully",
                            Toast.LENGTH_SHORT
                        ).show()
                        
                        // Reload payment methods after sync
                        loadPaymentMethods()
                    },
                    onFailure = { error ->
                        Toast.makeText(
                            this@TenantSettingsActivity,
                            "Failed to sync POS data: ${error.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                )
                
                val warehouseId = sharedPreferences.getDefaultWarehouse()
                if (warehouseId != null) {
                    // Trigger product sync
                    syncManager.triggerProductSync()
                    
                    // Also sync products directly to show progress
                    productRepository.syncAllProducts(warehouseId).fold(
                        onSuccess = { count ->
                            Toast.makeText(
                                this@TenantSettingsActivity,
                                "Synced $count products successfully",
                                Toast.LENGTH_SHORT
                            ).show()
                        },
                        onFailure = { error ->
                            Toast.makeText(
                                this@TenantSettingsActivity,
                                "Product sync failed: ${error.message}",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    )
                }
                
                // Trigger sale upload
                syncManager.triggerSaleUpload()
                
                binding.syncButton.isEnabled = true
                binding.syncButton.text = "Sync Data Now"
            } catch (e: Exception) {
                Toast.makeText(
                    this@TenantSettingsActivity,
                    "Sync failed: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
                binding.syncButton.isEnabled = true
                binding.syncButton.text = "Sync Data Now"
            }
        }
    }
    
    private fun loadPaymentMethods() {
        lifecycleScope.launch {
            try {
                paymentMethods = posDataRepository.getAllPaymentMethods().first()
                val paymentMethodNames = paymentMethods.map { it.name }
                paymentMethodAdapter = ArrayAdapter(
                    this@TenantSettingsActivity,
                    android.R.layout.simple_dropdown_item_1line,
                    paymentMethodNames
                )
                binding.paymentMethodSpinner.setAdapter(paymentMethodAdapter)
                
                // Set current selection
                val currentPaymentMethodId = sharedPreferences.getDefaultPaymentMethod()
                if (currentPaymentMethodId != null) {
                    val index = paymentMethods.indexOfFirst { it.id == currentPaymentMethodId }
                    if (index >= 0) {
                        binding.paymentMethodSpinner.setText(paymentMethods[index].name, false)
                    } else {
                        // Default to Cash if available
                        val cashMethod = paymentMethods.find { it.name.equals("Cash", ignoreCase = true) }
                        cashMethod?.let {
                            binding.paymentMethodSpinner.setText(it.name, false)
                            sharedPreferences.saveDefaultPaymentMethod(it.id)
                        } ?: paymentMethods.firstOrNull()?.let {
                            binding.paymentMethodSpinner.setText(it.name, false)
                            sharedPreferences.saveDefaultPaymentMethod(it.id)
                        }
                    }
                } else {
                    // Default to Cash if available
                    val cashMethod = paymentMethods.find { it.name.equals("Cash", ignoreCase = true) }
                    cashMethod?.let {
                        binding.paymentMethodSpinner.setText(it.name, false)
                        sharedPreferences.saveDefaultPaymentMethod(it.id)
                    } ?: paymentMethods.firstOrNull()?.let {
                        binding.paymentMethodSpinner.setText(it.name, false)
                        sharedPreferences.saveDefaultPaymentMethod(it.id)
                    }
                }
            } catch (e: Exception) {
                // Payment methods not loaded yet
            }
        }
    }
    
    private fun loadCurrentSettings() {
        val currentUrl = sharedPreferences.getApiBaseUrl()
        binding.currentDomainText.text = currentUrl ?: "Not configured"
    }
    
    private fun showSecretKeyDialog() {
        val dialogView = layoutInflater.inflate(com.jayma.pos.R.layout.dialog_secret_key, null)
        val secretKeyInput = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(com.jayma.pos.R.id.secretKeyInput)
        
        val dialog = MaterialAlertDialogBuilder(this)
            .setTitle("Enter Secret Key")
            .setMessage("Please enter the secret key to edit the domain settings")
            .setView(dialogView)
            .setPositiveButton("Verify") { _, _ ->
                val enteredKey = secretKeyInput.text.toString().trim()
                if (enteredKey == SECRET_KEY) {
                    isDomainEditingEnabled = true
                    updateDomainEditingState(true)
                    Toast.makeText(this, "Domain editing unlocked", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Invalid secret key", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .create()
        
        dialog.show()
        
        // Auto-focus the input field
        secretKeyInput.requestFocus()
        dialog.window?.setSoftInputMode(android.view.WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
    }
    
    private fun updateDomainEditingState(enabled: Boolean) {
        binding.domainEditText.isEnabled = enabled
        binding.domainEditText.isFocusable = enabled
        binding.domainEditText.isFocusableInTouchMode = enabled
        binding.saveButton.isEnabled = enabled
        binding.clearButton.isEnabled = enabled
        
        if (enabled) {
            binding.unlockDomainButton.text = "Lock Domain Editing"
            binding.unlockDomainButton.setIconResource(android.R.drawable.ic_lock_lock)
        } else {
            binding.unlockDomainButton.text = "Unlock Domain Editing"
            // Icon will be set programmatically if needed, or removed from XML
        }
    }
    
    private fun saveDomain() {
        if (!isDomainEditingEnabled) {
            Toast.makeText(this, "Please unlock domain editing first", Toast.LENGTH_SHORT).show()
            return
        }
        
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
        
        // Lock domain editing again after saving
        isDomainEditingEnabled = false
        updateDomainEditingState(false)
        
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
        if (!isDomainEditingEnabled) {
            Toast.makeText(this, "Please unlock domain editing first", Toast.LENGTH_SHORT).show()
            return
        }
        
        sharedPreferences.clearApiBaseUrl()
        // Reset to default "https://" with cursor after "//"
        binding.domainEditText.setText("https://")
        binding.domainEditText.setSelection(8) // Position after "https://"
        loadCurrentSettings()
        Toast.makeText(this, "Domain cleared. Please configure a new domain.", Toast.LENGTH_LONG).show()
    }
}
