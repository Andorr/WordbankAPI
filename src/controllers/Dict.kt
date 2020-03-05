package com.wordbank.controllers

import com.mongodb.client.model.Filters.regex
import com.wordbank.dtos.WordDto
import com.wordbank.dtos.WordPutDto
import com.wordbank.helpers.bindOrNull
import com.wordbank.helpers.bindPagination
import com.wordbank.helpers.principalSubject
import com.wordbank.helpers.whenNull
import com.wordbank.mappers.map
import com.wordbank.models.Word
import com.wordbank.models.WordType
import com.wordbank.services.Dictionary
import io.ktor.application.call
import io.ktor.auth.authenticate
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.ktor.routing.*
import org.bson.types.ObjectId
import org.kodein.di.Kodein
import org.kodein.di.generic.instance
import org.litote.kmongo.*
import org.litote.kmongo.id.toId
import org.litote.kmongo.util.idValue

fun Route.dict(kodein: Kodein) {
    val dict : Dictionary by kodein.instance<Dictionary>();

    route("api/v1/encyc/dict") {

        authenticate {
            post() {
                val userId = call.principalSubject()
                call.bindOrNull<WordDto>()
                    .whenNull {
                        call.respond(HttpStatusCode.BadRequest, mapOf("error" to "invalid body parameters. Is type a valid type?"))
                        return@post
                    }!!
                    .also {
                        it.word = it.word.trim()
                        it.translations.mapTo(it.translations){ t -> t.trim() }
                        it.tags.mapTo(it.tags){ t -> t.trim().toLowerCase() }
                    }
                    .let{ it.toWord(ObjectId(userId).toId()) }
                    .also { dict.add(it) }
                    .also {
                        call.respond(HttpStatusCode.Created, it)
                    }
            }

            get("/{id}") {
                call.parameters["id"]
                    ?.let { dict.get(it) }
                    .whenNull {
                        call.respond(HttpStatusCode.NotFound, mapOf("error" to "entity by given id does not exist"))
                        return@get
                    }!!
                    .also {
                        // Handle authorization
                        val userId = call.principalSubject()
                        if(it.owner.toString() != userId) {
                            call.respond(HttpStatusCode.Forbidden)
                            return@get
                        }
                    }
                    .also {

                        call.respond(HttpStatusCode.OK, it)
                    }
            }

            put("/{id}") {
                val put = call.bindOrNull<WordPutDto>()
                    .whenNull {
                        call.respond(HttpStatusCode.BadRequest, mapOf("error" to "invalid body parameters. Is type a valid type?"))
                        return@put
                    }!!
                    .also { it.clean() }

                val userId = call.principalSubject()
                call.parameters["id"]
                    .let { dict.get(it!!) }
                    .whenNull {
                        call.respond(HttpStatusCode.NotFound, mapOf("error" to "did not find resource with given id"))
                        return@put
                    }!!
                    .also {
                        // Handle authorization
                        if(it.owner.toString() != userId) {
                            call.respond(HttpStatusCode.Unauthorized)
                            return@put
                        }
                    }
                    .also { it.map(put) }
                    .also {
                        val result = dict.update(it.idValue!!, it)
                        if(!result.wasAcknowledged()) {
                            call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "was not able to update"))
                            return@put
                        }
                        call.respond(HttpStatusCode.OK, it)
                    }

            }

            get() {
                val userId = call.principalSubject()
                val filters = call.request.queryParameters
                    .let {params ->
                        (Word::owner eq ObjectId(userId).toId())
                            .let{
                                // Append search filter on word
                                if (!params.contains("search")) it else
                                and(it, or(
                                    Word::word regex "(?i)(.*${params["search"]}.*)",
                                    regex("translations", "(?i)(.*${params["search"]}.*)")
                                ))
                            }
                            .let {
                                // Append tag filter on word
                                if(!params.contains("tag")) it else
                                and(it, regex("tags", "${params["tag"]}"))
                            }
                    }

                call.bindPagination()
                .let { dict.getMany(it, filters) }
                .also {
                    call.respond(HttpStatusCode.OK, it)
                }
            }
        }

    }
}