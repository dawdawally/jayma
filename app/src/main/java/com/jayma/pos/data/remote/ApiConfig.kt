package com.jayma.pos.data.remote

object ApiConfig {
    // Production API base URL
    const val BASE_URL = "https://gmexperteng.com"
    
    // For localhost testing (development)
    // Change this in BuildConfig or use build variants
    const val BASE_URL_DEBUG = "http://10.0.2.2" // Android emulator localhost
    
    // API Endpoints
    const val ENDPOINT_POS_DATA = "pos_data.php"
    const val ENDPOINT_PRODUCTS = "products.php"
    const val ENDPOINT_CLIENTS = "clients.php"
    const val ENDPOINT_CREATE_SALE = "create_sale.php"
    const val ENDPOINT_DRAFTS = "drafts.php"
    const val ENDPOINT_SALES = "sales.php"
}
