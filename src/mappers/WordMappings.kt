package com.wordbank.mappers

import com.wordbank.dtos.WordPutDto
import com.wordbank.models.Word
import org.modelmapper.Conditions
import org.modelmapper.ModelMapper
import org.modelmapper.config.Configuration

val modelMapper = ModelMapper()
// Add type mapper for WordPutDto -> Word

fun initializeMappers() {
    modelMapper.configuration.isFieldMatchingEnabled = true
    modelMapper.configuration.fieldAccessLevel = Configuration.AccessLevel.PRIVATE;

    modelMapper.typeMap(WordPutDto::class.java, Word::class.java)
        .propertyCondition = Conditions.isNotNull()
}


fun Word.map(w: WordPutDto) = modelMapper.map(w, this)
