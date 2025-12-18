package com.mobile.quickbite.data

import com.google.android.gms.maps.model.LatLng

data class TrackingData(
    val driverPos: LatLng,
    val restoPos: LatLng,
    val status: String // Tambahan field status
)