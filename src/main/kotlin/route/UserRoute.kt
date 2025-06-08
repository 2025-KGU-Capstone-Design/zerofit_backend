package com.zerofit.route

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.zerofit.model.RequestLogin
import com.zerofit.model.User
import com.zerofit.service.UserService
import io.ktor.http.*
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.userRoute(userService: UserService) {

    get("/api/user/availability/{userId}") {
        val userId = call.parameters["userId"]
            ?: throw IllegalArgumentException("userId parameter is required")

        val isAvailable = userService.isUserIdAvailable(userId)

        call.respond(HttpStatusCode.OK, hashMapOf("Available" to isAvailable))
    }

    post("/api/login") {
        val credential = call.receive<RequestLogin>()
        val userId = userService.login(credential)

        val token = JWT.create()
            .withAudience(environment.config.property("ktor.jwt.audience").getString())
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

    authenticate("auth-jwt") {
        get("/api/user") {
            val principal = call.principal<JWTPrincipal>()
            val userId = principal?.payload?.getClaim("userId")?.asString()
                ?: throw IllegalArgumentException("User ID not found in token")

            val user = userService.getUser(userId)
            if (user != null) {
                call.respond(HttpStatusCode.OK, user)
            } else {
                call.respond(HttpStatusCode.NotFound, "User not found")
            }
        }
    }
}
