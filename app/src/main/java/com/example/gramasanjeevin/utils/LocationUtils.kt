package com.example.gramasanjeevin.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import kotlin.math.*

object LocationUtils {

    // The Haversine Formula: Calculates true distance between two GPS coordinates in Kilometers
    fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val earthRadius = 6371.0 // Radius of the earth in km
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        val distance = earthRadius * c
        return (distance * 10.0).roundToInt() / 10.0 // Round to 1 decimal place
    }

    // Securely hands the coordinates over to the Google Maps App
    fun launchGoogleMaps(context: Context, destLat: Double, destLng: Double, shopName: String) {
        // This creates a URI that drops a pin and labels it with the pharmacy's name!
        val gmmIntentUri = Uri.parse("geo:0,0?q=$destLat,$destLng($shopName)")
        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
        mapIntent.setPackage("com.google.android.apps.maps")

        // Tells Android to start the Maps app
        context.startActivity(mapIntent)
    }
}