package com.mobile.quickbite.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobile.quickbite.data.Order
import com.mobile.quickbite.data.TrackingData
import com.mobile.quickbite.network.RetrofitClient
import com.google.android.gms.maps.model.LatLng
import com.mobile.quickbite.data.OrderRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.coroutines.isActive
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TrackingHistoryViewModel : ViewModel() {

    private val repository = OrderRepository()
    private val api = RetrofitClient.instance

    // State untuk Filter Status (Default: null artinya "Semua")
    private val _filterStatus = MutableStateFlow<String?>(null)
    val filterStatus: StateFlow<String?> = _filterStatus

    // State untuk List Riwayat (Dikelompokkan per Tanggal)
    private val _historyState = MutableStateFlow<Map<String, List<Order>>>(emptyMap())

    val filteredHistory = _historyState.combine(_filterStatus) { historyMap, filter ->
        if (filter == null) {
            historyMap
        } else {
            historyMap.mapValues { (_, orders) ->
                orders.filter { it.status == filter }
            }.filterValues { it.isNotEmpty() }
        }
    }

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing = _isRefreshing.asStateFlow()

    // --- STATE UNTUK TRACKING ---
    private val _trackingState = MutableStateFlow<TrackingData?>(null)
    val trackingState = _trackingState.asStateFlow()

    private val _userLocation = MutableStateFlow<LatLng?>(null)
    val userLocation = _userLocation.asStateFlow()

    private val _etaState = MutableStateFlow("Menghitung Estimasi...")
    val etaState = _etaState.asStateFlow()

    init {
        refreshData(showLoading = true)
        startAutoRefresh()
    }

    private fun startAutoRefresh() {
        viewModelScope.launch {
            while (isActive) {
                delay(5000)
                refreshData(showLoading = false)
            }
        }
    }

    fun refreshData(showLoading: Boolean = true) {
        viewModelScope.launch {
            if (showLoading) _isRefreshing.value = true
            try {
                val ordersList = repository.getOrderHistory()
                val grouped = ordersList.groupBy { order ->
                    formatDate(order.orderTime)
                }
                _historyState.value = grouped
            } catch (e: Exception) {
                e.printStackTrace()
                _historyState.value = emptyMap()
            }
            if (showLoading) _isRefreshing.value = false
        }
    }

    // --- FUNGSI TRACKING ---
    fun startTracking(orderId: String) {
        viewModelScope.launch {
            while (isActive) {
                try {
                    val responseMap = api.getOrderHistory()
                    val order = responseMap[orderId]

                    if (order != null) {
                        val newTrackingData = TrackingData(
                            driverPos = LatLng(order.driverLat, order.driverLng),
                            restoPos = LatLng(order.restoLat, order.restoLng),
                            status = order.status
                        )
                        _trackingState.value = newTrackingData

                        // FIX: Hitung ETA dari Driver ke User Location
                        val currentUserLoc = _userLocation.value
                        if (currentUserLoc != null) {
                            if (order.status == "DIANTAR" || order.status == "ON_DELIVERY") {
                                calculateETA(newTrackingData.driverPos, currentUserLoc)
                            } else {
                                _etaState.value = "Status: ${order.status}"
                            }
                        } else {
                            _etaState.value = "Menunggu lokasi Anda..."
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                delay(3000)
            }
        }
    }

    fun updateUserLocation(lat: Double, lng: Double) {
        _userLocation.value = LatLng(lat, lng)
    }

    // --- FIX LOGIKA ETA (1 KM = 10 MENIT) ---
    private fun calculateETA(driver: LatLng, user: LatLng) {
        val results = FloatArray(1)
        android.location.Location.distanceBetween(
            driver.latitude, driver.longitude,
            user.latitude, user.longitude,
            results
        )
        val distanceMeters = results[0]
        val distanceKm = distanceMeters / 1000

        // Rumus: 1 km = 10 menit
        val timeMinutes = (distanceKm * 10).toInt()

        if (timeMinutes < 1) {
            _etaState.value = "Driver sudah dekat!"
        } else {
            // Menggunakan Locale.getDefault() untuk format angka desimal yang sesuai region pengguna
            // dan menghilangkan warning "implicitly using default locale"
            _etaState.value = "Estimasi: $timeMinutes menit (${String.format(Locale.getDefault(), "%.1f", distanceKm)} km)"
        }
    }

    private fun formatDate(timestamp: Long): String {
        // Menggunakan Locale.forLanguageTag("id-ID") untuk format tanggal Indonesia yang benar
        val sdf = SimpleDateFormat("dd MMMM yyyy", Locale.forLanguageTag("id-ID"))
        return sdf.format(Date(timestamp))
    }

    fun setFilter(status: String?) {
        _filterStatus.value = status
    }
}