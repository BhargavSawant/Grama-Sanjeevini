package com.example.gramasanjeevin.utils

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore

/**
 * Central provider for Firestore to ensure consistency across the app.
 * Requirement: project name and database name should be same ("GramaSanjeevin").
 */
object FirestoreProvider {
    fun getDb(): FirebaseFirestore {
        return try {
            // Attempt to connect to the database with ID "GramaSanjeevin" 
            // as per the requirement: "project name and database name should be same"
            FirebaseFirestore.getInstance("GramaSanjeevin")
        } catch (e: Exception) {
            // Fallback to the default instance if the named one isn't initialized/created
            Log.e("FirestoreProvider", "Could not get GramaSanjeevin database, using default", e)
            FirebaseFirestore.getInstance()
        }
    }
}
