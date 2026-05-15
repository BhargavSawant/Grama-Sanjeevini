package com.example.gramasanjeevin.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.gramasanjeevin.model.OrderItem
import com.example.gramasanjeevin.utils.L

private val Teal = Color(0xFF00695C)
private val TextPrimary = Color(0xFF1A2B35)
private val TextMuted = Color(0xFF6B7280)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PharmacistOrderReviewScreen(
    orderId: String,
    navController: NavController,
    viewModel: PharmacistViewModel = viewModel(),
    authViewModel: AuthViewModel = viewModel()
) {
    val currentOrder by viewModel.currentOrder.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isEnglish by authViewModel.isEnglish.collectAsState()

    var showRejectionDialog by remember { mutableStateOf(false) }
    var selectedItemForRejection by remember { mutableStateOf<OrderItem?>(null) }
    var rejectionReason by remember { mutableStateOf("") }

    LaunchedEffect(orderId) {
        viewModel.fetchOrderDetails(orderId)
    }

    // Force Light Theme for this screen
    MaterialTheme(
        colorScheme = lightColorScheme(
            surface = Color.White,
            background = Color.White,
            onSurface = TextPrimary,
            onBackground = TextPrimary
        )
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(L.s(isEnglish, "Review Order", "ಆರ್ಡರ್ ಪರಿಶೀಲಿಸಿ"), fontWeight = FontWeight.Bold, color = Color.White) },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = L.back(isEnglish), tint = Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Teal)
                )
            },
            containerColor = Color.White
        ) { padding ->
            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize().background(Color.White), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Teal)
                }
            } else if (currentOrder == null) {
                Box(modifier = Modifier.fillMaxSize().background(Color.White), contentAlignment = Alignment.Center) {
                    Text(L.s(isEnglish, "Order not found", "ಆರ್ಡರ್ ಕಂಡುಬಂದಿಲ್ಲ"), color = TextPrimary)
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.White)
                        .padding(padding)
                ) {
                    // 1. The Image Viewer (Prescription)
                    if (!currentOrder?.prescriptionUrl.isNullOrBlank()) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(250.dp)
                                .padding(16.dp),
                            shape = RoundedCornerShape(12.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            AsyncImage(
                                model = currentOrder?.prescriptionUrl,
                                contentDescription = "Prescription Image",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Fit
                            )
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp)
                                .padding(16.dp)
                                .background(Color.LightGray.copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(L.s(isEnglish, "No prescription uploaded", "ಯಾವುದೇ ಪ್ರಿಸ್ಕ್ರಿಪ್ಷನ್ ಅಪ್‌ಲೋಡ್ ಮಾಡಲಾಗಿಲ್ಲ"), color = TextMuted)
                        }
                    }

                    Text(
                        text = L.s(isEnglish, "Items in Order", "ಆರ್ಡರ್‌ನಲ್ಲಿರುವ ವಸ್ತುಗಳು"),
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        color = Teal
                    )

                    // 2. The Item List
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(currentOrder?.items ?: emptyList()) { item ->
                            OrderItemReviewCard(
                                item = item,
                                isEnglish = isEnglish,
                                onApprove = {
                                    viewModel.updateOrderItemStatus(orderId, item.medicineName, "Approved")
                                },
                                onReject = {
                                    selectedItemForRejection = item
                                    rejectionReason = ""
                                    showRejectionDialog = true
                                }
                            )
                        }

                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = {
                                    viewModel.completeOrderReview(orderId) {
                                        navController.popBackStack()
                                    }
                                },
                                modifier = Modifier.fillMaxWidth().height(56.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Teal, contentColor = Color.White),
                                shape = RoundedCornerShape(12.dp),
                                enabled = currentOrder?.items?.all { it.status != "Pending" } == true
                            ) {
                                Text(L.s(isEnglish, "Complete Review", "ಪರಿಶೀಲನೆ ಪೂರ್ಣಗೊಳಿಸಿ"), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            }
                        }
                    }
                }
            }
        }

        // 3. Rejection Flow (AlertDialog)
        if (showRejectionDialog && selectedItemForRejection != null) {
            AlertDialog(
                onDismissRequest = { showRejectionDialog = false },
                title = { Text(L.s(isEnglish, "Reject Item", "ವಸ್ತುವನ್ನು ತಿರಸ್ಕರಿಸಿ"), color = Teal, fontWeight = FontWeight.Bold) },
                text = {
                    Column {
                        Text(L.s(isEnglish, "Provide a reason for rejecting ${selectedItemForRejection?.medicineName}:", "${selectedItemForRejection?.medicineName} ಅನ್ನು ತಿರಸ್ಕರಿಸಲು ಕಾರಣ ನೀಡಿ:"), color = TextPrimary)
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(
                            value = rejectionReason,
                            onValueChange = { rejectionReason = it },
                            placeholder = { Text(L.s(isEnglish, "e.g., Out of stock, Not clearly legible", "ಉದಾಹರಣೆಗೆ: ದಾಸ್ತಾನು ಇಲ್ಲ, ಸ್ಪಷ್ಟವಾಗಿಲ್ಲ")) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            )
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.updateOrderItemStatus(
                                orderId,
                                selectedItemForRejection!!.medicineName,
                                "Rejected",
                                rejectionReason
                            )
                            showRejectionDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red, contentColor = Color.White),
                        enabled = rejectionReason.isNotBlank()
                    ) {
                        Text(L.confirm(isEnglish))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showRejectionDialog = false }) {
                        Text(L.cancel(isEnglish), color = Teal)
                    }
                },
                containerColor = Color.White
            )
        }
    }
}

