package com.example.gramasanjeevin.model

import com.google.firebase.Timestamp

data class TransferRequest(
    val transferId: String = "",
    val fromShopId: String = "",
    val toShopId: String = "",
    val fromShopName: String = "",
    val toShopName: String = "",
    val medicineName: String = "",
    val quantity: Int = 0,
    val timestamp: Timestamp = Timestamp.now(),
    val status: String = "PENDING" // PENDING, APPROVED, DECLINED, COMPLETED
)
