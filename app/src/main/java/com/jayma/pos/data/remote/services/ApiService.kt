package com.jayma.pos.data.remote.services

import com.google.gson.annotations.SerializedName
import com.jayma.pos.data.remote.models.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    
    // POS Setup
    @GET("pos_data.php")
    suspend fun getPosData(): Response<PosDataResponse>
    
    // Products
    @GET("products.php")
    suspend fun getProducts(
        @Query("warehouse_id") warehouseId: Int,
        @Query("page") page: Int = 1,
        @Query("category_id") categoryId: Int? = null,
        @Query("brand_id") brandId: Int? = null,
        @Query("stock") stock: Int? = null, // 1 = only in-stock
        @Query("product_combo") productCombo: Int? = null, // 1 = include combos
        @Query("product_service") productService: Int? = null // 1 = include services
    ): Response<ProductListResponse>
    
    // Clients
    @GET("clients.php")
    suspend fun getClients(): Response<List<ClientResponse>>
    
    @POST("clients.php")
    @Headers("Content-Type: application/json")
    suspend fun createClient(
        @Body request: CreateClientRequest
    ): Response<CreateClientResponse>
    
    // Sales
    @POST("create_sale.php")
    @Headers("Content-Type: application/json")
    suspend fun createSale(
        @Body request: CreateSaleRequest
    ): Response<CreateSaleResponse>
    
    @GET("sales.php")
    suspend fun getSales(
        @Query("limit") limit: Int? = null,
        @Query("page") page: Int? = null,
        @Query("search") search: String? = null,
        @Query("SortField") sortField: String? = null,
        @Query("SortType") sortType: String? = null
    ): Response<Any> // Response structure may vary
    
    @GET("sales.php")
    suspend fun getTodaySummary(
        @Query("today") today: Int = 1
    ): Response<Any> // Today's summary response
    
    @GET("sales.php")
    suspend fun getSaleProducts(
        @Query("products") saleId: Int
    ): Response<Any> // Sale line items
    
    @GET("sales.php")
    suspend fun getSalePayments(
        @Query("payments") saleId: Int
    ): Response<Any> // Sale payments
    
    // Drafts
    @GET("drafts.php")
    suspend fun getDrafts(
        @Query("limit") limit: Int? = null,
        @Query("page") page: Int? = null
    ): Response<Any>
    
    @GET("drafts.php")
    suspend fun getDraft(
        @Query("id") id: Int
    ): Response<Any>
    
    @POST("drafts.php")
    @Headers("Content-Type: application/json")
    suspend fun createDraft(
        @Body request: Any // Draft request (similar to CreateSaleRequest but without payments)
    ): Response<CreateClientResponse>
    
    @POST("drafts.php")
    @Headers("Content-Type: application/json")
    suspend fun submitDraft(
        @Body request: SubmitDraftRequest
    ): Response<CreateSaleResponse>
    
    @DELETE("drafts.php")
    suspend fun deleteDraft(
        @Query("id") id: Int
    ): Response<CreateClientResponse>
}

data class SubmitDraftRequest(
    @SerializedName("draft_sale_id")
    val draftSaleId: Int,
    val amount: Double,
    val payment: PaymentRequest
)
