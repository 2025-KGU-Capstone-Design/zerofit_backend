package com.zerofit

import com.zerofit.persistence.configureDatabases
import com.zerofit.web.*
import io.ktor.server.application.*

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    configureSerialization()
    configureMonitoring()
    configureDatabases()
    configureRouting()
    configureServices()
    configureHttp()
}
