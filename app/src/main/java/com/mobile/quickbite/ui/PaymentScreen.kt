package com.mobile.quickbite.ui

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Money
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.mobile.quickbite.ui.nav.Screen
import com.mobile.quickbite.viewmodel.CartViewModel
import kotlinx.coroutines.delay
import androidx.compose.foundation.BorderStroke

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentScreen(navController: NavController, cartViewModel: CartViewModel) {
    val context = LocalContext.current
    var isLoading by remember { mutableStateOf(false) }
    var selectedMethod by remember { mutableStateOf("E-Wallet") }
    val total = cartViewModel.getCartTotal()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pembayaran") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        },
        bottomBar = {
            Surface(shadowElevation = 16.dp) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Total Pembayaran", style = MaterialTheme.typography.titleMedium)
                        Text("Rp ${total.toInt()}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            isLoading = true
                            // Disini kita bisa kirim selectedMethod ke ViewModel jika perlu disimpan di database
                        },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = RoundedCornerShape(8.dp),
                        enabled = !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Memproses...")
                        } else {
                            Text("Bayar Sekarang")
                        }
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Text("Pilih Metode Pembayaran", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))

            PaymentOptionItem("E-Wallet", "GoPay, OVO, Dana", Icons.Default.Wallet, selectedMethod == "E-Wallet") { selectedMethod = "E-Wallet" }
            Spacer(modifier = Modifier.height(8.dp))
            PaymentOptionItem("Transfer Bank", "BCA, Mandiri, BRI", Icons.Default.CreditCard, selectedMethod == "Transfer Bank") { selectedMethod = "Transfer Bank" }
            Spacer(modifier = Modifier.height(8.dp))
            PaymentOptionItem("Cash on Delivery", "Bayar saat pesanan tiba", Icons.Default.Money, selectedMethod == "COD") { selectedMethod = "COD" }
        }
    }

    // Logic Pembayaran Dummy (Dipindah ke LaunchedEffect yang memantau isLoading)
    LaunchedEffect(isLoading) {
        if (isLoading) {
            delay(2000) // Simulasi Loading

            // Panggil fungsi submit di ViewModel
            cartViewModel.checkoutToFirebase {
                isLoading = false
                Toast.makeText(context, "Pembayaran Berhasil via $selectedMethod!", Toast.LENGTH_SHORT).show()
                navController.navigate(Screen.History.route) {
                    popUpTo(navController.graph.findStartDestination().id)
                    launchSingleTop = true
                }
            }
        }
    }
}

@Composable
fun PaymentOptionItem(title: String, subtitle: String, icon: ImageVector, isSelected: Boolean, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.White
        ),
        border = if (isSelected) BorderStroke(1.dp, MaterialTheme.colorScheme.primary) else null
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, null, tint = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray)
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
            RadioButton(selected = isSelected, onClick = onClick)
        }
    }
}