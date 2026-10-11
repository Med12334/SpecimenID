package com.specimenid.domain.network

import retrofit2.http.GET
import retrofit2.http.Path

// The JSON models returned by the API
data class ProductResponse(
    val status: Int,
    val product: ProductDetails?
)

data class ProductDetails(
    val product_name: String?,
    val ingredients_text: String?
)

// The Retrofit Interface
interface OpenFoodFactsApi {
    @GET("api/v0/product/{barcode}.json")
    suspend fun getProductByBarcode(@Path("barcode") barcode: String): ProductResponse
}
