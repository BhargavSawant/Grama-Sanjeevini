package com.example.gramasanjeevin.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.gramasanjeevin.model.Request
import com.example.gramasanjeevin.model.RequestType
import java.text.SimpleDateFormat
import java.util.Locale

private val DarkGreen = Color(0xFF00695C)
private val TextPrimary = Color(0xFF1A2B35)
private val TextMuted = Color(0xFF6B7280)
private val PageBg = Color(0xFFF8F9FB)
private val EmergencyRed = Color(0xFFC62828)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RestockRequestsScreen(
    navController: NavController,
    viewModel: PharmacistViewModel = viewModel()
) {
    val requests by viewModel.requests.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Customer Requests", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = PageBg
    ) { padding ->
        if (requests.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Notifications, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(64.dp))
                    Spacer(Modifier.height(16.dp))
                    Text("No pending requests", color = TextMuted)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(requests) { request ->
                    RequestCard(request) { newStatus ->
                        viewModel.updateRequestStatus(request.requestId, newStatus)
                    }
                }
            }
        }
    }
}

@Composable
fun RequestCard(request: Request, onStatusChange: (String) -> Unit) {
    val dateFormat = remember { SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()) }
    val timeString = dateFormat.format(request.timestamp.toDate())

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = if (request.type == RequestType.RESTOCK) Color(0xFFFFF3E0) else Color(0xFFE8F5E9),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = if (request.type == RequestType.RESTOCK) "RESTOCK REQUEST" else "DELIVERY REQUEST",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (request.type == RequestType.RESTOCK) Color(0xFFE65100) else Color(0xFF2E7D32)
                    )
                }
                Text(text = timeString, fontSize = 12.sp, color = TextMuted)
            }

            Spacer(Modifier.height(12.dp))
            // Task 4: Showing userName instead of userId
            Text(text = "From: ${request.userName}", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 16.sp)
            Spacer(Modifier.height(8.dp))
            
            Text(text = "Items:", fontSize = 12.sp, color = TextMuted, fontWeight = FontWeight.Bold)
            request.items.forEach { item ->
                Text(text = "• $item", fontSize = 14.sp, color = TextPrimary)
            }

            Spacer(Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Status: ${request.status}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = when(request.status) {
                        "PENDING" -> Color(0xFFE65100)
                        "APPROVED" -> Color(0xFF1976D2)
                        "DECLINED" -> EmergencyRed
                        "COMPLETED" -> Color(0xFF2E7D32)
                        else -> TextMuted
                    }
                )

                // Task 3: Approve and Decline options
                if (request.status == "PENDING") {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = { onStatusChange("DECLINED") },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = EmergencyRed),
                            border = androidx.compose.foundation.BorderStroke(1.dp, EmergencyRed),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Decline", fontSize = 12.sp)
                        }

                        Button(
                            onClick = { onStatusChange("APPROVED") },
                            colors = ButtonDefaults.buttonColors(containerColor = DarkGreen),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Approve", fontSize = 12.sp)
                        }
                    }
                } else if (request.status == "APPROVED") {
                    Button(
                        onClick = { onStatusChange("COMPLETED") },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Mark Completed", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}
