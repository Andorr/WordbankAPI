package com.wordbank.services

import com.mongodb.MongoClient
import com.wordbank.models.User
import org.bson.BsonDocument
import org.bson.BsonObjectId
import org.bson.Document
import org.bson.types.ObjectId
import org.litote.kmongo.eq
import org.litote.kmongo.findOne
import org.litote.kmongo.getCollection

interface UserDao {
    fun getUserByEmail(email: String): User?
    fun createUser(user: User)
}

class UserService(mongoClient: MongoClient, database: String) : UserDao {
    private val db = mongoClient.getDatabase(database)
    private val collection = db.getCollection<User>()

    override fun getUserByEmail(email: String): User? {
        return collection.findOne(User::email eq email)
    }

    override fun createUser(user: User) {
        collection.insertOne(user)

    }
}