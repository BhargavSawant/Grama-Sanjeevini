package com.example.gramasanjeevin.utils

import com.google.firebase.firestore.FirebaseFirestore

/**
 * Central provider for Firestore to ensure consistency across the app.
 * Using the default Firestore database instance.
 */
object FirestoreProvider {
    fun getDb(): FirebaseFirestore {
        return FirebaseFirestore.getInstance()
    }
}
