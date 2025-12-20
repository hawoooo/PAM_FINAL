package com.mobile.quickbite.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobile.quickbite.data.Food
import com.mobile.quickbite.data.SearchRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class FoodViewModel : ViewModel() {

    // Gunakan Repository baru yang connect ke Firebase
    private val repository = SearchRepository()

    // State untuk menampung hasil pencarian/menu
    private val _searchResult = MutableStateFlow<List<Food>>(emptyList())
    val searchResult = _searchResult.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()


    fun searchByCategory(query: String) {
        viewModelScope.launch {
            _isLoading.value = true // 1. Mulai Loading (Spinner Muncul)

            try {
                // Ambil semua data dari Firebase
                val allFoods = repository.getFoodsFromFirebase()

                if (query.isEmpty()) {
                    // Kalau gak ngetik apa-apa, tampilkan semua
                    _searchResult.value = allFoods
                } else {
                    // Kalau ada query, filter berdasarkan nama atau kategori
                    _searchResult.value = allFoods.filter {
                        it.name.contains(query, ignoreCase = true) ||
                                it.category.contains(query, ignoreCase = true)
                    }
                }
            } catch (e: Exception) {
                // Kalau error, list kosong
                _searchResult.value = emptyList()
            } finally {
                _isLoading.value = false // 2. Selesai Loading (Spinner Hilang)
            }
        }
    }
}