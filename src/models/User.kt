package com.wordbank.models

import com.wordbank.dtos.UserDto
import org.bson.codecs.pojo.annotations.BsonId
import org.litote.kmongo.Id
import org.litote.kmongo.newId

data class User(val email: String, val password: String) {
    @BsonId val id : Id<User> = newId()
}

fun User.toDto() : UserDto = UserDto(
    id, email
)

