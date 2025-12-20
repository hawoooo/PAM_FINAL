package com.mobile.quickbite.data

// --- LIBRARY FIREBASE
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

// --- LIBRARY GOOGLE MAPS ---
import com.google.android.gms.maps.model.LatLng

// --- LIBRARY RETROFIT ---
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
        try { FirebaseDatabase.getInstance().getReference("orders") } catch (_: Exception) { null } // FIX: e -> _
    }
    private val api = RetrofitClient.instance

    // =================================================================
    // FUNGSI 1: AMBIL HISTORY LIST (BERSIH DARI LOGIKA OTOMATIS)
    // =================================================================
    suspend fun getOrderHistory(): List<Order> = withContext(Dispatchers.IO) {
        try {
            // 1. Ambil data mentah dari Firebase
            val responseMap = api.getOrderHistory()

            // 2. Konversi Map ke List
            val updatedList = responseMap.map { (key, value) ->
                value.copy(id = key)
            }

            // 3. Urutkan dari yang terbaru (descending)
            return@withContext updatedList.sortedByDescending { it.orderTime }

        } catch (_: Exception) { // FIX: e -> _
            return@withContext emptyList()
        }
    }

    // =================================================================
    // FUNGSI 2: TRACKING LOKASI
    // =================================================================
    @Suppress("unused") // FIX: Suppress warning karena fungsi ini belum dipanggil di ViewModel saat ini
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
        } catch (e: Exception) { close(e) } // e digunakan di sini untuk close(e), jadi biarkan.

        awaitClose {
            try { database.child(orderId).removeEventListener(listener) } catch (_: Exception) {} // FIX: e -> _
        }
    }
}