package com.wordbank


import com.fasterxml.jackson.databind.SerializationFeature
import com.mongodb.*
import com.wordbank.auth.JWTHandler
import com.wordbank.auth.jwt
import com.wordbank.controllers.auth
import com.wordbank.controllers.dict
import com.wordbank.controllers.user
import com.wordbank.services.*
import io.ktor.application.Application
import io.ktor.application.install
import io.ktor.features.CallLogging
import io.ktor.features.ContentNegotiation
import io.ktor.features.DefaultHeaders
import io.ktor.http.HttpHeaders
import io.ktor.jackson.jackson
import io.ktor.routing.routing
import io.ktor.server.engine.commandLineEnvironment
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import org.kodein.di.*
import org.kodein.di.generic.bind
import org.kodein.di.generic.instance
import org.kodein.di.generic.singleton
import org.litote.kmongo.KMongo
import org.litote.kmongo.id.jackson.IdJacksonModule
import org.slf4j.event.Level


fun main(args: Array<String>) {
    println("Starting server on port ${8080}")
    embeddedServer(Netty, commandLineEnvironment(args)).start(wait = true)
}


fun Application.app() {
    val username = environment.config.property("db.username").getString()
    val password = environment.config.property("db.password").getString()
    val database = environment.config.property("db.database").getString()

    app(Kodein {
        bind<com.mongodb.MongoClient>() with singleton {
            KMongo.createClient(ConnectionString("mongodb+srv://${username}:${password}@maincluster-bknyx.gcp.mongodb.net/test?retryWrites=true"))
        }
        bind<UserDao>() with singleton {
            UserService(instance(), database)
        }
        bind<GrantStore>() with singleton {
            GrantStoreService(instance(), database)
        }
        bind<Dictionary>() with singleton {
            DictService()
        }
        bind<JWTHandler>() with singleton {
            JWTHandler(
                environment.config.property("ktor.jwt.key").getString(),
                environment.config.property("ktor.jwt.issuer").getString(),
                environment.config.property("ktor.jwt.audience").getString()
            )
        }
    })
}

fun Application.app(kodein: Kodein) {
    val jwtHandler by kodein.instance<JWTHandler>()

    jwt(jwtHandler)

    routing {
        auth(kodein)
        user(kodein)
        dict(kodein)
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