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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.gramasanjeevin.utils.L
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

private val DarkGreen = Color(0xFF00695C)
private val TextPrimary = Color(0xFF1A2B35)
private val TextMuted = Color(0xFF6B7280)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PharmacyVerificationScreen(
    navController: NavController,
    viewModel: PharmacistViewModel,
    authViewModel: AuthViewModel = viewModel()
) {
    val shopDetails by viewModel.shopDetails.collectAsState()
    val isEnglish by authViewModel.isEnglish.collectAsState()
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
                        Text(L.gramaSanjeevini(isEnglish), fontWeight = FontWeight.Bold, color = DarkGreen)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = L.back(isEnglish))
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
                text = L.pharmacyVerification(isEnglish),
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = L.s(isEnglish, 
                    "Please provide your pharmacy details to register on the Grama-Sanjeevini network. This ensures trusted medical access for the community.",
                    "ಗ್ರಾಮ-ಸಂಜೀವಿನಿ ನೆಟ್‌ವರ್ಕ್‌ನಲ್ಲಿ ನೋಂದಾಯಿಸಲು ದಯವಿಟ್ಟು ನಿಮ್ಮ ಫಾರ್ಮಸಿ ವಿವರಗಳನ್ನು ಒದಗಿಸಿ. ಇದು ಸಮುದಾಯಕ್ಕೆ ವಿಶ್ವಾಸಾರ್ಹ ವೈದ್ಯಕೀಯ ಪ್ರವೇಶವನ್ನು ಖಚಿತಪಡಿಸುತ್ತದೆ."),
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
                StepItem(number = "1", label = L.s(isEnglish, "Account", "ಖಾತೆ"), isCompleted = true, activeColor = DarkGreen)
                Box(modifier = Modifier.weight(1f).height(2.dp).background(DarkGreen).padding(horizontal = 8.dp))
                StepItem(number = "2", label = L.s(isEnglish, "Pharmacy", "ಫಾರ್ಮಸಿ"), isActive = true, activeColor = DarkGreen)
                Box(modifier = Modifier.weight(1f).height(2.dp).background(Color(0xFFE0E0E0)).padding(horizontal = 8.dp))
                StepItem(number = "3", label = L.s(isEnglish, "Review", "ಪರಿಶೀಲನೆ"), activeColor = DarkGreen)
            }

            Spacer(modifier = Modifier.height(32.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF1F1F1))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(L.s(isEnglish, "Shop Name", "ಅಂಗಡಿಯ ಹೆಸರು"), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    OutlinedTextField(
                        value = shopName,
                        onValueChange = { shopName = it },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        placeholder = { Text(L.s(isEnglish, "e.g., Sri Sai Medicals", "ಉದಾಹರಣೆಗೆ: ಶ್ರೀ ಸಾಯಿ ಮೆಡಿಕಲ್ಸ್")) },
                        leadingIcon = { Icon(Icons.Default.Store, contentDescription = null) },
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    Text(L.s(isEnglish, "Full Shop Address", "ಸಂಪೂರ್ಣ ಅಂಗಡಿ ವಿಳಾಸ"), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    OutlinedTextField(
                        value = shopAddress,
                        onValueChange = { shopAddress = it },
                        modifier = Modifier.fillMaxWidth().height(120.dp).padding(vertical = 8.dp),
                        placeholder = { Text(L.s(isEnglish, "Enter complete address including village/ward...", "ಗ್ರಾಮ/ವಾರ್ಡ್ ಸೇರಿದಂತೆ ಸಂಪೂರ್ಣ ವಿಳಾಸವನ್ನು ನಮೂದಿಸಿ...")) },
                        leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null) },
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    Text(L.s(isEnglish, "Pharmacist License Number", "ಫಾರ್ಮಾಸಿಸ್ಟ್ ಪರವಾನಗಿ ಸಂಖ್ಯೆ"), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    OutlinedTextField(
                        value = licenseNumber,
                        onValueChange = { licenseNumber = it },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        placeholder = { Text("e.g., AP-PHA-12345") },
                        leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null) },
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(24.dp))
                    Text(L.s(isEnglish, "Upload License Copy", "ಪರವಾನಗಿ ಪ್ರತಿಯನ್ನು ಅಪ್‌ಲೋಡ್ ಮಾಡಿ"), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text(L.s(isEnglish, "Please upload a clear image of your valid pharmacy license.", "ದಯವಿಟ್ಟು ನಿಮ್ಮ ಮಾನ್ಯ ಫಾರ್ಮಸಿ ಪರವಾನಗಿಯ ಸ್ಪಷ್ಟ ಚಿತ್ರವನ್ನು ಅಪ್‌ಲೋಡ್ ಮಾಡಿ."), color = TextMuted, fontSize = 12.sp)
                    
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
                            Text(L.s(isEnglish, "Tap to Upload Photo", "ಫೋಟೋ ಅಪ್‌ಲೋಡ್ ಮಾಡಲು ಟ್ಯಾಪ್ ಮಾಡಿ"), color = DarkGreen, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("JPG, PNG or PDF (Max 5MB)", color = TextMuted, fontSize = 11.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))
                    Button(
                        onClick = {
                            val sid = shopDetails?.shopId
                            if (sid != null) {
                                scope.launch {
                                    val db = FirebaseFirestore.getInstance()
                                    db.collection("pharmacies").document(sid)
                                        .update(mapOf(
                                            "name" to shopName,
                                            "address" to shopAddress,
                                            "licenseNumber" to licenseNumber
                                        ))
                                    snackbarHostState.showSnackbar(L.s(isEnglish, "Pharmacy details updated!", "ಫಾರ್ಮಸಿ ವಿವರಗಳನ್ನು ನವೀಕರಿಸಲಾಗಿದೆ!"))
                                    navController.popBackStack()
                                }
                            } else {
                                scope.launch {
                                    snackbarHostState.showSnackbar(L.s(isEnglish, "No pharmacy found to update.", "ನವೀಕರಿಸಲು ಯಾವುದೇ ಫಾರ್ಮಸಿ ಕಂಡುಬಂದಿಲ್ಲ."))
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = DarkGreen, contentColor = Color.White),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(L.s(isEnglish, "Continue to Review", "ಪರಿಶೀಲನೆಗೆ ಮುಂದುವರಿಯಿರಿ"), fontWeight = FontWeight.Bold, fontSize = 16.sp)
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
                        Text(L.s(isEnglish, "Your information is securely encrypted.", "ನಿಮ್ಮ ಮಾಹಿತಿಯು ಸುರಕ್ಷಿತವಾಗಿ ಎನ್‌ಕ್ರಿಪ್ಟ್ ಮಾಡಲ್ಪಟ್ಟಿದೆ."), fontSize = 12.sp, color = TextMuted)
                    }
                }
            }
        }
    }
}

@Composable
fun StepItem(number: String, label: String, isActive: Boolean = false, isCompleted: Boolean = false, activeColor: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(if (isActive || isCompleted) activeColor else Color(0xFFE0E0E0)),
            contentAlignment = Alignment.Center
        ) {
            if (isCompleted) {
                Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
            } else {
                Text(number, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        }
        Spacer(Modifier.height(4.dp))
        Text(label, fontSize = 10.sp, fontWeight = FontWeight.Medium, color = if (isActive || isCompleted) activeColor else TextMuted)
    }
}
