package com.invenscan.app.data.remote

import com.invenscan.app.data.model.ApiResponse
import com.invenscan.app.data.model.AuthRequest
import com.invenscan.app.data.model.AuthResponse
import com.invenscan.app.data.model.ItemModel
import com.invenscan.app.data.model.LocationModel
import com.invenscan.app.data.model.SearchItemModel
import com.invenscan.app.data.model.StockInBulkInfoRequest
import com.invenscan.app.data.model.StockInSubmitRequest
import com.invenscan.app.data.model.StockInTagInfo
import com.invenscan.app.data.model.StockOutBulkInfoRequest
import com.invenscan.app.data.model.StockOutSubmitRequest
import com.invenscan.app.data.model.StockOutTagInfo
import com.invenscan.app.data.model.StockPrepDetailModel
import com.invenscan.app.data.model.StockPrepModel
import com.invenscan.app.data.model.StockPrepSubmitRequest
import com.invenscan.app.data.model.StockTakingModel
import com.invenscan.app.data.model.StockTakingSubmitRequest
import com.invenscan.app.data.model.StockTakingTagModel
import com.invenscan.app.data.model.TagModel
import com.invenscan.app.data.model.TagRegisterRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {

    // ── Auth ──────────────────────────────────────────────────────

    @POST("api/auth/login")
    suspend fun login(@Body request: AuthRequest): Response<ApiResponse<AuthResponse>>

    @POST("api/auth/refresh")
    suspend fun refreshToken(@Body body: Map<String, String>): Response<ApiResponse<AuthResponse>>

    // ── Health Check ──────────────────────────────────────────────

    @GET("api/ping")
    suspend fun ping(): Response<ApiResponse<String>>

    // ── Items ─────────────────────────────────────────────────────

    @GET("api/item")
    suspend fun getItems(): Response<ApiResponse<List<ItemModel>>>

    @GET("api/item/{id}")
    suspend fun getItemById(@Path("id") id: Long): Response<ApiResponse<ItemModel>>

    // ── Locations ─────────────────────────────────────────────────

    @GET("api/location")
    suspend fun getLocations(): Response<ApiResponse<List<LocationModel>>>

    // ── Tags ──────────────────────────────────────────────────────

    @GET("api/tag/{id}")
    suspend fun getTagById(@Path("id") id: String): Response<ApiResponse<TagModel>>

    @POST("api/tag/register")
    suspend fun registerTags(@Body request: TagRegisterRequest): Response<ApiResponse<Unit>>

    @POST("api/tag/reregister")
    suspend fun reregisterTag(@Body body: Map<String, String>): Response<ApiResponse<Unit>>

    // ── Stock In ──────────────────────────────────────────────────

    @GET("api/stockin")
    suspend fun lookupStockInTag(
        @Query("code") code: String,
        @Query("scannerType") scannerType: String
    ): Response<ApiResponse<StockInTagInfo>>

    @POST("api/stockin")
    suspend fun submitStockIn(@Body request: StockInSubmitRequest): Response<ApiResponse<Unit>>

    @POST("api/stockin/bulk-info")
    suspend fun getStockInBulkInfo(@Body request: StockInBulkInfoRequest): Response<ApiResponse<List<StockInTagInfo>>>

    // ── Stock Out ─────────────────────────────────────────────────

    @GET("api/stockout")
    suspend fun lookupStockOutTag(
        @Query("code") code: String,
        @Query("scannerType") scannerType: String
    ): Response<ApiResponse<StockOutTagInfo>>

    @POST("api/stockout")
    suspend fun submitStockOut(@Body request: StockOutSubmitRequest): Response<ApiResponse<Unit>>

    @POST("api/stockout/bulk-info")
    suspend fun getStockOutBulkInfo(@Body request: StockOutBulkInfoRequest): Response<ApiResponse<List<StockOutTagInfo>>>

    // ── Stock Taking ──────────────────────────────────────────────

    @POST("api/stock-taking")
    suspend fun createStockTakingSession(@Body body: Map<String, Any>): Response<ApiResponse<StockTakingModel>>

    @GET("api/stock-taking")
    suspend fun getStockTakingSessions(): Response<ApiResponse<List<StockTakingModel>>>

    @GET("api/stock-taking/active")
    suspend fun getActiveStockTakingSession(@Query("locationId") locationId: Long): Response<ApiResponse<StockTakingModel>>

    @GET("api/stock-taking/tags/{sttId}")
    suspend fun getStockTakingTags(@Path("sttId") sttId: Long): Response<ApiResponse<List<StockTakingTagModel>>>

    @GET("api/stock-taking/available-tags/{sttId}")
    suspend fun getAvailableTagsForSession(@Path("sttId") sttId: Long): Response<ApiResponse<List<StockTakingTagModel>>>

    @POST("api/stock-taking/operator-submit")
    suspend fun submitStockTakingResults(@Body request: StockTakingSubmitRequest): Response<ApiResponse<Unit>>

    // ── Stock Prep ────────────────────────────────────────────────

    @GET("api/stockprep")
    suspend fun getStockPrepList(): Response<ApiResponse<List<StockPrepModel>>>

    @GET("api/stockprep/{id}")
    suspend fun getStockPrepDetail(@Path("id") id: Long): Response<ApiResponse<StockPrepDetailModel>>

    @POST("api/stockprep/bulk")
    suspend fun submitStockPrepBulk(@Body request: StockPrepSubmitRequest): Response<ApiResponse<Unit>>

    // ── Search Item ────────────────────────────────────────────────

    @GET("api/search-item")
    suspend fun getAllItemsForCache(): Response<ApiResponse<List<SearchItemModel>>>

    @GET("api/search-item/{code}")
    suspend fun searchItemByCode(@Path("code") code: String): Response<ApiResponse<SearchItemModel>>
}
