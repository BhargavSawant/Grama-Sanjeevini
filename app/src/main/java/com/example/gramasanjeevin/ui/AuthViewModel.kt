package com.example.gramasanjeevin.ui

import android.app.Activity
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gramasanjeevin.model.User
import com.example.gramasanjeevin.model.UserRole
import com.example.gramasanjeevin.utils.FirestoreProvider
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.concurrent.TimeUnit

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    object OtpSent : AuthState()
    object Success : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthViewModel : ViewModel() {
    private val auth: FirebaseAuth = Firebase.auth
    private val db = FirestoreProvider.getDb()

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState

    private val _isEnglish = MutableStateFlow(true)
    val isEnglish: StateFlow<Boolean> = _isEnglish

    private var verificationId: String? = null

    val currentUserId: String?
        get() = auth.currentUser?.uid

    fun toggleLanguage() {
        _isEnglish.value = !_isEnglish.value
    }

    fun setLanguage(isEng: Boolean) {
        _isEnglish.value = isEng
    }

    /**
     * Sign Up with Email & Password
     */
    fun signUpWithEmail(email: String, pass: String, name: String, role: UserRole) {
        if (email.isBlank() || pass.isBlank() || name.isBlank()) {
            _authState.value = AuthState.Error("Please fill all fields")
            return
        }
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val result = auth.createUserWithEmailAndPassword(email, pass).await()
                val firebaseUser = result.user
                if (firebaseUser != null) {
                    saveUserProfile(
                        User(
                            userId = firebaseUser.uid,
                            name = name,
                            phone = "",
                            village = "",
                            healthId = "",
                            address = "",
                            role = role.name
                        )
                    )
                    _authState.value = AuthState.Success
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Registration Failed")
            }
        }
    }

    /**
     * Sign In with Email & Password
     */
    fun signInWithEmail(email: String, pass: String) {
        if (email.isBlank() || pass.isBlank()) {
            _authState.value = AuthState.Error("Please enter email and password")
            return
        }
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val result = auth.signInWithEmailAndPassword(email, pass).await()
                if (result.user != null) {
                    _authState.value = AuthState.Success
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Email Login Failed")
            }
        }
    }

    /**
     * Sign In with Google
     */
    fun signInWithGoogle(idToken: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val credential = GoogleAuthProvider.getCredential(idToken, null)
                val result = auth.signInWithCredential(credential).await()
                val firebaseUser = result.user
                
                if (firebaseUser != null) {
                    val doc = db.collection("users").document(firebaseUser.uid).get().await()
                    if (!doc.exists()) {
                        saveUserProfile(
                            User(
                                userId = firebaseUser.uid,
                                name = firebaseUser.displayName ?: "New User",
                                phone = firebaseUser.phoneNumber ?: "",
                                village = "",
                                healthId = "",
                                address = "",
                                role = "VILLAGER"
                            )
                        )
                    }
                    _authState.value = AuthState.Success
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Google Sign-In Failed")
            }
        }
    }

    /**
     * Send OTP to Phone Number
     */
    fun sendOTP(phoneNumber: String, activity: Activity) {
        _authState.value = AuthState.Loading
        
        val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                signInWithPhoneCredential(credential)
            }

            override fun onVerificationFailed(e: FirebaseException) {
                _authState.value = AuthState.Error(e.message ?: "Verification Failed")
            }

            override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) {
                this@AuthViewModel.verificationId = verificationId
                _authState.value = AuthState.OtpSent
            }
        }

        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(callbacks)
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    /**
     * Verify OTP Code
     */
    fun verifyOTP(code: String) {
        val id = verificationId ?: return
        if (code.length < 6) {
            _authState.value = AuthState.Error("Please enter a valid 6-digit OTP")
            return
        }
        val credential = PhoneAuthProvider.getCredential(id, code)
        signInWithPhoneCredential(credential)
    }

    private fun signInWithPhoneCredential(credential: PhoneAuthCredential) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val result = auth.signInWithCredential(credential).await()
                val firebaseUser = result.user
                
                if (firebaseUser != null) {
                    val doc = db.collection("users").document(firebaseUser.uid).get().await()
                    if (!doc.exists()) {
                        saveUserProfile(
                            User(
                                userId = firebaseUser.uid,
                                name = "User ${firebaseUser.phoneNumber}",
                                phone = firebaseUser.phoneNumber ?: "",
                                village = "",
                                healthId = "",
                                address = "",
                                role = "VILLAGER"
                            )
                        )
                    }
                    _authState.value = AuthState.Success
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "OTP Verification Failed")
            }
        }
    }

    private suspend fun saveUserProfile(user: User) {
        try {
            db.collection("users").document(user.userId).set(user).await()
        } catch (e: Exception) {
            Log.e("AuthViewModel", "Error saving user profile", e)
        }
    }

    fun resetState() {
        _authState.value = AuthState.Idle
    }
}
