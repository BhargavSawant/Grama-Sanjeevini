package com.example.gramasanjeevin.ui

import android.widget.Toast
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.gramasanjeevin.model.InventoryItem
import com.example.gramasanjeevin.utils.L
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
fun PharmacistScreen(
    navController: NavController,
    viewModel: PharmacistViewModel = viewModel(),
    authViewModel: AuthViewModel = viewModel()
) {
    val inventory by viewModel.inventory.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val shopDetails by viewModel.shopDetails.collectAsState()
    val stats by viewModel.stats.collectAsState()
    val isEnglish by authViewModel.isEnglish.collectAsState()

    var showUpdateDialog by remember { mutableStateOf(false) }
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedItem by remember { mutableStateOf<InventoryItem?>(null) }
    var searchQuery by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(L.inventory(isEnglish), fontWeight = FontWeight.Bold) },
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
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp)
        ) {
            Spacer(Modifier.height(16.dp))
            Text(
                text = L.myInventoryHeader(isEnglish, shopDetails?.name ?: ""),
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = L.manageUpdateStock(isEnglish),
                fontSize = 14.sp,
                color = TextMuted,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(Modifier.height(24.dp))

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(L.s(isEnglish, "Search medicines...", "ಔಷಧಗಳನ್ನು ಹುಡುಕಿ...")) },
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
                colors = ButtonDefaults.buttonColors(containerColor = DarkGreen, contentColor = Color.White),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(L.addNew(isEnglish), fontWeight = FontWeight.Bold)
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
                        PharmacistInventoryCard(item, isEnglish) {
                            selectedItem = item
                            showUpdateDialog = true
                        }
                    }

                    item {
                        InventoryHealthCard(stats, isEnglish)
                    }
                }
            }
        }
    }

    if (showUpdateDialog && selectedItem != null) {
        UpdateStockDialog(
            item = selectedItem!!,
            isEnglish = isEnglish,
            onConfirm = { newQty, newExpiry ->
                viewModel.updateMedicine(selectedItem!!.itemId, newQty, newExpiry)
                showUpdateDialog = false
            },
            onDismiss = { showUpdateDialog = false }
        )
    }

    if (showAddDialog) {
        AddMedicineDialog(
            isEnglish = isEnglish,
            onConfirm = { name, qty, isLifeSaving, expiryDate ->
                viewModel.addMedicine(name, qty, isLifeSaving, expiryDate)
                showAddDialog = false
            },
            onDismiss = { showAddDialog = false }
        )
    }
}

