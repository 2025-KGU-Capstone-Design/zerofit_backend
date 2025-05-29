package com.zerofit.persistence

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.application.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

fun Application.configureDatabases() {
    val dbHost = System.getenv("DB_HOST") ?: "localhost"
    val dbPort = System.getenv("DB_PORT") ?: "3306"
    val dbName = System.getenv("DB_NAME") ?: "zerofit"
    val dbUser = System.getenv("DB_USER") ?: "devuser"
    val dbPass = System.getenv("DB_PASSWORD") ?: "devpass"

    val config = HikariConfig().apply {
        driverClassName = "com.mysql.cj.jdbc.Driver"
        jdbcUrl = "jdbc:mysql://$dbHost:$dbPort/$dbName?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
        username = dbUser
        password = dbPass
        maximumPoolSize = 10
        isAutoCommit = false
        transactionIsolation = "TRANSACTION_REPEATABLE_READ"
        validate()
    }

    val dataSource = HikariDataSource(config)
    Database.connect(dataSource)

    transaction {
        SchemaUtils.create(
            Users,
            SearchHistoryTable,
            FacilitiesTable,
            SolutionTable
        )
    }
}

