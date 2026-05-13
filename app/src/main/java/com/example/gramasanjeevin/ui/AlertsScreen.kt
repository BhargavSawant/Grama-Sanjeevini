package com.example.gramasanjeevin.ui

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PendingActions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gramasanjeevin.model.Request
import com.example.gramasanjeevin.model.RequestType
import com.example.gramasanjeevin.utils.FirestoreProvider
import java.text.SimpleDateFormat
import java.util.Locale

private val Teal = Color(0xFF00695C)
private val TextPrimary = Color(0xFF1A2B35)
private val TextMuted = Color(0xFF6B7280)
private val PageBg = Color(0xFFF8F9FB)
private val EmergencyRed = Color(0xFFC62828)

@Composable
fun AlertsScreen() {
    val db = FirestoreProvider.getDb()
    var requests by remember { mutableStateOf<List<Request>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    // Use a real-time listener to see updates immediately as soon as pharmacist changes them
    DisposableEffect(Unit) {
        Log.d("AlertsScreen", "Listening to requests for user_001 in database 'GramaSanjeevin'")
        val registration = db.collection("requests")
            .whereEqualTo("userId", "user_001")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.e("AlertsScreen", "Listen failed.", e)
                    isLoading = false
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val list = snapshot.toObjects(Request::class.java)
                    // Sorting in-memory by timestamp descending
                    requests = list.sortedByDescending { it.timestamp }
                    Log.d("AlertsScreen", "Fetched ${list.size} requests")
                }
                isLoading = false
            }

        onDispose {
            registration.remove()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PageBg)
    ) {
        Text(
            text = "My Alerts & Status",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
            modifier = Modifier.padding(24.dp)
        )

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Teal)
            }
        } else if (requests.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp)) {
                    Icon(
                        Icons.Default.Notifications,
                        contentDescription = null,
                        tint = Color.LightGray,
                        modifier = Modifier.size(80.dp)
                    )
                    Spacer(Modifier.height(16.dp))
                    Text("No activity yet", fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text(
                        "Your delivery or restock requests will appear here with real-time status updates.",
                        color = TextMuted,
                        textAlign = TextAlign.Center,
                        fontSize = 14.sp
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(requests) { request ->
                    AlertItemCard(request)
                }
                item { Spacer(modifier = Modifier.height(24.dp)) }
            }
        }
    }
}

@Composable
fun AlertItemCard(request: Request) {
    val dateFormat = remember { SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()) }
    val timeString = try {
        dateFormat.format(request.timestamp.toDate())
    } catch (e: Exception) {
        "Just now"
    }

    val statusText = when (request.status) {
        "PENDING" -> "Waiting for approval"
        "APPROVED" -> if (request.type == RequestType.DELIVERY) "Delivery approved" else "Re-stock approved"
        "COMPLETED" -> "Order completed"
        "DECLINED" -> "Request declined"
        else -> request.status
    }

    val statusColor = when (request.status) {
        "PENDING" -> Color(0xFFE65100)
        "APPROVED" -> Color(0xFF1976D2)
        "COMPLETED" -> Color(0xFF2E7D32)
        "DECLINED" -> EmergencyRed
        else -> TextMuted
    }

    val icon = when (request.status) {
        "PENDING" -> Icons.Default.PendingActions
        "APPROVED" -> Icons.Default.Info
        "COMPLETED" -> Icons.Default.CheckCircle
        else -> Icons.Default.Notifications
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(statusColor.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = statusColor, modifier = Modifier.size(24.dp))
            }
            
            Spacer(Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = statusText,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = TextPrimary
                )
                Text(
                    text = "${if(request.type == RequestType.DELIVERY) "Delivery" else "Restock"} for ${request.items.joinToString(", ")}",
                    fontSize = 13.sp,
                    color = TextMuted,
                    maxLines = 1
                )
                Text(
                    text = timeString,
                    fontSize = 11.sp,
                    color = TextMuted.copy(alpha = 0.6f)
                )
            }
        }
    }
}
