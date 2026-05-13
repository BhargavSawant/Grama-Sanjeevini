package com.example.gramasanjeevin.ui

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.gramasanjeevin.model.InventoryItem
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.Date
import java.util.Locale

private val DarkGreen = Color(0xFF00695C)
private val EmergencyRed = Color(0xFFC62828)
private val TextPrimary = Color(0xFF1A2B35)
private val TextMuted = Color(0xFF6B7280)
private val PageBg = Color(0xFFF8F9FB)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PharmacistScreen(viewModel: PharmacistViewModel = viewModel()) {
    val inventory by viewModel.inventory.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val shopDetails by viewModel.shopDetails.collectAsState()
    val stats by viewModel.stats.collectAsState()

    var showUpdateDialog by remember { mutableStateOf(false) }
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedItem by remember { mutableStateOf<InventoryItem?>(null) }
    var searchQuery by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocalHospital, contentDescription = null, tint = DarkGreen)
                        Spacer(Modifier.width(8.dp))
                        Text("Grama-Sanjeevini", fontWeight = FontWeight.Bold, color = DarkGreen)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        floatingActionButton = {
            // Standard FAB without pulsing effect as requested
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = DarkGreen,
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add New")
            }
        },
        containerColor = PageBg
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp)
        ) {
            Spacer(Modifier.height(16.dp))
            Text(
                text = "My Inventory - ${shopDetails?.name ?: "Loading..."}",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = "Manage and update medicine stock levels for rural distribution.",
                fontSize = 14.sp,
                color = TextMuted,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(Modifier.height(24.dp))

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search medicines by name or salt...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextMuted) },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = DarkGreen,
                    unfocusedBorderColor = Color.LightGray,
                    unfocusedContainerColor = Color.White,
                    focusedContainerColor = Color.White
                )
            )

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = { showAddDialog = true },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = DarkGreen),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Add New", fontWeight = FontWeight.Bold)
            }

            Spacer(Modifier.height(24.dp))

            if (isLoading) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = DarkGreen)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    val filteredInventory = inventory.filter { 
                        it.medicineName.contains(searchQuery, ignoreCase = true) 
                    }
                    
                    items(filteredInventory) { item ->
                        PharmacistInventoryCard(item) {
                            selectedItem = item
                            showUpdateDialog = true
                        }
                    }

                    item {
                        InventoryHealthCard(stats)
                    }
                }
            }
        }
    }

    if (showUpdateDialog && selectedItem != null) {
        UpdateStockDialog(
            item = selectedItem!!,
            onConfirm = { newQty, newExpiry ->
                // Fixed: Correctly using updateMedicine to handle both qty and expiry
                viewModel.updateMedicine(selectedItem!!.itemId, newQty, newExpiry)
                showUpdateDialog = false
            },
            onDismiss = { showUpdateDialog = false }
        )
    }

    if (showAddDialog) {
        AddMedicineDialog(
            onConfirm = { name, qty, isLifeSaving, expiryDate ->
                // Fixed: Passing all 4 arguments to addMedicine
                viewModel.addMedicine(name, qty, isLifeSaving, expiryDate)
                showAddDialog = false
            },
            onDismiss = { showAddDialog = false }
        )
    }
}

