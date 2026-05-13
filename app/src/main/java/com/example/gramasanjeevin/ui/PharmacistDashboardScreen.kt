package com.example.gramasanjeevin.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController

private val DarkGreen = Color(0xFF00695C)
private val EmergencyRed = Color(0xFFC62828)
private val TextPrimary = Color(0xFF1A2B35)
private val TextMuted = Color(0xFF6B7280)
private val PageBg = Color(0xFFF8F9FB)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PharmacistDashboardScreen(
    navController: NavController,
    viewModel: PharmacistViewModel = viewModel()
) {
    val stats by viewModel.stats.collectAsState()
    val shopDetails by viewModel.shopDetails.collectAsState()
    val requests by viewModel.requests.collectAsState()
    val pendingRequests = requests.count { it.status == "PENDING" }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PageBg)
            .verticalScroll(rememberScrollState())
    ) {
        // Top Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.LocalHospital,
                    contentDescription = null,
                    tint = DarkGreen,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Grama-Sanjeevini",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkGreen
                )
            }
            
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .clickable { navController.navigate("pharmacist_profile") },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Person, contentDescription = "Profile", tint = DarkGreen)
            }
        }

        Column(modifier = Modifier.padding(horizontal = 24.dp)) {
            Text(
                text = "Pharmacy Dashboard",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = shopDetails?.name ?: "Manage your store inventory",
                fontSize = 16.sp,
                color = TextMuted
            )

            if (stats.criticalItems > 0 || stats.outOfStockItems > 0) {
                Spacer(Modifier.height(24.dp))

                // Action Required Card - Critical Stock (LINKED TO INVENTORY)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { navController.navigate("pharmacist_inventory") },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE))
                ) {
                    Row(
                        modifier = Modifier.padding(20.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(EmergencyRed.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = EmergencyRed)
                        }
                        Spacer(Modifier.width(16.dp))
                        Column {
                            Surface(
                                color = EmergencyRed,
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    "Action Required",
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "Stock Alerts",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = TextPrimary
                            )
                            Text(
                                "${stats.criticalItems} items are critically low and ${stats.outOfStockItems} are out of stock.",
                                fontSize = 13.sp,
                                color = TextMuted,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // Quick Actions
            Text(
                text = "Store Management",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Spacer(Modifier.height(16.dp))

            DashboardActionCard(
                title = "My Inventory",
                description = "View and update current stock",
                icon = Icons.Default.Inventory,
                containerColor = Color.White,
                onClick = { navController.navigate("pharmacist_inventory") }
            )

            Spacer(Modifier.height(12.dp))

            DashboardActionCard(
                title = "Customer Requests",
                description = if (pendingRequests > 0) "$pendingRequests new requests pending" else "No new requests",
                icon = Icons.Default.NotificationsActive,
                containerColor = Color.White,
                badgeCount = pendingRequests,
                onClick = { navController.navigate("restock_requests") }
            )

            Spacer(Modifier.height(12.dp))

            DashboardActionCard(
                title = "Pharmacy Verification",
                description = if (shopDetails?.isVerified == true) "Store is verified" else "Complete verification now",
                icon = Icons.Default.VerifiedUser,
                containerColor = Color.White,
                onClick = { navController.navigate("pharmacy_verification") }
            )

            Spacer(Modifier.height(24.dp))

            // Stats Row (LINKED TO INVENTORY)
            Row(modifier = Modifier.fillMaxWidth()) {
                PharmacyStatCard(
                    label = "Total Items",
                    value = stats.totalItems.toString(),
                    icon = Icons.Default.MedicalServices,
                    modifier = Modifier.weight(1f),
                    onClick = { navController.navigate("pharmacist_inventory") }
                )
                Spacer(Modifier.width(16.dp))
                PharmacyStatCard(
                    label = "Stock Health",
                    value = "${stats.healthPercentage}%",
                    icon = Icons.Default.Analytics,
                    modifier = Modifier.weight(1f),
                    onClick = { navController.navigate("pharmacist_inventory") }
                )
            }
            
            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
fun DashboardActionCard(
    title: String,
    description: String,
    icon: ImageVector,
    containerColor: Color,
    badgeCount: Int = 0,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(DarkGreen.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = DarkGreen)
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(description, color = if (badgeCount > 0) EmergencyRed else TextMuted, fontSize = 12.sp)
            }
            if (badgeCount > 0) {
                Surface(
                    color = EmergencyRed,
                    shape = CircleShape,
                    modifier = Modifier.size(24.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(badgeCount.toString(), color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(Modifier.width(8.dp))
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TextMuted)
        }
    }
}

@Composable
fun PharmacyStatCard(
    label: String, 
    value: String, 
    icon: ImageVector,
    modifier: Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Icon(icon, contentDescription = null, tint = DarkGreen, modifier = Modifier.size(20.dp))
            Spacer(Modifier.height(12.dp))
            Text(value, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Text(label, fontSize = 12.sp, color = TextMuted)
        }
    }
}
