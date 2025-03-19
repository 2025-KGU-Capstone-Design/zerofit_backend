package com.zerofit

//import com.zerofit.persistence.configureDatabases
import com.zerofit.web.configureMonitoring
import com.zerofit.web.configureRouting
import com.zerofit.web.configureSerialization
import com.zerofit.web.configureServices
import io.ktor.server.application.*

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    configureSerialization()
    configureMonitoring()
//    configureDatabases()
    configureRouting()
    configureServices()
}
