package com.wordbank.services

import com.mongodb.client.FindIterable
import com.mongodb.client.MongoCollection
import com.mongodb.client.result.UpdateResult
import com.wordbank.dtos.PagedConfig
import com.wordbank.dtos.PagedList
import org.bson.conversions.Bson
import org.litote.kmongo.deleteOneById
import org.litote.kmongo.findOneById
import org.litote.kmongo.updateOneById

interface CrudStore<T> {
    fun get(id: String): T?
    fun getMany(config: PagedConfig, filter: Bson?): PagedList<T>
    fun update(id: Any, updateFilter: Bson): UpdateResult
    fun add(item: T)
    fun delete(id: Any): Boolean
}

open class CrudService<T>(private val collection: MongoCollection<T>) : CrudStore<T> {

    override fun get(id: String): T? {
        return collection.findOneById(id)
    }

    override fun getMany(config: PagedConfig, filter: Bson?): PagedList<T> {
        return getMany(collection, config, filter)
    }

    override fun add(item: T) {
        collection.insertOne(item)
    }

    override fun delete(id: Any): Boolean {
        return collection.deleteOneById(id).deletedCount > 0
    }

    override fun update(id: Any, updateFilter: Bson): UpdateResult {
        return collection.updateOneById(id, updateFilter)
    }

    companion object {
        fun <K>getMany(collection: MongoCollection<K>, config: PagedConfig, filter: Bson? = null): PagedList<K> {
            val items : FindIterable<K> = collection.find()
                .takeIf{ filter != null }
                .let{ collection.find(filter) }
            val totalCount = items.count()

            return items
                .skip(config.limit*(config.page-1)).limit(config.limit).toList()
                .let {
                    PagedList<K>(
                        it,
                        totalCount,
                        totalCount > (it.count() + config.limit*(config.page-1))
                    )
                }
        }
    }
}