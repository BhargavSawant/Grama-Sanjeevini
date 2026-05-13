package com.example.gramasanjeevin.ui

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.NearMe
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
import com.example.gramasanjeevin.model.SearchResult

private val EmergencyRed = Color(0xFFC62828)
private val DarkGreen = Color(0xFF00695C)
private val LightGreenBg = Color(0xFFE8F5E9)
private val TextPrimary = Color(0xFF1A2B35)
private val TextMuted = Color(0xFF6B7280)

@Composable
fun EmergencyScreen(viewModel: EmergencyViewModel = viewModel()) {
    val emergencyList by viewModel.emergencyList.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val requestStatus by viewModel.requestStatus.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    val context = LocalContext.current

    LaunchedEffect(requestStatus) {
        requestStatus?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearStatus()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FB))
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.LocalHospital, contentDescription = null, tint = DarkGreen, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Grama-Sanjeevini", fontWeight = FontWeight.Bold, color = DarkGreen, fontSize = 20.sp)
        }

        Column(modifier = Modifier.padding(horizontal = 24.dp)) {
            Text(
                text = "Life Saving Medicines",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = EmergencyRed
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Urgent medical supplies available in your immediate vicinity. Every second counts.",
                fontSize = 14.sp,
                color = TextMuted,
                lineHeight = 20.sp
            )
            Spacer(modifier = Modifier.height(20.dp))

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search critical medicine...", fontSize = 14.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextMuted) },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.LightGray,
                    unfocusedBorderColor = Color.LightGray,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                ),
                singleLine = true
            )
        }

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = EmergencyRed)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(emergencyList.filter { it.item.medicineName.contains(searchQuery, ignoreCase = true) }) { result ->
                    EmergencyMedicineCard(result, onRestockClick = {
                        viewModel.sendRestockRequest(result.pharmacy.shopId, result.item.medicineName)
                    })
                }
            }
        }
    }
}

@Composable
fun EmergencyMedicineCard(result: SearchResult, onRestockClick: () -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val isOutOfStock = result.item.quantity <= 0

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(modifier = Modifier.height(IntrinsicSize.Min)) {
            // Left border accent
            Box(
                modifier = Modifier
                    .width(6.dp)
                    .fillMaxHeight()
                    .background(EmergencyRed)
            )

            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = result.item.medicineName,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = result.pharmacy.name,
                            fontSize = 15.sp,
                            color = TextMuted
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Surface(
                            color = if (isOutOfStock) Color(0xFFF1F1F1) else Color(0xFFFFEBEE),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Outlined.NearMe,
                                    contentDescription = null,
                                    tint = if (isOutOfStock) TextMuted else EmergencyRed,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Distance: ${result.distanceKm}km",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isOutOfStock) TextMuted else EmergencyRed
                                )
                            }
                        }
                        
                        if (!isOutOfStock) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Surface(
                                color = LightGreenBg,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "Quantity: ${result.item.quantity}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF2E7D32),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }

                if (isOutOfStock) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "OUT OF STOCK",
                        color = EmergencyRed,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.End
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = onRestockClick,
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = EmergencyRed,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Request Restock", fontWeight = FontWeight.Bold)
                    }
                } else {
                    Spacer(modifier = Modifier.height(16.dp))
                    // Map snippet placeholder
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF1E3A3A)) // Dark greenish from image
                    ) {
                         // Mocking the map line
                         Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
                             Icon(Icons.Default.Route, contentDescription = null, tint = Color(0xFF009688), modifier = Modifier.size(60.dp))
                         }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            com.example.gramasanjeevin.utils.LocationUtils.launchGoogleMaps(
                                context,
                                result.pharmacy.latitude,
                                result.pharmacy.longitude,
                                result.pharmacy.name
                            )
                        },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = DarkGreen),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Directions, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Get Directions", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
