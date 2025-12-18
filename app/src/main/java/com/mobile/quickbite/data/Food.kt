package com.mobile.quickbite.data

data class Food(
    val id: String = "",
    val name: String = "",
    val category: String = "", // Nama Restoran
    val price: Double = 0.0,
    val rating: Double = 0.0,
    val imageUrl: String = "",
    val description: String = "",
    val restoLat: Double = 0.0,
    val restoLng: Double = 0.0
)