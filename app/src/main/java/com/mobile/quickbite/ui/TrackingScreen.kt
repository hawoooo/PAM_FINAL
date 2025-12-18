package com.mobile.quickbite.ui

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
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
    val trackingData by viewModel.trackingState.collectAsState()
    val userLocation by viewModel.userLocation.collectAsState()
    val context = LocalContext.current

    // Client GPS HP
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    // Kamera Awal
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(-7.955, 112.615), 14f)
    }

    // Permission Launcher
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            try {
                fusedLocationClient.lastLocation.addOnSuccessListener { loc ->
                    if (loc != null) viewModel.updateUserLocation(loc.latitude, loc.longitude)
                }
            } catch (e: SecurityException) {}
        }
    }

    LaunchedEffect(Unit) {
        // 1. Mulai Tracking Driver (Realtime dari Firebase)
        viewModel.startTracking(orderId)

        // 2. Ambil Lokasi User (GPS HP)
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.lastLocation.addOnSuccessListener { loc ->
                if (loc != null) viewModel.updateUserLocation(loc.latitude, loc.longitude)
            }
        } else {
            launcher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    // Animasi Kamera ke Driver
    LaunchedEffect(trackingData) {
        trackingData?.let {
            cameraPositionState.animate(CameraUpdateFactory.newLatLng(it.driverPos))
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.weight(1f)) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState
            ) {
                // MARKER 1: DRIVER (Mobil/Hijau)
                trackingData?.let { data ->
                    Marker(
                        state = MarkerState(position = data.driverPos),
                        title = "Driver",
                        snippet = "Bergerak...",
                        icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)
                    )

                    // MARKER 2: RESTORAN (Merah)
                    Marker(
                        state = MarkerState(position = data.restoPos),
                        title = "Restoran",
                        icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)
                    )
                }

                // MARKER 3: USER (Biru - Dari GPS HP)
                userLocation?.let { latLng ->
                    Marker(
                        state = MarkerState(position = latLng),
                        title = "Lokasi Saya",
                        icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)
                    )
                }
            }
        }

        // Panel Status
        trackingData?.let { data ->
            Card(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Status Pesanan: ${data.status}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    if (data.status != "COMPLETED") {
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    }
                }
            }
        }
    }
}