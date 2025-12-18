package com.mobile.quickbite.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mobile.quickbite.viewmodel.FoodViewModel

@Composable
fun ResultScreen(
    category: String, // Ini adalah query pencarian
    viewModel: FoodViewModel = viewModel()
) {
    // Saat layar dibuka, langsung cari ke Firebase
    LaunchedEffect(category) {
        viewModel.searchByCategory(category)
    }

    val results by viewModel.searchResult.collectAsState()

    Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        Text("Hasil Pencarian: '$category'", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(16.dp))

        LazyColumn(contentPadding = PaddingValues(bottom = 24.dp)) {
            items(results) { food ->
                Card(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(food.name, style = MaterialTheme.typography.titleMedium) // Nama Menu
                        Text("Resto: ${food.category}") // Nama Resto
                        Text("Rating: ⭐ ${food.rating}") // Dummy Rating
                    }
                }
            }
            if (results.isEmpty()) {
                item { Text("Data tidak ditemukan di Firebase.") }
            }
        }
    }
}