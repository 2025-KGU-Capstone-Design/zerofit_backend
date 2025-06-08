package com.zerofit.persistence

import org.jetbrains.exposed.sql.Table

object Users : Table() {
    val user_id = varchar("id", length = 50)
    val password = varchar("password", length = 100)
    val email = varchar("email", 100)
    val phone = varchar("phone", 15)
    val company_name = varchar("company", 100)

    override val primaryKey = PrimaryKey(user_id)
}
