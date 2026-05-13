package com.example.gramasanjeevin.model

import com.google.firebase.Timestamp

enum class RequestType {
    RESTOCK, DELIVERY
}

data class Request(
    val requestId: String = "",
    val shopId: String = "",
    val userId: String = "user_001",
    val userName: String = "Unknown User", // Display name instead of ID
    val type: RequestType = RequestType.DELIVERY,
    val items: List<String> = emptyList(),
    val timestamp: Timestamp = Timestamp.now(),
    val status: String = "PENDING" // PENDING, APPROVED, DECLINED, COMPLETED
)
