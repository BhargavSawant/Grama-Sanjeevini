package com.example.gramasanjeevin.ui

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ManageSearch
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.gramasanjeevin.model.SearchResult
import com.example.gramasanjeevin.utils.L
import kotlinx.coroutines.launch

private val Teal = Color(0xFF00695C)
private val TealLight = Color(0xFFE0F2F1)
private val TextPrimary = Color(0xFF1A2B35)
private val TextMuted = Color(0xFF6B7280)

@Composable
fun SearchScreen(
    navController: NavController,
    viewModel: SearchViewModel = viewModel(),
    cartViewModel: CartViewModel = viewModel(),
    authViewModel: AuthViewModel = viewModel()
) {
    val searchResults by viewModel.searchResults.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    
    val isEnglish by authViewModel.isEnglish.collectAsState()

    var showFirstAidDialog by remember { mutableStateOf(false) }

    // Voice Search Launcher
    val speechLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val spokenText = data?.get(0) ?: ""
            searchQuery = spokenText
            viewModel.searchMedicine(spokenText)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = Color(0xFFF8F9FB)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Search Header
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(24.dp)
            ) {
                Text(
                    text = L.searchMedicine(isEnglish),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = {
                        searchQuery = it
                        viewModel.searchMedicine(it)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text(L.s(isEnglish, "Search for a medicine...", "ಔಷಧಿಗಾಗಿ ಹುಡುಕಿ...")) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Teal) },
                    trailingIcon = {
                        Row {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = ""; viewModel.searchMedicine("") }) {
                                    Icon(Icons.Default.Close, contentDescription = "Clear")
                                }
                            }
                            IconButton(onClick = {
                                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                                    putExtra(RecognizerIntent.EXTRA_PROMPT, L.s(isEnglish, "Speak medicine name...", "ಔಷಧದ ಹೆಸರನ್ನು ಹೇಳಿ..."))
                                }
                                speechLauncher.launch(intent)
                            }) {
                                Icon(Icons.Default.Mic, contentDescription = "Voice Search", tint = Teal)
                            }
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Teal,
                        unfocusedBorderColor = Color.LightGray,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    ),
                    singleLine = true
                )
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                if (searchQuery.isEmpty()) {
                    item { SearchEmptyState(isEnglish) }

                    item {
                        Text(
                            text = L.s(isEnglish, "Popular in your area", "ನಿಮ್ಮ ಪ್ರದೇಶದಲ್ಲಿ ಜನಪ್ರಿಯ"),
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
                        )
                    }

                    // Updated Popular items
                    items(listOf("Paracetamol 650mg", "Amoxicillin 500mg", "Dolo 650 Tablet", "Insulin Glargine", "ORS Sachet")) { med ->
                        RecentSearchItem(
                            name = med,
                            onSearchRequested = {
                                searchQuery = med
                                viewModel.searchMedicine(med)
                            },
                            onAddToCart = {
                                cartViewModel.addToCart(med)
                                scope.launch {
                                    snackbarHostState.showSnackbar(L.s(isEnglish, "$med added to cart", "$med ಕಾರ್ಟ್‌ಗೆ ಸೇರಿಸಲಾಗಿದೆ"))
                                }
                            }
                        )
                    }

                    item {
                        Text(
                            L.s(isEnglish, "Health Resources", "ಆರೋಗ್ಯ ಸಂಪನ್ಮೂಲಗಳು"),
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
                        )
                    }

                    item {
                        Row(modifier = Modifier.padding(horizontal = 24.dp)) {
                            ResourceCard(
                                title = L.s(isEnglish, "Monsoon Health Guide", "ಮುಂಗಾರು ಆರೋಗ್ಯ ಮಾರ್ಗದರ್ಶಿ"),
                                modifier = Modifier.weight(1f).height(160.dp),
                                color = Color(0xFF2E7D32)
                            )
                            Spacer(Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                ResourceSmallCard(
                                    title = L.firstAid(isEnglish), 
                                    icon = Icons.Default.MedicalServices, 
                                    color = Color.White,
                                    onClick = { showFirstAidDialog = true }
                                )
                                Spacer(Modifier.height(12.dp))
                                ResourceSmallCard(
                                    title = L.helpline(isEnglish), 
                                    icon = Icons.Default.Phone, 
                                    color = Color.White,
                                    onClick = {
                                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:104"))
                                        context.startActivity(intent)
                                    }
                                )
                            }
                        }
                    }
                } else {
                    if (isLoading) {
                        item {
                            Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(color = Teal)
                            }
                        }
                    } else if (searchResults.isEmpty()) {
                        item {
                            Box(modifier = Modifier.fillMaxWidth().padding(48.dp), contentAlignment = Alignment.Center) {
                                Text(L.s(isEnglish, "No medicines found matching \"$searchQuery\"", "\"$searchQuery\" ಗೆ ಹೊಂದಿಕೆಯಾಗುವ ಯಾವುದೇ ಔಷಧಗಳು ಕಂಡುಬಂದಿಲ್ಲ"), color = TextMuted, textAlign = TextAlign.Center)
                            }
                        }
                    } else {
                        items(searchResults) { result ->
                            MedicineSearchResultCard(
                                result = result, 
                                isEnglish = isEnglish,
                                onAddToCart = {
                                    cartViewModel.addToCart(result.item.medicineName)
                                    scope.launch {
                                        snackbarHostState.showSnackbar(L.s(isEnglish, "${result.item.medicineName} added to cart", "${result.item.medicineName} ಕಾರ್ಟ್‌ಗೆ ಸೇರಿಸಲಾಗಿದೆ"))
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showFirstAidDialog) {
        FirstAidDialog(onDismiss = { showFirstAidDialog = false })
    }
}

@Composable
fun ZoomableMedicineImage(imageResName: String, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val imageResId = remember(imageResName) {
        if (imageResName.isNotEmpty()) {
            context.resources.getIdentifier(imageResName, "drawable", context.packageName)
        } else 0
    }
    var showDialog by remember { mutableStateOf(false) }

    if (imageResId != 0) {
        Box(
            modifier = modifier
                .size(80.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White)
                .border(1.dp, Color(0xFFEEEEEE), RoundedCornerShape(12.dp))
                .clickable { showDialog = true },
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = imageResId),
                contentDescription = null,
                modifier = Modifier.padding(8.dp).fillMaxSize(),
                contentScale = ContentScale.Fit
            )
        }

        if (showDialog) {
            Dialog(onDismissRequest = { showDialog = false }) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .padding(16.dp),
                    shape = RoundedCornerShape(24.dp),
                    color = Color.White,
                    tonalElevation = 8.dp
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        var scale by remember { mutableStateOf(1f) }
                        var offset by remember { mutableStateOf(Offset.Zero) }
                        val state = rememberTransformableState { zoomChange, offsetChange, _ ->
                            scale *= zoomChange
                            offset += offsetChange
                        }

                        Image(
                            painter = painterResource(id = imageResId),
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp)
                                .graphicsLayer(
                                    scaleX = scale.coerceIn(1f, 5f),
                                    scaleY = scale.coerceIn(1f, 5f),
                                    translationX = offset.x,
                                    translationY = offset.y
                                )
                                .transformable(state = state),
                            contentScale = ContentScale.Fit
                        )
                        
                        IconButton(
                            onClick = { showDialog = false },
                            modifier = Modifier.align(Alignment.TopEnd).padding(8.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = TextPrimary)
                        }
                    }
                }
            }
        }
    } else {
        // Fallback placeholder
        Box(
            modifier = modifier
                .size(80.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(TealLight),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.MedicalServices, contentDescription = null, tint = Teal, modifier = Modifier.size(32.dp))
        }
    }
}

