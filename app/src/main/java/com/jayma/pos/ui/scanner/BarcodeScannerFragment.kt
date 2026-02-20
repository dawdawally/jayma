package com.jayma.pos.ui.scanner

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.jayma.pos.data.local.entities.ProductEntity
import com.jayma.pos.databinding.FragmentBarcodeScannerBinding
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import com.jayma.pos.ui.cart.CartFragment
import com.jayma.pos.ui.viewmodel.BarcodeScannerViewModel
import com.jayma.pos.ui.viewmodel.CartViewModel
import com.jayma.pos.util.scanner.BarcodeScannerService
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import javax.inject.Inject

@AndroidEntryPoint
class BarcodeScannerFragment : Fragment() {
    
    private var _binding: FragmentBarcodeScannerBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: BarcodeScannerViewModel by viewModels()
    private lateinit var cartViewModel: CartViewModel
    
    @Inject
    lateinit var barcodeScannerService: BarcodeScannerService
    
    private var imageAnalysis: ImageAnalysis? = null
    private var cameraExecutor: ExecutorService? = null
    private var isScanning = false
    private var lastScannedBarcode: String? = null
    
    // Permission launcher using the new Activity Result API
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            startCamera()
            isScanning = true
            _binding?.scanButton?.text = getString(com.jayma.pos.R.string.scanning)
        } else {
            Toast.makeText(
                context,
                "Camera permission is required for barcode scanning",
                Toast.LENGTH_LONG
            ).show()
            parentFragmentManager.popBackStack()
        }
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBarcodeScannerBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Initialize CartViewModel from activity scope
        cartViewModel = ViewModelProvider(requireActivity())[CartViewModel::class.java]
        
        cameraExecutor = Executors.newSingleThreadExecutor()
        
        setupObservers()
        setupButtons()
        
        // Request camera permission if not granted
        if (allPermissionsGranted()) {
            startCamera()
            // Start scanning automatically
            isScanning = true
            binding.scanButton.text = getString(com.jayma.pos.R.string.scanning)
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }
    
    private fun setupObservers() {
        lifecycleScope.launch {
            viewModel.scanResult.collect { result ->
                when (result) {
                    is BarcodeScanResult.Success -> {
                        handleScanSuccess(result.product)
                    }
                    is BarcodeScanResult.NotFound -> {
                        Toast.makeText(
                            context,
                            "Product not found for barcode: ${result.barcode}",
                            Toast.LENGTH_LONG
                        ).show()
                        isScanning = false
                    }
                    is BarcodeScanResult.Error -> {
                        Toast.makeText(
                            context,
                            "Error: ${result.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                        isScanning = false
                    }
                    is BarcodeScanResult.Idle -> {
                        // Do nothing
                    }
                }
            }
        }
    }
    
    private fun setupButtons() {
        binding.scanButton.setOnClickListener {
            if (!isScanning) {
                captureAndScan()
            }
        }
        
        binding.closeButton.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }
    
    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        
        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
            
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(binding.previewView.surfaceProvider)
                }
            
            // Setup ImageAnalysis for continuous barcode scanning
            imageAnalysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also {
                    it.setAnalyzer(cameraExecutor!!) { imageProxy ->
                        processImageProxy(imageProxy)
                    }
                }
            
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
            
            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    viewLifecycleOwner,
                    cameraSelector,
                    preview,
                    imageAnalysis
                )
            } catch (e: Exception) {
                Toast.makeText(context, "Camera initialization failed", Toast.LENGTH_SHORT).show()
            }
        }, ContextCompat.getMainExecutor(requireContext()))
    }
    
    private fun processImageProxy(imageProxy: ImageProxy) {
        if (!isScanning) {
            imageProxy.close()
            return
        }
        
        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val image = InputImage.fromMediaImage(
                mediaImage,
                imageProxy.imageInfo.rotationDegrees
            )
            
            lifecycleScope.launch {
                try {
                    // Use ML Kit directly for better performance
                    val scanner = com.google.mlkit.vision.barcode.BarcodeScanning.getClient()
                    val barcodes = scanner.process(image).await()
                    
                    val barcode = barcodes.firstOrNull()?.rawValue
                    if (barcode != null && barcode != lastScannedBarcode) {
                        lastScannedBarcode = barcode
                        viewModel.scanBarcode(barcode)
                    }
                } catch (e: Exception) {
                    // Ignore errors during scanning
                } finally {
                    imageProxy.close()
                }
            }
        } else {
            imageProxy.close()
        }
    }
    
    private fun captureAndScan() {
        // Toggle continuous scanning
        if (isScanning) {
            isScanning = false
            binding.scanButton.text = getString(com.jayma.pos.R.string.scan_barcode)
            lastScannedBarcode = null
        } else {
            isScanning = true
            binding.scanButton.text = getString(com.jayma.pos.R.string.scanning)
            lastScannedBarcode = null // Reset to allow re-scanning same barcode
        }
    }
    
    private fun handleScanSuccess(product: ProductEntity) {
        // Add product to cart immediately
        cartViewModel.addToCart(product, 1.0)
        
        Toast.makeText(
            context,
            "${product.name} added to cart",
            Toast.LENGTH_SHORT
        ).show()
        
        // Navigate to Cart screen to show real-time updates
        try {
            val mainActivity = requireActivity()
            if (mainActivity is com.jayma.pos.ui.MainActivity) {
                // Navigate to cart fragment
                mainActivity.supportFragmentManager.beginTransaction()
                    .replace(com.jayma.pos.R.id.fragmentContainer, CartFragment())
                    .commit()
                
                // Update bottom navigation to show cart is selected
                mainActivity.binding.bottomNavigation.selectedItemId = com.jayma.pos.R.id.nav_cart
            }
        } catch (e: Exception) {
            // Fallback: just pop back stack
            parentFragmentManager.popBackStack()
        }
        
        // Reset scanner for next scan
        viewModel.reset()
        lastScannedBarcode = null
    }
    
    private fun allPermissionsGranted() = ContextCompat.checkSelfPermission(
        requireContext(),
        Manifest.permission.CAMERA
    ) == PackageManager.PERMISSION_GRANTED
    
    override fun onDestroyView() {
        super.onDestroyView()
        cameraExecutor?.shutdown()
        _binding = null
    }
}

/**
 * Sealed class representing barcode scan results
 */
sealed class BarcodeScanResult {
    object Idle : BarcodeScanResult()
    data class Success(val product: ProductEntity) : BarcodeScanResult()
    data class NotFound(val barcode: String) : BarcodeScanResult()
    data class Error(val message: String) : BarcodeScanResult()
}
