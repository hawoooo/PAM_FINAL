package com.mobile.quickbite.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobile.quickbite.data.Food
import com.mobile.quickbite.data.SearchRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class FoodViewModel : ViewModel() {

    // Gunakan Repository baru yang connect ke Firebase
    private val repository = SearchRepository()

    private val _searchResult = MutableStateFlow<List<Food>>(emptyList())
    val searchResult: StateFlow<List<Food>> = _searchResult

    fun searchByCategory(query: String) {
        viewModelScope.launch {
            // 1. Ambil semua data dari Firebase
            val allFoods = repository.getFoodsFromFirebase()

            // 2. Filter data di sini (Logic Pencarian)
            if (query.isBlank()) {
                _searchResult.value = emptyList()
            } else {
                val filtered = allFoods.filter {
                    it.name.contains(query, ignoreCase = true) ||
                            it.category.contains(query, ignoreCase = true)
                }
                _searchResult.value = filtered
            }
        }
    }
}