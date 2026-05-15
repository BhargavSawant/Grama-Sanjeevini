package com.example.gramasanjeevin.ui

import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.gramasanjeevin.model.UserRole
import com.example.gramasanjeevin.utils.L
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException

// ── Shared colour tokens ──────────────────────────────────────────────────────
private val PowderBlue   = Color(0xFFB0E0E6)
private val Teal         = Color(0xFF00695C)
private val TealDark     = Color(0xFF004D40)
private val BlueSurface  = Color(0xFFF0F9FA)
private val GreenSurface = Color(0xFFF2F7F2)
private val TextPrimary  = Color(0xFF1A2B35)
private val TextMuted    = Color(0xFF6B7280)

@Composable
fun LoginScreen(
    onVillagerSelected: () -> Unit,
    onPharmacistLoginSuccess: () -> Unit,
    authViewModel: AuthViewModel,
    pharmacistViewModel: PharmacistViewModel
) {
    var selectedRole by remember { mutableStateOf<UserRole?>(null) }
    val isEnglish by authViewModel.isEnglish.collectAsState()
    val authState by authViewModel.authState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(authState) {
        if (authState is AuthState.Success) {
            if (selectedRole == UserRole.VILLAGER) {
                onVillagerSelected()
            } else {
                onPharmacistLoginSuccess()
            }
            authViewModel.resetState()
        } else if (authState is AuthState.Error) {
            Toast.makeText(context, (authState as AuthState.Error).message, Toast.LENGTH_LONG).show()
            authViewModel.resetState()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        BackgroundDecorations()

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TopUtilityBar(
                isEnglish = isEnglish,
                onLanguageToggle = { authViewModel.toggleLanguage() }
            )

            HeroHeader(isEnglish = isEnglish)

            AnimatedContent(
                targetState = selectedRole,
                transitionSpec = {
                    fadeIn(animationSpec = tween(400)) + slideInHorizontally { if (targetState != null) it else -it } togetherWith
                    fadeOut(animationSpec = tween(400)) + slideOutHorizontally { if (targetState != null) -it else it }
                },
                label = "login_step"
            ) { role ->
                if (role == null) {
                    RolePickerStep(
                        isEnglish = isEnglish,
                        onRoleSelected = { selectedRole = it }
                    )
                } else {
                    UniversalAuthStep(
                        role = role,
                        isEnglish = isEnglish,
                        onBack = { selectedRole = null },
                        authViewModel = authViewModel
                    )
                }
            }
            
            Spacer(modifier = Modifier.weight(1f))
            FooterSection(isEnglish = isEnglish)
        }
        
        if (authState is AuthState.Loading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.4f)),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(color = Teal)
                        Spacer(Modifier.height(16.dp))
                        Text(L.loading(isEnglish), fontWeight = FontWeight.Medium)
                    }
                }
            }
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
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextButton(
            onClick = onLanguageToggle,
            colors = ButtonDefaults.textButtonColors(contentColor = Teal)
        ) {
            Icon(Icons.Default.Language, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(6.dp))
            Text(
                text = if (isEnglish) "ಕನ್ನಡ" else "English",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }

        IconButton(onClick = { /* Help context */ }) {
            Icon(Icons.AutoMirrored.Filled.HelpOutline, contentDescription = L.help(isEnglish), tint = TextMuted)
        }
    }
}

@Composable
private fun HeroHeader(isEnglish: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 20.dp, bottom = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .shadow(12.dp, CircleShape)
                .background(Brush.linearGradient(listOf(Teal, TealDark)), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.LocalHospital,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(44.dp)
            )
        }

        Spacer(Modifier.height(24.dp))

        Text(
            text = L.gramaSanjeevini(isEnglish),
            fontSize = 32.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Teal,
            letterSpacing = (-0.5).sp
        )
        Text(
            text = L.yourHealthPriority(isEnglish),
            fontSize = 15.sp,
            color = TextMuted,
            letterSpacing = 1.sp
        )
    }
}

@Composable
private fun RolePickerStep(
    isEnglish: Boolean,
    onRoleSelected: (UserRole) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = L.welcomeBack(isEnglish),
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        Text(
            text = L.selectRole(isEnglish),
            fontSize = 14.sp,
            color = TextMuted,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(32.dp))

        RoleCard(
            title = L.villager(isEnglish),
            subtitle = L.villagerSub(isEnglish),
            icon = Icons.Default.People,
            color = BlueSurface,
            accentColor = Color(0xFF2196F3),
            onClick = { onRoleSelected(UserRole.VILLAGER) }
        )

        Spacer(Modifier.height(16.dp))

        RoleCard(
            title = L.pharmacist(isEnglish),
            subtitle = L.pharmacistSub(isEnglish),
            icon = Icons.Default.MedicalServices,
            color = GreenSurface,
            accentColor = Teal,
            onClick = { onRoleSelected(UserRole.PHARMACIST) }
        )
    }
}

@Composable
private fun RoleCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    color: Color,
    accentColor: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(110.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = color),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .background(Color.White, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(28.dp))
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = TextPrimary)
                Text(subtitle, fontSize = 13.sp, color = TextMuted)
            }
            Icon(Icons.AutoMirrored.Filled.ArrowForwardIos, contentDescription = null, tint = TextMuted, modifier = Modifier.size(16.dp))
        }
    }
}

