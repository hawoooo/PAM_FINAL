package com.mobile.quickbite.ui

import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.mobile.quickbite.data.Order
import com.mobile.quickbite.ui.nav.Screen
import com.mobile.quickbite.viewmodel.CartViewModel // Import CartViewModel
import com.mobile.quickbite.viewmodel.TrackingHistoryViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterialApi::class, ExperimentalFoundationApi::class)
@Composable
fun HistoryScreen(
    navController: NavController,
    viewModel: TrackingHistoryViewModel,
    cartViewModel: CartViewModel // <-- PARAMETER BARU
) {

    // 1. Ambil Data History dari Server (Sudah terfilter statusnya dari VM)
    val serverHistoryData by viewModel.filteredHistory.collectAsState(initial = emptyMap())

    // 2. Ambil Data Keranjang Lokal
    val cartItems by cartViewModel.cartItems.collectAsState()
    val cartTotal = cartViewModel.getCartTotal()

    // 3. Ambil Filter yang sedang aktif
    val currentFilter by viewModel.filterStatus.collectAsState()

    val isRefreshing by viewModel.isRefreshing.collectAsState(initial = false)
    val context = LocalContext.current

    LaunchedEffect(Unit) { viewModel.refreshData() }

    val pullRefreshState = rememberPullRefreshState(
        refreshing = isRefreshing,
        onRefresh = { viewModel.refreshData() }
    )

    // LOGIKA PENGGABUNGAN DATA (CART + HISTORY)
    // Kita buat variabel baru 'displayData' yang menggabungkan keduanya
    val displayData = remember(serverHistoryData, cartItems, currentFilter) {
        val combinedMap = serverHistoryData.toMutableMap()

        // Syarat menampilkan Keranjang:
        // 1. Keranjang tidak kosong
        // 2. Filter sedang "Semua" (null) ATAU filter sedang "BELUM_BAYAR"
        if (cartItems.isNotEmpty() && (currentFilter == null || currentFilter == "BELUM_BAYAR")) {

            // Buat Order Dummy untuk Keranjang
            val dummyCartOrder = Order(
                id = "CART_TEMP_ID", // ID Khusus penanda ini keranjang
                items = cartItems,
                totalPrice = cartTotal,
                status = "BELUM_BAYAR",
                orderTime = System.currentTimeMillis() // Waktu sekarang
            )

            // Masukkan ke Tanggal Hari Ini
            val todayKey = SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID")).format(Date())

            val currentList = combinedMap[todayKey] ?: emptyList()
            // Taruh paling atas (prepend)
            combinedMap[todayKey] = listOf(dummyCartOrder) + currentList
        }

        // Return Map yang sudah disortir kuncinya (Tanggal)
        combinedMap.toSortedMap(compareByDescending { it })
    }

    val tabs = listOf(
        "Semua" to null,
        "Belum Bayar" to "BELUM_BAYAR",
        "Dikemas" to "DIKEMAS",
        "Diantar" to "DIANTAR",
        "Selesai" to "SELESAI"
    )

    Column(modifier = Modifier.fillMaxSize().background(Color.White)) {

        ScrollableTabRow(
            selectedTabIndex = tabs.indexOfFirst { it.second == currentFilter }.coerceAtLeast(0),
            containerColor = Color.White,
            edgePadding = 16.dp,
            indicator = { tabPositions ->
                val index = tabs.indexOfFirst { it.second == currentFilter }.coerceAtLeast(0)
                if (index < tabPositions.size) {
                    TabRowDefaults.Indicator(
                        Modifier.tabIndicatorOffset(tabPositions[index]),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        ) {
            tabs.forEach { (title, status) ->
                Tab(
                    selected = currentFilter == status,
                    onClick = { viewModel.setFilter(status) },
                    text = {
                        Text(
                            title,
                            color = if(currentFilter == status) MaterialTheme.colorScheme.primary else Color.Gray,
                            fontWeight = if(currentFilter == status) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
            }
        }

        Divider()

        Box(
            modifier = Modifier
                .weight(1f)
                .pullRefresh(pullRefreshState)
        ) {
            // Gunakan 'displayData' bukan 'serverHistoryData'
            if (displayData.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = if (currentFilter == "BELUM_BAYAR") "Keranjang Kosong" else "Tidak ada pesanan",
                            color = Color.Gray
                        )
                    }
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
                    displayData.forEach { (dateKey, orders) ->
                        stickyHeader {
                            Text(
                                text = dateKey,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFFF9F9F9))
                                    .padding(vertical = 8.dp),
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                        }

                        items(orders) { order ->
                            val firstItem = order.items.firstOrNull()
                            val foodData = firstItem?.food
                            val restaurantName = foodData?.category ?: "Restoran"
                            val imageUrl = foodData?.imageUrl ?: ""
                            val menuSummary = order.items.joinToString(", ") { "${it.food.name} (${it.quantity})" }

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp)
                                    .clickable {
                                        // LOGIKA KLIK YANG SUDAH DIPERBAIKI (SUPPORT INDO & INGGRIS)
                                        if (order.id == "CART_TEMP_ID") {
                                            // 1. Item Keranjang
                                            navController.navigate(Screen.Cart.route)
                                        }
                                        // Cek status DIANTAR (Indo) ATAU ON_DELIVERY (Inggris)
                                        else if (order.status == "DIANTAR" || order.status == "ON_DELIVERY") {
                                            Toast.makeText(context, "Melacak Driver...", Toast.LENGTH_SHORT).show()
                                            navController.navigate(Screen.Tracking.createRoute(order.id))
                                        }
                                        else if (order.status == "SELESAI" || order.status == "COMPLETED") {
                                            Toast.makeText(context, "Pesanan Selesai", Toast.LENGTH_SHORT).show()
                                        }
                                        else {
                                            // Status Dikemas/Lainnya
                                            Toast.makeText(context, "Mohon tunggu pesanan...", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                colors = CardDefaults.cardColors(
                                    // Beri warna background sedikit beda jika ini item Keranjang
                                    containerColor = if(order.id == "CART_TEMP_ID") Color(0xFFFFF8E1) else Color.White
                                ),
                                elevation = CardDefaults.cardElevation(3.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    AsyncImage(
                                        model = imageUrl,
                                        contentDescription = null,
                                        modifier = Modifier.size(70.dp).clip(RoundedCornerShape(8.dp)).background(Color.LightGray),
                                        contentScale = ContentScale.Crop
                                    )

                                    Spacer(modifier = Modifier.width(16.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(restaurantName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                                            StatusText(order.status)
                                        }

                                        Text(text = menuSummary, style = MaterialTheme.typography.bodySmall, color = Color.Gray, maxLines = 1)
                                        Spacer(modifier = Modifier.height(8.dp))

                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text("Rp ${order.totalPrice.toInt()}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)

                                            // Tambahan Text instruksi jika keranjang
                                            if(order.id == "CART_TEMP_ID") {
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text("(Klik untuk Bayar)", style = MaterialTheme.typography.labelSmall, color = Color.Red)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            PullRefreshIndicator(isRefreshing, pullRefreshState, Modifier.align(Alignment.TopCenter))
        }
    }
}

@Composable
fun StatusText(status: String) {
    val (color, text) = when(status) {
        "BELUM_BAYAR" -> Color.Red to "Belum Bayar"
        "DIKEMAS" -> Color(0xFFFFA500) to "Dikemas"
        "DIANTAR" -> Color(0xFF2196F3) to "Diantar"
        "SELESAI" -> Color(0xFF4CAF50) to "Selesai"
        else -> Color.Gray to status
    }

    Surface(
        color = color.copy(alpha = 0.1f),
        shape = RoundedCornerShape(4.dp)
    ) {
        Text(
            text = text,
            color = color,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}