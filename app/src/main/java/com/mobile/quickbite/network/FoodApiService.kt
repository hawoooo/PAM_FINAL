package com.mobile.quickbite.network

import com.mobile.quickbite.data.Food
import com.mobile.quickbite.data.Order
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Path

interface FoodApiService {

    // 1. Ambil Menu dari node "restaurants"
    @GET("restaurants.json")
    suspend fun getMenu(): Map<String, Food>

    // 2. Ambil Riwayat dari node "orders"
    @GET("orders.json")
    suspend fun getOrderHistory(): Map<String, Order>

    // 3. Simpan Order Baru ke node "orders/{orderId}"
    // Kita pakai PUT agar bisa menentukan ID sendiri (misal berdasarkan timestamp)
    @PUT("orders/{orderId}.json")
    suspend fun createOrder(
        @Path("orderId") orderId: String,
        @Body order: Order
    ): Order
}