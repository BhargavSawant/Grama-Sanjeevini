package com.example.gramasanjeevin.ui

import android.content.Intent
import android.net.Uri
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.gramasanjeevin.model.SearchResult
import com.example.gramasanjeevin.utils.L

private val EmergencyRed = Color(0xFFC62828)
private val DarkGreen = Color(0xFF00695C)
private val LightGreenBg = Color(0xFFE8F5E9)
private val TextPrimary = Color(0xFF1A2B35)
private val TextMuted = Color(0xFF6B7280)

@Composable
fun EmergencyScreen(
    viewModel: EmergencyViewModel = viewModel(),
    authViewModel: AuthViewModel = viewModel()
) {
    val emergencyList by viewModel.emergencyList.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val requestStatus by viewModel.requestStatus.collectAsState()
    val isEnglish by authViewModel.isEnglish.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var showFirstAidDialog by remember { mutableStateOf(false) }
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
            Text(L.gramaSanjeevini(isEnglish), fontWeight = FontWeight.Bold, color = DarkGreen, fontSize = 20.sp)
        }

        Column(modifier = Modifier.padding(horizontal = 24.dp)) {
            Text(
                text = L.lifeSavingMedicines(isEnglish),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = EmergencyRed
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = L.emergencySub(isEnglish),
                fontSize = 14.sp,
                color = TextMuted,
                lineHeight = 20.sp
            )
            Spacer(modifier = Modifier.height(20.dp))

            // Emergency Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = {
                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:104"))
                        context.startActivity(intent)
                    },
                    modifier = Modifier.weight(1f).height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = EmergencyRed, contentColor = Color.White),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp)
                ) {
                    Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(L.helpline(isEnglish), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }

                Button(
                    onClick = { showFirstAidDialog = true },
                    modifier = Modifier.weight(1f).height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2), contentColor = Color.White),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp)
                ) {
                    Icon(Icons.Default.Info, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(L.firstAidTips(isEnglish), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(L.searchCritical(isEnglish), fontSize = 14.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextMuted) },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black,
                    focusedBorderColor = Color.LightGray,
                    unfocusedBorderColor = Color.LightGray,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                ),
                textStyle = TextStyle(color = Color.Black),
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
                    EmergencyMedicineCard(result, isEnglish) {
                        viewModel.sendRestockRequest(result.pharmacy.shopId, result.item.medicineName)
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
fun EmergencyMedicineCard(result: SearchResult, isEnglish: Boolean, onRestockClick: () -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val isOutOfStock = result.item.quantity <= 0

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(modifier = Modifier.height(IntrinsicSize.Min)) {
            Box(
                modifier = Modifier
                    .width(6.dp)
                    .fillMaxHeight()
                    .background(EmergencyRed)
            )

            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ZoomableMedicineImage(imageResName = result.item.imageResName)
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = result.item.medicineName,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = result.pharmacy.name,
                            fontSize = 14.sp,
                            color = TextMuted
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
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
                                text = L.distanceAway(isEnglish, result.distanceKm),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isOutOfStock) TextMuted else EmergencyRed
                            )
                        }
                    }
                    
                    if (!isOutOfStock) {
                        Surface(
                            color = LightGreenBg,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = L.qtyLabel(isEnglish, result.item.quantity),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2E7D32),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                if (isOutOfStock) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = onRestockClick,
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = EmergencyRed, contentColor = Color.White),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(L.requestRestock(isEnglish), fontWeight = FontWeight.Bold)
                    }
                } else {
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
                        colors = ButtonDefaults.buttonColors(containerColor = DarkGreen, contentColor = Color.White),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Directions, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(L.getDirections(isEnglish), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
