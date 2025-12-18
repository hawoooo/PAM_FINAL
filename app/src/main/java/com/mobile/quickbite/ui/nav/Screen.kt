package com.mobile.quickbite.ui.nav

import java.net.URLEncoder
import java.nio.charset.StandardCharsets

sealed class Screen(val route: String) {
    object Menu : Screen("menu")
    object Cart : Screen("cart")
    object Payment : Screen("payment")
    object History : Screen("history")
    object Search : Screen("search") // <-- RUTE BARU DITAMBAHKAN

    object Tracking : Screen("tracking/{orderId}") {
        fun createRoute(orderId: String) = "tracking/$orderId"
    }

    object SearchFood : Screen("search_food")
    object FoodResult : Screen("food_result/{query}") {
        fun createRoute(query: String) = "food_result/$query"
    }

    // --- PERBAIKAN DI SINI (DETAIL SCREEN) ---
    // Kita tambah {price} dan {imageUrl} di URL
    object Detail : Screen("detail/{id}/{name}/{category}/{price}/{rating}/{imageUrl}") {
        fun createRoute(
            id: String,
            name: String,
            category: String,
            price: Double,   // Parameter Baru
            rating: Double,
            imageUrl: String // Parameter Baru
        ): String {
            // PENTING: Encode URL Gambar agar karakter '/' tidak merusak navigasi
            val encodedUrl = URLEncoder.encode(imageUrl, StandardCharsets.UTF_8.toString())

            return "detail/$id/$name/$category/$price/$rating/$encodedUrl"
        }
    }
}