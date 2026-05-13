package com.example.gramasanjeevin.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gramasanjeevin.model.Pharmacy
import com.example.gramasanjeevin.utils.FirestoreProvider
import com.example.gramasanjeevin.utils.LocationUtils
import kotlinx.coroutines.tasks.await

private val Teal = Color(0xFF00695C)
private val TextPrimary = Color(0xFF1A2B35)
private val TextMuted = Color(0xFF6B7280)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HubsScreen(onBack: () -> Unit) {
    val db = FirestoreProvider.getDb()
    var pharmacies by remember { mutableStateOf<List<Pharmacy>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    // Mock User Location (Bengaluru)
    val userLat = 13.0334
    val userLng = 77.5891

    LaunchedEffect(Unit) {
        try {
            val snapshot = db.collection("pharmacies").get().await()
            val list = snapshot.toObjects(Pharmacy::class.java)
            // Sort by distance
            pharmacies = list.sortedBy { 
                LocationUtils.calculateDistance(userLat, userLng, it.latitude, it.longitude)
            }
        } catch (e: Exception) {
            // handle error
        } finally {
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nearest Medical Hubs", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.LocationOn, contentDescription = "Back", tint = Teal)
                    }
                }
            )
        },
        containerColor = Color(0xFFF8F9FB)
    ) { padding ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Teal)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(pharmacies) { pharmacy ->
                    HubCard(pharmacy, userLat, userLng)
                }
            }
        }
    }
}

@Composable
fun HubCard(pharmacy: Pharmacy, userLat: Double, userLng: Double) {
    val context = LocalContext.current
    val distance = LocationUtils.calculateDistance(userLat, userLng, pharmacy.latitude, pharmacy.longitude)
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(pharmacy.name, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = TextPrimary)
                    Text(pharmacy.village, fontSize = 14.sp, color = TextMuted)
                }
                Surface(
                    color = Color(0xFFE0F2F1),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "$distance km",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Teal
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Map Placeholder
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFE0E0E0)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Map, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(40.dp))
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    LocationUtils.launchGoogleMaps(context, pharmacy.latitude, pharmacy.longitude, pharmacy.name)
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Teal),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Directions, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Get Directions", fontWeight = FontWeight.Bold)
            }
        }
    }
}