@Composable
fun PharmacistInventoryCard(item: InventoryItem, onUpdateClick: () -> Unit) {
    val today = Date().toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
    val expiryDate = item.expiryDate.toDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
    val daysUntilExpiry = ChronoUnit.DAYS.between(today, expiryDate)
    
    val isOutOfStock = item.quantity <= 0
    val isCriticallyLow = item.quantity in 1..10
    val isExpiringSoon = daysUntilExpiry in 0..30

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isExpiringSoon) {
                        Surface(color = Color(0xFFFFF3E0), shape = RoundedCornerShape(8.dp)) {
                            Text(
                                "EXPIRING IN $daysUntilExpiry DAYS",
                                color = Color(0xFFE65100),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    } else if (isCriticallyLow) {
                        Surface(color = Color(0xFFFFEBEE), shape = RoundedCornerShape(8.dp)) {
                            Text(
                                "CRITICALLY LOW",
                                color = EmergencyRed,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    } else if (isOutOfStock) {
                        Surface(color = Color(0xFFFFEBEE), shape = RoundedCornerShape(8.dp)) {
                            Text(
                                "OUT OF STOCK",
                                color = EmergencyRed,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
                
                Surface(
                    color = if (isOutOfStock) Color(0xFFF1F1F1) else Color(0xFFE8F5E9),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = if (isOutOfStock) "UNAVAILABLE" else "IN STOCK",
                        color = if (isOutOfStock) TextMuted else Color(0xFF2E7D32),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(Modifier.height(12.dp))
            Text(item.medicineName, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Text("${item.category} / ${item.form}", fontSize = 14.sp, color = TextMuted)
            
            val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            Text(
                text = "Expires: ${dateFormat.format(item.expiryDate.toDate())}", 
                fontSize = 12.sp, 
                color = if (isExpiringSoon) Color(0xFFE65100) else TextMuted,
                fontWeight = if (isExpiringSoon) FontWeight.Bold else FontWeight.Normal
            )

            Spacer(Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = String.format(Locale.getDefault(), "%02d", item.quantity),
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isOutOfStock || isCriticallyLow) EmergencyRed else TextPrimary
                )
                Spacer(Modifier.width(8.dp))
                Text("Units Left", color = TextMuted, modifier = Modifier.padding(bottom = 6.dp))
            }

            Spacer(Modifier.height(16.dp))
            Button(
                onClick = onUpdateClick,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = DarkGreen),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Update Stock", fontWeight = FontWeight.Bold)
                Spacer(Modifier.width(8.dp))
                Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
fun InventoryHealthCard(stats: PharmacyStats) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E3A3A))
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text("Inventory Health", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("${stats.healthPercentage}%", color = Color.White, fontSize = 40.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.width(12.dp))
                Column {
                    Text("Overall", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                    Text("Stock Status", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                }
            }
            Spacer(Modifier.height(12.dp))
            val problemCount = stats.criticalItems + stats.outOfStockItems
            Text(
                "$problemCount items require immediate attention due to low stock or expiry.",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 14.sp
            )
            Spacer(Modifier.height(24.dp))
            Button(
                onClick = { /* Generate Report */ },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color(0xFF1E3A3A)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Generate Report", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun UpdateStockDialog(item: InventoryItem, onConfirm: (Int, Timestamp) -> Unit, onDismiss: () -> Unit) {
    var quantityInput by remember { mutableStateOf(item.quantity.toString()) }
    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }
    var expiryInput by remember { mutableStateOf(dateFormat.format(item.expiryDate.toDate())) }
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Update Stock:\n${item.medicineName}", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }
                Text("Adjust the current inventory quantity and expiry for this medicine.", color = TextMuted, fontSize = 14.sp)
                
                Spacer(Modifier.height(24.dp))
                Text("New Quantity", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                
                OutlinedTextField(
                    value = quantityInput,
                    onValueChange = { quantityInput = it },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    trailingIcon = { Text("Units", color = TextMuted, modifier = Modifier.padding(end = 12.dp)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(10, 50, 100).forEach { amount ->
                        OutlinedButton(
                            onClick = {
                                val current = quantityInput.toIntOrNull() ?: 0
                                quantityInput = (current + amount).toString()
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("+$amount")
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))
                Text("Expiry Date (YYYY-MM-DD)", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                OutlinedTextField(
                    value = expiryInput,
                    onValueChange = { expiryInput = it },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    shape = RoundedCornerShape(12.dp),
                    placeholder = { Text("2026-12-31") }
                )

                Spacer(Modifier.height(32.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = { 
                            val qty = quantityInput.toIntOrNull() ?: 0
                            val date = try {
                                Timestamp(dateFormat.parse(expiryInput)!!)
                            } catch (e: Exception) {
                                item.expiryDate
                            }
                            onConfirm(qty, date) 
                        },
                        modifier = Modifier.weight(1f).height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = DarkGreen),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Confirm")
                    }
                }
            }
        }
    }
}

@Composable
fun AddMedicineDialog(onConfirm: (String, Int, Boolean, Timestamp) -> Unit, onDismiss: () -> Unit) {
    var name by remember { mutableStateOf("") }
    var qty by remember { mutableStateOf("") }
    var isLifeSaving by remember { mutableStateOf(false) }
    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }
    var expiryInput by remember { mutableStateOf("2026-12-31") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text("Add New Medicine", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Medicine Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = qty,
                    onValueChange = { qty = it },
                    label = { Text("Initial Quantity") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = expiryInput,
                    onValueChange = { expiryInput = it },
                    label = { Text("Expiry Date (YYYY-MM-DD)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = isLifeSaving, onCheckedChange = { isLifeSaving = it })
                    Text("Life Saving Medicine")
                }
                
                Spacer(Modifier.height(24.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) { Text("Cancel") }
                    Button(
                        onClick = { 
                            val date = try {
                                Timestamp(dateFormat.parse(expiryInput)!!)
                            } catch (e: Exception) {
                                Timestamp.now()
                            }
                            onConfirm(name, qty.toIntOrNull() ?: 0, isLifeSaving, date) 
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = DarkGreen)
                    ) {
                        Text("Add")
                    }
                }
            }
        }
    }
}
