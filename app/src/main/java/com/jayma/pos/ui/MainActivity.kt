package com.jayma.pos.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.jayma.pos.databinding.ActivityMainBinding
import com.jayma.pos.ui.products.ProductListFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityMainBinding
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Load product list fragment
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(binding.fragmentContainer.id, ProductListFragment())
                .commit()
        }
    }
}
