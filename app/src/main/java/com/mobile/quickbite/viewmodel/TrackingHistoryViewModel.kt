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

    // LOGIKA FILTERING UTAMA
    // Menggabungkan data history mentah dengan status filter yang dipilih user
    val filteredHistory = _historyState.combine(_filterStatus) { historyMap, filter ->
        if (filter == null) {
            // Tab SEMUA: Tampilkan apa adanya
            historyMap
        } else {
            // Tab SPESIFIK: Filter list di dalam map berdasarkan status
            historyMap.mapValues { (_, orders) ->
                orders.filter { it.status == filter }
            }.filterValues { it.isNotEmpty() } // Hapus tanggal yang kosong setelah difilter
        }
    }

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
        // Panggil refresh sekali saat awal (showLoading = true biar user tau lagi loading)
        refreshData(showLoading = true)

        // Mulai Timer Otomatis
        startAutoRefresh()
    }

    // TIMER OTOMATIS ---
    private fun startAutoRefresh() {
        viewModelScope.launch {
            // Loop selamanya selama halaman ini masih dibuka (isActive)
            while (isActive) {
                delay(5000) // Tunggu 5 Detik


                refreshData(showLoading = false)
            }
        }
    }

    // --- FUNGSI 1: AMBIL DATA RIWAYAT & UPDATE STATUS OTOMATIS ---
    // Parameter showLoading ditambahkan agar kita bisa mengontrol kapan spinner muncul
    fun refreshData(showLoading: Boolean = true) {
        viewModelScope.launch {
            // Hanya tampilkan loading jika diminta (misal: saat awal buka atau tarik layar)
            if (showLoading) _isRefreshing.value = true

            try {

                val ordersList = repository.getOrderHistory()

                // 2. Kelompokkan berdasarkan Tanggal (Untuk Sticky Header UI)
                val grouped = ordersList.groupBy { order ->
                    formatDate(order.orderTime)
                }

                _historyState.value = grouped

            } catch (e: Exception) {
                e.printStackTrace()
                _historyState.value = emptyMap()
            }

            // Matikan loading jika tadi dinyalakan
            if (showLoading) _isRefreshing.value = false
        }
    }

    // --- FUNGSI 2: TRACKING LOGIC ---
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
                        calculateETA(newTrackingData.driverPos, newTrackingData.restoPos)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                delay(3000) // Update tiap 3 detik agar gerakan driver lebih halus
            }
        }
    }

    fun updateUserLocation(lat: Double, lng: Double) {
        _userLocation.value = LatLng(lat, lng)
    }

    private fun calculateETA(driver: LatLng, dest: LatLng) {
        val results = FloatArray(1)
        android.location.Location.distanceBetween(
            driver.latitude, driver.longitude,
            dest.latitude, dest.longitude,
            results
        )
        val distanceMeters = results[0]
        val timeMinutes = (distanceMeters / 400).toInt()
        _etaState.value = if (timeMinutes <= 0) "Tiba sebentar lagi" else "$timeMinutes menit lagi"
    }

    private fun formatDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID"))
        return sdf.format(Date(timestamp))
    }

    fun setFilter(status: String?) {
        _filterStatus.value = status
    }
}