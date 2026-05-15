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

        Log.d("DatabaseSeeder", "Seeding database with North Bangalore pharmacies and 17 specific medicines...")

        // 1. Mock Users
        val users = listOf(
            User("user_001", "Ramesh Kumar", "Yelahanka", "+91 9876543210", "ABHA-1234-5678", "123, Gandhi Circle, Yelahanka"),
            User("user_002", "Suresh Gowda", "Hebbal", "+91 9876543211", "ABHA-2345-6789", "45, Temple Road, Hebbal"),
            User("user_003", "Lakshmamma", "Jakkur", "+91 9876543212", "ABHA-3456-7890", "House #12, Near Lake, Jakkur")
        )
        users.forEach { user -> db.collection("users").document(user.userId).set(user) }

        // 2. 4 Specific Pharmacies with realistic North Bangalore map coordinates
        val shops = listOf(
            Pharmacy(
                shopId = "shop_001",
                ownerId = "owner_001",
                name = "Raju Medical & General Stores",
                ownerName = "Raju K.",
                village = "Yelahanka Old Town",
                address = "D-23, Old Bus Stand Road, Yelahanka, Bengaluru 560064",
                latitude = 13.1007,
                longitude = 77.5963,
                phone = "080-27622222",
                licenseNumber = "LIC-001-YEL",
                isVerified = true
            ),
            Pharmacy(
                shopId = "shop_002",
                name = "Sanjeevini Pharma",
                ownerName = "Dr. Suresh Gowda",
                village = "Hebbal",
                address = "Main Road, Opp. Hebbal Flyover, Bengaluru 560024",
                latitude = 13.0358,
                longitude = 77.5970,
                phone = "080-27723333",
                licenseNumber = "LIC-002-HEB",
                isVerified = true
            ),
            Pharmacy(
                shopId = "shop_003",
                name = "Sri Rama Medicals",
                ownerName = "Manjunath",
                village = "Jakkur",
                address = "Jakkur Main Road, Near Jakkur Airfield, Bengaluru 560064",
                latitude = 13.0784,
                longitude = 77.6046,
                phone = "08151-234567",
                licenseNumber = "LIC-003-RAMA",
                isVerified = true
            ),
            Pharmacy(
                shopId = "shop_004",
                name = "Janatha Bazar Drugs",
                ownerName = "Basavaraj",
                village = "Sahakar Nagar",
                address = "Market Square, Sahakar Nagar, Bengaluru 560092",
                latitude = 13.0623,
                longitude = 77.5871,
                phone = "080-27745555",
                licenseNumber = "LIC-004-MGD",
                isVerified = true
            )
        )
        shops.forEach { shop -> db.collection("pharmacies").document(shop.shopId).set(shop) }

        // 3. 17 Specific Medicines with mapped lowercase .png drawable filenames
        val medicines = listOf(
            InventoryItem(medicineName = "ORS Sachet", category = "Hydration", form = "Powder", imageResName = "ors", isLifeSaving = false, requiresPrescription = false),
            InventoryItem(medicineName = "Insulin Glargine", category = "Anti-Diabetic", form = "Injection", imageResName = "insulin", isLifeSaving = true, requiresPrescription = true),
            InventoryItem(medicineName = "Rabezol DSR", category = "Gastrointestinal", form = "Capsules", imageResName = "rabezol", isLifeSaving = false, requiresPrescription = true),
            InventoryItem(medicineName = "Meftal P", category = "Analgesic", form = "Syrup", imageResName = "meftal_p", isLifeSaving = false, requiresPrescription = false),
            InventoryItem(medicineName = "Calpol Syrup", category = "Analgesic", form = "Syrup", imageResName = "calpol_syp", isLifeSaving = false, requiresPrescription = false),
            InventoryItem(medicineName = "Delcon Syrup", category = "Anti-Allergic", form = "Syrup", imageResName = "delcon_syp", isLifeSaving = false, requiresPrescription = false),
            InventoryItem(medicineName = "Amoxicillin 500mg", category = "Antibiotic", form = "Capsules", imageResName = "amoxicillin", isLifeSaving = false, requiresPrescription = true),
            InventoryItem(medicineName = "Levolin Syrup", category = "Respiratory", form = "Syrup", imageResName = "levolin_syp", isLifeSaving = false, requiresPrescription = true),
            InventoryItem(medicineName = "Paracetamol 650mg", category = "Analgesic", form = "Tablets", imageResName = "paracetamol", isLifeSaving = false, requiresPrescription = false),
            InventoryItem(medicineName = "Sermosone M", category = "Dermatology", form = "Cream", imageResName = "sermosone_m", isLifeSaving = false, requiresPrescription = true),
            InventoryItem(medicineName = "Emasoft Soap", category = "Dermatology", form = "Soap", imageResName = "emasoft_soap", isLifeSaving = false, requiresPrescription = false),
            InventoryItem(medicineName = "Tab Novanib T", category = "Oncology", form = "Tablets", imageResName = "tab_novanib_t", isLifeSaving = false, requiresPrescription = true),
            InventoryItem(medicineName = "Dolo 650 Tablet", category = "Analgesic", form = "Tablets", imageResName = "dolo_650_tablet", isLifeSaving = false, requiresPrescription = false),
            InventoryItem(medicineName = "Vetadeiv Lotion", category = "Dermatology", form = "Lotion", imageResName = "vetadeiv_lotion", isLifeSaving = false, requiresPrescription = false),
            InventoryItem(medicineName = "Chymoral AP Tablet", category = "Anti-inflammatory", form = "Tablets", imageResName = "chymoral_ap_tablet", isLifeSaving = false, requiresPrescription = true),
            InventoryItem(medicineName = "Cetirizine 10mg", category = "Anti-Allergic", form = "Tablets", imageResName = "cetirizine_10mg_tablet", isLifeSaving = false, requiresPrescription = false),
            InventoryItem(medicineName = "Azithromycin 500mg", category = "Antibiotic", form = "Tablets", imageResName = "azithromycin_500mg_tablet", isLifeSaving = false, requiresPrescription = true)
        )

        var itemIdCounter = 1
        shops.forEach { shop ->
            medicines.forEach { med ->
                val quantity = (10..100).random()
                val item = med.copy(
                    itemId = "item_${itemIdCounter++}",
                    shopId = shop.shopId,
                    quantity = quantity,
                    expiryDate = Timestamp(dateFormat.parse("2026-${(1..12).random()}-${(1..28).random()}")!!)
                )
                db.collection("inventory").document(item.itemId).set(item)
            }
        }
    }
}
