package com.example.gramasanjeevin.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Storefront
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.gramasanjeevin.model.Pharmacy
import com.example.gramasanjeevin.utils.FirestoreProvider
import com.example.gramasanjeevin.utils.L
import com.example.gramasanjeevin.utils.LocationUtils
import kotlinx.coroutines.tasks.await
import java.util.Locale

private val Teal = Color(0xFF00695C)
private val TextPrimary = Color(0xFF1A2B35)
private val TextMuted = Color(0xFF6B7280)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HubsScreen(
    authViewModel: AuthViewModel = viewModel(),
    onBack: () -> Unit
) {
    val db = FirestoreProvider.getDb()
    var pharmacies by remember { mutableStateOf<List<Pharmacy>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    val isEnglish by authViewModel.isEnglish.collectAsState()

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
                title = { Text(L.nearbyHubs(isEnglish), fontWeight = FontWeight.Bold, color = Color.Black) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = L.back(isEnglish), tint = Color.Black)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
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
                contentPadding = PaddingValues(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(pharmacies) { pharmacy ->
                    PharmacyHubCard(pharmacy, userLat, userLng, isEnglish)
                }
            }
        }
    }
}

@Composable
fun PharmacyHubCard(pharmacy: Pharmacy, userLat: Double, userLng: Double, isEnglish: Boolean) {
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
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Styled Storefront Icon
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Teal.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Storefront, contentDescription = null, tint = Teal, modifier = Modifier.size(28.dp))
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = pharmacy.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = TextPrimary
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = TextMuted, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = pharmacy.address,
                            fontSize = 13.sp,
                            color = TextMuted,
                            maxLines = 1
                        )
                    }
                }
                
                Surface(
                    color = Color(0xFFE0F2F1),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = String.format(Locale.getDefault(), "%.1f km", distance),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Teal
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Get Directions Button (Visually distinct)
            Button(
                onClick = {
                    LocationUtils.launchGoogleMaps(context, pharmacy.latitude, pharmacy.longitude, pharmacy.name)
                },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Teal,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Directions, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(L.getDirections(isEnglish), fontWeight = FontWeight.Bold)
            }
        }
    }
}