@Composable
fun SearchEmptyState(isEnglish: Boolean) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.size(140.dp).clip(CircleShape).background(TealLight),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.AutoMirrored.Filled.ManageSearch, contentDescription = null, tint = Teal, modifier = Modifier.size(60.dp))
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = L.s(isEnglish, "Search for a medicine", "ಔಷಧಕ್ಕಾಗಿ ಹುಡುಕಿ"),
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = TextPrimary
        )
        Text(
            text = L.s(isEnglish, "Enter the name of the medicine you need and we'll find the nearest shops for you.", "ನಿಮಗೆ ಬೇಕಾದ ಔಷಧದ ಹೆಸರನ್ನು ನಮೂದಿಸಿ ಮತ್ತು ನಾವು ನಿಮಗಾಗಿ ಹತ್ತಿರದ ಅಂಗಡಿಗಳನ್ನು ಕಂಡುಕೊಳ್ಳುತ್ತೇವೆ."),
            textAlign = TextAlign.Center,
            color = TextMuted,
            fontSize = 14.sp,
            modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 8.dp)
        )
    }
}

@Composable
fun RecentSearchItem(name: String, onSearchRequested: () -> Unit, onAddToCart: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 6.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.History, contentDescription = null, tint = TextMuted, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = name, 
                fontWeight = FontWeight.Medium, 
                color = TextPrimary, 
                modifier = Modifier.weight(1f).clickable { onSearchRequested() }
            )
            IconButton(onClick = onAddToCart) {
                Icon(Icons.Default.Add, contentDescription = "Quick Add", tint = Teal)
            }
        }
    }
}

@Composable
fun MedicineSearchResultCard(result: SearchResult, isEnglish: Boolean, onAddToCart: () -> Unit) {
    val isOutOfStock = result.item.quantity <= 0
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ZoomableMedicineImage(imageResName = result.item.imageResName)
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(result.item.medicineName, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = TextPrimary)
                        Text(result.pharmacy.name, color = TextMuted, fontSize = 14.sp)
                        Text(result.pharmacy.village, color = TextMuted, fontSize = 12.sp)
                    }
                    Surface(
                        color = if (isOutOfStock) Color(0xFFFFEBEE) else if (result.item.quantity > 5) Color(0xFFE8F5E9) else Color(0xFFFFF3E0),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = if (isOutOfStock) L.s(isEnglish, "OUT", "ಖಾಲಿ") else "${result.item.quantity}",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isOutOfStock) Color.Red else if (result.item.quantity > 5) Color(0xFF2E7D32) else Color(0xFFE65100)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, tint = Teal, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("${result.distanceKm} km", color = TextMuted, fontSize = 14.sp)
                    Spacer(modifier = Modifier.weight(1f))
                    
                    if (isOutOfStock) {
                        IconButton(onClick = { /* re-stock */ }) {
                            Icon(Icons.Default.Refresh, contentDescription = "Request", tint = Teal)
                        }
                    } else {
                        Button(
                            onClick = onAddToCart,
                            colors = ButtonDefaults.buttonColors(containerColor = Teal, contentColor = Color.White),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Icon(Icons.Default.AddShoppingCart, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(L.s(isEnglish, "Add", "ಸೇರಿಸಿ"), fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ResourceCard(title: String, modifier: Modifier, color: Color) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = color)
    ) {
        Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            Text(title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.align(Alignment.BottomStart))
        }
    }
}

@Composable
fun ResourceSmallCard(title: String, icon: ImageVector, color: Color, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(74.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = color),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(modifier = Modifier.fillMaxSize().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = Teal, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Text(title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextPrimary, lineHeight = 16.sp)
        }
    }
}
