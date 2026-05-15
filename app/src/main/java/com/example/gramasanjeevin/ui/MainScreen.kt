package com.example.gramasanjeevin.ui

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import com.example.gramasanjeevin.utils.L
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
    val context = LocalContext.current

    // HOISTED ViewModels to share state across screens
    val authViewModel: AuthViewModel = viewModel()
    val sharedCartViewModel: CartViewModel = viewModel()
    val pharmacistViewModel: PharmacistViewModel = viewModel()

    val isEnglish by authViewModel.isEnglish.collectAsState()

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
                        isEnglish = isEnglish,
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
                        isEnglish = isEnglish,
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
                    },
                    authViewModel = authViewModel,
                    pharmacistViewModel = pharmacistViewModel
                )
            }

            // ── Villager Flow ─────────────────────────────────────────────
            composable("home")      { HomeScreen(navController, authViewModel) }
            composable("search")    { SearchScreen(navController, cartViewModel = sharedCartViewModel, authViewModel = authViewModel) }
            composable("cart")      { CartScreen(navController, viewModel = sharedCartViewModel, authViewModel = authViewModel) }
            composable("alerts")    { AlertsScreen(authViewModel = authViewModel) }
            composable("profile")   { ProfileScreen(navController, authViewModel = authViewModel) }

            // ── Sub-features ──────────────────────────────────────────────
            composable("emergency")    { EmergencyScreen(authViewModel = authViewModel) }
            composable("hubs")         { HubsScreen(authViewModel = authViewModel, onBack = { navController.popBackStack() }) }
            composable("prescription") { 
                PrescriptionScannerScreen(
                    navController = navController,
                    cartViewModel = sharedCartViewModel,
                    authViewModel = authViewModel,
                    onPrescriptionVerified = {
                        Toast.makeText(context, L.s(isEnglish, "Prescription Processed!", "ಪ್ರಿಸ್ಕ್ರಿಪ್ಷನ್ ಪ್ರಕ್ರಿಯೆಗೊಳಿಸಲಾಗಿದೆ!"), Toast.LENGTH_SHORT).show()
                    }
                ) 
            }
            
            // ── Pharmacist Flow ───────────────────────────────────────────
            composable("pharmacist_dashboard") { 
                PharmacistDashboardScreen(navController, viewModel = pharmacistViewModel, authViewModel = authViewModel) 
            }
            composable("pharmacist_inventory") { 
                PharmacistScreen(navController, viewModel = pharmacistViewModel, authViewModel = authViewModel) 
            }
            composable("restock_requests") {
                RestockRequestsScreen(navController, viewModel = pharmacistViewModel, authViewModel = authViewModel)
            }
            composable("pharmacist_profile") { 
                PharmacistProfileScreen(navController, viewModel = pharmacistViewModel, authViewModel = authViewModel) 
            }
            composable("pharmacy_verification") { 
                PharmacyVerificationScreen(navController, viewModel = pharmacistViewModel, authViewModel = authViewModel) 
            }
            
            // New Pharmacist Order Review Screen
            composable("pharmacist_order_review/{orderId}") { backStackEntry ->
                val orderId = backStackEntry.arguments?.getString("orderId") ?: ""
                PharmacistOrderReviewScreen(orderId, navController, viewModel = pharmacistViewModel, authViewModel = authViewModel)
            }
        }
    }
}

@Composable
private fun VillagerBottomNav(
    currentRoute: String?,
    isEnglish: Boolean,
    onNavigate: (String) -> Unit
) {
    val items = listOf(
        NavItem("home",      L.home(isEnglish),      Icons.Filled.Home,       Icons.Outlined.Home),
        NavItem("search",    L.search(isEnglish),    Icons.Filled.Search,     Icons.Outlined.Search),
        NavItem("cart",      L.cart(isEnglish),      Icons.Filled.ShoppingCart, Icons.Outlined.ShoppingCart),
        NavItem("alerts",    L.alerts(isEnglish),    Icons.Filled.Notifications, Icons.Outlined.Notifications),
        NavItem("profile",   L.profile(isEnglish),   Icons.Filled.Person,     Icons.Outlined.Person)
    )
    BottomNavBase(items, currentRoute, onNavigate)
}

