package com.zerofit.web

import com.zerofit.route.solutionRoute
import com.zerofit.route.userRoute
import com.zerofit.service.SolutionService
import com.zerofit.service.UserService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Application.configureRouting() {
    install(StatusPages) {
        exception<IllegalArgumentException> { call, cause ->
            call.respondText(
                text = cause.message ?: "Invalid request",
                status = HttpStatusCode.BadRequest
            )
        }

        exception<Throwable> { call, cause ->
            call.respondText(text = "500: $cause", status = HttpStatusCode.InternalServerError)
        }
    }

    val userService by inject<UserService>()
    val solutionService by inject<SolutionService>()

    routing {
        get("/") {
            call.respondText("Hello World!")
        }

        userRoute(userService)
        solutionRoute(solutionService)
    }
}
