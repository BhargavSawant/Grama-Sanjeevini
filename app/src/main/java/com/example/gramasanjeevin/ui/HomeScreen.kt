package com.example.gramasanjeevin.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.gramasanjeevin.utils.FirestoreProvider
import com.example.gramasanjeevin.utils.L
import kotlinx.coroutines.delay
import kotlinx.coroutines.tasks.await

private val Teal = Color(0xFF00695C)
private val TealLight = Color(0xFFE0F2F1)
private val EmergencyRed = Color(0xFFC62828)
private val TextPrimary = Color(0xFF1A2B35)
private val TextMuted = Color(0xFF6B7280)
private val PageBg = Color(0xFFF8F9FB)

@Composable
fun HomeScreen(navController: NavController, authViewModel: AuthViewModel) {
    val db = FirestoreProvider.getDb()
    var hubCount by remember { mutableStateOf("...") }
    val context = LocalContext.current
    var showFirstAidDialog by remember { mutableStateOf(false) }
    
    val isEnglish by authViewModel.isEnglish.collectAsState()

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
                text = L.gramaSanjeevini(isEnglish),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Teal
            )
        }

        Column(modifier = Modifier.padding(horizontal = 24.dp)) {
            Text(
                text = L.gramaSanjeevini(isEnglish),
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = L.ruralFinder(isEnglish),
                fontSize = 18.sp,
                color = TextMuted
            )

            Spacer(Modifier.height(24.dp))

            // Part 2: Hero Banner Carousel (Localized)
            HeroCarousel(isEnglish)

            Spacer(Modifier.height(24.dp))

            // Main Action Buttons
            HomeActionCard(
                title = L.searchMedicine(isEnglish),
                description = L.searchMedicineSub(isEnglish),
                icon = Icons.Default.Search,
                containerColor = Teal,
                onClick = { navController.navigate("search") }
            )

            Spacer(Modifier.height(16.dp))

            HomeActionCard(
                title = L.lifeSaving(isEnglish),
                description = L.lifeSavingSub(isEnglish),
                icon = Icons.Default.NotificationsActive,
                containerColor = EmergencyRed,
                onClick = { navController.navigate("emergency") }
            )

            Spacer(Modifier.height(24.dp))

            // Stats Row
            Row(modifier = Modifier.fillMaxWidth()) {
                StatCard(
                    label = L.nearbyHubs(isEnglish),
                    value = hubCount,
                    icon = Icons.Default.LocationOn,
                    modifier = Modifier.weight(1f).clickable {
                        navController.navigate("hubs")
                    },
                    isEnglish = isEnglish
                )
                Spacer(Modifier.width(16.dp))
                StatCard(
                    label = L.alertLevel(isEnglish),
                    value = L.normal(isEnglish),
                    icon = Icons.Default.CheckCircleOutline,
                    modifier = Modifier.weight(1f),
                    isEnglish = isEnglish
                )
            }

            Spacer(Modifier.height(24.dp))

            // Helper Buttons Section
            Text(
                L.emergencySupport(isEnglish),
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = TextPrimary,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 1. 24/7 Helpline (pre-fills 104)
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .height(80.dp)
                        .clickable {
                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:104"))
                            context.startActivity(intent)
                        },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = EmergencyRed)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Default.Call, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.height(4.dp))
                        Text(L.helpline(isEnglish), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }

                // 2. First-Aid Basics (triggers pop-up)
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .height(80.dp)
                        .clickable { showFirstAidDialog = true },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1976D2))
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Default.MedicalServices, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.height(4.dp))
                        Text(L.firstAid(isEnglish), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
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
                            L.needHelp(isEnglish),
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = TextPrimary
                        )
                        Text(
                            L.contactHub(isEnglish),
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

    // First Aid Basics Pop-up
    if (showFirstAidDialog) {
        FirstAidDialog(authViewModel = authViewModel, onDismiss = { showFirstAidDialog = false })
    }
}

@Composable
fun HeroCarousel(isEnglish: Boolean) {
    val pagerState = rememberPagerState(pageCount = { 3 })
    
    // Auto-slide every 3 seconds
    LaunchedEffect(Unit) {
        while(true) {
            delay(3000)
            val nextPage = (pagerState.currentPage + 1) % pagerState.pageCount
            pagerState.animateScrollToPage(nextPage)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .clip(RoundedCornerShape(16.dp))
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            val (title, colors) = when(page) {
                0 -> L.slide1(isEnglish) to listOf(Color(0xFF2E7D32), Color(0xFF1B5E20))
                1 -> L.slide2(isEnglish) to listOf(Color(0xFFC62828), Color(0xFFB71C1C))
                else -> L.slide3(isEnglish) to listOf(Teal, Color(0xFF004D40))
            }
            
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Brush.verticalGradient(colors))
            ) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(24.dp),
                    verticalArrangement = Arrangement.Bottom
                ) {
                    Text(
                        text = title,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        lineHeight = 28.sp
                    )
                }
            }
        }
        
        // Pager Indicators
        Row(
            Modifier
                .height(40.dp)
                .fillMaxWidth()
                .align(Alignment.BottomCenter),
            horizontalArrangement = Arrangement.Center
        ) {
            repeat(pagerState.pageCount) { iteration ->
                val color = if (pagerState.currentPage == iteration) Color.White else Color.White.copy(alpha = 0.4f)
                Box(
                    modifier = Modifier
                        .padding(4.dp)
                        .clip(CircleShape)
                        .background(color)
                        .size(8.dp)
                )
            }
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
fun StatCard(label: String, value: String, icon: ImageVector, modifier: Modifier, isEnglish: Boolean) {
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
