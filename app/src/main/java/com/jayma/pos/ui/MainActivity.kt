package com.jayma.pos.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.jayma.pos.databinding.ActivityMainBinding
import com.jayma.pos.sync.SyncInitializer
import com.jayma.pos.ui.cart.CartFragment
import com.jayma.pos.ui.products.ProductListFragment
import com.jayma.pos.ui.reports.SalesReportFragment
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    
    @Inject
    lateinit var syncInitializer: SyncInitializer
    
    private lateinit var binding: ActivityMainBinding
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Initialize background sync
        syncInitializer.initialize()
        
        setupBottomNavigation()
        
        // Load product list fragment by default
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(binding.fragmentContainer.id, ProductListFragment())
                .commit()
        }
    }
    
    private fun setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                com.jayma.pos.R.id.nav_products -> {
                    supportFragmentManager.beginTransaction()
                        .replace(binding.fragmentContainer.id, ProductListFragment())
                        .commit()
                    true
                }
                com.jayma.pos.R.id.nav_cart -> {
                    supportFragmentManager.beginTransaction()
                        .replace(binding.fragmentContainer.id, CartFragment())
                        .commit()
                    true
                }
                com.jayma.pos.R.id.nav_reports -> {
                    supportFragmentManager.beginTransaction()
                        .replace(binding.fragmentContainer.id, SalesReportFragment())
                        .commit()
                    true
                }
                else -> false
            }
        }
    }
}

