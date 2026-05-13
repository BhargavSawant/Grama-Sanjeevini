package com.example.gramasanjeevin.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gramasanjeevin.model.InventoryItem
import com.example.gramasanjeevin.model.Pharmacy
import com.example.gramasanjeevin.model.Request
import com.example.gramasanjeevin.model.RequestType
import com.example.gramasanjeevin.model.User
import com.example.gramasanjeevin.utils.FirestoreProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID

data class CartItem(
    val medicineName: String,
    val quantity: Int = 1
)

data class PharmacyMatch(
    val pharmacy: Pharmacy,
    val distanceKm: Double,
    val matchedItems: List<InventoryItem>,
    val missingItems: List<String>,
    val matchPercentage: Int
)

class CartViewModel : ViewModel() {
    private val db = FirestoreProvider.getDb()

    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    val cartItems: StateFlow<List<CartItem>> = _cartItems

    private val _matches = MutableStateFlow<List<PharmacyMatch>>(emptyList())
    val matches: StateFlow<List<PharmacyMatch>> = _matches

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _requestStatus = MutableStateFlow<String?>(null)
    val requestStatus: StateFlow<String?> = _requestStatus

    fun addToCart(medicineName: String) {
        val currentList = _cartItems.value.toMutableList()
        if (currentList.none { it.medicineName.equals(medicineName, ignoreCase = true) }) {
            currentList.add(CartItem(medicineName))
            _cartItems.value = currentList
            findSmartMatches()
        }
    }

    fun addMultipleToCart(names: List<String>) {
        val currentList = _cartItems.value.toMutableList()
        var added = false
        names.forEach { name ->
            if (currentList.none { it.medicineName.equals(name, ignoreCase = true) }) {
                currentList.add(CartItem(name))
                added = true
            }
        }
        if (added) {
            _cartItems.value = currentList
            findSmartMatches()
        }
    }

    fun removeFromCart(medicineName: String) {
        val currentList = _cartItems.value.filterNot { it.medicineName.equals(medicineName, ignoreCase = true) }
        _cartItems.value = currentList
        findSmartMatches()
    }

    fun findSmartMatches() {
        if (_cartItems.value.isEmpty()) {
            _matches.value = emptyList()
            _isLoading.value = false
            return
        }

        _isLoading.value = true
        viewModelScope.launch {
            try {
                val neededMedicines = _cartItems.value.map { it.medicineName }
                val pharmaciesSnapshot = db.collection("pharmacies").get().await()
                val pharmacies = pharmaciesSnapshot.toObjects(Pharmacy::class.java)

                val results = mutableListOf<PharmacyMatch>()

                val userLat = 13.0334
                val userLng = 77.5891

                for (pharmacy in pharmacies) {
                    val inventorySnapshot = db.collection("inventory")
                        .whereEqualTo("shopId", pharmacy.shopId)
                        .get()
                        .await()
                    
                    val pharmacyInventory = inventorySnapshot.toObjects(InventoryItem::class.java)
                    val matchedItems = mutableListOf<InventoryItem>()
                    val missingItems = mutableListOf<String>()

                    for (needed in neededMedicines) {
                        val item = pharmacyInventory.find { 
                            it.medicineName.contains(needed, ignoreCase = true) && it.quantity > 0 
                        }
                        if (item != null) {
                            matchedItems.add(item)
                        } else {
                            missingItems.add(needed)
                        }
                    }

                    if (matchedItems.isNotEmpty()) {
                        val percentage = (matchedItems.size * 100) / neededMedicines.size
                        
                        val realDistance = com.example.gramasanjeevin.utils.LocationUtils.calculateDistance(
                            userLat, userLng, pharmacy.latitude, pharmacy.longitude
                        )
                        
                        results.add(PharmacyMatch(
                            pharmacy = pharmacy,
                            distanceKm = realDistance,
                            matchedItems = matchedItems,
                            missingItems = missingItems,
                            matchPercentage = percentage
                        ))
                    }
                }

                results.sortWith(compareByDescending<PharmacyMatch> { it.matchPercentage }.thenBy { it.distanceKm })
                _matches.value = results
            } catch (e: Exception) {
                // handle error
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun sendRequest(shopId: String, type: RequestType, items: List<String>) {
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
                    type = type,
                    items = items,
                    status = "PENDING"
                )
                db.collection("requests").document(requestId).set(request).await()
                _requestStatus.value = "Request Sent Successfully!"
            } catch (e: Exception) {
                _requestStatus.value = "Failed to send request: ${e.message}"
            }
        }
    }

    fun clearRequestStatus() {
        _requestStatus.value = null
    }
}
