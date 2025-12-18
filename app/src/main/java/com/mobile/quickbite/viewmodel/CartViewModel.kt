package com.mobile.quickbite.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobile.quickbite.data.CartItem
import com.mobile.quickbite.data.Food
import com.mobile.quickbite.data.SearchRepository // Pastikan import ini ada
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CartViewModel : ViewModel() {

    // --- BAGIAN INI YANG KURANG ---
    // Kita harus membuat instance Repository agar bisa akses submitOrder
    private val repository = SearchRepository()
    // -----------------------------

    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    val cartItems = _cartItems.asStateFlow()

    // Fungsi Tambah ke Keranjang
    fun addToCart(food: Food, quantity: Int) {
        val currentList = _cartItems.value.toMutableList()
        val existingItem = currentList.find { it.food.id == food.id }

        if (existingItem != null) {
            existingItem.quantity += quantity
        } else {
            currentList.add(CartItem(food, quantity))
        }
        _cartItems.value = currentList
    }

    // Fungsi Update Jumlah
    fun updateQuantity(item: CartItem, newQuantity: Int) {
        if (newQuantity <= 0) {
            removeItem(item)
            return
        }
        val currentList = _cartItems.value.toMutableList()
        val index = currentList.indexOfFirst { it.food.id == item.food.id }
        if (index != -1) {
            currentList[index] = item.copy(quantity = newQuantity)
            _cartItems.value = currentList
        }
    }

    // Fungsi Hapus Item
    fun removeItem(item: CartItem) {
        val currentList = _cartItems.value.toMutableList()
        currentList.removeIf { it.food.id == item.food.id }
        _cartItems.value = currentList
    }

    // Hitung Total Harga
    fun getCartTotal(): Double {
        return _cartItems.value.sumOf { it.subtotal }
    }

    // Kosongkan Keranjang
    fun clearCart() {
        _cartItems.value = emptyList()
    }

    // --- FUNGSI CHECKOUT KE FIREBASE ---
    fun checkoutToFirebase(onSuccess: () -> Unit) {
        viewModelScope.launch {
            if (_cartItems.value.isNotEmpty()) {
                val total = getCartTotal()

                // Panggil fungsi submitOrder milik repository
                val resultId = repository.submitOrder(_cartItems.value, total)

                if (resultId != null) {
                    clearCart() // Kosongkan keranjang di UI
                    onSuccess() // Pindah ke layar History
                }
            }
        }
    }
}