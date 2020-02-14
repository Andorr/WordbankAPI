package com.wordbank

import auth.JWTHandler
import auth.jwt
import com.auth0.jwt.JWT
import com.fasterxml.jackson.databind.SerializationFeature
import com.mongodb.*
import com.mongodb.client.MongoClient
import controllers.auth
import org.litote.kmongo.*
import controllers.user
import io.ktor.application.Application
import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.CallLogging
import io.ktor.features.ContentNegotiation
import io.ktor.features.DefaultHeaders
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.jackson.jackson
import io.ktor.response.respond
import io.ktor.routing.Routing
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.routing
import io.ktor.server.engine.commandLineEnvironment
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.EngineMain
import io.ktor.server.netty.Netty
import models.User
import org.kodein.di.*
import org.kodein.di.generic.bind
import org.kodein.di.generic.instance
import org.kodein.di.generic.provider
import org.kodein.di.generic.singleton
import org.litote.kmongo.KMongo
import org.litote.kmongo.id.jackson.IdJacksonModule
import org.slf4j.event.Level
import services.UserDao
import services.UserService


fun main(args: Array<String>) {
    println("Starting server on port ${8080}")
   embeddedServer(Netty, commandLineEnvironment(args)).start(wait = true)
}


fun Application.app() {
    val username = environment.config.property("db.username").getString()
    val password = environment.config.property("db.password").getString()

    app(Kodein {
        bind<com.mongodb.MongoClient>() with singleton {
            KMongo.createClient(ConnectionString("mongodb+srv://${username}:${password}@maincluster-bknyx.gcp.mongodb.net/test?retryWrites=true"))
        }
        bind<UserDao>() with singleton {
            UserService(instance(), "test")
        }
        bind<JWTHandler>() with singleton {
            JWTHandler(environment.config.property("ktor.jwt.key").getString())
        }
    })
}

fun Application.app(kodein: Kodein) {
    val userService by kodein.instance<UserDao>()
    val jwtHandler by kodein.instance<JWTHandler>()

    jwt(jwtHandler)

    routing {
        auth(userService, jwtHandler)
        user(userService)
    }

    install(DefaultHeaders) { header(HttpHeaders.Server, "Workbank")}
    install(CallLogging) { level = Level.INFO }
    install(ContentNegotiation) {
        jackson {
            enable(SerializationFeature.INDENT_OUTPUT) // Pretty print JSON
            registerModule(IdJacksonModule())
        }
    }


}

suspend fun ApplicationCall.send(statusCode: HttpStatusCode, message : Any) {
    respond(message)
    response.status(statusCode)
}