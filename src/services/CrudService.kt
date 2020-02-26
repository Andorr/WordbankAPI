package com.wordbank.services

import com.mongodb.client.MongoCollection
import com.mongodb.client.result.UpdateResult
import com.wordbank.dtos.PagedConfig
import com.wordbank.dtos.PagedList
import org.bson.conversions.Bson
import org.litote.kmongo.deleteOneById
import org.litote.kmongo.findOneById
import org.litote.kmongo.updateOneById

interface CrudStore<Base> {
    fun get(id: String): Base?
    fun getMany(config: PagedConfig, filter: Bson): PagedList<Base>
    fun update(id: Any, updateFilter: Bson): UpdateResult
    fun add(item: Base)
    fun delete(id: Any): Boolean
}

open class CrudService<T>(private val collection: MongoCollection<T>) : CrudStore<T> {

    override fun get(id: String): T? {
        return collection.findOneById(id)
    }

    override fun getMany(config: PagedConfig, filter: Bson): PagedList<T> {
        val totalCount = collection.find(filter).count()
        val items = collection.find(filter).skip(config.limit*(config.page-1)).limit(config.limit)
        return PagedList<T>(
            items.asIterable(),
            totalCount,
            totalCount > (items.count() + config.limit*(config.page-1))
        )
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

}