@Composable
fun PharmacistInventoryCard(item: InventoryItem, isEnglish: Boolean, onUpdateClick: () -> Unit) {
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
                                L.expiringInDays(isEnglish, daysUntilExpiry),
                                color = Color(0xFFE65100),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    } else if (isCriticallyLow) {
                        Surface(color = Color(0xFFFFEBEE), shape = RoundedCornerShape(8.dp)) {
                            Text(
                                L.criticallyLow(isEnglish),
                                color = EmergencyRed,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    } else if (isOutOfStock) {
                        Surface(color = Color(0xFFFFEBEE), shape = RoundedCornerShape(8.dp)) {
                            Text(
                                L.outOfStockCaps(isEnglish),
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
                        text = if (isOutOfStock) L.s(isEnglish, "UNAVAILABLE", "ಲಭ್ಯವಿಲ್ಲ") else L.s(isEnglish, "IN STOCK", "ದಾಸ್ತಾನು ಇದೆ"),
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
                text = L.expires(isEnglish, dateFormat.format(item.expiryDate.toDate())), 
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
                Text(L.unitsLeft(isEnglish), color = TextMuted, modifier = Modifier.padding(bottom = 6.dp))
            }

            Spacer(Modifier.height(16.dp))
            Button(
                onClick = onUpdateClick,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = DarkGreen, contentColor = Color.White),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(L.updateStock(isEnglish), fontWeight = FontWeight.Bold)
                Spacer(Modifier.width(8.dp))
                Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
fun InventoryHealthCard(stats: PharmacyStats, isEnglish: Boolean) {
    val context = LocalContext.current
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E3A3A))
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text(L.stockHealth(isEnglish), color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("${stats.healthPercentage}%", color = Color.White, fontSize = 40.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(L.s(isEnglish, "Overall", "ಒಟ್ಟಾರೆ"), color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                    Text(L.s(isEnglish, "Stock Status", "ದಾಸ್ತಾನು ಸ್ಥಿತಿ"), color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                }
            }
            Spacer(Modifier.height(12.dp))
            val problemCount = stats.criticalItems + stats.outOfStockItems
            Text(
                if (isEnglish) 
                    "$problemCount items require immediate attention due to low stock or expiry."
                else
                    "$problemCount ವಸ್ತುಗಳಿಗೆ ಕಡಿಮೆ ದಾಸ್ತಾನು ಅಥವಾ ಅವಧಿ ಮುಕ್ತಾಯದ ಕಾರಣ ತಕ್ಷಣದ ಗಮನ ಅಗತ್ಯವಿದೆ.",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 14.sp
            )
            Spacer(Modifier.height(24.dp))
            Button(
                onClick = { 
                    Toast.makeText(context, L.s(isEnglish, "Inventory report generated successfully.", "ದಾಸ್ತಾನು ವರದಿಯನ್ನು ಯಶಸ್ವಿಯಾಗಿ ಸಿದ್ಧಪಡಿಸಲಾಗಿದೆ."), Toast.LENGTH_LONG).show()
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color(0xFF1E3A3A)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(L.s(isEnglish, "Generate Report", "ವರದಿ ಸಿದ್ಧಪಡಿಸಿ"), fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun UpdateStockDialog(item: InventoryItem, isEnglish: Boolean, onConfirm: (Int, Timestamp) -> Unit, onDismiss: () -> Unit) {
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
                    Text(L.s(isEnglish, "Update Stock:\n${item.medicineName}", "ದಾಸ್ತಾನು ನವೀಕರಿಸಿ:\n${item.medicineName}"), fontSize = 24.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = L.close(isEnglish))
                    }
                }
                Text(L.s(isEnglish, "Adjust the current inventory quantity and expiry for this medicine.", "ಈ ಔಷಧಕ್ಕಾಗಿ ಪ್ರಸ್ತುತ ದಾಸ್ತಾನು ಪ್ರಮಾಣ ಮತ್ತು ಅವಧಿ ಮುಕ್ತಾಯವನ್ನು ಹೊಂದಿಸಿ."), color = TextMuted, fontSize = 14.sp)
                
                Spacer(Modifier.height(24.dp))
                Text(L.s(isEnglish, "New Quantity", "ಹೊಸ ಪ್ರಮಾಣ"), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                
                OutlinedTextField(
                    value = quantityInput,
                    onValueChange = { quantityInput = it },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    trailingIcon = { Text(L.s(isEnglish, "Units", "ಘಟಕಗಳು"), color = TextMuted, modifier = Modifier.padding(end = 12.dp)) },
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
                Text(L.s(isEnglish, "Expiry Date (YYYY-MM-DD)", "ಅವಧಿ ಮುಗಿಯುವ ದಿನಾಂಕ (YYYY-MM-DD)"), fontWeight = FontWeight.Bold, fontSize = 14.sp)
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
                        Text(L.cancel(isEnglish))
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
                        colors = ButtonDefaults.buttonColors(containerColor = DarkGreen, contentColor = Color.White),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(L.confirm(isEnglish))
                    }
                }
            }
        }
    }
}

@Composable
fun AddMedicineDialog(isEnglish: Boolean, onConfirm: (String, Int, Boolean, Timestamp) -> Unit, onDismiss: () -> Unit) {
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
                Text(L.addNew(isEnglish), fontSize = 24.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(L.s(isEnglish, "Medicine Name", "ಔಷಧದ ಹೆಸರು")) },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = qty,
                    onValueChange = { qty = it },
                    label = { Text(L.s(isEnglish, "Initial Quantity", "ಆರಂಭಿಕ ಪ್ರಮಾಣ")) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = expiryInput,
                    onValueChange = { expiryInput = it },
                    label = { Text(L.s(isEnglish, "Expiry Date (YYYY-MM-DD)", "ಅವಧಿ ಮುಗಿಯುವ ದಿನಾಂಕ (YYYY-MM-DD)")) },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = isLifeSaving, onCheckedChange = { isLifeSaving = it })
                    Text(L.s(isEnglish, "Life Saving Medicine", "ಜೀವ ಉಳಿಸುವ ಔಷಧ"))
                }
                
                Spacer(Modifier.height(24.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) { Text(L.cancel(isEnglish)) }
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
                        colors = ButtonDefaults.buttonColors(containerColor = DarkGreen, contentColor = Color.White)
                    ) {
                        Text(L.s(isEnglish, "Add", "ಸೇರಿಸಿ"))
                    }
                }
            }
        }
    }
}
