package com.wordbank.controllers

import com.wordbank.dtos.PagedConfig
import com.wordbank.dtos.WordDto
import com.wordbank.helpers.bindOrNull
import com.wordbank.helpers.bindPagination
import com.wordbank.helpers.principalSubject
import com.wordbank.helpers.whenNull
import com.wordbank.models.Word
import com.wordbank.models.WordType
import com.wordbank.services.Dictionary
import io.ktor.application.call
import io.ktor.auth.authenticate
import io.ktor.auth.authentication
import io.ktor.auth.jwt.JWTPrincipal
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.request.receiveOrNull
import io.ktor.response.respond
import io.ktor.routing.*
import org.bson.types.ObjectId
import org.kodein.di.Kodein
import org.kodein.di.generic.instance
import org.litote.kmongo.eq
import org.litote.kmongo.id.toId

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
                    .let{ it.toWord(ObjectId(userId).toId()) }
                    .also { dict.add(it) }
                    .also {
                        call.respond(HttpStatusCode.Created, it)
                    }
            }

            put("/:id") {

            }

            get() {
                val userId = call.principalSubject()
                call.bindPagination()
                .let { dict.getMany(it, Word::owner eq ObjectId(userId).toId()) }
                .also {
                    call.respond(HttpStatusCode.OK, it)
                }
            }
        }

    }
}