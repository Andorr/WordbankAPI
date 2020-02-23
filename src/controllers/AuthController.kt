package com.wordbank.controllers

import com.wordbank.auth.JWTHandler
import com.wordbank.dtos.LogInCredentials
import com.wordbank.helpers.bindOrNull
import com.wordbank.helpers.whenNull
import com.wordbank.services.UserDao
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.request.receiveOrNull
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.post
import io.ktor.routing.route
import org.mindrot.jbcrypt.BCrypt

fun Route.auth(userService: UserDao, jwtHandler: JWTHandler) {

    route("api/v1/auth") {

        post("token") {
            val post = call.bindOrNull<LogInCredentials>()
                .whenNull {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to "missing email or password field"))
                    return@post
                }!!

            val user = userService.getUserByEmail(post.email)

            user.whenNull {
                // Check if user exists
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "invalid credentials"))
                return@post
            }!!.also {
                // Validate password
                BCrypt.checkpw(post.password, it.password)
                    .takeIf{ it }
                    .whenNull {
                        call.respond(HttpStatusCode.BadRequest, mapOf("error" to "invalid credentials"))
                        return@post
                    }
            }.also {
                call.respond(
                    HttpStatusCode.OK,
                    mapOf(
                        "access_token" to jwtHandler.sign(it, 60000*60),
                        "expires_in" to 60000*60
                    )
                )
            }
        }

    }

}