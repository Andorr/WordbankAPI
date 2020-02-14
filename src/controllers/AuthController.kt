package controllers

import auth.JWTHandler
import dtos.LogInCredentials
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.post
import io.ktor.routing.route
import org.mindrot.jbcrypt.BCrypt
import services.UserDao

fun Route.auth(userService: UserDao, jwtHandler: JWTHandler) {

    route("api/v1/auth") {

        post("token") {
            val post = call.receive<LogInCredentials>()
            val user = userService.getUserByUserName(post.username)

            if (user == null) {
                call.response.status(HttpStatusCode.BadRequest)
                error("user with that username was not found")
            }

            // Validate password
            val hashedPwd = BCrypt.hashpw(post.password, BCrypt.gensalt())
            if (user.password != hashedPwd) {
                error("invalid credentials")
            }

            // Create JWT token
            val token = jwtHandler.sign(user.username)
            call.respond(mapOf("token" to token))
        }

    }

}