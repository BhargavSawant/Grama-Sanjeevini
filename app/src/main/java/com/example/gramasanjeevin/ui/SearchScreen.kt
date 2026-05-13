package com.example.gramasanjeevin.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.gramasanjeevin.model.SearchResult
import kotlinx.coroutines.launch

private val Teal = Color(0xFF00695C)
private val TealLight = Color(0xFFE0F2F1)
private val TextPrimary = Color(0xFF1A2B35)
private val TextMuted = Color(0xFF6B7280)

@Composable
fun SearchScreen(
    navController: NavController,
    viewModel: SearchViewModel = viewModel(),
    cartViewModel: CartViewModel = viewModel()
) {
    val searchResults by viewModel.searchResults.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

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
                    text = "Find Medicine",
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
                    placeholder = { Text("Search for a medicine...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Teal) },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Teal,
                        unfocusedBorderColor = Color.LightGray,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    ),
                    singleLine = true,
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = ""; viewModel.searchMedicine("") }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear")
                            }
                        }
                    }
                )
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                if (searchQuery.isEmpty()) {
                    item { SearchEmptyState() }

                    item {
                        Text(
                            text = "Popular in your area",
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
                        )
                    }

                    // Enhanced Popular items with both search and add to cart functionality
                    items(listOf("Paracetamol", "Amoxicillin", "Dolo 650", "Insulin", "ORS")) { med ->
                        RecentSearchItem(
                            name = med,
                            onSearchRequested = {
                                searchQuery = med
                                viewModel.searchMedicine(med)
                            },
                            onAddToCart = {
                                cartViewModel.addToCart(med)
                                scope.launch {
                                    snackbarHostState.showSnackbar("$med added to cart")
                                }
                            }
                        )
                    }

                    item {
                        Text(
                            "Health Resources",
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
                        )
                    }

                    item {
                        Row(modifier = Modifier.padding(horizontal = 24.dp)) {
                            ResourceCard(
                                title = "Monsoon Health Guide",
                                modifier = Modifier.weight(1f).height(160.dp),
                                color = Color(0xFF2E7D32)
                            )
                            Spacer(Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                ResourceSmallCard("First Aid Basics", Icons.Default.MedicalServices, Color.White)
                                Spacer(Modifier.height(12.dp))
                                ResourceSmallCard("24/7 Helpline", Icons.Default.Phone, Color.White)
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
                                Text("No medicines found matching \"$searchQuery\"", color = TextMuted, textAlign = TextAlign.Center)
                            }
                        }
                    } else {
                        items(searchResults) { result ->
                            MedicineSearchResultCard(
                                result = result, 
                                onAddToCart = {
                                    cartViewModel.addToCart(result.item.medicineName)
                                    scope.launch {
                                        snackbarHostState.showSnackbar("${result.item.medicineName} added to cart")
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SearchEmptyState() {
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
            text = "Search for a medicine",
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = TextPrimary
        )
        Text(
            text = "Enter the name of the medicine you need and we'll find the nearest shops for you.",
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
fun MedicineSearchResultCard(result: SearchResult, onAddToCart: () -> Unit) {
    val isOutOfStock = result.item.quantity <= 0
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
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
                        text = if (isOutOfStock) "OUT OF STOCK" else "${result.item.quantity} available",
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
                Text("${result.distanceKm} km away", color = TextMuted, fontSize = 14.sp)
                Spacer(modifier = Modifier.weight(1f))
                
                if (isOutOfStock) {
                    OutlinedButton(
                        onClick = { /* logic for re-stock request */ },
                        modifier = Modifier.height(36.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Teal),
                        border = BorderStroke(1.dp, Teal),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Request Re-stock", fontSize = 11.sp)
                    }
                } else {
                    Button(
                        onClick = onAddToCart,
                        colors = ButtonDefaults.buttonColors(containerColor = Teal),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Icon(Icons.Default.AddShoppingCart, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Add to Cart", fontSize = 12.sp)
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
fun ResourceSmallCard(title: String, icon: ImageVector, color: Color) {
    Card(
        modifier = Modifier.fillMaxWidth().height(74.dp),
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