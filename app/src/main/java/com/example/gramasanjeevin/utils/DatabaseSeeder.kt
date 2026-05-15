package com.example.gramasanjeevin.utils

import android.util.Log
import com.example.gramasanjeevin.model.*
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Locale

object DatabaseSeeder {

    fun seedDatabase() {
        val db = FirebaseFirestore.getInstance()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        Log.d("DatabaseSeeder", "Seeding database with rural Karnataka pharmacies and Rx medicines...")

        // 1. Mock Users
        val users = listOf(
            User("user_001", "Ramesh Kumar", "Doddaballapur", "+91 9876543210", "ABHA-1234-5678", "123, Gandhi Circle, Doddaballapur"),
            User("user_002", "Suresh Gowda", "Nelamangala", "+91 9876543211", "ABHA-2345-6789", "45, Temple Road, Nelamangala"),
            User("user_003", "Lakshmamma", "Malur", "+91 9876543212", "ABHA-3456-7890", "House #12, Near Lake, Malur")
        )
        users.forEach { user -> db.collection("users").document(user.userId).set(user) }

        // 2. Real-ish Rural Karnataka Pharmacies
        val shops = listOf(
            Pharmacy(
                shopId = "shop_001",
                name = "Raju Medical & General Stores",
                ownerName = "Raju K.",
                village = "Doddaballapur",
                address = "D-23, Old Bus Stand Road, Doddaballapur, Karnataka 561203",
                latitude = 13.2934,
                longitude = 77.5448,
                phone = "080-27622222",
                licenseNumber = "LIC-001-DBP",
                isVerified = true
            ),
            Pharmacy(
                shopId = "SANJEEVINI_MOCK_SHOP",
                name = "Sanjeevini Mock Medical Store",
                ownerName = "Dr. Suresh Mock",
                village = "Sanjeevini Village",
                address = "Mock Street, Sanjeevini, Karnataka",
                latitude = 13.0975,
                longitude = 77.3942,
                phone = "080-27723333",
                licenseNumber = "LIC-MOCK-SANJ",
                isVerified = true
            )
        )

        shops.forEach { shop -> db.collection("pharmacies").document(shop.shopId).set(shop) }

        // 3. Mock Inventory
        val medicineTemplates = listOf(
            Triple("Amoxicillin 500mg", "Antibiotic", "Capsules"),
            Triple("Paracetamol 650mg", "Analgesic", "Tablets"),
            Triple("Metformin 500mg", "Anti-Diabetic", "Tablets"),
            Triple("Cetirizine 10mg", "Anti-Allergic", "Tablets"),
            Triple("Insulin Glargine", "Anti-Diabetic", "Injection")
        )
        
        val rxMedicines = listOf(
            Triple("Diazepam 5mg", "Sedative", "Tablets"),
            Triple("Tramadol 50mg", "Opioid Analgesic", "Capsules")
        )

        var itemIdCounter = 1
        shops.forEach { shop ->
            medicineTemplates.forEach { (name, category, form) ->
                db.collection("inventory").document("item_${itemIdCounter++}").set(
                    InventoryItem(
                        itemId = "item_${itemIdCounter}",
                        shopId = shop.shopId,
                        medicineName = name,
                        category = category,
                        form = form,
                        quantity = (50..200).random(),
                        isLifeSaving = name.contains("Insulin"),
                        requiresPrescription = false,
                        expiryDate = Timestamp(dateFormat.parse("2026-12-31")!!)
                    )
                )
            }
            rxMedicines.forEach { (name, category, form) ->
                db.collection("inventory").document("item_${itemIdCounter++}").set(
                    InventoryItem(
                        itemId = "item_${itemIdCounter}",
                        shopId = shop.shopId,
                        medicineName = name,
                        category = category,
                        form = form,
                        quantity = (10..30).random(),
                        isLifeSaving = false,
                        requiresPrescription = true,
                        expiryDate = Timestamp(dateFormat.parse("2026-12-31")!!)
                    )
                )
            }
        }

        // 4. Initial Requests for Mock Shop
        val mockRequests = listOf(
            Request(
                requestId = "mock_req_001",
                shopId = "SANJEEVINI_MOCK_SHOP",
                userId = "user_001",
                userName = "Ramesh Kumar",
                type = RequestType.DELIVERY,
                items = listOf(
                    OrderItem(medicineName = "Amoxicillin 500mg", quantity = 1, requiresPrescription = false),
                    OrderItem(medicineName = "Diazepam 5mg", quantity = 1, requiresPrescription = true)
                ),
                status = "PENDING",
                prescriptionUrl = "https://example.com/prescription.jpg"
            ),
            Request(
                requestId = "mock_req_002",
                shopId = "SANJEEVINI_MOCK_SHOP",
                userId = "user_002",
                userName = "Suresh Gowda",
                type = RequestType.RESTOCK,
                items = listOf(
                    OrderItem(medicineName = "Insulin Glargine", quantity = 2, requiresPrescription = false)
                ),
                status = "PENDING"
            )
        )
        
        mockRequests.forEach { req ->
            db.collection("requests").document(req.requestId).set(req)
        }
    }
}
