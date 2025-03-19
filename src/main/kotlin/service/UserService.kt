package com.zerofit.service

import com.zerofit.domain.User

interface UserService {
    fun getUser(userId: Int): User
}

open class UserServiceImpl : UserService {
    override fun getUser(userId: Int): User = User(userId, "mario")
}
