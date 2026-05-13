package com.example.gramasanjeevin.utils

import android.util.Log
import com.example.gramasanjeevin.model.*
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Locale

object DatabaseSeeder {

    fun seedDatabase() {
        // Use the centralized FirestoreProvider to ensure we connect to the "GramaSanjeevin" database
        val db = FirestoreProvider.getDb()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        Log.d("DatabaseSeeder", "Seeding database with default mock data...")

        // 1. Mock User (Ramesh Kumar)
        val mockUser = User(
            userId = "user_001",
            name = "Ramesh Kumar",
            village = "Doddaballapur",
            phone = "+91 9876543210",
            healthId = "ABHA-1234-5678",
            address = "123, Gandhi Circle, Doddaballapur"
        )
        db.collection("users").document(mockUser.userId).set(mockUser)
            .addOnSuccessListener { Log.d("DatabaseSeeder", "User user_001 seeded successfully") }
            .addOnFailureListener { Log.e("DatabaseSeeder", "Failed to seed user", it) }

        // 2. Mock Pharmacies
        val shops = listOf(
            Pharmacy(
                shopId = "shop_001",
                name = "Raju Medical",
                ownerName = "Raju K.",
                village = "Doddaballapur",
                address = "Near Bus Stand, Doddaballapur",
                latitude = 13.2924,
                longitude = 77.5438,
                phone = "9876543210",
                licenseNumber = "LIC-001-RAJU",
                isVerified = true,
                inventoryIds = listOf("item_1", "item_2", "item_3", "item_4", "item_5")
            )
        )

        shops.forEach { shop ->
            db.collection("pharmacies").document(shop.shopId).set(shop)
                .addOnSuccessListener { Log.d("DatabaseSeeder", "Shop ${shop.shopId} seeded") }
        }

        // 3. Mock Inventory
        val inventory = listOf(
            InventoryItem("item_1", "shop_001", "Amoxicillin 500mg", "Antibiotic", "Capsules", 142, false, Timestamp(dateFormat.parse("2026-05-15")!!)),
            InventoryItem("item_2", "shop_001", "Paracetamol 650mg", "Analgesic", "Tablets", 8, false, Timestamp(dateFormat.parse("2026-12-01")!!)),
            InventoryItem("item_3", "shop_001", "Metformin 500mg", "Anti-Diabetic", "Tablets", 450, false, Timestamp(dateFormat.parse("2027-01-10")!!)),
            InventoryItem("item_4", "shop_001", "Cetirizine 10mg", "Anti-Allergic", "Tablets", 210, false, Timestamp(dateFormat.parse("2026-08-20")!!) ),
            InventoryItem("item_5", "shop_001", "Insulin Glargine", "Anti-Diabetic", "Injection", 5, true, Timestamp(dateFormat.parse("2025-10-10")!!) )
        )

        inventory.forEach { item ->
            db.collection("inventory").document(item.itemId).set(item)
                .addOnSuccessListener { Log.d("DatabaseSeeder", "Item ${item.itemId} seeded") }
        }
    }
}
