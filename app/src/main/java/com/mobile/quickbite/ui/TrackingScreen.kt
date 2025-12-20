package com.mobile.quickbite.ui

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import com.mobile.quickbite.viewmodel.TrackingHistoryViewModel

@Composable
fun TrackingScreen(
    orderId: String,
    viewModel: TrackingHistoryViewModel
) {
    // 1. Observasi State dari ViewModel
    val trackingData by viewModel.trackingState.collectAsState()
    val userLocation by viewModel.userLocation.collectAsState()
    val etaText by viewModel.etaState.collectAsState() // State teks ETA

    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    // Posisi Kamera Awal (Malang)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(-7.955, 112.615), 14f)
    }

    // Handler Izin Lokasi
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            try {
                fusedLocationClient.lastLocation.addOnSuccessListener { loc ->
                    if (loc != null) viewModel.updateUserLocation(loc.latitude, loc.longitude)
                }
            } catch (_: SecurityException) {} // FIX: Mengganti 'e' dengan '_' karena tidak digunakan
        }
    }

    // Effect: Mulai Tracking & Cek Lokasi User saat layar dibuka
    LaunchedEffect(Unit) {
        viewModel.startTracking(orderId)

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.lastLocation.addOnSuccessListener { loc ->
                if (loc != null) viewModel.updateUserLocation(loc.latitude, loc.longitude)
            }
        } else {
            launcher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    // Effect: Animasi kamera mengikuti Driver jika sedang diantar
    LaunchedEffect(trackingData) {
        trackingData?.let {
            // Hanya pindah kamera otomatis jika driver bergerak
            cameraPositionState.animate(CameraUpdateFactory.newLatLng(it.driverPos))
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // --- BAGIAN PETA (Weight 1f agar memenuhi sisa layar) ---
        Box(modifier = Modifier.weight(1f)) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState
            ) {
                trackingData?.let { data ->
                    // MARKER DRIVER (Hijau) - Munculkan ETA saat diklik
                    Marker(
                        state = MarkerState(position = data.driverPos),
                        title = "Driver",
                        snippet = etaText,
                        icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)
                    )

                    // MARKER RESTORAN (Merah)
                    Marker(
                        state = MarkerState(position = data.restoPos),
                        title = "Restoran",
                        icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)
                    )
                }

                // MARKER USER (Biru)
                userLocation?.let { latLng ->
                    Marker(
                        state = MarkerState(position = latLng),
                        title = "Lokasi Saya",
                        icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)
                    )
                }
            }
        }

        // --- BAGIAN PANEL STATUS (Card di Bawah) ---
        trackingData?.let { data ->
            Card(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Status: ${data.status}",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )

                    // Tampilkan ETA hanya jika status DIANTAR atau ON_DELIVERY
                    if (data.status == "DIANTAR" || data.status == "ON_DELIVERY") {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = etaText,
                            color = Color(0xFF2196F3), // Warna Biru
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }

                    // Tampilkan Progress Bar jika belum Selesai
                    if (data.status != "COMPLETED" && data.status != "SELESAI") {
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    }
                }
            }
        }
    }
}