package com.example.gramasanjeevin.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Notifications
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
import com.example.gramasanjeevin.model.Request
import com.example.gramasanjeevin.model.RequestType
import com.example.gramasanjeevin.utils.L
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
    viewModel: PharmacistViewModel = viewModel(),
    authViewModel: AuthViewModel = viewModel()
) {
    val requests by viewModel.requests.collectAsState()
    val isEnglish by authViewModel.isEnglish.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(L.customerRequests(isEnglish), fontWeight = FontWeight.Bold, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = L.back(isEnglish), tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkGreen)
            )
        },
        containerColor = PageBg
    ) { padding ->
        if (requests.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Notifications, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(64.dp))
                    Spacer(Modifier.height(16.dp))
                    Text(L.noPendingRequests(isEnglish), color = TextMuted)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(requests) { request ->
                    RequestCard(
                        request = request,
                        isEnglish = isEnglish,
                        onReview = {
                            navController.navigate("pharmacist_order_review/${request.requestId}")
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun RequestCard(
    request: Request, 
    isEnglish: Boolean,
    onReview: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()) }
    val timeString = try {
        dateFormat.format(request.timestamp.toDate())
    } catch (e: Exception) {
        L.s(isEnglish, "Just now", "ಈಗ ತಾನೇ")
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onReview() },
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
                        text = if (request.type == RequestType.RESTOCK) L.s(isEnglish, "RESTOCK", "ಮರುಪೂರಣ") else L.s(isEnglish, "DELIVERY", "ವಿತರಣೆ"),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (request.type == RequestType.RESTOCK) Color(0xFFE65100) else Color(0xFF2E7D32)
                    )
                }
                Text(text = timeString, fontSize = 12.sp, color = TextMuted)
            }

            Spacer(Modifier.height(12.dp))
            Text(text = "${L.s(isEnglish, "From:", "ಇಂದ:")} ${request.userName}", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 16.sp)
            Spacer(Modifier.height(8.dp))
            
            Text(text = "${L.items(isEnglish)} (${request.items.size}):", fontSize = 12.sp, color = TextMuted, fontWeight = FontWeight.Bold)
            request.items.take(3).forEach { item ->
                Text(text = "• ${item.medicineName}", fontSize = 14.sp, color = TextPrimary)
            }
            if (request.items.size > 3) {
                Text(text = "...${L.s(isEnglish, "and ${request.items.size - 3} more", "ಮತ್ತು ${request.items.size - 3} ಹೆಚ್ಚು")}", fontSize = 12.sp, color = TextMuted)
            }

            Spacer(Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${L.status(isEnglish)}: ${
                        when(request.status) {
                            "PENDING" -> L.s(isEnglish, "PENDING", "ಬಾಕಿ ಇದೆ")
                            "APPROVED" -> L.s(isEnglish, "APPROVED", "ಅನುಮೋದಿಸಲಾಗಿದೆ")
                            "COMPLETED" -> L.s(isEnglish, "COMPLETED", "ಪೂರ್ಣಗೊಂಡಿದೆ")
                            "REJECTED" -> L.s(isEnglish, "REJECTED", "ತಿರಸ್ಕರಿಸಲಾಗಿದೆ")
                            "REVIEWED" -> L.s(isEnglish, "REVIEWED", "ಪರಿಶೀಲಿಸಲಾಗಿದೆ")
                            else -> request.status
                        }
                    }",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = when(request.status) {
                        "PENDING" -> Color(0xFFE65100)
                        "APPROVED", "REVIEWED" -> DarkGreen
                        "COMPLETED" -> Color(0xFF2E7D32)
                        "REJECTED" -> EmergencyRed
                        else -> TextMuted
                    }
                )

                if (request.status == "PENDING") {
                    Button(
                        onClick = onReview,
                        colors = ButtonDefaults.buttonColors(containerColor = DarkGreen, contentColor = Color.White),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(L.s(isEnglish, "Review Order", "ಆರ್ಡರ್ ಪರಿಶೀಲಿಸಿ"), fontSize = 12.sp, color = Color.White)
                    }
                } else {
                    TextButton(onClick = onReview) {
                        Text(L.s(isEnglish, "View Details", "ವಿವರಗಳನ್ನು ನೋಡಿ"), color = DarkGreen, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}
