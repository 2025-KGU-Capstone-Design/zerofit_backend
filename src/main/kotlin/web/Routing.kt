package com.zerofit.web

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.zerofit.persistence.User
import com.zerofit.service.UserService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.request.receive
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import org.koin.ktor.ext.inject

@Serializable
data class RequestLogin(
    val userId: String,
    val password: String
)

fun Application.configureRouting() {
    install(StatusPages) {
        exception<IllegalArgumentException> { call, cause ->
            call.respondText(
                text = cause.message ?: "Invalid request",
                status = HttpStatusCode.Unauthorized
            )
        }

        exception<Throwable> { call, cause ->
            call.respondText(text = "500: $cause", status = HttpStatusCode.InternalServerError)
        }
    }

    val userService by inject<UserService>()

    routing {
        get("/") {
            call.respondText("Hello World!")
        }

        get("/user/{id}") {
            val userId = call.parameters["id"]

            if (userId == null) {
                call.respond(HttpStatusCode.BadRequest, "User ID cannot be empty")
                return@get
            }

            val user = userService.getUser(userId)

            if (user != null) {
                call.respond(HttpStatusCode.OK, user)
            } else {
                call.respond(HttpStatusCode.NotFound)
            }
        }

        get("/api/user/availability/{userId}") {
            val userId = call.parameters["userId"]
            if (userId == null) {
                call.respond(HttpStatusCode.BadRequest, "User ID cannot be empty")
                return@get
            }

            val isAvailable = userService.isUserIdAvailable(userId)
            call.respond(HttpStatusCode.OK, hashMapOf("Available" to isAvailable))
        }

        post("/api/login") {
            val credential = call.receive<RequestLogin>()
            val userId = userService.login(credential)

            val token = JWT.create()
                .withIssuer(environment.config.property("ktor.jwt.issuer").getString())
                .withClaim("userId", userId)
                .sign(Algorithm.HMAC256(environment.config.property("ktor.jwt.secret").getString()))

            call.respond(HttpStatusCode.OK, hashMapOf("token" to token))
        }

        post("/api/user") {
            val user = call.receive<User>()
            val id = userService.createUser(user)
            call.respond(HttpStatusCode.Created, hashMapOf("userId" to id))
        }
    }
}
