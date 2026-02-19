package com.jayma.pos.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.jayma.pos.data.local.converters.Converters
import com.jayma.pos.data.local.dao.*
import com.jayma.pos.data.local.entities.*

@Database(
    entities = [
        ProductEntity::class,
        ClientEntity::class,
        WarehouseEntity::class,
        CategoryEntity::class,
        BrandEntity::class,
        PaymentMethodEntity::class,
        SaleEntity::class,
        SaleDetailEntity::class,
        PaymentEntity::class,
        DraftEntity::class,
        DraftDetailEntity::class,
        SyncStatusEntity::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    
    abstract fun productDao(): ProductDao
    abstract fun clientDao(): ClientDao
    abstract fun saleDao(): SaleDao
    abstract fun draftDao(): DraftDao
    abstract fun syncStatusDao(): SyncStatusDao
    
    // Additional DAOs can be added here as needed
    // abstract fun warehouseDao(): WarehouseDao
    // abstract fun categoryDao(): CategoryDao
    // abstract fun brandDao(): BrandDao
    // abstract fun paymentMethodDao(): PaymentMethodDao
}
