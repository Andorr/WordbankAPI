package com.wordbank.services

import com.mongodb.MongoClient
import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoDatabase
import com.wordbank.dtos.PagedConfig
import com.wordbank.dtos.PagedList
import com.wordbank.models.Folder
import com.wordbank.models.User
import com.wordbank.models.Word
import org.litote.kmongo.getCollection

interface Dictionary: CrudStore<Word> {
    fun getFolders(config: PagedConfig): PagedList<Folder>
}

class DictService(
    mongoClient: MongoClient,
    database: String
) : CrudService<Word>(mongoClient.getDatabase(database).getCollection<Word>()), Dictionary {
    private val db = mongoClient.getDatabase(database)
    private val folderCollection = db.getCollection<Folder>()

    override fun getFolders(config: PagedConfig): PagedList<Folder> {
        return CrudService.getMany(folderCollection, config)
    }
}