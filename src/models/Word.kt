package com.wordbank.models

import org.bson.codecs.pojo.annotations.BsonId
import org.litote.kmongo.Id
import org.litote.kmongo.newId

enum class WordType {
    None,
    Verb,
    Noun,
    Adjective,
    Adverb,
    Pronoun,
    Conjunction,
    Determiner,
    Preposition,
    Other
}

data class Word(
    val word : String,
    val type : WordType
): Base() {
    val tags : MutableList<String> = ArrayList()
    val translations : MutableList<String> = ArrayList()
}
