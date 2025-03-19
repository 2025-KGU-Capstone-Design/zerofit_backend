package com.zerofit.web

import com.zerofit.service.UserService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Application.configureRouting() {
    install(StatusPages) {
        exception<Throwable> { call, cause ->
            call.respondText(text = "500: $cause", status = HttpStatusCode.InternalServerError)
        }
    }

    val service by inject<UserService>()

    routing {
        get("/") {
            call.respondText("Hello World!")
        }

        get("/user/{id}") {
            val id = call.parameters["id"]?.toInt() ?: throw IllegalArgumentException("Invalid ID")
            call.respond(HttpStatusCode.OK, service.getUser(id))
        }
    }
}
