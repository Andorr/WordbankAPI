package com.wordbank.auth

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.wordbank.models.User
import io.ktor.application.Application
import io.ktor.application.install
import io.ktor.auth.Authentication
import io.ktor.auth.UserIdPrincipal
import io.ktor.auth.jwt.jwt
import org.kodein.di.Kodein
import org.kodein.di.generic.instance
import org.litote.kmongo.util.idValue
import java.util.*


open class JWTHandler(val secret : String, val issuer: String, val audience : String) {
    private val algorithm = Algorithm.HMAC256(secret)
    val verifier = JWT.require(algorithm).build()

    fun sign(user: User, duration: Long): String {

        val builder = JWT
            .create()
            .withIssuedAt(Date())
            .withExpiresAt(Date(Date().time + duration))
            .withIssuer(issuer)
            .withAudience(audience)
            .withClaim("sub", user.id.toString())
            .withClaim("email", user.email)

        return builder.sign(algorithm)
    }
}

fun Application.jwt(jwtHandler: JWTHandler) {

    /*val jwtIssuer = environment.config.property("jwt.domain").getString()
    val jwtAudience = environment.config.property("jwt.audience").getString()
    val jwtRealm = environment.config.property("jwt.realm").getString()*/

    install(Authentication) {
        jwt {
            verifier(jwtHandler.verifier)
            validate {
                UserIdPrincipal(it.payload.getClaim("name").asString())
            }
        }
    }
}