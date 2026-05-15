package com.example.gramasanjeevin.ui

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.gramasanjeevin.utils.FirestoreProvider
import com.example.gramasanjeevin.utils.L
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import org.json.JSONArray

// ============================================================================
// VIEWMODEL: Handles the Gemini API Logic with Auto-Fill Logic
// ============================================================================
class PrescriptionScannerViewModel : ViewModel() {

    private val _extractedText = MutableStateFlow("")
    val extractedText: StateFlow<String> = _extractedText

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _scanResultMessage = MutableStateFlow<String?>(null)
    val scanResultMessage: StateFlow<String?> = _scanResultMessage

    private val _selectedImageBitmap = MutableStateFlow<Bitmap?>(null)
    val selectedImageBitmap: StateFlow<Bitmap?> = _selectedImageBitmap

    private val db = FirestoreProvider.getDb()

    // Task 1: Use "gemini-3-flash" model and exact structured prompt
    // Note: If "gemini-3-flash" results in an error due to SDK limits, gemini-1.5-flash is the stable fallback
    private val generativeModel = GenerativeModel(
        modelName = "gemini-3-flash",
        apiKey = "AIzaSyAci0Yct9GjIvRvc4SGcg7287W2VQD0Z5A" // Provided API Key
    )

    fun resetState(isEnglish: Boolean) {
        _extractedText.value = if (isEnglish) "Upload a prescription to begin..." else "ಪ್ರಾರಂಭಿಸಲು ಪ್ರಿಸ್ಕ್ರಿಪ್ಷನ್ ಅಪ್‌ಲೋಡ್ ಮಾಡಿ..."
        _scanResultMessage.value = null
        _isLoading.value = false
        _selectedImageBitmap.value = null
    }

    fun updateSelectedBitmap(bitmap: Bitmap?) {
        _selectedImageBitmap.value = bitmap
    }

    fun analyzePrescription(bitmap: Bitmap, cartViewModel: CartViewModel, isEnglish: Boolean) {
        viewModelScope.launch {
            _isLoading.value = true
            // Requirement 4: AI scanning prescription...
            _extractedText.value = L.aiScanning(isEnglish)

            try {
                // Requirement 1: The Gemini Structured Prompt
                val inputContent = content {
                    image(bitmap)
                    text(
                        "You are an expert medical OCR AI. Read this handwritten prescription (which may contain English and Kannada). Extract only the names of the medicines. Translate any Kannada to English. Return ONLY a valid JSON array of strings representing the medicine names, and absolutely no other text, markdown, or backticks. Example: [\"Paracetamol\", \"Amoxicillin\"]"
                    )
                }

                val response = generativeModel.generateContent(inputContent)
                val responseText = response.text ?: ""
                Log.d("GeminiOCR", "Raw Response: $responseText")
                
                // Requirement 2: Parse the JSON string
                val extractedMedicines = parseMedicinesJson(responseText)

                if (extractedMedicines.isEmpty()) {
                    _extractedText.value = L.ocrNoMedicines(isEnglish)
                    return@launch
                }

                _extractedText.value = L.ocrCrossReferencing(isEnglish)
                
                // Requirement 2: Match Database (Cross-reference with inventory)
                val inventorySnapshot = db.collection("inventory").get().await()
                val dbMedicines = inventorySnapshot.documents.mapNotNull { it.getString("medicineName") }
                
                val addedItems = mutableListOf<String>()
                
                // Requirement 3: Auto-Fill the Cart for matching medicines
                for (extractedMed in extractedMedicines) {
                    val extractedLower = extractedMed.lowercase().trim()
                    
                    // Case-insensitive .contains() match
                    val matchedDbName = dbMedicines.find { dbName ->
                        val dbLower = dbName.lowercase().trim()
                        dbLower.contains(extractedLower) || extractedLower.contains(dbLower)
                    }
                    
                    if (matchedDbName != null) {
                        cartViewModel.addToCart(matchedDbName, isAutoFilled = true)
                        addedItems.add(matchedDbName)
                    }
                }

                if (addedItems.isNotEmpty()) {
                    // Requirement 4: Display Snackbar message content
                    _scanResultMessage.value = L.aiFoundAdded(isEnglish, addedItems.size)
                    _extractedText.value = L.ocrSuccessfullyExtracted(isEnglish, addedItems.joinToString(", "))
                } else {
                    _extractedText.value = L.ocrNoMatches(isEnglish, extractedMedicines.joinToString(", "))
                }
                
            } catch (e: Exception) {
                _extractedText.value = "${L.errorLabel(isEnglish)}: ${e.message}"
                Log.e("PrescriptionScanner", "Analysis error", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun parseMedicinesJson(jsonString: String): List<String> {
        return try {
            // Clean markdown if present
            val cleaned = jsonString.trim()
                .replace("```json", "")
                .replace("```", "")
                .replace("`", "")
                .replace("json", "", ignoreCase = true)
                .trim()
            
            val start = cleaned.indexOf('[')
            val end = cleaned.lastIndexOf(']')
            val jsonToParse = if (start != -1 && end != -1) {
                cleaned.substring(start, end + 1)
            } else {
                cleaned
            }

            val jsonArray = JSONArray(jsonToParse)
            val list = mutableListOf<String>()
            for (i in 0 until jsonArray.length()) {
                list.add(jsonArray.getString(i))
            }
            list
        } catch (e: Exception) {
            Log.e("PrescriptionScanner", "JSON Parse error: ${e.message}")
            emptyList()
        }
    }

    fun clearScanResult() {
        _scanResultMessage.value = null
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrescriptionScannerScreen(
    navController: androidx.navigation.NavController,
    viewModel: PrescriptionScannerViewModel = viewModel(),
    cartViewModel: CartViewModel = viewModel(),
    authViewModel: AuthViewModel = viewModel(),
    onPrescriptionVerified: () -> Unit = {} 
) {
    val context = LocalContext.current
    val isEnglish by authViewModel.isEnglish.collectAsState()
    val extractedText by viewModel.extractedText.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val scanResultMessage by viewModel.scanResultMessage.collectAsState()
    val selectedImageBitmap by viewModel.selectedImageBitmap.collectAsState()
    
    val snackbarHostState = remember { SnackbarHostState() }
    var hasConsented by remember { mutableStateOf(false) }
    val Teal = Color(0xFF00695C)

    // Initial load
    LaunchedEffect(Unit) {
        if (extractedText.isEmpty()) {
            viewModel.resetState(isEnglish)
        }
    }

    // Requirement 4: Handle scan completion feedback and auto-navigation to Cart
    LaunchedEffect(scanResultMessage) {
        scanResultMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            delay(1500)
            navController.navigate("cart") {
                popUpTo("prescription") { inclusive = true }
            }
            viewModel.clearScanResult()
        }
    }

    // Launcher for Gallery
    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver, it))
            } else {
                @Suppress("DEPRECATION")
                MediaStore.Images.Media.getBitmap(context.contentResolver, it)
            }
            val softwareBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
            viewModel.updateSelectedBitmap(softwareBitmap)
            
            // AI Extraction + Simultaneous Upload
            viewModel.analyzePrescription(softwareBitmap, cartViewModel, isEnglish)
            cartViewModel.uploadPrescription(it)
        }
    }

