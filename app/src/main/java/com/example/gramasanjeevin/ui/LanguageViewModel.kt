package com.example.gramasanjeevin.ui

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class LanguageViewModel : ViewModel() {
    private val _isEnglish = MutableStateFlow(true)
    val isEnglish: StateFlow<Boolean> = _isEnglish

    fun setLanguage(isEnglish: Boolean) {
        _isEnglish.value = isEnglish
    }

    fun toggleLanguage() {
        _isEnglish.value = !_isEnglish.value
    }
}
