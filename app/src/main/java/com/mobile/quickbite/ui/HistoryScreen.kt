package com.mobile.quickbite.ui

import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.mobile.quickbite.ui.nav.Screen
import com.mobile.quickbite.viewmodel.TrackingHistoryViewModel

@OptIn(ExperimentalMaterialApi::class, ExperimentalFoundationApi::class)
@Composable
fun HistoryScreen(navController: NavController, viewModel: TrackingHistoryViewModel) {

    val historyData by viewModel.historyState.collectAsState(initial = emptyMap())
    val isRefreshing by viewModel.isRefreshing.collectAsState(initial = false)
    val context = LocalContext.current

    // Auto Refresh saat dibuka
    LaunchedEffect(Unit) {
        viewModel.refreshData()
    }

    val pullRefreshState = rememberPullRefreshState(
        refreshing = isRefreshing,
        onRefresh = { viewModel.refreshData() }
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pullRefresh(pullRefreshState)
            .background(Color.White)
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {

            Text("Riwayat Pesanan", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))

            if (historyData.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Belum ada riwayat pesanan.", color = Color.Gray)
                }
            }

            LazyColumn(modifier = Modifier.fillMaxSize()) {
                historyData.forEach { (dateKey, orders) ->
                    stickyHeader {
                        Text(
                            text = dateKey,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFF0F0F0))
                                .padding(8.dp),
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    }

                    items(orders) { order ->
                        // --- DEFINISI VARIABEL YANG BENAR ---
                        val firstItem = order.items.firstOrNull()
                        val foodData = firstItem?.food // KITA PAKAI NAMA 'foodData'

                        // Ambil data dari foodData yang sudah didefinisikan di atas
                        val restaurantName = foodData?.category ?: "Restoran"
                        val imageUrl = foodData?.imageUrl ?: "" // Ambil URL Gambar

                        val menuSummary = order.items.joinToString(", ") {
                            "${it.food.name} (${it.quantity})"
                        }

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable {
                                    Toast.makeText(context, "Melacak: $restaurantName", Toast.LENGTH_SHORT).show()
                                    navController.navigate(Screen.Tracking.createRoute(order.id))
                                },
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(2.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // GAMBAR MAKANAN
                                AsyncImage(
                                    model = imageUrl, // Menggunakan imageUrl yang sudah benar
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(64.dp)
                                        .background(Color.LightGray),
                                    contentScale = ContentScale.Crop
                                )

                                Spacer(modifier = Modifier.width(16.dp))

                                // INFO TEXT
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(restaurantName, fontWeight = FontWeight.Bold)
                                    Text(
                                        text = menuSummary,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color.Gray,
                                        maxLines = 2
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Rp ${order.totalPrice.toInt()}", fontWeight = FontWeight.Bold)

                                        val statusColor = if(order.status == "COMPLETED") Color(0xFF4CAF50) else Color(0xFF2196F3)
                                        Text(order.status, color = statusColor, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        PullRefreshIndicator(
            refreshing = isRefreshing,
            state = pullRefreshState,
            modifier = Modifier.align(Alignment.TopCenter),
            backgroundColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary
        )
    }
}