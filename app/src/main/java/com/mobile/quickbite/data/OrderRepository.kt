package com.mobile.quickbite.data

// --- LIBRARY FIREBASE (Untuk Tracking Real-time) ---
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

    // Setup Database Firebase (Hanya dipakai untuk Tracking)
    private val db by lazy {
        try {
            FirebaseDatabase.getInstance().getReference("orders")
        } catch (e: Exception) {
            null
        }
    }

    // =================================================================
    // FUNGSI 1: AMBIL HISTORY LIST (MENGGUNAKAN RETROFIT)
    // =================================================================
    // Memenuhi Syarat: Retrofit, GSON, List JSON, Threading
    suspend fun getOrderHistory(): List<Order> = withContext(Dispatchers.IO) {
        try {
            // 1. Request HTTP GET ke Firebase via Retrofit
            val responseMap = RetrofitClient.instance.getOrderHistory()

            // 2. Konversi Map JSON ke List<Order>
            val ordersList = responseMap.map { (key, value) ->
                value.copy(id = key) // Masukkan Key Firebase sebagai ID
            }
            return@withContext ordersList
        } catch (e: Exception) {
            // Jika error/offline, kembalikan list kosong (nanti ViewModel load dummy)
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
                val rLat = snapshot.child("restaurantLat").value.toString().toDoubleOrNull() ?: 0.0
                val rLng = snapshot.child("restaurantLng").value.toString().toDoubleOrNull() ?: 0.0
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