    // Launcher for Camera
    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap: Bitmap? ->
        bitmap?.let {
            val softwareBitmap = it.copy(Bitmap.Config.ARGB_8888, true)
            viewModel.updateSelectedBitmap(softwareBitmap)
            
            // AI Extraction + Simultaneous Upload
            viewModel.analyzePrescription(softwareBitmap, cartViewModel, isEnglish)
            cartViewModel.uploadPrescriptionBitmap(it)
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            cameraLauncher.launch(null)
        } else {
            Toast.makeText(context, if (isEnglish) "Camera permission required" else "ಕ್ಯಾಮರಾ ಅನುಮತಿ ಅಗತ್ಯವಿದೆ", Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(L.aiScannerTitle(isEnglish), fontWeight = FontWeight.Bold, fontSize = 20.sp) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = L.back(isEnglish))
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.White, titleContentColor = Teal)
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Consent Card
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = hasConsented, onCheckedChange = { hasConsented = it })
                    Text(
                        text = L.prescriptionConsent(isEnglish),
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                Button(
                    onClick = {
                        viewModel.resetState(isEnglish)
                        val hasPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
                        if (hasPermission) cameraLauncher.launch(null) else cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                    },
                    enabled = hasConsented,
                    colors = ButtonDefaults.buttonColors(containerColor = Teal, contentColor = Color.White),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f).padding(end = 8.dp)
                ) {
                    Icon(Icons.Default.PhotoCamera, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(L.takePhoto(isEnglish))
                }

                Button(
                    onClick = {
                        viewModel.resetState(isEnglish)
                        galleryLauncher.launch("image/*")
                    },
                    enabled = hasConsented,
                    colors = ButtonDefaults.buttonColors(containerColor = Teal, contentColor = Color.White),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f).padding(start = 8.dp)
                ) {
                    Icon(Icons.Default.Collections, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(L.gallery(isEnglish))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (selectedImageBitmap != null) {
                Card(
                    modifier = Modifier.fillMaxWidth().height(200.dp),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Image(bitmap = selectedImageBitmap!!.asImageBitmap(), contentDescription = "Preview", modifier = Modifier.fillMaxSize())
                }
            } else {
                Surface(
                    modifier = Modifier.fillMaxWidth().height(150.dp),
                    color = Color(0xFFF5F5F5),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Image, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(48.dp))
                            Text(L.noImageSelected(isEnglish), color = Color.Gray)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier.fillMaxWidth().weight(1f),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp).verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = L.extractedMedicines(isEnglish),
                        fontWeight = FontWeight.Bold,
                        color = Teal,
                        modifier = Modifier.align(Alignment.Start)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    if (isLoading) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth().padding(top = 24.dp)) {
                            CircularProgressIndicator(color = Teal)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(L.aiScanning(isEnglish), color = Teal, fontWeight = FontWeight.Medium)
                        }
                    } else {
                        Text(text = extractedText, modifier = Modifier.align(Alignment.Start))
                        
                        if (selectedImageBitmap != null && !isLoading && extractedText.contains(",")) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = {
                                    onPrescriptionVerified()
                                    navController.navigate("cart") {
                                        popUpTo("cart") { inclusive = true }
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32), contentColor = Color.White),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(L.confirmContinueCart(isEnglish), fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}
