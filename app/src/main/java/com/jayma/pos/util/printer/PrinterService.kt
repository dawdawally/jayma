package com.jayma.pos.util.printer

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import com.jayma.pos.data.local.entities.SaleEntity
import com.jayma.pos.data.local.entities.SaleDetailEntity
import com.jayma.pos.data.local.entities.PaymentEntity
import com.sunmi.printerx.Printer
import com.sunmi.printerx.PrinterCallback
import com.sunmi.printerx.SdkException
import com.sunmi.printerx.enums.AlignType
import com.sunmi.printerx.enums.FontType
import com.sunmi.printerx.enums.PaperType
import java.text.SimpleDateFormat
import java.util.*

/**
 * Printer Service for SUNMI POS devices
 * 
 * Uses SUNMI Printer SDK (com.sunmi:printerx:1.0.17)
 * Documentation: https://developer.sunmi.com/docs/en-US/cdixeghjk491/xdzceghjk502
 */
class PrinterService(private val context: Context) {
    
    companion object {
        private const val TAG = "PrinterService"
        private const val LINE_WIDTH = 48 // Characters per line for 80mm paper
    }
    
    private var printer: Printer? = null
    private var isInitialized = false
    
    /**
     * Initialize printer connection
     */
    fun initialize(): Boolean {
        return try {
            if (isInitialized && printer != null) {
                return true
            }
            
            // Get printer instance for built-in printer (PaperType.RECEIPT)
            printer = Printer.getInstance(context, PaperType.RECEIPT)
            
            // Initialize printer
            printer?.initPrinter(object : PrinterCallback {
                override fun onException(e: SdkException) {
                    Log.e(TAG, "Printer initialization exception", e)
                    isInitialized = false
                }
            })
            
            isInitialized = true
            Log.d(TAG, "Printer initialized")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize printer", e)
            isInitialized = false
            false
        }
    }
    
