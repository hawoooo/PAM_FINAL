package com.mobile.quickbite.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobile.quickbite.data.Order
import com.mobile.quickbite.data.TrackingData
import com.mobile.quickbite.network.RetrofitClient
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TrackingHistoryViewModel : ViewModel() {

    private val api = RetrofitClient.instance

    // State untuk List Riwayat (Dikelompokkan per Tanggal)
    // Map<"20 Agustus 2024", List<Order>>
    private val _historyState = MutableStateFlow<Map<String, List<Order>>>(emptyMap())
    val historyState = _historyState.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing = _isRefreshing.asStateFlow()

    // --- STATE UNTUK TRACKING ---
    private val _trackingState = MutableStateFlow<TrackingData?>(null)
    val trackingState = _trackingState.asStateFlow()

    private val _userLocation = MutableStateFlow<LatLng?>(null)
    val userLocation = _userLocation.asStateFlow()

    private val _etaState = MutableStateFlow("Menghitung...")
    val etaState = _etaState.asStateFlow()

    init {
        refreshData()
    }

    // --- FUNGSI 1: AMBIL DATA RIWAYAT ---
    fun refreshData() {
        viewModelScope.launch {
            _isRefreshing.value = true
            try {
                // 1. Ambil dari Firebase (Node "orders")
                val responseMap = api.getOrderHistory()

                // 2. Konversi Map ke List
                val ordersList = responseMap.map { (key, value) ->
                    value.copy(id = key)
                }.sortedByDescending { it.orderTime } // Urutkan dari yang terbaru

                // 3. Kelompokkan berdasarkan Tanggal (Untuk Sticky Header)
                val grouped = ordersList.groupBy { order ->
                    formatDate(order.orderTime)
                }

                _historyState.value = grouped

            } catch (e: Exception) {
                e.printStackTrace()
                _historyState.value = emptyMap()
            }
            _isRefreshing.value = false
        }
    }

    // --- FUNGSI 2: TRACKING LOGIC ---
    fun startTracking(orderId: String) {
        viewModelScope.launch {
            while (true) {
                try {
                    // Ambil data fresh dari API
                    val responseMap = api.getOrderHistory()
                    val order = responseMap[orderId]

                    if (order != null) {
                        val newTrackingData = TrackingData(
                            driverPos = LatLng(order.driverLat, order.driverLng),
                            restoPos = LatLng(order.restoLat, order.restoLng),
                            status = order.status
                        )
                        _trackingState.value = newTrackingData
                        calculateETA(newTrackingData.driverPos, newTrackingData.restoPos)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                delay(5000) // Update tiap 5 detik
            }
        }
    }

    fun updateUserLocation(lat: Double, lng: Double) {
        _userLocation.value = LatLng(lat, lng)
    }

    private fun calculateETA(driver: LatLng, dest: LatLng) {
        // Simulasi hitung jarak sederhana
        val results = FloatArray(1)
        android.location.Location.distanceBetween(
            driver.latitude, driver.longitude,
            dest.latitude, dest.longitude,
            results
        )
        val distanceMeters = results[0]
        val timeMinutes = (distanceMeters / 400).toInt() // Asumsi kecepatan rata-rata
        _etaState.value = "$timeMinutes menit lagi"
    }

    // Helper: Format Tanggal (Long -> String)
    private fun formatDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID"))
        return sdf.format(Date(timestamp))
    }
}