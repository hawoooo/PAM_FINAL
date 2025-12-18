package com.mobile.quickbite.ui

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.mobile.quickbite.ui.nav.Screen
import com.mobile.quickbite.viewmodel.CartViewModel
import kotlinx.coroutines.delay

@Composable
fun PaymentScreen(navController: NavController, cartViewModel: CartViewModel) {
    val context = LocalContext.current
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        delay(2000) // Simulasi Loading

        cartViewModel.checkoutToFirebase {
            isLoading = false
            Toast.makeText(context, "Pesanan Berhasil!", Toast.LENGTH_SHORT).show()

            // --- PERBAIKAN NAVIGASI DI SINI ---
            // Kita navigasi persis seperti cara BottomBar bekerja
            navController.navigate(Screen.History.route) {
                // Hapus tumpukan sampai Menu
                popUpTo(navController.graph.findStartDestination().id) {
                    // saveState = true <-- JANGAN PAKAI
                }
                launchSingleTop = true
                // restoreState = true <-- JANGAN PAKAI
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            if (isLoading) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(24.dp))
                Text("Memproses Pembayaran...", style = MaterialTheme.typography.titleMedium)
            } else {
                Text("Selesai!", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}