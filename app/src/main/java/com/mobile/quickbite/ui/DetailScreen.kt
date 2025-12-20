package com.mobile.quickbite.ui

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
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
import com.mobile.quickbite.data.Food
import com.mobile.quickbite.viewmodel.CartViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    navController: NavController,
    cartViewModel: CartViewModel,
    id: String,
    name: String,
    category: String,
    price: Double,     // Parameter Baru
    rating: Double,
    imageUrl: String   // Parameter Baru
) {
    var quantity by remember { mutableIntStateOf(1) }
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detail Menu") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        },
        bottomBar = {
            Surface(shadowElevation = 16.dp) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Total Harga", style = MaterialTheme.typography.bodySmall)
                        // FIX: Tampilkan Total Harga yang Benar
                        Text(
                            "Rp ${(price * quantity).toInt()}",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Button(
                        onClick = {
                            // FIX: Buat Objek Food LENGKAP (Ada Image & Price)
                            val foodItem = Food(
                                id = id,
                                name = name,
                                category = category,
                                price = price,       // PENTING
                                rating = rating,
                                imageUrl = imageUrl, // PENTING
                                restoLat = -7.955,
                                restoLng = 112.615
                            )

                            cartViewModel.addToCart(foodItem, quantity)
                            Toast.makeText(context, "Masuk Keranjang!", Toast.LENGTH_SHORT).show()
                            navController.popBackStack()
                        },
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Tambah ke Keranjang")
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
            // FIX: Gambar menggunakan URL asli
            AsyncImage(
                model = imageUrl,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.LightGray),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.height(24.dp))
            Text(name, style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
            Text(category, style = MaterialTheme.typography.titleMedium, color = Color.Gray)

            Spacer(modifier = Modifier.height(8.dp))
            Text("Rp ${price.toInt()}", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)

            Spacer(modifier = Modifier.height(24.dp))
            Divider()
            Spacer(modifier = Modifier.height(24.dp))

            // Logic Quantity (Sama seperti sebelumnya)
            Row(verticalAlignment = Alignment.CenterVertically) {
                FilledTonalIconButton(onClick = { if(quantity > 1) quantity-- }) {
                    Icon(Icons.Default.Remove, null)
                }
                Text("$quantity", modifier = Modifier.padding(horizontal = 24.dp), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                FilledTonalIconButton(onClick = { quantity++ }) {
                    Icon(Icons.Default.Add, null)
                }
            }
        }
    }
}