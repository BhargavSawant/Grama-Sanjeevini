package com.example.gramasanjeevin.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gramasanjeevin.model.InventoryItem
import com.example.gramasanjeevin.model.Pharmacy
import com.example.gramasanjeevin.model.SearchResult
import com.example.gramasanjeevin.utils.FirestoreProvider
import com.example.gramasanjeevin.utils.LocationUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class SearchViewModel : ViewModel() {
    private val db = FirestoreProvider.getDb()

    private val _searchResults = MutableStateFlow<List<SearchResult>>(emptyList())
    val searchResults: StateFlow<List<SearchResult>> = _searchResults

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    // Mock User Location (Bengaluru)
    private val userLat = 13.0334
    private val userLng = 77.5891

    fun searchMedicine(query: String) {
        if (query.isBlank()) {
            _searchResults.value = emptyList()
            return
        }

        _isLoading.value = true

        viewModelScope.launch {
            try {
                // 1. Query the 'inventory' collection for prefix match
                val formattedQuery = query.lowercase().replaceFirstChar { it.uppercase() }
                
                val inventorySnapshot = db.collection("inventory")
                    .orderBy("medicineName")
                    .startAt(formattedQuery)
                    .endAt(formattedQuery + "\uf8ff")
                    .get()
                    .await()

                val items = inventorySnapshot.toObjects(InventoryItem::class.java)
                val results = mutableListOf<SearchResult>()

                // 2. Fetch Pharmacy details and calculate real distance
                for (item in items) {
                    val shopSnapshot = db.collection("pharmacies").document(item.shopId).get().await()
                    val pharmacy = shopSnapshot.toObject(Pharmacy::class.java)

                    if (pharmacy != null) {
                        val realDistance = LocationUtils.calculateDistance(
                            userLat, userLng, pharmacy.latitude, pharmacy.longitude
                        )
                        results.add(SearchResult(item, pharmacy, realDistance))
                    }
                }
                
                // Sort by distance
                results.sortBy { it.distanceKm }

                _searchResults.value = results

            } catch (e: Exception) {
                Log.e("SearchViewModel", "Error fetching data: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }
}
