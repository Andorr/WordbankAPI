package com.wordbank.services

import com.mongodb.MongoClient
import com.wordbank.models.PersistedGrant
import org.litote.kmongo.eq
import org.litote.kmongo.findOne
import org.litote.kmongo.getCollection
import org.litote.kmongo.lt
import java.util.*
import kotlin.math.exp

interface GrantStore {
    fun insertItem(key: String, type: String, subjectId: String, creationTime: Date, expirationDate: Date): PersistedGrant
    fun removeItem(key: String): Boolean
    fun getItem(key: String): PersistedGrant?
    fun getAndRemoveItem(key: String): PersistedGrant?
    fun deleteExpired(): Long
}

class GrantStoreService(mongoClient: MongoClient, database: String): GrantStore {
    private val db = mongoClient.getDatabase(database)
    private val collection = db.getCollection<PersistedGrant>()

    override fun insertItem(
        key: String,
        type: String,
        subjectId: String,
        creationTime: Date,
        expirationDate: Date
    ): PersistedGrant {
        PersistedGrant(key, type, subjectId, creationTime, expirationDate )
            .also { collection.insertOne(it) }
            .also { return it }
    }

    override fun removeItem(key: String): Boolean {
        return collection.deleteOne(PersistedGrant::key eq key).deletedCount > 0
    }

    override fun getItem(key: String): PersistedGrant? {
        return collection.findOne(PersistedGrant::key eq key)
    }

    override fun getAndRemoveItem(key: String): PersistedGrant? {
        val grant: PersistedGrant? = getItem(key)
        grant?.also { removeItem(it.key) }
        return grant
    }

    override fun deleteExpired(): Long {
        return collection.deleteMany(PersistedGrant::expirationTime lt Date()).deletedCount
    }

}