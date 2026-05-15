package com.example.gramasanjeevin.ui

import android.Manifest
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import kotlinx.coroutines.delay

private val Teal = Color(0xFF00695C)
private val TealLight = Color(0xFFE0F2F1)
private val TextPrimary = Color(0xFF1A2B35)
private val TextMuted = Color(0xFF6B7280)

@Composable
fun PrescriptionScreen(
    onMedicinesFound: () -> Unit,
    cartViewModel: CartViewModel = viewModel()
) {
    var isScanning by remember { mutableStateOf(false) }
    var medicinesFound by remember { mutableStateOf<List<String>>(emptyList()) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var capturedBitmap by remember { mutableStateOf<Bitmap?>(null) }

    var showPrescriptionDialog by remember { mutableStateOf(false) }
    var acceptedTerms by remember { mutableStateOf(false) }
    var showSourcePicker by remember { mutableStateOf(false) }

    val context = LocalContext.current

    // Activity Result Launchers
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            capturedBitmap = null
            isScanning = true
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        bitmap?.let {
            capturedBitmap = it
            selectedImageUri = null
            isScanning = true
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val cameraGranted = permissions[Manifest.permission.CAMERA] == true
        val storageGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions[Manifest.permission.READ_MEDIA_IMAGES] == true
        } else {
            permissions[Manifest.permission.READ_EXTERNAL_STORAGE] == true
        }
        
        // Even if only camera is granted, we can allow camera. 
        // Note: GetContent doesn't strictly need storage permission on many Android versions.
    }

    if (isScanning) {
        LaunchedEffect(Unit) {
            delay(3000) // Simulate OCR process
            medicinesFound = listOf("Amoxicillin 500mg", "Paracetamol 650mg", "Diazepam 5mg")
            isScanning = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Upload Prescription",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
            modifier = Modifier.fillMaxWidth()
        )
        Text(
            text = "We'll scan it and find your medicines.",
            fontSize = 16.sp,
            color = TextMuted,
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Upload Selection Box / Preview
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFFF8F9FA))
                .border(2.dp, Color(0xFFE0E0E0), RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            if (selectedImageUri != null || capturedBitmap != null) {
                AsyncImage(
                    model = selectedImageUri ?: capturedBitmap,
                    contentDescription = "Prescription Preview",
                    modifier = Modifier.fillMaxSize().padding(8.dp).clip(RoundedCornerShape(12.dp))
                )
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.AddPhotoAlternate,
                        contentDescription = null,
                        tint = Teal,
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Upload a clear photo",
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Ensure all medicine names are visible.",
                        fontSize = 13.sp,
                        color = TextMuted
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Button(
                        onClick = { showPrescriptionDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Teal),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Text("Select Photo", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        if (selectedImageUri != null || capturedBitmap != null) {
            TextButton(
                onClick = { showPrescriptionDialog = true },
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Text("Retake/Change Photo", color = Teal)
            }
        }

        AnimatedVisibility(visible = isScanning) {
            Column(
                modifier = Modifier.padding(vertical = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator(color = Teal, strokeWidth = 3.dp)
                Spacer(modifier = Modifier.height(12.dp))
                Text("Scanning Prescription...", color = Teal, fontWeight = FontWeight.Bold)
            }
        }

        AnimatedVisibility(visible = medicinesFound.isNotEmpty() && !isScanning) {
            Column(modifier = Modifier.padding(top = 32.dp)) {
                Text(
                    text = "Found Medicines",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                medicinesFound.forEach { med ->
                    MedicineFoundCard(name = med, onAdd = { cartViewModel.addToCart(med) })
                    Spacer(modifier = Modifier.height(12.dp))
                }

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = {
                        cartViewModel.addMultipleToCart(medicinesFound)
                        onMedicinesFound()
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Teal),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Add All to Cart", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    // Consent Dialog
    if (showPrescriptionDialog) {
        AlertDialog(
            onDismissRequest = { showPrescriptionDialog = false },
            title = { Text("Prescription Upload", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("The selected/uploaded images of your prescription will be shared with the pharmacist and admin if necessary.")
                    Spacer(Modifier.height(16.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = acceptedTerms, onCheckedChange = { acceptedTerms = it })
                        Text(
                            text = "I Accept",
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
                        showSourcePicker = true
                        showPrescriptionDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Teal)
                ) {
                    Text("Continue")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPrescriptionDialog = false }) {
                    Text("Decline", color = Color.Red)
                }
            }
        )
    }

    // Source Picker Dialog
    if (showSourcePicker) {
        AlertDialog(
            onDismissRequest = { showSourcePicker = false },
            title = { Text("Choose Source") },
            text = { Text("How would you like to provide the prescription?") },
            confirmButton = {
                TextButton(onClick = {
                    galleryLauncher.launch("image/*")
                    showSourcePicker = false
                }) {
                    Text("Gallery", color = Teal, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    cameraLauncher.launch()
                    showSourcePicker = false
                }) {
                    Text("Camera", color = Teal, fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

@Composable
fun MedicineFoundCard(name: String, onAdd: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = name, fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Surface(
                    color = Color(0xFFE8F5E9),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = "In Stock",
                        color = Color(0xFF2E7D32),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }
            IconButton(
                onClick = onAdd,
                modifier = Modifier.background(TealLight, CircleShape)
            ) {
                Icon(Icons.Default.AddShoppingCart, contentDescription = null, tint = Teal, modifier = Modifier.size(20.dp))
            }
        }
    }
}
