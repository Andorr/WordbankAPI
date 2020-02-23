package com.wordbank.dtos

data class LogInCredentials(
    val email: String,
    val password: String,
    val grantType : String?
)