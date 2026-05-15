package com.example.gramasanjeevin.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gramasanjeevin.model.InventoryItem
import com.example.gramasanjeevin.model.OrderItem
import com.example.gramasanjeevin.model.Pharmacy
import com.example.gramasanjeevin.model.Request
import com.example.gramasanjeevin.model.TransferRequest
import com.example.gramasanjeevin.utils.FirestoreProvider
import com.google.firebase.Timestamp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID

data class PharmacyStats(
    val totalItems: Int = 0,
    val criticalItems: Int = 0,
    val outOfStockItems: Int = 0,
    val healthPercentage: Int = 100
)

class PharmacistViewModel : ViewModel() {
    private val db = FirestoreProvider.getDb()
    private val auth = Firebase.auth

    private val _inventory = MutableStateFlow<List<InventoryItem>>(emptyList())
    val inventory: StateFlow<List<InventoryItem>> = _inventory

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _shopDetails = MutableStateFlow<Pharmacy?>(null)
    val shopDetails: StateFlow<Pharmacy?> = _shopDetails

    private val _requests = MutableStateFlow<List<Request>>(emptyList())
    val requests: StateFlow<List<Request>> = _requests

    private val _currentOrder = MutableStateFlow<Request?>(null)
    val currentOrder: StateFlow<Request?> = _currentOrder

    private val _transferRequests = MutableStateFlow<List<TransferRequest>>(emptyList())
    val transferRequests: StateFlow<List<TransferRequest>> = _transferRequests

    private val _stats = MutableStateFlow(PharmacyStats())
    val stats: StateFlow<PharmacyStats> = _stats

    private var requestsListener: ListenerRegistration? = null
    private var inventoryListener: ListenerRegistration? = null
    private var transferListener: ListenerRegistration? = null

    private var currentShopId: String? = null

    init {
        loadPharmacistData()
    }