@Composable
private fun PharmacistBottomNav(
    currentRoute: String?,
    isEnglish: Boolean,
    onNavigate: (String) -> Unit
) {
    val items = listOf(
        NavItem("pharmacist_dashboard", L.s(isEnglish, "Dashboard", "ಡ್ಯಾಶ್‌ಬೋರ್ಡ್"), Icons.Filled.Dashboard, Icons.Outlined.Dashboard),
        NavItem("pharmacist_inventory", L.inventory(isEnglish), Icons.Filled.Inventory, Icons.Outlined.Inventory),
        NavItem("restock_requests",    L.s(isEnglish, "Requests", "ವಿನಂತಿಗಳು"),  Icons.Filled.Notifications, Icons.Outlined.Notifications),
        NavItem("pharmacist_profile",   L.profile(isEnglish),   Icons.Filled.Person,     Icons.Outlined.Person)
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
fun ProfileScreen(navController: androidx.navigation.NavController, authViewModel: AuthViewModel) {
    var user by remember { mutableStateOf<User?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var isEditing by remember { mutableStateOf(false) }
    
    val isEnglish by authViewModel.isEnglish.collectAsState()
    val currentUserId = authViewModel.currentUserId ?: "user_001"

    // Form fields
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var village by remember { mutableStateOf("") }
    var healthId by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }

    val db = FirestoreProvider.getDb()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    LaunchedEffect(currentUserId) {
        try {
            val doc = db.collection("users").document(currentUserId).get().await()
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
                    text = if (name.isEmpty()) L.unknownUser(isEnglish) else name,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = if (village.isEmpty()) L.unknownVillage(isEnglish) else "$village ${L.village(isEnglish)}",
                    fontSize = 16.sp,
                    color = TextMuted
                )
            } else {
                Text(L.editProfile(isEnglish), fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Teal)
            }
            
            Spacer(modifier = Modifier.height(32.dp))

            // Language Selection in Profile
            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Language, contentDescription = null, tint = Teal, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(12.dp))
                        Text(L.language(isEnglish), fontWeight = FontWeight.Bold, color = TextPrimary)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (isEnglish) "English" else "ಕನ್ನಡ",
                            color = Teal,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.clickable { authViewModel.toggleLanguage() }
                        )
                        Spacer(Modifier.width(8.dp))
                        Switch(
                            checked = !isEnglish,
                            onCheckedChange = { authViewModel.toggleLanguage() },
                            colors = SwitchDefaults.colors(checkedThumbColor = Teal, checkedTrackColor = TealLight)
                        )
                    }
                }
            }
            
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
                            label = { Text(L.fullName(isEnglish)) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        Spacer(Modifier.height(12.dp))
                        OutlinedTextField(
                            value = phone,
                            onValueChange = { phone = it },
                            label = { Text(L.phoneNumber(isEnglish)) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        Spacer(Modifier.height(12.dp))
                        OutlinedTextField(
                            value = village,
                            onValueChange = { village = it },
                            label = { Text(L.village(isEnglish)) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        Spacer(Modifier.height(12.dp))
                        OutlinedTextField(
                            value = healthId,
                            onValueChange = { healthId = it },
                            label = { Text(L.healthId(isEnglish)) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        Spacer(Modifier.height(12.dp))
                        OutlinedTextField(
                            value = address,
                            onValueChange = { address = it },
                            label = { Text(L.address(isEnglish)) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            minLines = 2
                        )
                    } else {
                        ProfileDetailRow(label = L.phoneNumber(isEnglish), value = if (phone.isEmpty()) L.notSet(isEnglish) else phone)
                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFFF1F1F1))
                        ProfileDetailRow(label = L.healthId(isEnglish), value = if (healthId.isEmpty()) L.notSet(isEnglish) else healthId)
                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFFF1F1F1))
                        ProfileDetailRow(label = L.address(isEnglish), value = if (address.isEmpty()) L.notSet(isEnglish) else address)
                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFFF1F1F1))
                        ProfileDetailRow(label = L.village(isEnglish), value = if (village.isEmpty()) L.notSet(isEnglish) else village)
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
                                    userId = currentUserId,
                                    name = name,
                                    village = village,
                                    phone = phone,
                                    healthId = healthId,
                                    address = address,
                                    role = user?.role ?: ""
                                )
                                db.collection("users").document(currentUserId).set(updatedUser).await()
                                user = updatedUser
                                isEditing = false
                                Toast.makeText(context, L.profileUpdated(isEnglish), Toast.LENGTH_SHORT).show()
                            } catch (e: Exception) {
                                Toast.makeText(context, "${L.s(isEnglish, "Update Failed", "ನವೀಕರಣ ವಿಫಲವಾಗಿದೆ")}: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Teal, contentColor = Color.White),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(L.saveChanges(isEnglish), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
                Spacer(Modifier.height(8.dp))
                TextButton(onClick = { isEditing = false }) {
                    Text(L.cancel(isEnglish), color = Color.Gray)
                }
            } else {
                Button(
                    onClick = { isEditing = true },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Teal, contentColor = Color.White),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(L.editProfile(isEnglish), fontWeight = FontWeight.Bold, fontSize = 16.sp)
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
                    Text(L.logout(isEnglish), fontWeight = FontWeight.Bold, fontSize = 16.sp)
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
