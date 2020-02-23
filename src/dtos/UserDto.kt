package com.wordbank.dtos

import com.wordbank.models.User
import org.litote.kmongo.Id

data class UserDto (
    val id : Id<User>,
    val email : String
)