    /**
     * Check printer status
     */
    fun checkPrinterStatus(): PrinterStatus {
        return try {
            if (!isInitialized || printer == null) {
                return PrinterStatus.ERROR
            }
            
            // Check printer status
            val status = printer?.getPrinterStatus()
            
            when (status) {
                null -> PrinterStatus.ERROR
                0 -> PrinterStatus.READY // Normal status
                1 -> PrinterStatus.OUT_OF_PAPER
                2 -> PrinterStatus.OVERHEATED
                else -> PrinterStatus.ERROR
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to check printer status", e)
            PrinterStatus.ERROR
        }
    }
    
    /**
     * Print receipt for a sale
     */
    fun printReceipt(
        sale: SaleEntity,
        saleDetails: List<SaleDetailEntity>,
        payments: List<PaymentEntity>,
        storeName: String = "Jayma POS",
        storeAddress: String = "",
        storePhone: String = ""
    ): PrintResult {
        return try {
            if (!initialize()) {
                return PrintResult(false, "Failed to initialize printer")
            }
            
            val status = checkPrinterStatus()
            if (status != PrinterStatus.READY) {
                return PrintResult(false, "Printer not ready: $status")
            }
            
            // Start printing
            startPrint()
            
            // Print header
            printHeader(storeName, storeAddress, storePhone)
            
            // Print sale info
            printSaleInfo(sale)
            
            // Store sale details for use in printTotals
            this.saleDetails = saleDetails
            
            // Print items
            printItems(saleDetails)
            
            // Print totals
            printTotals(sale)
            
            // Print payments
            printPayments(payments)
            
            // Print footer
            printFooter()
            
            // Cut paper
            cutPaper()
            
            PrintResult(true, "Receipt printed successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to print receipt", e)
            PrintResult(false, "Print error: ${e.message}")
        }
    }
    
    private fun startPrint() {
        try {
            if (printer == null || !isInitialized) {
                throw IllegalStateException("Printer not initialized")
            }
            
            // Start a new print job
            printer?.start()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start print", e)
            throw e
        }
    }
    
    private fun printHeader(storeName: String, storeAddress: String, storePhone: String) {
        printCentered(storeName, true, true)
        printLine()
        
        if (storeAddress.isNotEmpty()) {
            printCentered(storeAddress)
        }
        if (storePhone.isNotEmpty()) {
            printCentered(storePhone)
        }
        printLine()
    }
    
    private fun printSaleInfo(sale: SaleEntity) {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val dateStr = dateFormat.format(Date(sale.createdAt))
        
        printLine("Date: $dateStr")
        printLine("Sale ID: ${sale.serverId ?: sale.localId}")
        printLine("--------------------------------")
    }
    
    private fun printItems(items: List<SaleDetailEntity>) {
        printLine("Items:")
        printLine()
        
        items.forEach { item ->
            // Product name (may wrap)
            printLine(item.productName)
            
            // Quantity, price, and subtotal
            val line = String.format(
                "%s x $%.2f = $%.2f",
                item.quantity.toString(),
                item.unitPrice,
                item.subtotal
            )
            printLine(line)
            
            // Discount if applicable
            if (item.discount > 0) {
                printLine("  Discount: -$${String.format("%.2f", item.discount)}")
            }
            
            printLine()
        }
        
        printLine("--------------------------------")
    }
    
    private fun printTotals(sale: SaleEntity) {
        val subtotal = saleDetails.sumOf { it.subtotal }
        
        printLine(String.format("Subtotal: $%.2f", subtotal))
        
        if (sale.taxNet > 0) {
            printLine(String.format("Tax: $%.2f", sale.taxNet))
        }
        
        if (sale.discount > 0) {
            printLine(String.format("Discount: -$%.2f", sale.discount))
        }
        
        if (sale.shipping > 0) {
            printLine(String.format("Shipping: $%.2f", sale.shipping))
        }
        
        printLine("--------------------------------")
        printLine(String.format("TOTAL: $%.2f", sale.grandTotal), true, true)
        printLine()
    }
    
    private fun printPayments(payments: List<PaymentEntity>) {
        printLine("Payment:")
        
        payments.forEach { payment ->
            val paymentMethod = when (payment.paymentMethodId) {
                1 -> "Cash"
                2 -> "Credit Card"
                else -> "Payment Method ${payment.paymentMethodId}"
            }
            
            printLine(String.format("$paymentMethod: $%.2f", payment.amount))
            
            if (payment.change > 0) {
                printLine(String.format("Change: $%.2f", payment.change))
            }
        }
        
        printLine()
    }
    
    private fun printFooter() {
        printLine("--------------------------------")
        printCentered("Thank you for your business!")
        printLine()
        printLine()
        printLine()
    }
    
    private fun printLine(text: String = "", bold: Boolean = false, large: Boolean = false) {
        try {
            if (printer == null || !isInitialized) {
                Log.w(TAG, "Printer not initialized, skipping print: $text")
                return
            }
            
            // Set alignment to left
            printer?.setAlign(AlignType.LEFT)
            
            // Set font style
            if (bold && large) {
                printer?.setFontSize(FontType.BOLD_LARGE)
            } else if (bold) {
                printer?.setFontSize(FontType.BOLD)
            } else if (large) {
                printer?.setFontSize(FontType.LARGE)
            } else {
                printer?.setFontSize(FontType.NORMAL)
            }
            
            // Print text
            if (text.isNotEmpty()) {
                printer?.printText(text)
            }
            
            // Print newline
            printer?.printText("\n")
            
            Log.d(TAG, "Print: $text")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to print line: $text", e)
        }
    }
    
    private fun printCentered(text: String, bold: Boolean = false, large: Boolean = false) {
        try {
            if (printer == null || !isInitialized) {
                Log.w(TAG, "Printer not initialized, skipping print: $text")
                return
            }
            
            // Set alignment to center
            printer?.setAlign(AlignType.CENTER)
            
            // Set font style
            if (bold && large) {
                printer?.setFontSize(FontType.BOLD_LARGE)
            } else if (bold) {
                printer?.setFontSize(FontType.BOLD)
            } else if (large) {
                printer?.setFontSize(FontType.LARGE)
            } else {
                printer?.setFontSize(FontType.NORMAL)
            }
            
            // Print text
            printer?.printText(text)
            
            // Print newline
            printer?.printText("\n")
            
            Log.d(TAG, "Print centered: $text")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to print centered: $text", e)
        }
    }
    
    private fun cutPaper() {
        try {
            if (printer == null || !isInitialized) {
                Log.w(TAG, "Printer not initialized, cannot cut paper")
                return
            }
            
            // Cut paper (partial cut)
            printer?.cutPaper()
            
            // Commit the print job
            printer?.commit()
            
            Log.d(TAG, "Paper cut and print job committed")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to cut paper", e)
        }
    }
    
    /**
     * Print test page
     */
    fun printTestPage(): PrintResult {
        return try {
            if (!initialize()) {
                return PrintResult(false, "Failed to initialize printer")
            }
            
            startPrint()
            printCentered("PRINTER TEST", true, true)
            printLine()
            printLine("This is a test print")
            printLine("If you can read this, the printer is working correctly.")
            printLine()
            printFooter()
            cutPaper()
            
            PrintResult(true, "Test page printed")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to print test page", e)
            PrintResult(false, "Print error: ${e.message}")
        }
    }
    
    // Helper property for calculating subtotal (used in printTotals)
    private var saleDetails: List<SaleDetailEntity> = emptyList()
}

data class PrintResult(
    val success: Boolean,
    val message: String
)

enum class PrinterStatus {
    READY,
    OUT_OF_PAPER,
    OVERHEATED,
    ERROR
}
