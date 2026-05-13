package com.example.gramasanjeevin.ui

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import com.example.gramasanjeevin.model.User
import com.example.gramasanjeevin.utils.FirestoreProvider
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

// ── Colour tokens ─────────────────────────────────────────────────────────────
private val Teal        = Color(0xFF00695C) // Dark Green from images
private val TealLight   = Color(0xFFE0F2F1)
private val TextMuted   = Color(0xFF6B7280)
private val TextPrimary = Color(0xFF1A2B35)

/**
 * Root navigation container for Grama-Sanjeevini villager and pharmacist flows.
 */
@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val navBackStack by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStack?.destination?.route

    // HOISTED ViewModels
    val sharedCartViewModel: CartViewModel = viewModel()
    val pharmacistViewModel: PharmacistViewModel = viewModel()

    // Navigation routes that show the respective bottom bars
    val villagerRoutes = setOf("home", "search", "cart", "alerts", "profile")
    val pharmacistRoutes = setOf("pharmacist_dashboard", "pharmacist_inventory", "restock_requests", "pharmacist_profile")
    
    val showVillagerNav = currentRoute in villagerRoutes
    val showPharmacistNav = currentRoute in pharmacistRoutes

    Scaffold(
        containerColor = Color.White,
        bottomBar = {
            if (showVillagerNav) {
                AnimatedVisibility(
                    visible = true,
                    enter = slideInVertically { it },
                    exit = slideOutVertically { it }
                ) {
                    VillagerBottomNav(
                        currentRoute = currentRoute,
                        onNavigate = { route ->
                            navController.navigate(route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            } else if (showPharmacistNav) {
                AnimatedVisibility(
                    visible = true,
                    enter = slideInVertically { it },
                    exit = slideOutVertically { it }
                ) {
                    PharmacistBottomNav(
                        currentRoute = currentRoute,
                        onNavigate = { route ->
                            navController.navigate(route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "login",
            modifier = Modifier.padding(innerPadding)
        ) {
            // ── Auth ──────────────────────────────────────────────────────
            composable("login") {
                LoginScreen(
                    onVillagerSelected = {
                        navController.navigate("home") {
                            popUpTo("login") { inclusive = true }
                        }
                    },
                    onPharmacistLoginSuccess = {
                        navController.navigate("pharmacist_dashboard") {
                            popUpTo("login") { inclusive = true }
                        }
                    }
                )
            }

            // ── Villager Flow ─────────────────────────────────────────────
            composable("home")      { HomeScreen(navController) }
            composable("search")    { SearchScreen(navController, cartViewModel = sharedCartViewModel) }
            composable("cart")      { CartScreen(navController, viewModel = sharedCartViewModel) }
            composable("alerts")    { AlertsScreen() } // Uses dedicated AlertsScreen.kt
            composable("profile")   { ProfileScreen(navController) }

            // ── Sub-features ──────────────────────────────────────────────
            composable("emergency")    { EmergencyScreen() }
            composable("hubs")         { HubsScreen(onBack = { navController.popBackStack() }) }
            composable("prescription") { 
                PrescriptionScreen(
                    onMedicinesFound = { navController.navigate("cart") },
                    cartViewModel = sharedCartViewModel
                ) 
            }
            
            // ── Pharmacist Flow ───────────────────────────────────────────
            composable("pharmacist_dashboard") { 
                PharmacistDashboardScreen(navController, viewModel = pharmacistViewModel) 
            }
            composable("pharmacist_inventory") { 
                PharmacistScreen(viewModel = pharmacistViewModel) 
            }
            composable("restock_requests") {
                RestockRequestsScreen(navController, viewModel = pharmacistViewModel)
            }
            composable("pharmacist_profile") { 
                PharmacistProfileScreen(navController, viewModel = pharmacistViewModel) 
            }
            composable("pharmacy_verification") { 
                PharmacyVerificationScreen(navController, viewModel = pharmacistViewModel) 
            }
        }
    }
}

@Composable
private fun VillagerBottomNav(
    currentRoute: String?,
    onNavigate: (String) -> Unit
) {
    val items = listOf(
        NavItem("home",      "Home",      Icons.Filled.Home,       Icons.Outlined.Home),
        NavItem("search",    "Search",    Icons.Filled.Search,     Icons.Outlined.Search),
        NavItem("cart",      "Cart",      Icons.Filled.ShoppingCart, Icons.Outlined.ShoppingCart),
        NavItem("alerts",    "Alerts",    Icons.Filled.Notifications, Icons.Outlined.Notifications),
        NavItem("profile",   "Profile",   Icons.Filled.Person,     Icons.Outlined.Person)
    )
    BottomNavBase(items, currentRoute, onNavigate)
}

@Composable
private fun PharmacistBottomNav(
    currentRoute: String?,
    onNavigate: (String) -> Unit
) {
    val items = listOf(
        NavItem("pharmacist_dashboard", "Dashboard", Icons.Filled.Dashboard, Icons.Outlined.Dashboard),
        NavItem("pharmacist_inventory", "Inventory", Icons.Filled.Inventory, Icons.Outlined.Inventory),
        NavItem("restock_requests",    "Requests",  Icons.Filled.Notifications, Icons.Outlined.Notifications),
        NavItem("pharmacist_profile",   "Profile",   Icons.Filled.Person,     Icons.Outlined.Person)
    )
    BottomNavBase(items, currentRoute, onNavigate)
}

@Composable
private fun BottomNavBase(
    items: List<NavItem>,
    currentRoute: String?,
    onNavigate: (String) -> Unit
) {
    Column {
        HorizontalDivider(color = Color(0xFFF1F1F1), thickness = 1.dp)
        NavigationBar(
            containerColor = Color.White,
            tonalElevation = 0.dp
        ) {
            items.forEach { item ->
                val selected = currentRoute == item.route
                NavigationBarItem(
                    selected = selected,
                    onClick = { onNavigate(item.route) },
                    icon = {
                        Icon(
                            imageVector = if (selected) item.selectedIcon else item.defaultIcon,
                            contentDescription = item.label,
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    label = {
                        Text(
                            text = item.label,
                            fontSize = 11.sp,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = TealLight,
                        selectedIconColor = Teal,
                        selectedTextColor = Teal,
                        unselectedIconColor = TextMuted,
                        unselectedTextColor = TextMuted
                    )
                )
            }
        }
    }
}

private data class NavItem(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val defaultIcon: ImageVector
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(navController: androidx.navigation.NavController) {
    var user by remember { mutableStateOf<User?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var isEditing by remember { mutableStateOf(false) }
    
    // Form fields
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var village by remember { mutableStateOf("") }
    var healthId by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }

    val db = FirestoreProvider.getDb()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        try {
            val doc = db.collection("users").document("user_001").get().await()
            val u = doc.toObject(User::class.java)
            user = u
            u?.let {
                name = it.name
                phone = it.phone
                village = it.village
                healthId = it.healthId
                address = it.address
            }
        } catch (e: Exception) {
            // Error handling
        } finally {
            isLoading = false
        }
    }

    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Teal)
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF8F9FB))
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(TealLight),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Person, contentDescription = null, tint = Teal, modifier = Modifier.size(60.dp))
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            if (!isEditing) {
                Text(
                    text = if (name.isEmpty()) "Unknown User" else name,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = if (village.isEmpty()) "Unknown Village" else "$village Village",
                    fontSize = 16.sp,
                    color = TextMuted
                )
            } else {
                Text("Edit Your Profile", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Teal)
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    if (isEditing) {
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("Full Name") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        Spacer(Modifier.height(12.dp))
                        OutlinedTextField(
                            value = phone,
                            onValueChange = { phone = it },
                            label = { Text("Phone Number") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        Spacer(Modifier.height(12.dp))
                        OutlinedTextField(
                            value = village,
                            onValueChange = { village = it },
                            label = { Text("Village") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        Spacer(Modifier.height(12.dp))
                        OutlinedTextField(
                            value = healthId,
                            onValueChange = { healthId = it },
                            label = { Text("Health ID (ABHA)") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        Spacer(Modifier.height(12.dp))
                        OutlinedTextField(
                            value = address,
                            onValueChange = { address = it },
                            label = { Text("Full Address") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            minLines = 2
                        )
                    } else {
                        ProfileDetailRow(label = "Phone", value = if (phone.isEmpty()) "Not set" else phone)
                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFFF1F1F1))
                        ProfileDetailRow(label = "Health ID", value = if (healthId.isEmpty()) "Not set" else healthId)
                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFFF1F1F1))
                        ProfileDetailRow(label = "Address", value = if (address.isEmpty()) "Not set" else address)
                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFFF1F1F1))
                        ProfileDetailRow(label = "Village", value = if (village.isEmpty()) "Not set" else village)
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            if (isEditing) {
                Button(
                    onClick = {
                        scope.launch {
                            try {
                                val updatedUser = User(
                                    userId = "user_001",
                                    name = name,
                                    village = village,
                                    phone = phone,
                                    healthId = healthId,
                                    address = address
                                )
                                db.collection("users").document("user_001").set(updatedUser).await()
                                user = updatedUser
                                isEditing = false
                                Toast.makeText(context, "Profile Updated Successfully!", Toast.LENGTH_SHORT).show()
                            } catch (e: Exception) {
                                Toast.makeText(context, "Update Failed: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Teal),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Save Changes", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
                Spacer(Modifier.height(8.dp))
                TextButton(onClick = { isEditing = false }) {
                    Text("Cancel", color = Color.Gray)
                }
            } else {
                Button(
                    onClick = { isEditing = true },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Teal),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Edit Profile", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedButton(
                    onClick = { 
                        navController.navigate("login") {
                            popUpTo(0)
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFC62828)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFC62828)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Logout", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun ProfileDetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = TextMuted, fontSize = 14.sp)
        Text(text = value, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
    }
}
