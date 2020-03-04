package com.wordbank.services

import com.mongodb.client.FindIterable
import com.mongodb.client.MongoCollection
import com.mongodb.client.result.UpdateResult
import com.wordbank.dtos.PagedConfig
import com.wordbank.dtos.PagedList
import com.wordbank.models.Base
import org.bson.conversions.Bson
import org.bson.types.ObjectId
import org.litote.kmongo.deleteOneById
import org.litote.kmongo.find
import org.litote.kmongo.findOneById
import org.litote.kmongo.updateOneById
import java.util.*

interface CrudStore<T: Base> {
    fun get(id: String): T?
    fun getMany(config: PagedConfig, filter: Bson): PagedList<T>
    fun getMany(config: PagedConfig, filter: String?): PagedList<T>
    fun update(id: Any, updateFilter: Bson): UpdateResult
    fun update(id: Any, item: T): UpdateResult
    fun add(item: T)
    fun delete(id: Any): Boolean
}

open class CrudService<T: Base>(private val collection: MongoCollection<T>) : CrudStore<T> {

    override fun get(id: String): T? {
        return collection.findOneById(ObjectId(id))
    }

    override fun getMany(config: PagedConfig, filter: Bson): PagedList<T> {
        return getMany(collection, config, filter)
    }

    override fun getMany(config: PagedConfig, filter: String?): PagedList<T> {
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

    override fun update(id: Any, item: T): UpdateResult {
        item.updatedAt = Date()
        return collection.updateOneById(id, item)
    }

    companion object {
        private fun <K>paginate(iterable: FindIterable<K>, config: PagedConfig): PagedList<K> {
            val totalCount = iterable.count()

            return iterable
                .skip(config.limit*(config.page-1)).limit(config.limit).toList()
                .let {
                    PagedList<K>(
                        it,
                        totalCount,
                        config.limit,
                        totalCount > (it.count() + config.limit*(config.page-1))
                    )
                }
        }

        fun <K>getMany(collection: MongoCollection<K>, config: PagedConfig, filter: Bson): PagedList<K> {
            val items : FindIterable<K> = collection.find()
                .let{ collection.find(filter) }

            return paginate(items, config)
        }

        fun <K>getMany(collection: MongoCollection<K>, config: PagedConfig, filter: String? = null): PagedList<K> {
            val items : FindIterable<K> = collection.find()
                .takeIf{ filter != null }
                .let{ collection.find(filter!!) }

            return paginate(items, config)
        }
    }
}