package com.mobile.quickbite.data

// --- LIBRARY FIREBASE
import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

// --- LIBRARY GOOGLE MAPS ---
import com.google.android.gms.maps.model.LatLng

// --- LIBRARY RETROFIT (Untuk History List - Syarat Tugas) ---
import com.mobile.quickbite.network.RetrofitClient

// --- LIBRARY COROUTINES ---
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.withContext

class OrderRepository {

    // Setup Database Firebase
    private val db by lazy {
        try { FirebaseDatabase.getInstance().getReference("orders") } catch (e: Exception) { null }
    }
    private val api = RetrofitClient.instance

    // =================================================================
    // FUNGSI 1: AMBIL HISTORY LIST (MENGGUNAKAN RETROFIT)
    // =================================================================
    // Memenuhi Syarat: Retrofit, GSON, List JSON, Threading
    suspend fun getOrderHistory(): List<Order> = withContext(Dispatchers.IO) {
        try {
            // 1. Ambil data mentah dari Firebase
            val responseMap = api.getOrderHistory()
            val currentTime = System.currentTimeMillis()

            val updatedList = responseMap.map { (key, value) ->
                var order = value.copy(id = key)

                // --- LOGIKA WAKTU OTOMATIS DISINI ---
                val timeDiff = currentTime - order.orderTime

                // ATURAN 1: DIKEMAS -> DIANTAR
                // Cek jika statusnya DIKEMAS (Indo) ATAU PAID (Inggris/Lama)
                if ((order.status == "DIKEMAS" || order.status == "PAID") && timeDiff > 30000) {
                    try {
                        // Kita paksa update ke format BARU (DIANTAR)
                        api.updateOrderStatus(key, mapOf("status" to "DIANTAR"))
                        order = order.copy(status = "DIANTAR")
                    } catch (e: Exception) { Log.e("Repo", "Gagal update Diantar") }
                }

                // ATURAN 2: DIANTAR -> SELESAI
                // Cek jika statusnya DIANTAR (Indo) ATAU ON_DELIVERY (Inggris)
                if ((order.status == "DIANTAR" || order.status == "ON_DELIVERY") && timeDiff > 60000) {
                    try {
                        // Kita paksa update ke format BARU (SELESAI)
                        api.updateOrderStatus(key, mapOf("status" to "SELESAI"))
                        order = order.copy(status = "SELESAI")
                    } catch (e: Exception) { Log.e("Repo", "Gagal update Selesai") }
                }
                // -------------------------------------

                order
            }
            // Urutkan dari yang terbaru (descending)
            return@withContext updatedList.sortedByDescending { it.orderTime }

        } catch (e: Exception) {
            return@withContext emptyList()
        }
    }

    // =================================================================
    // FUNGSI 2: TRACKING LOKASI (MENGGUNAKAN FIREBASE SDK)
    // =================================================================
    // Tetap Real-time agar peta bergerak mulus
    fun trackOrderLocation(orderId: String): Flow<TrackingData> = callbackFlow {
        val database = db
        if (database == null) {
            close(Exception("Firebase not initialized"))
            return@callbackFlow
        }

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val dLat = snapshot.child("driverLat").value.toString().toDoubleOrNull() ?: 0.0
                val dLng = snapshot.child("driverLng").value.toString().toDoubleOrNull() ?: 0.0
                val rLat = snapshot.child("restoLat").value.toString().toDoubleOrNull() ?: 0.0
                val rLng = snapshot.child("restoLng").value.toString().toDoubleOrNull() ?: 0.0
                val status = snapshot.child("status").value.toString()

                trySend(
                    TrackingData(
                        driverPos = LatLng(dLat, dLng),
                        restoPos = LatLng(rLat, rLng),
                        status = status
                    )
                )
            }
            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }

        try {
            database.child(orderId).addValueEventListener(listener)
        } catch (e: Exception) { close(e) }

        awaitClose {
            try { database.child(orderId).removeEventListener(listener) } catch (e: Exception) {}
        }
    }
}