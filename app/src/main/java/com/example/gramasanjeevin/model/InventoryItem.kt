package com.example.gramasanjeevin.model

import com.google.firebase.Timestamp

data class InventoryItem(
    val itemId: String = "",
    val shopId: String = "",
    val medicineName: String = "",
    val category: String = "General",
    val form: String = "Tablets",
    val quantity: Int = 0,
    val isLifeSaving: Boolean = false,
    val expiryDate: Timestamp = Timestamp.now()
)
