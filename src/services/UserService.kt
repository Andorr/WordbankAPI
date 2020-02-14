package services

import com.mongodb.MongoClient
import models.User
import org.bson.BsonDocument
import org.bson.BsonObjectId
import org.bson.Document
import org.bson.types.ObjectId
import org.litote.kmongo.eq
import org.litote.kmongo.findOne
import org.litote.kmongo.getCollection

interface UserDao {
    fun getUserByUserName(username: String): User?
    fun createUser(user: User)
}

class UserService(mongoClient: MongoClient, database: String) : UserDao {
    private val db = mongoClient.getDatabase(database)
    private val collection = db.getCollection<User>()

    override fun getUserByUserName(username: String): User? {
        val user = collection.findOne(User::username eq username)
        return user
    }

    override fun createUser(user: User) {
        collection.insertOne(user)

    }
}