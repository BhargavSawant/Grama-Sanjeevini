package com.example.gramasanjeevin.model

data class SearchResult(
    val item: InventoryItem,
    val pharmacy: Pharmacy,
    val distanceKm: Double
)