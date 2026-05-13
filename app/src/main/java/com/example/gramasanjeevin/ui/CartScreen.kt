package com.example.gramasanjeevin.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.gramasanjeevin.model.RequestType
import com.example.gramasanjeevin.utils.LocationUtils
import kotlinx.coroutines.launch

private val Teal = Color(0xFF00695C)
private val TextPrimary = Color(0xFF1A2B35)
private val TextMuted = Color(0xFF6B7280)

@Composable
fun CartScreen(
    navController: androidx.navigation.NavController,
    viewModel: CartViewModel = viewModel()
) {
    val matches by viewModel.matches.collectAsState()
    val cartItems by viewModel.cartItems.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val requestStatus by viewModel.requestStatus.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(requestStatus) {
        requestStatus?.let {
            scope.launch {
                snackbarHostState.showSnackbar(it)
                viewModel.clearRequestStatus()
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color(0xFFF8F9FB)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.LocalHospital, contentDescription = null, tint = Teal)
                Spacer(Modifier.width(8.dp))
                Text("Grama-Sanjeevini", fontWeight = FontWeight.Bold, color = Teal, fontSize = 18.sp)
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Text(
                        text = "Smart Cart Matches",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    if (cartItems.isNotEmpty()) {
                        Text(
                            text = "Find the best pharmacy options for your ${cartItems.size} needed items.",
                            fontSize = 16.sp,
                            color = TextMuted,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }

                if (cartItems.isNotEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(12.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Items in Cart:", fontWeight = FontWeight.Bold, color = Teal, modifier = Modifier.padding(bottom = 8.dp))
                                cartItems.forEach { item ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(item.medicineName, color = TextPrimary)
                                        IconButton(onClick = { viewModel.removeFromCart(item.medicineName) }, modifier = Modifier.size(24.dp)) {
                                            Icon(Icons.Default.Delete, contentDescription = "Remove", tint = Color.Red, modifier = Modifier.size(18.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                if (isLoading) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = Teal)
                        }
                    }
                } else if (cartItems.isEmpty()) {
                    item {
                        EmptyCartView(onGoSearch = { navController.navigate("search") })
                    }
                } else {
                    // Complete Matches
                    val completeMatches = matches.filter { it.matchPercentage == 100 }
                    if (completeMatches.isNotEmpty()) {
                        item {
                            SectionHeader(title = "COMPLETE MATCHES (ALL ITEMS)", icon = Icons.Default.CheckCircle, color = Color(0xFF2E7D32))
                        }
                        items(completeMatches) { match ->
                            PharmacyMatchCard(match = match, isComplete = true, viewModel = viewModel)
                        }
                    }

                    // Partial Matches
                    val partialMatches = matches.filter { it.matchPercentage in 1..99 }
                    if (partialMatches.isNotEmpty()) {
                        item {
                            SectionHeader(title = "PARTIAL MATCHES", icon = Icons.Default.Info, color = Color(0xFFE65100))
                        }
                        items(partialMatches) { match ->
                            PharmacyMatchCard(match = match, isComplete = false, viewModel = viewModel)
                        }
                    }
                    
                    if (matches.isEmpty()) {
                        item {
                            Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                                Text("No matching pharmacies found for these items.", color = TextMuted)
                            }
                        }
                    }
                }
                
                item { Spacer(modifier = Modifier.height(32.dp)) }
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 8.dp)) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(8.dp))
        Text(text = title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = color)
    }
}

@Composable
private fun PharmacyMatchCard(match: PharmacyMatch, isComplete: Boolean, viewModel: CartViewModel) {
    val context = LocalContext.current
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = match.pharmacy.name, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = TextMuted, modifier = Modifier.size(14.dp))
                        Text(text = "${match.distanceKm} km away", fontSize = 14.sp, color = TextMuted)
                    }
                }
                
                Surface(
                    color = if (isComplete) Color(0xFFE8F5E9) else Color(0xFFFFF3E0),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = if (isComplete) "100% Match" else "${match.matchedItems.size} Items",
                        color = if (isComplete) Color(0xFF2E7D32) else Color(0xFFE65100),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Items List
            match.matchedItems.forEach { item ->
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 2.dp)) {
                    Text(text = item.medicineName, modifier = Modifier.weight(1f), color = TextPrimary)
                    Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFF2E7D32), modifier = Modifier.size(16.dp))
                }
            }

            if (match.missingItems.isNotEmpty()) {
                Text(text = "Missing:", color = Color(0xFFC62828), fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp))
                match.missingItems.forEach { missing ->
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 2.dp)) {
                        Text(text = missing, modifier = Modifier.weight(1f), color = Color(0xFFC62828))
                        Icon(Icons.Default.Close, contentDescription = null, tint = Color(0xFFC62828), modifier = Modifier.size(16.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 1. Get Directions Button
            Button(
                onClick = { 
                    LocationUtils.launchGoogleMaps(context, match.pharmacy.latitude, match.pharmacy.longitude, match.pharmacy.name)
                },
                modifier = Modifier.fillMaxWidth().height(44.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Teal),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.Directions, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Get Directions")
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 2. Request Re-stock Button (Order requested: Above Request Delivery, below Get Directions)
            if (match.missingItems.isNotEmpty()) {
                OutlinedButton(
                    onClick = { 
                        viewModel.sendRequest(match.pharmacy.shopId, RequestType.RESTOCK, match.missingItems)
                    },
                    modifier = Modifier.fillMaxWidth().height(44.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFE65100)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE65100)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Request Re-stock")
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            // 3. Request Home Delivery Button (Now per medical shop)
            Button(
                onClick = { 
                    viewModel.sendRequest(match.pharmacy.shopId, RequestType.DELIVERY, match.matchedItems.map { it.medicineName })
                },
                modifier = Modifier.fillMaxWidth().height(44.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.LocalShipping, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Request Home Delivery")
            }
        }
    }
}

@Composable
private fun EmptyCartView(onGoSearch: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 64.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(Icons.Default.ShoppingCart, contentDescription = null, tint = Color(0xFFE0F2F1), modifier = Modifier.size(80.dp))
        Spacer(modifier = Modifier.height(16.dp))
        Text("Your cart is empty", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = TextPrimary)
        Text("Add medicines to see smart matches.", color = TextMuted)
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onGoSearch, colors = ButtonDefaults.buttonColors(containerColor = Teal)) {
            Text("Go to Search")
        }
    }
}
