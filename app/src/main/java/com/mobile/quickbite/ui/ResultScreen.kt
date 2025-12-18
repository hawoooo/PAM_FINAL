package com.mobile.quickbite.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.mobile.quickbite.ui.nav.Screen
import com.mobile.quickbite.viewmodel.FoodViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultScreen(
    navController: NavController,
    query: String, // Ini adalah query pencarian
    viewModel: FoodViewModel = viewModel()
) {
    val results by viewModel.searchResult.collectAsState()
    var isLoading by remember { mutableStateOf(true) }

    // Saat layar dibuka, langsung cari ke Firebase
    LaunchedEffect(query) {
        isLoading = true
        viewModel.searchByCategory(query)
        isLoading = false // Anggap selesai setelah panggil
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Hasil untuk '$query'") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Kembali")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (results.isEmpty()) {
                Text("Menu tidak ditemukan.", modifier = Modifier.align(Alignment.Center))
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(results) { food ->
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
                                    modifier = Modifier.size(80.dp).clip(RoundedCornerShape(8.dp)).background(Color.LightGray),
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
                                    Text("Rp${food.price.toInt()}", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}