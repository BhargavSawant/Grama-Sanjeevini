package com.example.gramasanjeevin.model

enum class UserRole { VILLAGER, PHARMACIST }

data class User(
    val userId: String = "",
    val name: String = "",
    val village: String = "",
    val phone: String = "",
    val healthId: String = "",
    val address: String = "",
    val role: String = ""
)
