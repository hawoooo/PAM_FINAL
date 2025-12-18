package com.mobile.quickbite.data

data class Order(
    val id: String = "",
    val items: List<CartItem> = emptyList(),
    val totalPrice: Double = 0.0,
    val status: String = "PENDING", // PENDING, PAID, COMPLETED
    val orderTime: Long = System.currentTimeMillis(),

    // Info Lokasi (Dibuat saat Checkout/Bayar)
    val driverLat: Double = 0.0,
    val driverLng: Double = 0.0,
    val restoLat: Double = 0.0, // Diambil dari Food pertama
    val restoLng: Double = 0.0
)