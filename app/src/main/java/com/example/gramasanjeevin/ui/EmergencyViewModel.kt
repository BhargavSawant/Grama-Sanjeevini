package com.example.gramasanjeevin.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gramasanjeevin.model.InventoryItem
import com.example.gramasanjeevin.model.Pharmacy
import com.example.gramasanjeevin.model.Request
import com.example.gramasanjeevin.model.RequestType
import com.example.gramasanjeevin.model.SearchResult
import com.example.gramasanjeevin.model.User
import com.example.gramasanjeevin.utils.FirestoreProvider
import com.example.gramasanjeevin.utils.LocationUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID

class EmergencyViewModel : ViewModel() {
    private val db = FirestoreProvider.getDb()

    private val _emergencyList = MutableStateFlow<List<SearchResult>>(emptyList())
    val emergencyList: StateFlow<List<SearchResult>> = _emergencyList

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _requestStatus = MutableStateFlow<String?>(null)
    val requestStatus: StateFlow<String?> = _requestStatus

    init {
        fetchEmergencyStock()
    }

    fun fetchEmergencyStock() {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val inventorySnapshot = db.collection("inventory")
                    .whereEqualTo("isLifeSaving", true)
                    .get()
                    .await()

                val items = inventorySnapshot.toObjects(InventoryItem::class.java)
                val results = mutableListOf<SearchResult>()

                val userLat = 13.0334
                val userLng = 77.5891

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

                results.sortBy { it.distanceKm }
                _emergencyList.value = results
            } catch (e: Exception) {
                Log.e("EmergencyVM", "Error: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun sendRestockRequest(shopId: String, medicineName: String) {
        viewModelScope.launch {
            try {
                val userDoc = db.collection("users").document("user_001").get().await()
                val user = userDoc.toObject(User::class.java)
                val userName = user?.name ?: "Unknown User"

                val requestId = UUID.randomUUID().toString()
                val request = Request(
                    requestId = requestId,
                    shopId = shopId,
                    userId = "user_001",
                    userName = userName,
                    type = RequestType.RESTOCK,
                    items = listOf(medicineName),
                    status = "PENDING"
                )
                db.collection("requests").document(requestId).set(request).await()
                _requestStatus.value = "Restock request sent!"
            } catch (e: Exception) {
                _requestStatus.value = "Failed: ${e.message}"
            }
        }
    }

    fun clearStatus() {
        _requestStatus.value = null
    }
}
