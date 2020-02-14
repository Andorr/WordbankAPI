package controllers

import dtos.LogInCredentials
import io.ktor.application.call
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.route
import models.User
import services.UserDao

fun Route.user(userService: UserDao) {

    route("/api/v1/user") {

        post("/create") {
            println("Times change")
            val post = call.receive<LogInCredentials>()
            println("Times has not changed")

            // Check if the username is available
            val existingUser = userService.getUserByUserName(post.username)
            if(existingUser != null) {
                error("username is unavailable")
            }

            // Validate password
            if(post.password.length < 8) {
                error("password is too short")
            }

            // Create user
            val user = User(post.username, post.password)
            userService.createUser(user)

            call.respond(user)
        }

        get("/me") {

            call.respond(User( "Anders", "asdf"))


        }

    }

}