@Composable
private fun UniversalAuthStep(
    role: UserRole,
    isEnglish: Boolean,
    onBack: () -> Unit,
    authViewModel: AuthViewModel
) {
    val context = LocalContext.current
    var isSignUp by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = Color.Black,
        unfocusedTextColor = Color.Black,
        focusedBorderColor = Teal,
        unfocusedBorderColor = Color(0xFFDADCE0),
        focusedLabelColor = Teal,
        unfocusedLabelColor = TextMuted,
        cursorColor = Teal
    )
    val inputTextStyle = TextStyle(color = Color.Black, fontSize = 16.sp)

    val gso = remember {
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestProfile()
            .requestIdToken(context.getString(com.example.gramasanjeevin.R.string.default_web_client_id))
            .build()
    }
    
    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                account?.idToken?.let { authViewModel.signInWithGoogle(it) }
            } catch (e: ApiException) {
                Toast.makeText(context, if (isEnglish) "Google Sign-In failed" else "Google ಸೈನ್-ಇನ್ ವಿಫಲವಾಗಿದೆ", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.Start
    ) {
        IconButton(
            onClick = onBack,
            modifier = Modifier.size(44.dp).background(Color(0xFFF5F5F5), CircleShape)
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = L.back(isEnglish), tint = TextPrimary)
        }

        Spacer(Modifier.height(20.dp))

        Text(
            text = if (isSignUp) {
                if (role == UserRole.VILLAGER) L.signUp(isEnglish) else L.pharmacist(isEnglish) + " " + L.signUp(isEnglish)
            } else {
                if (role == UserRole.VILLAGER) L.signIn(isEnglish) else L.pharmacist(isEnglish) + " " + L.signIn(isEnglish)
            },
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        Text(
            text = L.authStepSubTitle(isEnglish, isSignUp),
            fontSize = 14.sp,
            color = TextMuted
        )

        Spacer(Modifier.height(32.dp))

        if (isSignUp) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(L.fullName(isEnglish)) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = Teal) },
                colors = textFieldColors,
                textStyle = inputTextStyle
            )
            Spacer(Modifier.height(12.dp))
        }

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text(L.email(isEnglish)) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = Teal) },
            colors = textFieldColors,
            textStyle = inputTextStyle
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text(L.password(isEnglish)) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = Teal) },
            trailingIcon = {
                val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(imageVector = image, contentDescription = null, tint = Teal)
                }
            },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            colors = textFieldColors,
            textStyle = inputTextStyle
        )
        Spacer(Modifier.height(20.dp))
        Button(
            onClick = { 
                if (isSignUp) {
                    authViewModel.signUpWithEmail(email, password, name, role)
                } else {
                    authViewModel.signInWithEmail(email, password)
                }
            },
            modifier = Modifier.fillMaxWidth().height(54.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Teal, contentColor = Color.White)
        ) {
            Text(
                text = if (isSignUp) L.signUp(isEnglish) else L.signIn(isEnglish),
                fontWeight = FontWeight.Bold, 
                fontSize = 16.sp
            )
        }

        Spacer(Modifier.height(16.dp))

        TextButton(
            onClick = { isSignUp = !isSignUp },
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text(
                text = if (isSignUp) L.alreadyHaveAccount(isEnglish) else L.dontHaveAccount(isEnglish),
                color = Teal,
                fontWeight = FontWeight.Medium
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFFEEEEEE))
            Text(
                text = L.orLabel(isEnglish),
                modifier = Modifier.padding(horizontal = 12.dp),
                color = TextMuted,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
            HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFFEEEEEE))
        }

        OutlinedButton(
            onClick = { 
                val gsc = GoogleSignIn.getClient(context, gso)
                googleSignInLauncher.launch(gsc.signInIntent)
            },
            modifier = Modifier.fillMaxWidth().height(54.dp),
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFDADCE0)),
            colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White)
        ) {
            Icon(Icons.Filled.AccountCircle, contentDescription = null, tint = Color(0xFF4285F4), modifier = Modifier.size(24.dp))
            Spacer(Modifier.width(12.dp))
            Text(
                text = L.googleContinue(isEnglish),
                color = TextPrimary,
                fontWeight = FontWeight.Medium
            )
        }
        
        Spacer(Modifier.height(40.dp))
    }
}

@Composable
private fun BackgroundDecorations() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        drawCircle(
            color = PowderBlue.copy(alpha = 0.2f),
            radius = 150.dp.toPx(),
            center = Offset(size.width * 0.9f, size.height * 0.1f)
        )
        drawCircle(
            color = Teal.copy(alpha = 0.05f),
            radius = 200.dp.toPx(),
            center = Offset(size.width * -0.1f, size.height * 0.8f)
        )
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
        Text(
            text = L.authSecured(isEnglish),
            color = TextMuted,
            fontSize = 11.sp,
            letterSpacing = 0.5.sp
        )
        Spacer(Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(10.dp), tint = TextMuted)
            Spacer(Modifier.width(4.dp))
            Text(
                text = L.termsPrivacy(isEnglish),
                color = Teal,
                fontSize = 11.sp,
                textDecoration = TextDecoration.Underline,
                modifier = Modifier.clickable { }
            )
        }
    }
}
