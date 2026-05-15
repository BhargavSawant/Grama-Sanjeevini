package com.example.gramasanjeevin.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
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
private val TealLight = Color(0xFFE0F2F1)
private val TextPrimary = Color(0xFF1A2B35)
private val TextMuted = Color(0xFF6B7280)
private val PageBg = Color(0xFFF8F9FB)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PharmacistProfileScreen(
    navController: NavController,
    viewModel: PharmacistViewModel = viewModel(),
    authViewModel: AuthViewModel = viewModel()
) {
    val shopDetails by viewModel.shopDetails.collectAsState()
    val isEnglish by authViewModel.isEnglish.collectAsState()
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(L.profile(isEnglish), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack, 
                            contentDescription = L.back(isEnglish),
                            tint = Color.Black
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = PageBg
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .background(TealLight),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Store, contentDescription = null, tint = DarkGreen, modifier = Modifier.size(48.dp))
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = shopDetails?.name ?: L.s(isEnglish, "Store Name", "ಅಂಗಡಿಯ ಹೆಸರು"),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "ID: ${shopDetails?.shopId ?: "N/A"}",
                        fontSize = 14.sp,
                        color = TextMuted
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Surface(
                        color = if (shopDetails?.isVerified == true) Color(0xFFE8F5E9) else Color(0xFFFFF3E0),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (shopDetails?.isVerified == true) Icons.Default.Verified else Icons.Default.Pending,
                                contentDescription = null,
                                tint = if (shopDetails?.isVerified == true) Color(0xFF2E7D32) else Color(0xFFE65100),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = if (shopDetails?.isVerified == true) L.s(isEnglish, "Verified Store", "ಪರಿಶೀಲಿಸಿದ ಅಂಗಡಿ") else L.s(isEnglish, "Verification Pending", "ಪರಿಶೀಲನೆ ಬಾಕಿ ಇದೆ"),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (shopDetails?.isVerified == true) Color(0xFF2E7D32) else Color(0xFFE65100)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Language Selection Card
            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
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
                        Icon(Icons.Default.Language, contentDescription = null, tint = DarkGreen, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(12.dp))
                        Text(L.language(isEnglish), fontWeight = FontWeight.Bold, color = TextPrimary)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (isEnglish) "English" else "ಕನ್ನಡ",
                            color = DarkGreen,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.clickable { authViewModel.toggleLanguage() }
                        )
                        Spacer(Modifier.width(8.dp))
                        Switch(
                            checked = !isEnglish,
                            onCheckedChange = { authViewModel.toggleLanguage() },
                            colors = SwitchDefaults.colors(checkedThumbColor = DarkGreen, checkedTrackColor = TealLight)
                        )
                    }
                }
            }

            // Details Section
            Text(
                text = L.s(isEnglish, "Business Information", "ವ್ಯಾಪಾರ ಮಾಹಿತಿ"),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                modifier = Modifier.fillMaxWidth().padding(start = 8.dp, bottom = 12.dp)
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    ProfileDetailItem(
                        icon = Icons.Default.Person,
                        label = L.s(isEnglish, "Store Owner", "ಅಂಗಡಿ ಮಾಲೀಕರು"),
                        value = shopDetails?.ownerName ?: "N/A"
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFFF1F1F1))
                    ProfileDetailItem(
                        icon = Icons.Default.Badge,
                        label = L.s(isEnglish, "License Number", "ಪರವಾನಗಿ ಸಂಖ್ಯೆ"),
                        value = shopDetails?.licenseNumber ?: "N/A"
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFFF1F1F1))
                    ProfileDetailItem(
                        icon = Icons.Default.Phone,
                        label = L.phoneNumber(isEnglish),
                        value = shopDetails?.phone ?: "N/A"
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFFF1F1F1))
                    ProfileDetailItem(
                        icon = Icons.Default.Map,
                        label = L.village(isEnglish),
                        value = shopDetails?.village ?: "N/A"
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFFF1F1F1))
                    ProfileDetailItem(
                        icon = Icons.Default.LocationOn,
                        label = L.address(isEnglish),
                        value = shopDetails?.address ?: "N/A"
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Logout Button
            Button(
                onClick = { 
                    navController.navigate("login") {
                        popUpTo(0)
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC62828), contentColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null)
                Spacer(modifier = Modifier.width(12.dp))
                Text(L.s(isEnglish, "Logout from Panel", "ಪ್ಯಾನಲ್‌ನಿಂದ ಹೊರಬನ್ನಿ"), fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
            
            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}

@Composable
private fun ProfileDetailItem(icon: ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(TealLight.copy(alpha = 0.5f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = DarkGreen, modifier = Modifier.size(18.dp))
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(text = label, color = TextMuted, fontSize = 12.sp)
            Text(text = value, color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
        }
    }
}
