package com.example.gramasanjeevin.model

/**
 * Model representing a Pharmacy/Medical Store.
 * Includes details for the store profile and verification.
 */
data class Pharmacy(
    val shopId: String = "",
    val name: String = "",          // Store Name
    val ownerName: String = "",     // Store Owner
    val village: String = "",
    val address: String = "",       // Store Address
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val phone: String = "",         // Store Number
    val licenseNumber: String = "",
    val isVerified: Boolean = false,
    val licenseUrls: List<String> = emptyList(), // List of license document URLs
    val inventoryIds: List<String> = emptyList() // List of item IDs in this store
)
