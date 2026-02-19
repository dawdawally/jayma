package com.jayma.pos.database

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.jayma.pos.data.local.AppDatabase
import com.jayma.pos.data.local.entities.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AppDatabaseTest {
    
    private lateinit var database: AppDatabase
    
    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()
    }
    
    @After
    fun tearDown() {
        database.close()
    }
    
    @Test
    fun insertAndRetrieveProduct() = runTest {
        // Given
        val product = ProductEntity(
            id = 1,
            productVariantId = 1,
            code = "P001",
            name = "Test Product",
            barcode = "123456789",
            image = null,
            qte = 100.0,
            qteSale = 100.0,
            unitSale = "pcs",
            productType = "standard",
            netPrice = 10.0,
            synced = true
        )
        
        // When
        database.productDao().insertProduct(product)
        val retrieved = database.productDao().getProductById(1)
        
        // Then
        assertNotNull(retrieved)
        assertEquals("Test Product", retrieved?.name)
        assertEquals("P001", retrieved?.code)
    }
    
    @Test
    fun insertAndRetrieveSale() = runTest {
        // Given
        val sale = SaleEntity(
            localId = 1,
            serverId = null,
            clientId = 1,
            warehouseId = 1,
            taxRate = 10.0,
            taxNet = 10.0,
            discount = 0.0,
            shipping = 0.0,
            grandTotal = 110.0,
            notes = null,
            synced = false,
            createdAt = System.currentTimeMillis()
        )
        
        // When
        database.saleDao().insertSale(sale)
        val retrieved = database.saleDao().getSaleByLocalId(1)
        
        // Then
        assertNotNull(retrieved)
        assertEquals(110.0, retrieved?.grandTotal, 0.01)
        assertFalse(retrieved?.synced ?: true)
    }
    
    @Test
    fun getUnsyncedSales() = runTest {
        // Given
        val syncedSale = SaleEntity(
            localId = 1,
            serverId = 100,
            clientId = 1,
            warehouseId = 1,
            synced = true,
            grandTotal = 100.0,
            createdAt = System.currentTimeMillis()
        )
        val unsyncedSale = SaleEntity(
            localId = 2,
            serverId = null,
            clientId = 1,
            warehouseId = 1,
            synced = false,
            grandTotal = 200.0,
            createdAt = System.currentTimeMillis()
        )
        
        // When
        database.saleDao().insertSale(syncedSale)
        database.saleDao().insertSale(unsyncedSale)
        val unsynced = database.saleDao().getUnsyncedSales()
        
        // Then
        assertEquals(1, unsynced.size)
        assertEquals(2L, unsynced[0].localId)
    }
    
    @Test
    fun searchProductsByCode() = runTest {
        // Given
        val product1 = ProductEntity(
            id = 1,
            productVariantId = 1,
            code = "P001",
            name = "Product 1",
            barcode = "BAR001",
            qte = 100.0,
            qteSale = 100.0,
            unitSale = "pcs",
            productType = "standard",
            netPrice = 10.0,
            synced = true
        )
        val product2 = ProductEntity(
            id = 2,
            productVariantId = 2,
            code = "P002",
            name = "Product 2",
            barcode = "BAR002",
            qte = 100.0,
            qteSale = 100.0,
            unitSale = "pcs",
            productType = "standard",
            netPrice = 20.0,
            synced = true
        )
        
        // When
        database.productDao().insertProduct(product1)
        database.productDao().insertProduct(product2)
        val found = database.productDao().getProductByCode("P001")
        
        // Then
        assertNotNull(found)
        assertEquals("Product 1", found?.name)
    }
}
