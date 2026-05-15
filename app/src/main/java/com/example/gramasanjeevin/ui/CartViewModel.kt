package com.example.gramasanjeevin.ui

import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gramasanjeevin.model.InventoryItem
import com.example.gramasanjeevin.model.OrderItem
import com.example.gramasanjeevin.model.Pharmacy
import com.example.gramasanjeevin.model.Request
import com.example.gramasanjeevin.model.RequestType
import com.example.gramasanjeevin.model.User
import com.example.gramasanjeevin.utils.FirestoreProvider
import com.google.firebase.Timestamp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream
import java.util.UUID

// Updated CartItem to match the new requirements
data class CartItem(
    val medicineName: String,
    val quantity: Int = 1,
    val requiresPrescription: Boolean = false,
    val isAutoFilled: Boolean = false,
    val status: String = "Pending",
    val rejectionReason: String? = null,
    val imageResName: String = "" // Added for medicine image support
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
    private val storage = FirebaseStorage.getInstance()
    private val auth = Firebase.auth

    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    val cartItems: StateFlow<List<CartItem>> = _cartItems

    private val _matches = MutableStateFlow<List<PharmacyMatch>>(emptyList())
    val matches: StateFlow<List<PharmacyMatch>> = _matches

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _requestStatus = MutableStateFlow<String?>(null)
    val requestStatus: StateFlow<String?> = _requestStatus

    private val _prescriptionUri = MutableStateFlow<Uri?>(null)
    val prescriptionUri: StateFlow<Uri?> = _prescriptionUri

    private val _prescriptionUrl = MutableStateFlow<String?>(null)
    val prescriptionUrl: StateFlow<String?> = _prescriptionUrl

    private val _isUploadingPrescription = MutableStateFlow(false)
    val isUploadingPrescription: StateFlow<Boolean> = _isUploadingPrescription

    /**
     * Clear local cart state completely.
     */
    fun clearCart() {
        _cartItems.value = emptyList()
        _matches.value = emptyList()
        _prescriptionUri.value = null
        _prescriptionUrl.value = null
        _requestStatus.value = null
        _isLoading.value = false
    }

    fun addToCart(medicineName: String, isAutoFilled: Boolean = false) {
        viewModelScope.launch {
            val currentList = _cartItems.value.toMutableList()
            if (currentList.none { it.medicineName.equals(medicineName, ignoreCase = true) }) {
                // Fetch item details to see if it requires prescription and get image
                val itemSnapshot = db.collection("inventory")
                    .whereEqualTo("medicineName", medicineName)
                    .limit(1)
                    .get()
                    .await()
                
                var requiresPrescription = false
                var imageResName = ""
                
                if (!itemSnapshot.isEmpty) {
                    val doc = itemSnapshot.documents[0]
                    requiresPrescription = doc.getBoolean("requiresPrescription") ?: false
                    imageResName = doc.getString("imageResName") ?: ""
                }

                currentList.add(CartItem(
                    medicineName = medicineName, 
                    requiresPrescription = requiresPrescription,
                    isAutoFilled = isAutoFilled,
                    imageResName = imageResName
                ))
                _cartItems.value = currentList
                findSmartMatches()
            }
        }
    }

    fun addMultipleToCart(names: List<String>) {
        viewModelScope.launch {
            val currentList = _cartItems.value.toMutableList()
            var added = false
            for (name in names) {
                if (currentList.none { it.medicineName.equals(name, ignoreCase = true) }) {
                    val itemSnapshot = db.collection("inventory")
                        .whereEqualTo("medicineName", name)
                        .limit(1)
                        .get()
                        .await()
                    
                    var requiresPrescription = false
                    var imageResName = ""
                    
                    if (!itemSnapshot.isEmpty) {
                        val doc = itemSnapshot.documents[0]
                        requiresPrescription = doc.getBoolean("requiresPrescription") ?: false
                        imageResName = doc.getString("imageResName") ?: ""
                    }

                    currentList.add(CartItem(
                        medicineName = name, 
                        requiresPrescription = requiresPrescription,
                        imageResName = imageResName
                    ))
                    added = true
                }
            }
            if (added) {
                _cartItems.value = currentList
                findSmartMatches()
            }
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

    fun uploadPrescription(uri: Uri) {
        _isUploadingPrescription.value = true
        val fileName = "prescriptions/${UUID.randomUUID()}.jpg"
        val ref = storage.reference.child(fileName)

        viewModelScope.launch {
            try {
                val downloadUri = ref.putFile(uri).continueWithTask { task ->
                    if (!task.isSuccessful) {
                        task.exception?.let { throw it }
                    }
                    ref.downloadUrl
                }.await()
                
                _prescriptionUrl.value = downloadUri.toString()
                _prescriptionUri.value = uri
                _requestStatus.value = "Prescription uploaded successfully!"
            } catch (e: Exception) {
                _requestStatus.value = "Prescription upload failed: ${e.message}"
            } finally {
                _isUploadingPrescription.value = false
            }
        }
    }

    fun uploadPrescriptionBitmap(bitmap: Bitmap) {
        _isUploadingPrescription.value = true
        val fileName = "prescriptions/${UUID.randomUUID()}.jpg"
        val ref = storage.reference.child(fileName)

        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()

        viewModelScope.launch {
            try {
                val downloadUri = ref.putBytes(data).continueWithTask { task ->
                    if (!task.isSuccessful) {
                        task.exception?.let { throw it }
                    }
                    ref.downloadUrl
                }.await()
                
                _prescriptionUrl.value = downloadUri.toString()
                _prescriptionUri.value = null
                _requestStatus.value = "Prescription photo uploaded!"
            } catch (e: Exception) {
                _requestStatus.value = "Photo upload failed: ${e.message}"
            } finally {
                _isUploadingPrescription.value = false
            }
        }
    }

    fun sendRequest(shopId: String, type: RequestType) {
        val currentUserId = auth.currentUser?.uid ?: return
        
        viewModelScope.launch {
            try {
                val userDoc = db.collection("users").document(currentUserId).get().await()
                val user = userDoc.toObject(User::class.java)
                val userName = user?.name ?: "Unknown User"

                val orderItems = _cartItems.value.map { 
                    OrderItem(
                        medicineName = it.medicineName,
                        quantity = it.quantity,
                        requiresPrescription = it.requiresPrescription,
                        isAutoFilled = it.isAutoFilled,
                        status = "Pending",
                        rejectionReason = it.rejectionReason,
                        imageResName = it.imageResName
                    )
                }

                val requestId = UUID.randomUUID().toString()
                val request = Request(
                    requestId = requestId,
                    shopId = shopId,
                    userId = currentUserId,
                    userName = userName,
                    type = type,
                    items = orderItems,
                    status = "PENDING",
                    prescriptionUrl = _prescriptionUrl.value
                )
                db.collection("requests").document(requestId).set(request).await()
                _requestStatus.value = "Request Sent Successfully!"
                
                clearCart()
            } catch (e: Exception) {
                _requestStatus.value = "Failed to send request: ${e.message}"
            }
        }
    }

    fun clearRequestStatus() {
        _requestStatus.value = null
    }

    fun clearPrescription() {
        _prescriptionUri.value = null
        _prescriptionUrl.value = null
    }
}
