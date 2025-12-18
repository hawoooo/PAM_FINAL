package com.mobile.quickbite

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.mobile.quickbite.ui.*
import com.mobile.quickbite.ui.nav.Screen // Pastikan Import ini benar
import com.mobile.quickbite.ui.theme.QuickBiteTheme
import com.mobile.quickbite.viewmodel.CartViewModel
import com.mobile.quickbite.viewmodel.FoodViewModel
import com.mobile.quickbite.viewmodel.TrackingHistoryViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            QuickBiteTheme {
                val navController = rememberNavController()

                // Inisialisasi Semua ViewModel
                val historyViewModel: TrackingHistoryViewModel = viewModel()
                val foodViewModel: FoodViewModel = viewModel()
                val cartViewModel: CartViewModel = viewModel()

                // Logic Bottom Bar (Hanya Muncul di Menu & History)
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route
                val showBottomBar = currentRoute in listOf(Screen.Menu.route, Screen.History.route)

                Scaffold(
                    containerColor = MaterialTheme.colorScheme.background,
                    bottomBar = {
                        if (showBottomBar) {
                            BottomNavigationBar(navController = navController)
                        }
                    }
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = Screen.Menu.route,
                        modifier = Modifier.padding(innerPadding)
                    ) {

                        // 1. MENU (Tab Kiri)
                        composable(Screen.Menu.route) {
                            MenuScreen(navController, foodViewModel, cartViewModel)
                        }

                        // 2. RIWAYAT (Tab Kanan)
                        composable(Screen.History.route) {
                            HistoryScreen(navController, historyViewModel)
                        }

                        // --- ALUR PENCARIAN BARU ---

                        // 3. LAYAR UNTUK INPUT PENCARIAN
                        composable(Screen.Search.route) {
                            SearchScreen(navController, foodViewModel)
                        }

                        // 4. LAYAR UNTUK MENAMPILKAN HASIL
                        composable(
                            route = Screen.FoodResult.route,
                            arguments = listOf(navArgument("query") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val query = backStackEntry.arguments?.getString("query") ?: ""
                            ResultScreen(navController, query, foodViewModel)
                        }

                        // --- ALUR LAINNYA ---

                        // Detail Makanan
                        composable(
                            route = Screen.Detail.route,
                            arguments = listOf(
                                navArgument("id") { type = NavType.StringType },
                                navArgument("name") { type = NavType.StringType },
                                navArgument("category") { type = NavType.StringType },
                                navArgument("price") { type = NavType.FloatType },
                                navArgument("rating") { type = NavType.FloatType },
                                navArgument("imageUrl") { type = NavType.StringType }
                            )
                        ) { entry ->
                            val id = entry.arguments?.getString("id") ?: ""
                            val name = entry.arguments?.getString("name") ?: ""
                            val category = entry.arguments?.getString("category") ?: ""
                            val price = entry.arguments?.getFloat("price")?.toDouble() ?: 0.0
                            val rating = entry.arguments?.getFloat("rating")?.toDouble() ?: 0.0
                            val imageUrl = entry.arguments?.getString("imageUrl") ?: ""

                            DetailScreen(
                                navController = navController,
                                cartViewModel = cartViewModel,
                                id = id,
                                name = name,
                                category = category,
                                price = price,
                                rating = rating,
                                imageUrl = imageUrl
                            )
                        }

                        // Keranjang
                        composable(Screen.Cart.route) {
                            CartScreen(navController, cartViewModel)
                        }

                        // Pembayaran
                        composable(Screen.Payment.route) {
                            PaymentScreen(navController, cartViewModel)
                        }

                        // Tracking
                        composable(Screen.Tracking.route) { backStackEntry ->
                            val orderId = backStackEntry.arguments?.getString("orderId")
                            if (orderId != null) {
                                historyViewModel.startTracking(orderId)
                                TrackingScreen(orderId, historyViewModel)
                            }
                        }
                    }
                }
            }
        }
    }
}