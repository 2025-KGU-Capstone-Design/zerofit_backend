package com.zerofit.route

import com.zerofit.service.UserService
import io.ktor.http.HttpStatusCode
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import org.koin.ktor.ext.inject

fun Route.userRouting() {

    val userService by inject<UserService>()

//    get("user/{id}") {
//        val userId = call.parameters["id"]?.toIntOrNull()
//        println(userId)
//
//        if (userId != null) {
//            val user = userService.getUser(userId)
//            print(user.toString())
//
//            if (user != null) {
//                call.respond(HttpStatusCode.OK, user)
//            } else {
//                call.respond(HttpStatusCode.NotFound)
//            }
//        } else {
//            call.respond(HttpStatusCode.BadRequest, "Invalid user ID")
//        }
//    }
}
