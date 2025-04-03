package com.zerofit.web

import io.ktor.server.application.*
import io.ktor.server.plugins.cors.routing.*

fun Application.configureHttp() {
    install(CORS) {
        anyHost()
    }
}
