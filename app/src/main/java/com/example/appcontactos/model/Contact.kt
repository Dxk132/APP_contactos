package com.example.appcontactos.model

data class Contact(
    val id: String = "",
    val name: String = "",
    val phone: String = "",
    val email: String = "",
    val address: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val userId: String = ""
)
