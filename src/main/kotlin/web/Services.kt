package com.zerofit.web

import com.zerofit.service.UserService
import com.zerofit.service.UserServiceImpl
import io.ktor.server.application.*
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger

fun Application.configureServices() {
    install(Koin) {
        slf4jLogger()
        modules(module {
            single<UserService> { UserServiceImpl() }
        })
    }
}
