package com.example.gramasanjeevin.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.gramasanjeevin.utils.FirestoreProvider
import kotlinx.coroutines.tasks.await

private val Teal = Color(0xFF00695C)
private val TealLight = Color(0xFFE0F2F1)
private val EmergencyRed = Color(0xFFC62828)
private val TextPrimary = Color(0xFF1A2B35)
private val TextMuted = Color(0xFF6B7280)
private val PageBg = Color(0xFFF8F9FB)

@Composable
fun HomeScreen(navController: NavController) {
    val db = FirestoreProvider.getDb()
    var hubCount by remember { mutableStateOf("...") }

    LaunchedEffect(Unit) {
        try {
            val snapshot = db.collection("pharmacies").get().await()
            hubCount = snapshot.size().toString()
        } catch (e: Exception) {
            hubCount = "0"
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PageBg)
            .verticalScroll(rememberScrollState())
    ) {
        // Top Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.LocalHospital,
                contentDescription = null,
                tint = Teal,
                modifier = Modifier.size(24.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = "Grama-Sanjeevini",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Teal
            )
        }

        Column(modifier = Modifier.padding(horizontal = 24.dp)) {
            Text(
                text = "Grama-Sanjeevini",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = "Rural Medicine Finder",
                fontSize = 18.sp,
                color = TextMuted
            )

            Spacer(Modifier.height(24.dp))

            // Hero Image Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Box {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Brush.verticalGradient(listOf(Color(0xFF2E7D32), Color(0xFF1B5E20))))
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize().padding(16.dp),
                            verticalArrangement = Arrangement.Bottom
                        ) {
                            Text(
                                "Ensuring health for every village.",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // Main Action Buttons
            HomeActionCard(
                title = "Search Medicine",
                description = "Check availability in local pharmacies",
                icon = Icons.Default.Search,
                containerColor = Teal,
                onClick = { navController.navigate("search") }
            )

            Spacer(Modifier.height(16.dp))

            HomeActionCard(
                title = "Life Saving Drugs",
                description = "Emergency inventory & critical supplies",
                icon = Icons.Default.NotificationsActive,
                containerColor = EmergencyRed,
                onClick = { navController.navigate("emergency") }
            )

            Spacer(Modifier.height(24.dp))

            // Stats Row
            Row(modifier = Modifier.fillMaxWidth()) {
                StatCard(
                    label = "Nearby Hubs",
                    value = hubCount,
                    icon = Icons.Default.LocationOn,
                    modifier = Modifier.weight(1f).clickable {
                        navController.navigate("hubs")
                    }
                )
                Spacer(Modifier.width(16.dp))
                StatCard(
                    label = "Alert Level",
                    value = "Normal",
                    icon = Icons.Default.CheckCircleOutline,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(24.dp))

            // Assistance Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(TealLight),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.HelpOutline, contentDescription = null, tint = Teal)
                    }
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Text(
                            "Need Assistance?",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = TextPrimary
                        )
                        Text(
                            "Call your community healthcare worker directly from the app.",
                            fontSize = 13.sp,
                            color = TextMuted,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
            
            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
fun HomeActionCard(
    title: String,
    description: String,
    icon: ImageVector,
    containerColor: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = Color.White)
            }
            Spacer(Modifier.width(16.dp))
            Column {
                Text(title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text(description, color = Color.White.copy(alpha = 0.8f), fontSize = 13.sp)
            }
        }
    }
}

@Composable
fun StatCard(label: String, value: String, icon: ImageVector, modifier: Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F3F4))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(label, fontSize = 13.sp, color = TextMuted)
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = Teal, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text(value, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            }
        }
    }
}
