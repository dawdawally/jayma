package com.jayma.pos.util.scanner

import android.graphics.Bitmap
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import com.google.android.gms.tasks.Tasks
import kotlinx.coroutines.tasks.await

/**
 * Service for scanning barcodes using ML Kit
 */
class BarcodeScannerService {
    
    private val scanner = BarcodeScanning.getClient()
    
    /**
     * Scan barcode from a bitmap image
     * @return The barcode value (raw value) or null if no barcode found
     */
    suspend fun scanBarcode(bitmap: Bitmap): String? {
        return try {
            val image = InputImage.fromBitmap(bitmap, 0)
            val barcodes = scanner.process(image).await()
            
            // Return the first barcode found
            barcodes.firstOrNull()?.rawValue
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Scan barcode from a bitmap image and return all barcodes
     * @return List of barcode values found
     */
    suspend fun scanAllBarcodes(bitmap: Bitmap): List<String> {
        return try {
            val image = InputImage.fromBitmap(bitmap, 0)
            val barcodes = scanner.process(image).await()
            
            barcodes.mapNotNull { it.rawValue }
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    /**
     * Close the scanner (release resources)
     */
    fun close() {
        scanner.close()
    }
}
