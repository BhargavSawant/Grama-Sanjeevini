package com.example.gramasanjeevin.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gramasanjeevin.model.InventoryItem
import com.example.gramasanjeevin.model.Pharmacy
import com.example.gramasanjeevin.model.Request
import com.example.gramasanjeevin.model.RequestType
import com.example.gramasanjeevin.utils.FirestoreProvider
import com.google.firebase.Timestamp
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID

class PharmacistViewModel : ViewModel() {
    private val db = FirestoreProvider.getDb()

    private val _inventory = MutableStateFlow<List<InventoryItem>>(emptyList())
    val inventory: StateFlow<List<InventoryItem>> = _inventory

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _shopDetails = MutableStateFlow<Pharmacy?>(null)
    val shopDetails: StateFlow<Pharmacy?> = _shopDetails

    private val _requests = MutableStateFlow<List<Request>>(emptyList())
    val requests: StateFlow<List<Request>> = _requests

    private val _stats = MutableStateFlow(PharmacyStats())
    val stats: StateFlow<PharmacyStats> = _stats

    private var requestsListener: ListenerRegistration? = null
    private var inventoryListener: ListenerRegistration? = null

    // For V1, we mock the login by hardcoding a shop ID (Raju Medical)
    private val currentShopId = "shop_001"

    init {
        fetchShopDetails()
        startRealTimeRequests()
        startRealTimeInventory()
    }

    private fun fetchShopDetails() {
        viewModelScope.launch {
            try {
                val doc = db.collection("pharmacies").document(currentShopId).get().await()
                _shopDetails.value = doc.toObject(Pharmacy::class.java)
            } catch (e: Exception) {
                Log.e("PharmacistVM", "Error fetching shop details: ${e.message}")
            }
        }
    }

    private fun startRealTimeInventory() {
        _isLoading.value = true
        inventoryListener?.remove()
        inventoryListener = db.collection("inventory")
            .whereEqualTo("shopId", currentShopId)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.e("PharmacistVM", "Inventory listen failed", e)
                    _isLoading.value = false
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val items = snapshot.toObjects(InventoryItem::class.java)
                    _inventory.value = items
                    calculateStats(items)
                }
                _isLoading.value = false
            }
    }

    private fun startRealTimeRequests() {
        requestsListener?.remove()
        requestsListener = db.collection("requests")
            .whereEqualTo("shopId", currentShopId)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.e("PharmacistVM", "Requests listen failed", e)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val list = snapshot.toObjects(Request::class.java)
                    _requests.value = list.sortedByDescending { it.timestamp }
                    Log.d("PharmacistVM", "Updated requests list: ${list.size} items")
                }
            }
    }

    private fun calculateStats(items: List<InventoryItem>) {
        if (items.isEmpty()) {
            _stats.value = PharmacyStats()
            return
        }
        val total = items.size
        val critical = items.count { it.quantity in 1..10 }
        val outOfStock = items.count { it.quantity <= 0 }
        val healthy = items.count { it.quantity > 10 }
        val healthPercentage = if (total > 0) (healthy * 100) / total else 100
        
        _stats.value = PharmacyStats(
            totalItems = total,
            criticalItems = critical,
            outOfStockItems = outOfStock,
            healthPercentage = healthPercentage
        )
    }

    fun updateMedicine(itemId: String, newQuantity: Int, newExpiry: Timestamp) {
        viewModelScope.launch {
            try {
                db.collection("inventory").document(itemId)
                    .update(mapOf(
                        "quantity" to newQuantity,
                        "expiryDate" to newExpiry
                    ))
                    .await()
            } catch (e: Exception) {
                Log.e("PharmacistVM", "Update failed: ${e.message}")
            }
        }
    }

    fun addMedicine(name: String, quantity: Int, isLifeSaving: Boolean, expiryDate: Timestamp) {
        viewModelScope.launch {
            try {
                val newItemId = UUID.randomUUID().toString()
                val newItem = InventoryItem(
                    itemId = newItemId,
                    shopId = currentShopId,
                    medicineName = name,
                    quantity = quantity,
                    isLifeSaving = isLifeSaving,
                    expiryDate = expiryDate
                )
                db.collection("inventory").document(newItemId).set(newItem).await()
            } catch (e: Exception) {
                Log.e("PharmacistVM", "Add failed: ${e.message}")
            }
        }
    }

    fun updateRequestStatus(requestId: String, newStatus: String) {
        viewModelScope.launch {
            try {
                val requestDoc = db.collection("requests").document(requestId).get().await()
                val request = requestDoc.toObject(Request::class.java) ?: return@launch
                
                // Update status
                db.collection("requests").document(requestId).update("status", newStatus).await()
                Log.d("PharmacistVM", "Request $requestId status updated to $newStatus")
                
                // Inventory Management Logic
                if (newStatus == "COMPLETED" && request.type == RequestType.DELIVERY) {
                    for (itemName in request.items) {
                        val inventorySnapshot = db.collection("inventory")
                            .whereEqualTo("shopId", request.shopId)
                            .whereEqualTo("medicineName", itemName)
                            .get()
                            .await()
                        
                        if (!inventorySnapshot.isEmpty) {
                            val doc = inventorySnapshot.documents[0]
                            val currentQty = doc.getLong("quantity") ?: 0
                            if (currentQty > 0) {
                                db.collection("inventory").document(doc.id)
                                    .update("quantity", currentQty - 1)
                                    .await()
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("PharmacistVM", "Error updating request: ${e.message}")
            }
        }
    }

    fun updatePharmacyProfile(name: String, address: String, license: String) {
        viewModelScope.launch {
            try {
                db.collection("pharmacies").document(currentShopId)
                    .update(mapOf(
                        "name" to name,
                        "address" to address,
                        "licenseNumber" to license
                    )).await()
            } catch (e: Exception) {
                Log.e("PharmacistVM", "Error updating profile: ${e.message}")
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        requestsListener?.remove()
        inventoryListener?.remove()
    }
}

data class PharmacyStats(
    val totalItems: Int = 0,
    val criticalItems: Int = 0,
    val outOfStockItems: Int = 0,
    val healthPercentage: Int = 100
)
