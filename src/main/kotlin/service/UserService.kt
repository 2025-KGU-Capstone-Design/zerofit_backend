package com.zerofit.service

import com.zerofit.persistence.User
import com.zerofit.persistence.Users
import com.zerofit.web.RequestLogin
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.mindrot.jbcrypt.BCrypt

interface UserService {
    suspend fun getUser(userId: String): User?
    suspend fun createUser(user: User): String
    suspend fun login(credential: RequestLogin): String
    suspend fun isUserIdAvailable(userId: String): Boolean
}

open class UserServiceImpl : UserService {

    override suspend fun getUser(userId: String): User? = dbQuery {
        Users.selectAll()
            .where { Users.user_id eq userId }
            .map {
                User(
                    userId = it[Users.user_id],
                    password = it[Users.password],
                    email = it[Users.email],
                    phone = it[Users.phone],
                    companyName = it[Users.company_name]
                )
            }
            .singleOrNull()
    }

    override suspend fun createUser(user: User): String = dbQuery {
        val hashedPassword = BCrypt.hashpw(user.password, BCrypt.gensalt())

        Users.insert {
            it[user_id] = user.userId
            it[password] = hashedPassword
            it[email] = user.email
            it[phone] = user.phone
            it[company_name] = user.companyName
        }[Users.user_id]
    }

    override suspend fun login(credential: RequestLogin): String {
        val user = getUser(credential.userId)
        if (user == null) {
            throw IllegalArgumentException("User not found")
        }

        return if (BCrypt.checkpw(credential.password, user.password)) {
            user.userId
        } else {
            throw IllegalArgumentException("Invalid credentials")
        }
    }

    override suspend fun isUserIdAvailable(userId: String): Boolean = dbQuery {
        Users.selectAll().where { Users.user_id eq userId }.count() == 0.toLong()
    }

    private suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }
}
