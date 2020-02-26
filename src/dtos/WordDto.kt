package com.wordbank.dtos

import com.wordbank.models.Base
import com.wordbank.models.User
import com.wordbank.models.Word
import com.wordbank.models.WordType
import org.litote.kmongo.Id
import org.litote.kmongo.newId

data class WordDto(
    val word : String,
    val type : WordType
    ): Base() {

    var owner: String? = null
    val tags : MutableList<String> = ArrayList()
    val translations : MutableList<String> = ArrayList()

    fun toWord(id: Id<User>): Word {
        val dtoTags = tags
        val dtoTranslations = translations
        return Word(word, type, id).apply {
            tags.addAll(dtoTags)
            translations.addAll(dtoTranslations)
        }
    }
}

