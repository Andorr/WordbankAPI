package com.wordbank.controllers

import com.wordbank.auth.JWTHandler
import com.wordbank.auth.LIFETIME_ONE_HOUR
import com.wordbank.auth.REFRESH_GRANT
import com.wordbank.auth.createExpirationDate
import com.wordbank.dtos.LogInCredentials
import com.wordbank.dtos.RefreshGrantDto
import com.wordbank.helpers.bindOrNull
import com.wordbank.helpers.whenNull
import com.wordbank.models.User
import com.wordbank.services.GrantStore
import com.wordbank.services.GrantStoreService
import com.wordbank.services.UserDao
import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.request.receiveOrNull
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.post
import io.ktor.routing.route
import kotlinx.coroutines.launch
import org.kodein.di.Kodein
import org.kodein.di.generic.instance
import org.mindrot.jbcrypt.BCrypt
import java.time.Duration
import java.util.*

fun Route.auth(kodein: Kodein) {
    val userService by kodein.instance<UserDao>()
    val grantStore by kodein.instance<GrantStore>()
    val jwtHandler by kodein.instance<JWTHandler>()

    route("api/v1/auth") {

        post("token") {
            val post = call.bindOrNull<LogInCredentials>()
                .whenNull {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to "missing email or password field"))
                    return@post
                }!!

            userService
                .getUserByEmail(post.email.toLowerCase())
                .whenNull {
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
                    // Occasionally delete expired grants from the grant-store
                    if ((0..7).random() == 0) {
                        launch {
                            grantStore.deleteExpired()
                        }
                    }

                    call.handleTokenRequest(it, jwtHandler, grantStore)
                }
        }

        post("token/refresh") {
            val grantDto = call.bindOrNull<RefreshGrantDto>()
                .whenNull {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to "missing field 'refreshToken'"))
                    return@post
                }!!

            // Validate refreshToken
            grantDto
            .let { jwtHandler.verifyRefresh(it.refreshToken) }
            .whenNull {
                // Invalid token
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "invalid token"))
                return@post
            }!!
            // Refresh token is valid, check if it is in grant-store
            .let { grantStore.getAndRemoveItem(grantDto.refreshToken) }
            .whenNull {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "invalid token"))
                return@post
            }!!
            .also {
                // Check if time has expired
                if(Date().after(it.expirationTime)) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to "invalid token"))
                    return@post
                }
            }
            .also {
                // Occasionally delete expired grants from the grant-store
                if ((0..7).random() == 0) {
                    launch {
                        grantStore.deleteExpired()
                    }
                }

                // Create accessToken and refreshToken
                userService.getUserById(it.subjectId)
                .whenNull {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to "user does not exists"))
                    return@post
                }!!
                .also {
                    call.handleTokenRequest(it, jwtHandler, grantStore)
                }
            }
        }
    }
}

suspend fun ApplicationCall.handleTokenRequest(user: User, jwtHandler: JWTHandler, grantStore: GrantStore) {
    // Create refresh token
    val refreshExpireDate = com.wordbank.auth.createExpirationDate(java.time.Duration.ofDays(10))
    val refreshToken = jwtHandler.signRefresh(user.id.toString(), refreshExpireDate)
    grantStore.insertItem(refreshToken,
        com.wordbank.auth.REFRESH_GRANT, user.id.toString(),
        java.util.Date(), refreshExpireDate)

    respond(
        io.ktor.http.HttpStatusCode.OK,
        kotlin.collections.mapOf(
            "tokenType" to "Bearer",
            "accessToken" to jwtHandler.signAccess(user, com.wordbank.auth.LIFETIME_ONE_HOUR),
            "expiresIn" to com.wordbank.auth.LIFETIME_ONE_HOUR,
            "refreshToken" to refreshToken
        )
    )
}