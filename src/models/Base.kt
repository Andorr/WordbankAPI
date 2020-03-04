package com.wordbank.models

import org.bson.codecs.pojo.annotations.BsonId
import org.litote.kmongo.Id
import org.litote.kmongo.newId
import java.util.Date;

abstract class Base() {
    @BsonId
    val id : Id<User> = newId()
    val createdAt: Date = Date()
    var updatedAt: Date = Date()
}