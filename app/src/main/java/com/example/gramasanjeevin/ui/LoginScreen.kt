package com.example.gramasanjeevin.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview

// ── Shared colour tokens ──────────────────────────────────────────────────────
private val PowderBlue   = Color(0xFFB0E0E6)
private val Teal         = Color(0xFF4A9DAB)
private val TealDark     = Color(0xFF2E7D8A)
private val BlueSurface  = Color(0xFFF0F9FA)
private val GreenSurface = Color(0xFFF2F7F2)
private val TextPrimary  = Color(0xFF1A2B35)
private val TextMuted    = Color(0xFF6B7280)

/**
 * Enhanced Entry screen — role selector first, pharmacist auth on demand.
 * Features a modern, clean UI with soft gradients, subtle animations,
 * and support for both English and Kannada.
 */
@Composable
fun LoginScreen(
    onVillagerSelected: () -> Unit,
    onPharmacistLoginSuccess: () -> Unit
) {
    var showPharmacistAuth by remember { mutableStateOf(false) }
    var isEnglish by remember { mutableStateOf(true) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // Background Decorative Elements
        BackgroundDecorations()

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Utilities Bar
            TopUtilityBar(
                isEnglish = isEnglish,
                onLanguageToggle = { isEnglish = !isEnglish }
            )

            // ── Hero header ─────────────────────────────────────────────────
            HeroHeader(isEnglish = isEnglish)

            // ── Animated content: role picker ↔ pharmacist auth ─────────────
            AnimatedContent(
                targetState = showPharmacistAuth,
                transitionSpec = {
                    fadeIn(animationSpec = tween(400)) + slideInHorizontally { if (targetState) it else -it } togetherWith
                    fadeOut(animationSpec = tween(400)) + slideOutHorizontally { if (targetState) -it else it }
                },
                label = "login_step"
            ) { isPharmacistAuth ->
                if (!isPharmacistAuth) {
                    RolePickerStep(
                        isEnglish = isEnglish,
                        onVillagerSelected = onVillagerSelected,
                        onPharmacistSelected = { showPharmacistAuth = true }
                    )
                } else {
                    PharmacistAuthStep(
                        isEnglish = isEnglish,
                        onBack = { showPharmacistAuth = false },
                        onLoginSuccess = onPharmacistLoginSuccess
                    )
                }
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Footer with Links
            FooterSection(isEnglish = isEnglish)
        }
    }
}

@Composable
private fun TopUtilityBar(
    isEnglish: Boolean,
    onLanguageToggle: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp, start = 16.dp, end = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Help Button
        TextButton(
            onClick = { /* Help functionality */ },
            colors = ButtonDefaults.textButtonColors(contentColor = Teal)
        ) {
            Icon(Icons.AutoMirrored.Filled.HelpOutline, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(6.dp))
            Text(if (isEnglish) "Help" else "ಸಹಾಯ", fontSize = 14.sp, fontWeight = FontWeight.Medium)
        }

        // Language Switcher
        Surface(
            onClick = onLanguageToggle,
            shape = RoundedCornerShape(20.dp),
            color = PowderBlue.copy(alpha = 0.2f),
            modifier = Modifier.height(36.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isEnglish) "ಕನ್ನಡ" else "English",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = TealDark
                )
            }
        }
    }
}

