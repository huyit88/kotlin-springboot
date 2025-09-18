package com.example.users.api

import org.springframework.stereotype.Service
import com.example.users.UserRepository

@Service
class UserFacade(
    val uRepo: UserRepository
) : UserReadPort{
    override fun getSummary(id: Long): UserSummary?{
        val user = uRepo.findById(id)
        return user?.toSummary()
    }
}