@Composable
fun OrderItemReviewCard(
    item: OrderItem,
    isEnglish: Boolean,
    onApprove: () -> Unit,
    onReject: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = when (item.status) {
                "Approved" -> Color(0xFFE8F5E9)
                "Rejected" -> Color(0xFFFFEBEE)
                else -> Color.White
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ZoomableMedicineImage(imageResName = item.imageResName)
                
                Spacer(Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = item.medicineName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = TextPrimary
                        )
                        // AI Badge
                        if (item.isAutoFilled) {
                            Spacer(Modifier.width(8.dp))
                            Surface(
                                color = Teal.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                ) {
                                    Icon(
                                        Icons.Default.AutoAwesome,
                                        contentDescription = null,
                                        tint = Teal,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(Modifier.width(4.dp))
                                    Text(
                                        if (isEnglish) "AI Auto-filled" else "AI ಸ್ವಯಂ-ಭರ್ತಿ",
                                        fontSize = 10.sp,
                                        color = Teal,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                    Text(L.qtyLabel(isEnglish, item.quantity), color = TextMuted)
                    if (item.status != "Pending") {
                        Text(
                            "${L.status(isEnglish)}: ${if (item.status == "Approved") L.s(isEnglish, "Approved", "ಅನುಮೋದಿಸಲಾಗಿದೆ") else L.s(isEnglish, "Rejected", "ತಿರಸ್ಕರಿಸಲಾಗಿದೆ")}",
                            color = if (item.status == "Approved") Color(0xFF2E7D32) else Color.Red,
                            fontWeight = FontWeight.Medium,
                            fontSize = 12.sp
                        )
                        if (item.rejectionReason != null) {
                            Text("${L.s(isEnglish, "Reason", "ಕಾರಣ")}: ${item.rejectionReason}", color = Color.Red, fontSize = 12.sp)
                        }
                    }
                }

                if (item.status == "Pending") {
                    Row {
                        IconButton(
                            onClick = onApprove,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFE8F5E9))
                        ) {
                            Icon(Icons.Default.Check, contentDescription = L.approve(isEnglish), tint = Color(0xFF2E7D32))
                        }
                        Spacer(Modifier.width(8.dp))
                        IconButton(
                            onClick = onReject,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFFFEBEE))
                        ) {
                            Icon(Icons.Default.Close, contentDescription = L.decline(isEnglish), tint = Color(0.8f, 0f, 0f))
                        }
                    }
                }
            }
        }
    }
}
