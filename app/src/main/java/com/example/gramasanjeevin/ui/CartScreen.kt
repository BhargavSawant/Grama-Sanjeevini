package com.example.gramasanjeevin.ui

import android.Manifest
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import coil.compose.AsyncImage
import com.example.gramasanjeevin.model.RequestType
import com.example.gramasanjeevin.utils.L
import com.example.gramasanjeevin.utils.LocationUtils
import kotlinx.coroutines.launch

private val Teal = Color(0xFF00695C)
private val TextPrimary = Color(0xFF1A2B35)
private val TextMuted = Color(0xFF6B7280)

@Composable
fun CartScreen(
    navController: androidx.navigation.NavController,
    viewModel: CartViewModel = viewModel(),
    authViewModel: AuthViewModel = viewModel()
) {
    val matches by viewModel.matches.collectAsState()
    val cartItems by viewModel.cartItems.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val requestStatus by viewModel.requestStatus.collectAsState()
    val prescriptionUri by viewModel.prescriptionUri.collectAsState()
    val isUploading by viewModel.isUploadingPrescription.collectAsState()
    val prescriptionUrl by viewModel.prescriptionUrl.collectAsState()
    val isEnglish by authViewModel.isEnglish.collectAsState()
    
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var showPrescriptionDialog by remember { mutableStateOf(false) }
    var acceptedTerms by remember { mutableStateOf(false) }
    var showSourcePicker by remember { mutableStateOf(false) }

    val requiresPrescription = cartItems.any { it.requiresPrescription }
    val hasPrescription = prescriptionUri != null || prescriptionUrl != null

    // Activity Result Launchers
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.uploadPrescription(it) }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        bitmap?.let {
            viewModel.uploadPrescriptionBitmap(it)
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val cameraGranted = permissions[Manifest.permission.CAMERA] == true
        if (cameraGranted) {
            showSourcePicker = true
        } else {
            scope.launch {
                snackbarHostState.showSnackbar(if (isEnglish) "Camera and Storage permissions are needed to scan prescriptions." else "ಪ್ರಿಸ್ಕ್ರಿಪ್ಷನ್‌ಗಳನ್ನು ಸ್ಕ್ಯಾನ್ ಮಾಡಲು ಕ್ಯಾಮರಾ ಮತ್ತು ಸ್ಟೋರೇಜ್ ಅನುಮತಿಗಳ ಅಗತ್ಯವಿದೆ.")
            }
        }
    }

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
                Text(L.gramaSanjeevini(isEnglish), fontWeight = FontWeight.Bold, color = Teal, fontSize = 18.sp)
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Text(
                        text = L.smartMatches(isEnglish),
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    if (cartItems.isNotEmpty()) {
                        Text(
                            text = L.smartMatchesSub(isEnglish, cartItems.size),
                            fontSize = 16.sp,
                            color = TextMuted,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }

                if (cartItems.isNotEmpty()) {
                    // Warning Banner
                    if (requiresPrescription && !hasPrescription) {
                        item {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3F3)),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color.Red),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Warning, contentDescription = null, tint = Color.Red)
                                    Spacer(Modifier.width(12.dp))
                                    Text(
                                        text = L.prescriptionWarning(isEnglish),
                                        color = Color.Red,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    // Prescription Uploaded Preview
                    if (hasPrescription) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (prescriptionUri != null) {
                                        AsyncImage(
                                            model = prescriptionUri,
                                            contentDescription = "Prescription",
                                            modifier = Modifier.size(60.dp).clip(RoundedCornerShape(8.dp))
                                        )
                                    } else if (prescriptionUrl != null) {
                                        AsyncImage(
                                            model = prescriptionUrl,
                                            contentDescription = "Prescription",
                                            modifier = Modifier.size(60.dp).clip(RoundedCornerShape(8.dp))
                                        )
                                    } else {
                                        Icon(Icons.Default.Description, contentDescription = null, tint = Teal, modifier = Modifier.size(60.dp))
                                    }
                                    Spacer(Modifier.width(16.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(L.prescriptionAttached(isEnglish), fontWeight = FontWeight.Bold, color = Teal)
                                        Text(L.sharedWithPharmacist(isEnglish), fontSize = 12.sp, color = TextMuted)
                                    }
                                    IconButton(onClick = { viewModel.clearPrescription() }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Remove", tint = Color.Red)
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(12.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(L.cartItemsHeader(isEnglish), fontWeight = FontWeight.Bold, color = Teal, modifier = Modifier.padding(bottom = 8.dp))
                                cartItems.forEach { item ->
                                    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            ZoomableMedicineImage(imageResName = item.imageResName, size = 50)
                                            Spacer(Modifier.width(12.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Text(item.medicineName, color = TextPrimary, fontWeight = FontWeight.Medium)
                                                    if (item.requiresPrescription) {
                                                        Spacer(Modifier.width(4.dp))
                                                        Icon(Icons.Default.Description, contentDescription = "RX Required", tint = Color.Red, modifier = Modifier.size(14.dp))
                                                    }
                                                }
                                                if (item.isAutoFilled) {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Teal, modifier = Modifier.size(12.dp))
                                                        Spacer(Modifier.width(4.dp))
                                                        Text(if (isEnglish) "Auto-filled" else "ಸ್ವಯಂ-ಭರ್ತಿ", fontSize = 11.sp, color = Teal)
                                                    }
                                                }
                                            }
                                            IconButton(onClick = { viewModel.removeFromCart(item.medicineName) }, modifier = Modifier.size(24.dp)) {
                                                Icon(Icons.Default.Delete, contentDescription = "Remove", tint = Color.Red, modifier = Modifier.size(18.dp))
                                            }
                                        }
                                    }
                                }
                                
                                Spacer(Modifier.height(16.dp))
                                if (!hasPrescription) {
                                    Button(
                                        onClick = { showPrescriptionDialog = true },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = ButtonDefaults.buttonColors(containerColor = Teal, contentColor = Color.White),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Icon(Icons.Default.UploadFile, contentDescription = null)
                                        Spacer(Modifier.width(8.dp))
                                        Text(L.uploadPrescription(isEnglish))
                                    }
                                } else {
                                    Button(
                                        onClick = { showPrescriptionDialog = true },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color.Gray, contentColor = Color.White),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Icon(Icons.Default.AddAPhoto, contentDescription = null)
                                        Spacer(Modifier.width(8.dp))
                                        Text(L.changePrescription(isEnglish))
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
                        EmptyCartView(
                            isEnglish = isEnglish,
                            onGoSearch = { navController.navigate("search") }
                        )
                    }
                } else {
                    // Smart Matches Section
                    val isActionBlocked = requiresPrescription && !hasPrescription

                    // Complete Matches
                    val completeMatches = matches.filter { it.matchPercentage == 100 }
                    if (completeMatches.isNotEmpty()) {
                        item {
                            SectionHeader(title = L.completeMatches(isEnglish), icon = Icons.Default.CheckCircle, color = Color(0xFF2E7D32))
                        }
                        items(completeMatches) { match ->
                            PharmacyMatchCard(
                                match = match, 
                                isComplete = true, 
                                viewModel = viewModel,
                                isBlocked = isActionBlocked,
                                hasPrescription = hasPrescription,
                                isEnglish = isEnglish,
                                onAddPrescription = { showPrescriptionDialog = true }
                            )
                        }
                    }

                    // Partial Matches
                    val partialMatches = matches.filter { it.matchPercentage in 1..99 }
                    if (partialMatches.isNotEmpty()) {
                        item {
                            SectionHeader(title = L.partialMatches(isEnglish), icon = Icons.Default.Info, color = Color(0xFFE65100))
                        }
                        items(partialMatches) { match ->
                            PharmacyMatchCard(
                                match = match, 
                                isComplete = false, 
                                viewModel = viewModel,
                                isBlocked = isActionBlocked,
                                hasPrescription = hasPrescription,
                                isEnglish = isEnglish,
                                onAddPrescription = { showPrescriptionDialog = true }
                            )
                        }
                    }
                    
                    if (matches.isEmpty()) {
                        item {
                            Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                                Text(L.noMatchesFound(isEnglish), color = TextMuted)
                            }
                        }
                    }
                }
                
                item { Spacer(modifier = Modifier.height(32.dp)) }
            }
        }
    }

    if (isUploading) {
        androidx.compose.ui.window.Dialog(onDismissRequest = {}) {
            Card {
                Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = Teal)
                    Spacer(Modifier.height(16.dp))
                    Text(L.uploadingPrescription(isEnglish))
                }
            }
        }
    }

    // Consent Dialog
    if (showPrescriptionDialog) {
        AlertDialog(
            onDismissRequest = { showPrescriptionDialog = false },
            title = { Text(L.s(isEnglish, "Prescription Upload", "ಪ್ರಿಸ್ಕ್ರಿಪ್ಷನ್ ಅಪ್‌ಲೋಡ್"), fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(L.prescriptionConsent(isEnglish))
                    Spacer(Modifier.height(16.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = acceptedTerms, onCheckedChange = { acceptedTerms = it })
                        Text(
                            text = if (isEnglish) "I Accept" else "ನಾನು ಒಪ್ಪುತ್ತೇನೆ",
                            modifier = Modifier.clickable { acceptedTerms = !acceptedTerms }.padding(start = 8.dp)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    enabled = acceptedTerms,
                    onClick = {
                        val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            arrayOf(Manifest.permission.CAMERA, Manifest.permission.READ_MEDIA_IMAGES)
                        } else {
                            arrayOf(Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE)
                        }
                        permissionLauncher.launch(permissions)
                        showPrescriptionDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Teal, contentColor = Color.White)
                ) {
                    Text(L.confirm(isEnglish))
                }
            },
            dismissButton = {
                TextButton(onClick = { showPrescriptionDialog = false }) {
                    Text(L.s(isEnglish, "Decline", "ತಿರಸ್ಕರಿಸಿ"), color = Color.Red)
                }
            }
        )
    }

    // Source Picker Dialog
    if (showSourcePicker) {
        AlertDialog(
            onDismissRequest = { showSourcePicker = false },
            title = { Text(L.s(isEnglish, "Choose Source", "ಮೂಲವನ್ನು ಆಯ್ಕೆಮಾಡಿ")) },
            text = { Text(L.s(isEnglish, "Pick a photo from gallery or take a new one using camera.", "ಗ್ಯಾಲರಿಯಿಂದ ಫೋಟೋ ಆಯ್ಕೆಮಾಡಿ ಅಥವಾ ಕ್ಯಾಮರಾ ಬಳಸಿ ಹೊಸದನ್ನು ತೆಗೆಯಿರಿ.")) },
            confirmButton = {
                TextButton(onClick = {
                    galleryLauncher.launch("image/*")
                    showSourcePicker = false
                }) {
                    Text(L.gallery(isEnglish), color = Teal)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    cameraLauncher.launch(null) 
                    showSourcePicker = false
                }) {
                    Text(L.takePhoto(isEnglish), color = Teal)
                }
            }
        )
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
private fun PharmacyMatchCard(
    match: PharmacyMatch, 
    isComplete: Boolean, 
    viewModel: CartViewModel,
    isBlocked: Boolean,
    hasPrescription: Boolean,
    isEnglish: Boolean,
    onAddPrescription: () -> Unit
) {
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
                        Text(text = L.distanceAway(isEnglish, match.distanceKm), fontSize = 14.sp, color = TextMuted)
                    }
                }
                
                Surface(
                    color = if (isComplete) Color(0xFFE8F5E9) else Color(0xFFFFF3E0),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = if (isComplete) L.s(isEnglish, "100% Match", "100% ಹೊಂದಾಣಿಕೆ") else L.s(isEnglish, "${match.matchedItems.size} Items", "${match.matchedItems.size} ವಸ್ತುಗಳು"),
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
                    Text(text = item.medicineName, modifier = Modifier.weight(1f), color = if (isBlocked) TextMuted else TextPrimary)
                    Icon(Icons.Default.Check, contentDescription = null, tint = if (isBlocked) Color.Gray else Color(0xFF2E7D32), modifier = Modifier.size(16.dp))
                }
            }

            if (match.missingItems.isNotEmpty()) {
                Text(text = L.s(isEnglish, "Missing:", "ಕಾಣೆಯಾಗಿದೆ:"), color = Color(0xFFC62828), fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp))
                match.missingItems.forEach { missing ->
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 2.dp)) {
                        Text(text = missing, modifier = Modifier.weight(1f), color = Color(0xFFC62828).copy(alpha = if (isBlocked) 0.5f else 1f))
                        Icon(Icons.Default.Close, contentDescription = null, tint = Color(0xFFC62828).copy(alpha = if (isBlocked) 0.5f else 1f), modifier = Modifier.size(16.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            if (!hasPrescription) {
                OutlinedButton(
                    onClick = onAddPrescription,
                    modifier = Modifier.fillMaxWidth().height(44.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = if (isBlocked) Color.Red else Teal),
                    border = androidx.compose.foundation.BorderStroke(1.dp, if (isBlocked) Color.Red else Teal),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.UploadFile, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(if (isBlocked) L.s(isEnglish, "Upload Prescription to Unlock", "ಅನ್ಲಾಕ್ ಮಾಡಲು ಪ್ರಿಸ್ಕ್ರಿಪ್ಷನ್ ಅಪ್‌ಲೋಡ್ ಮಾಡಿ") else L.scanPrescription(isEnglish))
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Action Blockers (Disabled if isBlocked)
            
            // 1. Get Directions Button
            Button(
                onClick = { 
                    LocationUtils.launchGoogleMaps(context, match.pharmacy.latitude, match.pharmacy.longitude, match.pharmacy.name)
                },
                enabled = !isBlocked,
                modifier = Modifier.fillMaxWidth().height(44.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Teal, contentColor = Color.White),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.Directions, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(L.getDirections(isEnglish))
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 2. Request Restock Button
            if (match.missingItems.isNotEmpty()) {
                OutlinedButton(
                    onClick = { 
                        viewModel.sendRequest(match.pharmacy.shopId, RequestType.RESTOCK)
                    },
                    enabled = !isBlocked,
                    modifier = Modifier.fillMaxWidth().height(44.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFE65100)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE65100)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(L.requestRestock(isEnglish))
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            // 3. Request Home Delivery Button
            Button(
                onClick = { 
                    viewModel.sendRequest(match.pharmacy.shopId, RequestType.DELIVERY)
                },
                enabled = !isBlocked,
                modifier = Modifier.fillMaxWidth().height(44.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32), contentColor = Color.White),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.LocalShipping, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(L.requestDelivery(isEnglish))
            }
        }
    }
}

@Composable
private fun EmptyCartView(isEnglish: Boolean, onGoSearch: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 64.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(Icons.Default.ShoppingCart, contentDescription = null, tint = Color(0xFFE0F2F1), modifier = Modifier.size(80.dp))
        Spacer(modifier = Modifier.height(16.dp))
        Text(L.emptyCart(isEnglish), fontWeight = FontWeight.Bold, fontSize = 18.sp, color = TextPrimary)
        Text(L.emptyCartSub(isEnglish), color = TextMuted)
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onGoSearch, 
            colors = ButtonDefaults.buttonColors(containerColor = Teal, contentColor = Color.White), 
            modifier = Modifier.fillMaxWidth(0.7f)
        ) {
            Text(L.goToSearch(isEnglish))
        }
    }
}
