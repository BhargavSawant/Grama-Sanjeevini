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
import com.example.gramasanjeevin.utils.L

private val DarkGreen = Color(0xFF00695C)
private val EmergencyRed = Color(0xFFC62828)
private val TextPrimary = Color(0xFF1A2B35)
private val TextMuted = Color(0xFF6B7280)
private val PageBg = Color(0xFFF8F9FB)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PharmacistDashboardScreen(
    navController: NavController,
    viewModel: PharmacistViewModel = viewModel(),
    authViewModel: AuthViewModel = viewModel()
) {
    val stats by viewModel.stats.collectAsState()
    val shopDetails by viewModel.shopDetails.collectAsState()
    val requests by viewModel.requests.collectAsState()
    val isEnglish by authViewModel.isEnglish.collectAsState()
    
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
                    text = L.gramaSanjeevini(isEnglish),
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
                Icon(Icons.Default.Person, contentDescription = L.profile(isEnglish), tint = DarkGreen)
            }
        }

        Column(modifier = Modifier.padding(horizontal = 24.dp)) {
            Text(
                text = L.pharmacyDashboard(isEnglish),
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = shopDetails?.name ?: L.manageInventory(isEnglish),
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
                                    L.actionRequired(isEnglish),
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                            Spacer(Modifier.height(8.dp))
                            Text(
                                L.stockAlerts(isEnglish),
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = TextPrimary
                            )
                            Text(
                                if (isEnglish)
                                    "${stats.criticalItems} items are critically low and ${stats.outOfStockItems} are out of stock."
                                else
                                    "${stats.criticalItems} ವಸ್ತುಗಳು ಅತ್ಯಂತ ಕಡಿಮೆಯಿವೆ ಮತ್ತು ${stats.outOfStockItems} ದಾಸ್ತಾನು ಇಲ್ಲ.",
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
                text = L.storeManagement(isEnglish),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Spacer(Modifier.height(16.dp))

            DashboardActionCard(
                title = L.myInventory(isEnglish),
                description = L.manageUpdateStock(isEnglish),
                icon = Icons.Default.Inventory,
                containerColor = Color.White,
                onClick = { navController.navigate("pharmacist_inventory") }
            )

            Spacer(Modifier.height(12.dp))

            DashboardActionCard(
                title = L.customerRequests(isEnglish),
                description = if (pendingRequests > 0) L.pendingRequests(isEnglish, pendingRequests) else L.noPendingRequests(isEnglish),
                icon = Icons.Default.NotificationsActive,
                containerColor = Color.White,
                badgeCount = pendingRequests,
                onClick = { navController.navigate("restock_requests") }
            )

            Spacer(Modifier.height(12.dp))

            DashboardActionCard(
                title = L.pharmacyVerification(isEnglish),
                description = if (shopDetails?.isVerified == true) L.s(isEnglish, "Store is verified", "ಅಂಗಡಿ ಪರಿಶೀಲಿಸಲಾಗಿದೆ") else L.s(isEnglish, "Complete verification now", "ಪರಿಶೀಲನೆಯನ್ನು ಈಗಲೇ ಪೂರ್ಣಗೊಳಿಸಿ"),
                icon = Icons.Default.VerifiedUser,
                containerColor = Color.White,
                onClick = { navController.navigate("pharmacy_verification") }
            )

            Spacer(Modifier.height(24.dp))

            // Stats Row (LINKED TO INVENTORY)
            Row(modifier = Modifier.fillMaxWidth()) {
                PharmacyStatCard(
                    label = L.totalItems(isEnglish),
                    value = stats.totalItems.toString(),
                    icon = Icons.Default.MedicalServices,
                    modifier = Modifier.weight(1f),
                    onClick = { navController.navigate("pharmacist_inventory") }
                )
                Spacer(Modifier.width(16.dp))
                PharmacyStatCard(
                    label = L.stockHealth(isEnglish),
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
