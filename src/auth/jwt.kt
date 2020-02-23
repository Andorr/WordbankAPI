package com.wordbank.auth

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.JWTVerificationException
import com.auth0.jwt.interfaces.DecodedJWT
import com.wordbank.models.User
import io.ktor.application.Application
import io.ktor.application.install
import io.ktor.auth.Authentication
import io.ktor.auth.UserIdPrincipal
import io.ktor.auth.jwt.jwt
import java.time.Duration
import java.util.*


open class JWTHandler(val secret : String, val issuer: String, val audience : String) {
    private val algorithm = Algorithm.HMAC256(secret)
    val verifier = JWT.require(algorithm).withIssuer(issuer).withAudience(audience).build()

    fun signAccess(user: User, duration: Long): String {
        return JWT
            .create()
            .withIssuedAt(Date())
            .withExpiresAt(Date(Date().time + duration))
            .withIssuer(issuer)
            .withAudience(audience)
            .withSubject(user.id.toString())
            .withClaim("email", user.email)
            .sign(algorithm)
    }

    fun signRefresh(subjectId: String, expirationDate: Date): String {
        return JWT
            .create()
            .withIssuedAt(Date())
            .withExpiresAt(expirationDate)
            .withSubject(subjectId)
            .sign(algorithm)
    }

    fun verifyAccess(token: String): DecodedJWT? {
        return try {
            verifier.verify(token)
        } catch(e: JWTVerificationException) {
            null
        }
    }

    fun verifyRefresh(token: String): DecodedJWT? {
        return try {
            JWT
                .require(algorithm)
                .build()
                .verify(token)
        } catch(e: JWTVerificationException) {
            null
        }
    }



}

fun createExpirationDate(duration: Duration): Date {
    return Date(Date().toInstant().plus(duration).toEpochMilli())
}

fun Application.jwt(jwtHandler: JWTHandler) {

    install(Authentication) {
        jwt {
            verifier(jwtHandler.verifier)
            validate {
                UserIdPrincipal(it.payload.getClaim("name").asString())
            }
        }
    }
}