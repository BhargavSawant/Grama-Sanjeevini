package com.example.gramasanjeevin.model

import com.google.firebase.Timestamp

enum class RequestType {
    RESTOCK, DELIVERY
}

data class OrderItem(
    val medicineName: String = "",
    val quantity: Int = 1,
    val requiresPrescription: Boolean = false,
    val isAutoFilled: Boolean = false,
    val status: String = "Pending",
    val rejectionReason: String? = null,
    val imageResName: String = "" // Added for medicine image support
)

data class Request(
    val requestId: String = "",
    val shopId: String = "",
    val userId: String = "user_001",
    val userName: String = "Unknown User",
    val type: RequestType = RequestType.DELIVERY,
    val items: List<OrderItem> = emptyList(),
    val timestamp: Timestamp = Timestamp.now(),
    val status: String = "PENDING", // PENDING, APPROVED, DECLINED, COMPLETED
    val prescriptionUrl: String? = null // URL to the uploaded prescription photo
)
