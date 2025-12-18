package com.mobile.quickbite.data

import android.util.Log
import com.mobile.quickbite.network.RetrofitClient

class SearchRepository {

    // Ambil akses ke Internet (Retrofit)
    private val api = RetrofitClient.instance

    // --- FUNGSI 1: AMBIL DATA MENU (Untuk MenuScreen) ---
    // Mengambil data dari node "restaurants" di Firebase
    suspend fun getFoodsFromFirebase(): List<Food> {
        return try {
            val responseMap = api.getMenu() // Panggil API getMenu

            // Ubah format dari Map (Key-Value) ke List (Daftar)
            responseMap.map { (key, value) ->
                value.copy(id = key)
            }
        } catch (e: Exception) {
            Log.e("SearchRepo", "Error ambil menu: ${e.message}")
            emptyList()
        }
    }

    // --- FUNGSI 2: BAYAR & SIMPAN ORDER (Untuk PaymentScreen) ---
    // Mengirim data ke node "orders" dan membuat Driver Palsu
    suspend fun submitOrder(cartItems: List<CartItem>, total: Double): String? {
        return try {
            // 1. Buat ID Order Unik (misal: ORD-1708263...)
            val orderId = "ORD-${System.currentTimeMillis()}"

            // 2. Ambil Lokasi Restoran dari makanan pertama di keranjang
            // (Kita asumsikan beli dari resto yang sama dulu)
            val firstFood = cartItems.first().food
            val restoLat = firstFood.restoLat
            val restoLng = firstFood.restoLng

            // 3. GENERATE POSISI DRIVER (Simulasi)
            // Driver muncul sedikit di sebelah restoran (+0.002 derajat)
            val driverLat = restoLat + 0.002
            val driverLng = restoLng + 0.002

            // 4. Siapkan Data Order Lengkap
            val newOrder = Order(
                id = orderId,
                items = cartItems,
                totalPrice = total,
                status = "PAID", // Status Lunas
                orderTime = System.currentTimeMillis(),

                // Masukkan Lokasi
                restoLat = restoLat,
                restoLng = restoLng,
                driverLat = driverLat,
                driverLng = driverLng
            )

            // 5. Kirim ke Firebase
            api.createOrder(orderId, newOrder)

            // 6. Kembalikan ID (Sukses)
            orderId
        } catch (e: Exception) {
            Log.e("SearchRepo", "Error submit order: ${e.message}")
            null // Gagal
        }
    }
}