@Composable
private fun HeroHeader(isEnglish: Boolean) {
    val infiniteTransition = rememberInfiniteTransition(label = "hero_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    val floatingOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -8f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "float"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(240.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // App icon bubble with soft shadow and animation
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .graphicsLayer {
                        scaleX = pulseScale
                        scaleY = pulseScale
                        translationY = floatingOffset
                    }
                    .shadow(12.dp, CircleShape, spotColor = Teal.copy(alpha = 0.3f))
                    .clip(CircleShape)
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.LocalHospital,
                    contentDescription = null,
                    tint = Teal,
                    modifier = Modifier.size(48.dp)
                )
            }

            Spacer(Modifier.height(20.dp))

            Text(
                text = "Grama-Sanjeevini",
                fontSize = 30.sp,
                fontWeight = FontWeight.ExtraBold,
                color = TextPrimary,
                letterSpacing = (-0.5).sp
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = if (isEnglish) "Connecting Rural Healthcare" else "ಗ್ರಾಮೀಣ ಆರೋಗ್ಯ ಸಂಪರ್ಕ",
                fontSize = 14.sp,
                color = TextMuted,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun BackgroundDecorations() {
    Canvas(modifier = Modifier.fillMaxSize().alpha(0.4f)) {
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(PowderBlue.copy(alpha = 0.3f), Color.Transparent),
                center = Offset(size.width * 0.9f, size.height * 0.05f),
                radius = size.width * 0.7f
            ),
            center = Offset(size.width * 0.9f, size.height * 0.05f),
            radius = size.width * 0.7f
        )
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Teal.copy(alpha = 0.08f), Color.Transparent),
                center = Offset(size.width * 0.1f, size.height * 0.45f),
                radius = size.width * 0.6f
            ),
            center = Offset(size.width * 0.1f, size.height * 0.45f),
            radius = size.width * 0.6f
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Step 1 — Role picker
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun RolePickerStep(
    isEnglish: Boolean,
    onVillagerSelected: () -> Unit,
    onPharmacistSelected: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = if (isEnglish) "Continue as..." else "ಯಾರಾಗಿ ಮುಂದುವರಿಯುತ್ತೀರಿ?",
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextPrimary
        )
        Spacer(Modifier.height(28.dp))

        // Villager card
        RoleCard(
            icon            = Icons.Filled.PersonSearch,
            title           = if (isEnglish) "Villager" else "ಗ್ರಾಮಸ್ಥರು",
            description     = if (isEnglish) "Search for life-saving medicines" else "ಜೀವ ಉಳಿಸುವ ಔಷಧಿಗಳಿಗಾಗಿ ಹುಡುಕಿ",
            surfaceColor    = BlueSurface,
            iconTint        = Teal,
            iconBackground  = PowderBlue.copy(alpha = 0.5f),
            onClick         = onVillagerSelected
        )

        Spacer(Modifier.height(16.dp))

        // Pharmacist card
        RoleCard(
            icon            = Icons.Filled.Storefront,
            title           = if (isEnglish) "Pharmacist" else "ಔಷಧಿಕಾರರು",
            description     = if (isEnglish) "Manage and update medicine stock" else "ಔಷಧಿ ದಾಸ್ತಾನು ನಿರ್ವಹಿಸಿ",
            surfaceColor    = GreenSurface,
            iconTint        = Color(0xFF388E3C),
            iconBackground  = Color(0xFFE8F5E9),
            onClick         = onPharmacistSelected
        )

        Spacer(Modifier.height(40.dp))

        Surface(
            color = Color(0xFFF8F9FA),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = if (isEnglish) "Quick access for citizens" else "ನಾಗರಿಕರಿಗೆ ತ್ವರಿತ ಪ್ರವೇಶ",
                fontSize = 12.sp,
                color = TextMuted,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
            )
        }
    }
}

