package  com.wordbank.controllers

import com.wordbank.dtos.LogInCredentials
import com.wordbank.dtos.UserDto
import com.wordbank.helpers.bindOrNull
import com.wordbank.helpers.whenNull
import com.wordbank.models.User
import com.wordbank.models.toDto
import com.wordbank.services.UserDao
import io.ktor.application.call
import io.ktor.auth.authenticate
import io.ktor.auth.authentication
import io.ktor.auth.jwt.JWTPrincipal
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.route
import org.mindrot.jbcrypt.BCrypt

fun Route.user(userService: UserDao) {

    route("/api/v1/user") {

        post("") {
            val post = call.bindOrNull<LogInCredentials>()
                .whenNull {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to "missing email or password field"))
                    return@post
                }!!

            // Check if the username is available
            userService.getUserByEmail(post.email)
                ?.also {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to "email unavailable"))
                    return@post
                }

            // Validate password
            post.takeIf { it.password.length < 8 }
                ?.also {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to "password is too short"))
                    return@post
                }

            // Create user
            User(post.email, BCrypt.hashpw(post.password, BCrypt.gensalt(10)))
                .also { userService.createUser(it) }
                .also { call.respond(it.toDto()) }
        }

        authenticate {
            get("/me") {

                call.authentication.principal<JWTPrincipal>()!!
                    .let { userService.getUserById(it.payload.subject) }
                    .whenNull {
                        call.respond(HttpStatusCode.BadRequest, mapOf("error" to "user does not exists"))
                        return@get
                    }!!
                    .also {
                       call.respond(it.toDto())
                    }
            }
        }

    }

}