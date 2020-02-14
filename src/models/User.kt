package models

import org.bson.codecs.pojo.annotations.BsonId
import org.litote.kmongo.Id
import org.litote.kmongo.newId

data class User(val username: String, val password: String) {
    @BsonId val id : Id<User> = newId()
}