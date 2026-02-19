package com.jayma.pos.ui.cart

import android.content.Context
import com.jayma.pos.data.local.entities.SaleEntity
import com.jayma.pos.data.repository.SaleRepository
import com.jayma.pos.util.printer.PrinterService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReceiptPrintHelper @Inject constructor(
    private val saleRepository: SaleRepository
) {
    
    suspend fun printReceiptForSale(
        context: Context,
        saleLocalId: Long,
        storeName: String = "Jayma POS",
        storeAddress: String = "",
        storePhone: String = ""
    ): com.jayma.pos.util.printer.PrintResult {
        return withContext(Dispatchers.IO) {
            val sale = saleRepository.getSaleByLocalId(saleLocalId)
            if (sale == null) {
                return@withContext com.jayma.pos.util.printer.PrintResult(
                    false,
                    "Sale not found"
                )
            }
            
            val saleDetails = saleRepository.getSaleDetails(saleLocalId)
            val payments = saleRepository.getPayments(saleLocalId)
            
            val printerService = PrinterService(context)
            printerService.printReceipt(
                sale = sale,
                saleDetails = saleDetails,
                payments = payments,
                storeName = storeName,
                storeAddress = storeAddress,
                storePhone = storePhone
            )
        }
    }
}
