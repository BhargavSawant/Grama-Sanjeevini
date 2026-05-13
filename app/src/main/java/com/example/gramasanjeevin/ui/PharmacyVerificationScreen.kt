package com.example.gramasanjeevin.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

private val DarkGreen = Color(0xFF00695C)
private val TextPrimary = Color(0xFF1A2B35)
private val TextMuted = Color(0xFF6B7280)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PharmacyVerificationScreen(
    navController: NavController,
    viewModel: PharmacistViewModel
) {
    val shopDetails by viewModel.shopDetails.collectAsState()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    
    var shopName by remember { mutableStateOf("") }
    var shopAddress by remember { mutableStateOf("") }
    var licenseNumber by remember { mutableStateOf("") }

    LaunchedEffect(shopDetails) {
        shopDetails?.let {
            shopName = it.name
            shopAddress = it.address
            licenseNumber = it.licenseNumber
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { 
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocalHospital, contentDescription = null, tint = DarkGreen)
                        Spacer(Modifier.width(8.dp))
                        Text("Grama-Sanjeevini", fontWeight = FontWeight.Bold, color = DarkGreen)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        containerColor = Color.White
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
        ) {
            Text(
                text = "Pharmacy Verification",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = "Please provide your pharmacy details to register on the Grama-Sanjeevini network. This ensures trusted medical access for the community.",
                fontSize = 14.sp,
                color = TextMuted,
                modifier = Modifier.padding(top = 8.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Step Indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                StepItem(number = "1", label = "Account", isCompleted = true)
                Box(modifier = Modifier.weight(1f).height(2.dp).background(DarkGreen).padding(horizontal = 8.dp))
                StepItem(number = "2", label = "Pharmacy", isActive = true)
                Box(modifier = Modifier.weight(1f).height(2.dp).background(Color(0xFFE0E0E0)).padding(horizontal = 8.dp))
                StepItem(number = "3", label = "Review")
            }

            Spacer(modifier = Modifier.height(32.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF1F1F1))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Shop Name", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    OutlinedTextField(
                        value = shopName,
                        onValueChange = { shopName = it },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        placeholder = { Text("e.g., Sri Sai Medicals") },
                        leadingIcon = { Icon(Icons.Default.Store, contentDescription = null) },
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Full Shop Address", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    OutlinedTextField(
                        value = shopAddress,
                        onValueChange = { shopAddress = it },
                        modifier = Modifier.fillMaxWidth().height(120.dp).padding(vertical = 8.dp),
                        placeholder = { Text("Enter complete address including village/ward...") },
                        leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null) },
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Pharmacist License Number", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    OutlinedTextField(
                        value = licenseNumber,
                        onValueChange = { licenseNumber = it },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        placeholder = { Text("e.g., AP-PHA-12345") },
                        leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null) },
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(24.dp))
                    Text("Upload License Copy", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text("Please upload a clear image of your valid pharmacy license.", color = TextMuted, fontSize = 12.sp)
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFF8F9FA))
                            .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(12.dp))
                            .clickable { /* Upload logic */ },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.CloudUpload, contentDescription = null, tint = DarkGreen, modifier = Modifier.size(32.dp))
                            Text("Tap to Upload Photo", color = DarkGreen, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("JPG, PNG or PDF (Max 5MB)", color = TextMuted, fontSize = 11.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))
                    Button(
                        onClick = {
                            scope.launch {
                                // Save to DB
                                val db = FirebaseFirestore.getInstance()
                                db.collection("pharmacies").document("shop_001") // Mock ID
                                    .update(mapOf(
                                        "name" to shopName,
                                        "address" to shopAddress,
                                        "licenseNumber" to licenseNumber
                                    ))
                                snackbarHostState.showSnackbar("Pharmacy details updated!")
                                navController.popBackStack()
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = DarkGreen),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Continue to Review", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Spacer(Modifier.width(8.dp))
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(14.dp), tint = TextMuted)
                        Spacer(Modifier.width(4.dp))
                        Text("Your information is securely encrypted.", fontSize = 12.sp, color = TextMuted)
                    }
                }
            }
        }
    }
}

@Composable
fun StepItem(number: String, label: String, isActive: Boolean = false, isCompleted: Boolean = false) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(if (isActive || isCompleted) DarkGreen else Color(0xFFE0E0E0)),
            contentAlignment = Alignment.Center
        ) {
            if (isCompleted) {
                Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
            } else {
                Text(number, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        }
        Spacer(Modifier.height(4.dp))
        Text(label, fontSize = 10.sp, fontWeight = FontWeight.Medium, color = if (isActive || isCompleted) DarkGreen else TextMuted)
    }
}
