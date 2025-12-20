package com.mobile.quickbite.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.mobile.quickbite.ui.nav.Screen
import com.mobile.quickbite.viewmodel.CartViewModel
import com.mobile.quickbite.viewmodel.FoodViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuScreen(
    navController: NavController,
    foodViewModel: FoodViewModel,
    cartViewModel: CartViewModel
) {
    val menuList by foodViewModel.searchResult.collectAsState()

    // 1. PANGGIL STATE LOADING DARI VIEWMODEL
    val isLoading by foodViewModel.isLoading.collectAsState()

    val cartItems by cartViewModel.cartItems.collectAsState()

    LaunchedEffect(Unit) {
        foodViewModel.searchByCategory("")
    }

    Scaffold(
        floatingActionButton = {
            if (cartItems.isNotEmpty()) {
                FloatingActionButton(
                    onClick = { navController.navigate(Screen.Cart.route) },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White
                ) {
                    BadgedBox(badge = { Badge { Text("${cartItems.size}") } }) {
                        Icon(Icons.Default.ShoppingCart, "Cart")
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color.White)
                .padding(16.dp)
        ) {
            Box(modifier = Modifier.clickable { navController.navigate(Screen.Search.route) }) {
                OutlinedTextField(
                    value = "",
                    onValueChange = {},
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Cari menu lezat...") },
                    leadingIcon = { Icon(Icons.Default.Search, null) },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledContainerColor = Color.White,
                        disabledTextColor = Color.Black,
                        disabledLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        disabledPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        disabledBorderColor = MaterialTheme.colorScheme.outline
                    ),
                    enabled = false,
                    readOnly = true
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 2. LOGIKA LOADING LIST MENU
            if (isLoading) {
                // Tampilkan Loading HANYA jika sedang fetch data
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (menuList.isEmpty()) {
                // Tampilkan pesan kosong jika loading selesai TAPI data tidak ada
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Menu belum tersedia / Gagal Load", color = Color.Gray)
                }
            } else {
                // Tampilkan Data jika ada
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(menuList) { food ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    navController.navigate(
                                        Screen.Detail.createRoute(
                                            id = food.id,
                                            name = food.name,
                                            category = food.category,
                                            price = food.price,
                                            rating = food.rating,
                                            imageUrl = food.imageUrl
                                        )
                                    )
                                },
                            shape = RoundedCornerShape(12.dp),
                            elevation = CardDefaults.cardElevation(4.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                AsyncImage(
                                    model = food.imageUrl,
                                    contentDescription = food.name,
                                    modifier = Modifier
                                        .size(80.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color.LightGray),
                                    contentScale = ContentScale.Crop
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                Column {
                                    Text(food.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                    Text(food.category, style = MaterialTheme.typography.bodySmall, color = Color.Gray)

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Star, null, tint = Color(0xFFFFD700), modifier = Modifier.size(16.dp))
                                        Text(" ${food.rating}", style = MaterialTheme.typography.bodySmall)
                                    }

                                    Text(
                                        "Rp ${food.price.toInt()}",
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}