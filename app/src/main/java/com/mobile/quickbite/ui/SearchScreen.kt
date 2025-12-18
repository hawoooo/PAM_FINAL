package com.mobile.quickbite.ui

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.mobile.quickbite.ui.nav.Screen
import com.mobile.quickbite.viewmodel.FoodViewModel
import java.util.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun SearchScreen(
    navController: NavController,
    viewModel: FoodViewModel = viewModel()
) {
    var query by remember { mutableStateOf("") }
    val results by viewModel.searchResult.collectAsState()
    val context = LocalContext.current
    val focusRequester = remember { FocusRequester() }

    // Voice to text
    val voiceLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            val spokenText: String? =
                data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.get(0)
            if (!spokenText.isNullOrEmpty()) {
                query = spokenText
                viewModel.searchByCategory(spokenText)
            }
        }
    }

    // Permission for microphone
    val permissionState = rememberPermissionState(permission = Manifest.permission.RECORD_AUDIO)

    fun launchVoiceAssistance() {
        if (permissionState.status.isGranted) {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(
                    RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                )
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                putExtra(RecognizerIntent.EXTRA_PROMPT, "Bicara untuk mencari...")
            }
            voiceLauncher.launch(intent)
        } else {
            ActivityCompat.requestPermissions(
                context as Activity,
                arrayOf(Manifest.permission.RECORD_AUDIO),
                0
            )
        }
    }

    // Fokus Otomatis & Kosongkan hasil awal
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
        viewModel.searchByCategory("") // Kosongkan hasil untuk tampilan awal
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp)
    ) {
        // Search Bar
        OutlinedTextField(
            value = query,
            onValueChange = {
                query = it
                viewModel.searchByCategory(it) // Filter Real-time
            },
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester),
            placeholder = { Text("Cari menu (ex: Nasi, Ayam)...") },
            leadingIcon = { Icon(Icons.Default.Search, null) },
            trailingIcon = {
                IconButton(onClick = { launchVoiceAssistance() }) {
                    Icon(Icons.Default.Mic, contentDescription = "Pencarian Suara")
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // --- PERBAIKAN LOGIKA TAMPILAN ---
        if (query.isEmpty() && results.isEmpty()) {
            // 1. Tampilan Awal (belum mengetik)
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Mulai cari menu favoritmu...", color = Color.Gray)
            }
        } else if (query.isNotEmpty() && results.isEmpty()) {
            // 2. Tampilan Jika Hasil Tidak Ditemukan
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Menu '$query' tidak ditemukan", color = Color.Gray)
            }
        } else {
            // 3. Tampilan Jika Hasil Ditemukan
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(results) { food ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                // PERBAIKAN NAVIGASI (MENGHINDARI CRASH)
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