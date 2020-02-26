package com.wordbank.models

import com.wordbank.dtos.UserDto


data class User(val email: String, val password: String): Base()

fun User.toDto() : UserDto = UserDto(
    id, email
)