    private fun loadPharmacistData() {
        val userId = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Find shop owned by this user
                val shopQuery = db.collection("pharmacies")
                    .whereEqualTo("ownerId", userId)
                    .limit(1)
                    .get()
                    .await()
                
                if (!shopQuery.isEmpty) {
                    val shop = shopQuery.toObjects(Pharmacy::class.java).first()
                    currentShopId = shop.shopId
                    _shopDetails.value = shop
                    
                    // Now start real-time listeners with the actual shop ID
                    startRealTimeRequests(shop.shopId)
                    startRealTimeInventory(shop.shopId)
                    startRealTimeTransfers(shop.shopId)
                } else {
                    Log.d("PharmacistVM", "No shop found for user $userId")
                }
            } catch (e: Exception) {
                Log.e("PharmacistVM", "Error loading pharmacist data: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadMockShop(onSuccess: () -> Unit) {
        val mockShopId = "SANJEEVINI_MOCK_SHOP"
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val doc = db.collection("pharmacies").document(mockShopId).get().await()
                val mockShop = if (doc.exists()) {
                    doc.toObject(Pharmacy::class.java)!!
                } else {
                    val newMockShop = Pharmacy(
                        shopId = mockShopId,
                        name = "Sanjeevini Mock Medical Store",
                        ownerName = "Mock Pharmacist",
                        village = "Sanjeevini Village",
                        address = "Mock Street, Sanjeevini",
                        phone = "0000000000",
                        isVerified = true
                    )
                    db.collection("pharmacies").document(mockShopId).set(newMockShop).await()
                    newMockShop
                }
                
                currentShopId = mockShopId
                _shopDetails.value = mockShop
                
                // Add mock inventory if empty
                val invSnapshot = db.collection("inventory").whereEqualTo("shopId", mockShopId).get().await()
                if (invSnapshot.isEmpty) {
                    val mockItems = listOf(
                        InventoryItem(UUID.randomUUID().toString(), mockShopId, "Paracetamol", quantity = 50, isLifeSaving = true),
                        InventoryItem(UUID.randomUUID().toString(), mockShopId, "Amoxicillin", quantity = 5, isLifeSaving = true),
                        InventoryItem(UUID.randomUUID().toString(), mockShopId, "Cetirizine", quantity = 0, isLifeSaving = false),
                        InventoryItem(UUID.randomUUID().toString(), mockShopId, "Insulin", quantity = 12, isLifeSaving = true)
                    )
                    mockItems.forEach { db.collection("inventory").document(it.itemId).set(it) }
                }

                // Add mock requests if empty
                val reqSnapshot = db.collection("requests").whereEqualTo("shopId", mockShopId).get().await()
                if (reqSnapshot.isEmpty) {
                    val mockRequest = Request(
                        requestId = "MOCK_REQ_1",
                        userId = "mock_user",
                        userName = "Ravi Kumar",
                        shopId = mockShopId,
                        status = "PENDING",
                        items = listOf(
                            OrderItem("Paracetamol", 2, isAutoFilled = true),
                            OrderItem("Amoxicillin", 1, isAutoFilled = false)
                        ),
                        // Real public image URL for testing
                        prescriptionUrl = "https://images.sampletemplates.com/wp-content/uploads/2016/08/Doctor-Prescription-Template.jpg",
                        timestamp = Timestamp.now()
                    )
                    db.collection("requests").document(mockRequest.requestId).set(mockRequest).await()
                }

                startRealTimeRequests(mockShopId)
                startRealTimeInventory(mockShopId)
                startRealTimeTransfers(mockShopId)
                onSuccess()
            } catch (e: Exception) {
                Log.e("PharmacistVM", "Error loading mock shop: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun fetchOrderDetails(orderId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val doc = db.collection("requests").document(orderId).get().await()
                _currentOrder.value = doc.toObject(Request::class.java)
            } catch (e: Exception) {
                Log.e("PharmacistVM", "Error fetching order: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun startRealTimeInventory(shopId: String) {
        inventoryListener?.remove()
        inventoryListener = db.collection("inventory")
            .whereEqualTo("shopId", shopId)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.e("PharmacistVM", "Inventory listen failed", e)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val items = snapshot.toObjects(InventoryItem::class.java)
                    _inventory.value = items
                    calculateStats(items)
                }
            }
    }

    private fun startRealTimeRequests(shopId: String) {
        requestsListener?.remove()
        requestsListener = db.collection("requests")
            .whereEqualTo("shopId", shopId)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.e("PharmacistVM", "Requests listen failed", e)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val list = snapshot.toObjects(Request::class.java)
                    _requests.value = list.sortedByDescending { it.timestamp }
                }
            }
    }

    private fun startRealTimeTransfers(shopId: String) {
        transferListener?.remove()
        transferListener = db.collection("transfers")
            .addSnapshotListener { snapshot, e ->
                if (e != null) return@addSnapshotListener
                if (snapshot != null) {
                    val all = snapshot.toObjects(TransferRequest::class.java)
                    _transferRequests.value = all.filter { 
                        it.fromShopId == shopId || it.toShopId == shopId 
                    }.sortedByDescending { it.timestamp }
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
        val shopId = currentShopId ?: return
        viewModelScope.launch {
            try {
                val newItemId = UUID.randomUUID().toString()
                val newItem = InventoryItem(
                    itemId = newItemId,
                    shopId = shopId,
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
                db.collection("requests").document(requestId).update("status", newStatus).await()
            } catch (e: Exception) {
                Log.e("PharmacistVM", "Error updating request: ${e.message}")
            }
        }
    }

    fun updateOrderItemStatus(orderId: String, medicineName: String, status: String, reason: String? = null) {
        viewModelScope.launch {
            try {
                val docRef = db.collection("requests").document(orderId)
                val snapshot = docRef.get().await()
                val request = snapshot.toObject(Request::class.java)
                
                if (request != null) {
                    val updatedItems = request.items.map { item ->
                        if (item.medicineName == medicineName) {
                            item.copy(status = status, rejectionReason = reason)
                        } else {
                            item
                        }
                    }
                    docRef.update("items", updatedItems).await()
                    // Update local state if it's the current order
                    if (_currentOrder.value?.requestId == orderId) {
                        _currentOrder.value = request.copy(items = updatedItems)
                    }
                }
            } catch (e: Exception) {
                Log.e("PharmacistVM", "Error updating item status: ${e.message}")
            }
        }
    }

    fun completeOrderReview(orderId: String, onComplete: () -> Unit) {
        viewModelScope.launch {
            try {
                val docRef = db.collection("requests").document(orderId)
                val snapshot = docRef.get().await()
                val request = snapshot.toObject(Request::class.java)
                
                if (request != null) {
                    val allRejected = request.items.all { it.status == "Rejected" }
                    val finalStatus = if (allRejected) "REJECTED" else "REVIEWED"
                    
                    docRef.update("status", finalStatus).await()
                    onComplete()
                }
            } catch (e: Exception) {
                Log.e("PharmacistVM", "Error completing review: ${e.message}")
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        requestsListener?.remove()
        inventoryListener?.remove()
        transferListener?.remove()
    }
}
