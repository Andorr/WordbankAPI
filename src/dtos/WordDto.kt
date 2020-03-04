package com.wordbank.dtos

import com.wordbank.models.Base
import com.wordbank.models.User
import com.wordbank.models.Word
import com.wordbank.models.WordType
import org.litote.kmongo.Id
import org.litote.kmongo.newId

data class WordDto(
    var word : String,
    var type : WordType
    ): Base() {

    var owner: String? = null
    var tags : MutableList<String> = ArrayList()
    var translations : MutableList<String> = ArrayList()

    fun toWord(id: Id<User>): Word {
        val dtoTags = tags
        val dtoTranslations = translations
        return Word(word, type, id).apply {
            tags.addAll(dtoTags)
            translations.addAll(dtoTranslations)
        }
    }
}

data class WordPutDto(
    var word: String? = null,
    var type: WordType? = null,
    var tags: MutableList<String>? = null,
    var translations: MutableList<String>? = null
) {

    fun clean() {
        word = word?.trim()
        tags = tags?.let { it.map { t -> t.trim().toLowerCase() }.toMutableList() }
        translations = translations?.let{ it.map{ t -> t.trim() }.toMutableList() }
    }
}