@Composable
private fun RoleCard(
    icon: ImageVector,
    title: String,
    description: String,
    surfaceColor: Color,
    iconTint: Color,
    iconBackground: Color,
    onClick: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "scale"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(104.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .shadow(if (isPressed) 2.dp else 6.dp, RoundedCornerShape(24.dp), ambientColor = Color.LightGray),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = surfaceColor),
        onClick = {
            isPressed = true
            onClick()
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(iconBackground),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(30.dp))
            }

            Spacer(Modifier.width(20.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontSize = 19.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                Spacer(Modifier.height(4.dp))
                Text(description, fontSize = 13.sp, color = TextMuted, lineHeight = 18.sp)
            }

            Icon(
                Icons.AutoMirrored.Filled.ArrowForwardIos,
                contentDescription = null,
                tint = TextMuted.copy(alpha = 0.4f),
                modifier = Modifier.size(14.dp)
            )
        }
    }
    
    LaunchedEffect(isPressed) {
        if (isPressed) {
            kotlinx.coroutines.delay(150)
            isPressed = false
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Step 2 — Pharmacist authentication
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun PharmacistAuthStep(
    isEnglish: Boolean,
    onBack: () -> Unit,
    onLoginSuccess: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.Start
    ) {
        // Back Button
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .size(44.dp)
                .background(Color(0xFFF5F5F5), CircleShape)
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary, modifier = Modifier.size(20.dp))
        }

        Spacer(Modifier.height(24.dp))

        Text(
            text = if (isEnglish) "Pharmacist Login" else "ಔಷಧಿಕಾರರ ಲಾಗಿನ್",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        Text(
            text = if (isEnglish) "Secure access for shop owners" else "ಅಂಗಡಿ ಮಾಲೀಕರಿಗೆ ಸುರಕ್ಷಿತ ಪ್ರವೇಶ",
            fontSize = 14.sp,
            color = TextMuted
        )

        Spacer(Modifier.height(36.dp))

        // Google button
        OutlinedButton(
            onClick = { onLoginSuccess() },
            modifier = Modifier
                .fillMaxWidth()
                .height(58.dp),
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFDADCE0)),
            colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White)
        ) {
            Icon(
                imageVector = Icons.Filled.AccountCircle,
                contentDescription = null,
                tint = Color(0xFF4285F4),
                modifier = Modifier.size(24.dp)
            )
            Spacer(Modifier.width(12.dp))
            Text(
                text = if (isEnglish) "Sign in with Google" else "Google ಮೂಲಕ ಸೈನ್ ಇನ್ ಮಾಡಿ",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF3C4043)
            )
        }

        Spacer(Modifier.height(16.dp))

        // Phone button
        Button(
            onClick = { /* Implement Phone Auth */ },
            modifier = Modifier
                .fillMaxWidth()
                .height(58.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = TealDark)
        ) {
            Icon(Icons.Filled.Phone, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(12.dp))
            Text(
                text = if (isEnglish) "Continue with Phone" else "ಫೋನ್ ಮೂಲಕ ಮುಂದುವರಿಸಿ",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(Modifier.height(32.dp))

        // Demo Mode Info Box
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(Teal.copy(alpha = 0.08f))
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.Top) {
                Icon(
                    Icons.Filled.TipsAndUpdates,
                    contentDescription = null,
                    tint = TealDark,
                    modifier = Modifier.size(20.dp).padding(top = 2.dp)
                )
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        text = if (isEnglish) "Demo Environment" else "ಡೆಮೊ ಆವೃತ್ತಿ",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TealDark
                    )
                    Text(
                        text = if (isEnglish) 
                            "Real auth is disabled. Tap Google to enter the pharmacist dashboard." 
                        else 
                            "ನೈಜ ದೃಢೀಕರಣವನ್ನು ನಿಷ್ಕ್ರಿಯಗೊಳಿಸಲಾಗಿದೆ. ಡ್ಯಾಶ್‌ಬೋರ್ಡ್ ಪ್ರವೇಶಿಸಲು Google ಟ್ಯಾಪ್ ಮಾಡಿ.",
                        fontSize = 12.sp,
                        color = TealDark.copy(alpha = 0.8f),
                        lineHeight = 18.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun FooterSection(isEnglish: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (isEnglish) "Privacy Policy" else "ಗೌಪ್ಯತಾ ನೀತಿ",
                fontSize = 12.sp,
                color = Teal,
                textDecoration = TextDecoration.Underline
            )
            Spacer(Modifier.width(16.dp))
            Box(modifier = Modifier.size(4.dp).clip(CircleShape).background(TextMuted.copy(alpha = 0.3f)))
            Spacer(Modifier.width(16.dp))
            Text(
                text = if (isEnglish) "Terms of Service" else "ಸೇವಾ ನಿಯಮಗಳು",
                fontSize = 12.sp,
                color = Teal,
                textDecoration = TextDecoration.Underline
            )
        }
        
        Spacer(Modifier.height(12.dp))
        
        Text(
            text = "Grama-Sanjeevini · Health for All",
            style = MaterialTheme.typography.labelSmall,
            color = TextMuted.copy(alpha = 0.6f)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    LoginScreen({}, {})
}
