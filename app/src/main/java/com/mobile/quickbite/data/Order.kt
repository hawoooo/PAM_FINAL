package com.mobile.quickbite.data

data class Order(
    val id: String = "",
    val items: List<CartItem> = emptyList(),
    val totalPrice: Double = 0.0,
    // Kita standarisasi statusnya: "BELUM_BAYAR", "DIKEMAS", "DIANTAR", "SELESAI"
    val status: String = "BELUM_BAYAR",
    val orderTime: Long = System.currentTimeMillis(),

    // Info Lokasi
    val driverLat: Double = 0.0,
    val driverLng: Double = 0.0,
    val restoLat: Double = 0.0,
    val restoLng: Double = 0.0
)