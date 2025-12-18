package com.mobile.quickbite.data

data class CartItem(
    val food: Food,
    var quantity: Int
) {
    // FIX: Gunakan food.price asli, bukan rating lagi
    val subtotal: Double
        get() = food.price * quantity
}