package com.mobile.quickbite.network

import com.mobile.quickbite.data.Food
import com.mobile.quickbite.data.Order
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.PUT
import retrofit2.http.Path

interface FoodApiService {

    @GET("restaurants.json")
    suspend fun getMenu(): Map<String, Food>

    @GET("orders.json")
    suspend fun getOrderHistory(): Map<String, Order>

    @PUT("orders/{orderId}.json")
    suspend fun createOrder(
        @Path("orderId") orderId: String,
        @Body order: Order
    ): Order

    // Fungsi untuk update status saja (PATCH)
    @PATCH("orders/{orderId}.json")
    suspend fun updateOrderStatus(
        @Path("orderId") orderId: String,
        @Body statusMap: Map<String, String>
    ): Order
}