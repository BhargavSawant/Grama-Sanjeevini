package com.example.gramasanjeevin.ui

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PendingActions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.gramasanjeevin.model.Request
import com.example.gramasanjeevin.model.RequestType
import com.example.gramasanjeevin.utils.FirestoreProvider
import com.example.gramasanjeevin.utils.L
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Locale

private val Teal = Color(0xFF00695C)
private val TextPrimary = Color(0xFF1A2B35)
private val TextMuted = Color(0xFF6B7280)
private val PageBg = Color(0xFFF8F9FB)
private val EmergencyRed = Color(0xFFC62828)

@Composable
fun AlertsScreen(authViewModel: AuthViewModel = viewModel()) {
    val db = FirestoreProvider.getDb()
    val auth = Firebase.auth
    var requests by remember { mutableStateOf<List<Request>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val isEnglish by authViewModel.isEnglish.collectAsState()

    val currentUserId = auth.currentUser?.uid ?: "user_001" 

    DisposableEffect(currentUserId) {
        val registration = db.collection("requests")
            .whereEqualTo("userId", currentUserId)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    isLoading = false
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val list = snapshot.toObjects(Request::class.java)
                    requests = list.sortedByDescending { it.timestamp }
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
            text = L.s(isEnglish, "My Alerts & Status", "ನನ್ನ ಎಚ್ಚರಿಕೆಗಳು ಮತ್ತು ಸ್ಥಿತಿ"),
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
                    Text(L.s(isEnglish, "No activity yet", "ಇನ್ನೂ ಯಾವುದೇ ಚಟುವಟಿಕೆ ಇಲ್ಲ"), fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text(
                        L.s(isEnglish, "Your delivery or restock requests will appear here with real-time status updates.", "ನಿಮ್ಮ ವಿತರಣೆ ಅಥವಾ ಮರುಪೂರಣ ವಿನಂತಿಗಳು ಇಲ್ಲಿ ನೈಜ-ಸಮಯದ ಸ್ಥಿತಿ ಅಪ್‌ಡೇಟ್‌ಗಳೊಂದಿಗೆ ಗೋಚರಿಸುತ್ತವೆ."),
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
                    AlertItemCard(
                        request = request,
                        isEnglish = isEnglish,
                        onWithdraw = {
                            scope.launch {
                                try {
                                    db.collection("requests").document(request.requestId).delete().await()
                                    Toast.makeText(context, L.s(isEnglish, "Request withdrawn successfully", "ವಿನಂತಿಯನ್ನು ಯಶಸ್ವಿಯಾಗಿ ಹಿಂಪಡೆಯಲಾಗಿದೆ"), Toast.LENGTH_SHORT).show()
                                } catch (e: Exception) {
                                    Toast.makeText(context, "${L.s(isEnglish, "Failed to withdraw", "ಹಿಂಪಡೆಯಲು ವಿಫಲವಾಗಿದೆ")}: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    )
                }
                item { Spacer(modifier = Modifier.height(24.dp)) }
            }
        }
    }
}

@Composable
fun AlertItemCard(request: Request, isEnglish: Boolean, onWithdraw: () -> Unit) {
    val dateFormat = remember { SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()) }
    val timeString = try {
        dateFormat.format(request.timestamp.toDate())
    } catch (e: Exception) {
        L.s(isEnglish, "Just now", "ಈಗ ತಾನೇ")
    }

    val statusText = when (request.status) {
        "PENDING" -> L.s(isEnglish, "Waiting for approval", "ಅನುಮೋದನೆಗಾಗಿ ಕಾಯಲಾಗುತ್ತಿದೆ")
        "APPROVED" -> if (request.type == RequestType.DELIVERY) L.s(isEnglish, "Delivery approved", "ವಿತರಣೆ ಅನುಮೋದಿಸಲಾಗಿದೆ") else L.s(isEnglish, "Re-stock approved", "ಮರುಪೂರಣ ಅನುಮೋದಿಸಲಾಗಿದೆ")
        "COMPLETED" -> L.s(isEnglish, "Order completed", "ಆರ್ಡರ್ ಪೂರ್ಣಗೊಂಡಿದೆ")
        "DECLINED", "REJECTED" -> L.s(isEnglish, "Request declined", "ವಿನಂತಿಯನ್ನು ತಿರಸ್ಕರಿಸಲಾಗಿದೆ")
        "REVIEWED" -> L.s(isEnglish, "Review in progress", "ಪರಿಶೀಲನೆ ನಡೆಯುತ್ತಿದೆ")
        else -> request.status
    }

    val statusColor = when (request.status) {
        "PENDING" -> Color(0xFFE65100)
        "APPROVED", "COMPLETED" -> Color(0xFF2E7D32)
        "DECLINED", "REJECTED" -> EmergencyRed
        "REVIEWED" -> Color(0xFF1976D2)
        else -> TextMuted
    }

    val icon = when (request.status) {
        "PENDING" -> Icons.Default.PendingActions
        "APPROVED", "COMPLETED" -> Icons.Default.CheckCircle
        else -> Icons.Default.Notifications
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
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
                        text = "${if(request.type == RequestType.DELIVERY) L.s(isEnglish, "Delivery", "ವಿತರಣೆ") else L.s(isEnglish, "Restock", "ಮರುಪೂರಣ")} for ${request.items.joinToString(", ") { it.medicineName }}",
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

            if (request.status == "PENDING") {
                Spacer(Modifier.height(12.dp))
                HorizontalDivider(color = Color(0xFFF1F1F1))
                Spacer(Modifier.height(8.dp))
                TextButton(
                    onClick = onWithdraw,
                    modifier = Modifier.align(Alignment.End),
                    colors = ButtonDefaults.textButtonColors(contentColor = EmergencyRed)
                ) {
                    Icon(Icons.Default.DeleteForever, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(L.s(isEnglish, "Withdraw Request", "ವಿನಂತಿ ಹಿಂಪಡೆಯಿರಿ"), fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
