package com.example

import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Service


@Service
class UserAdminService{
    @PreAuthorize("hasRole('ADMIN')")
    fun get(id: Long){
        println("get sensitive data of user $id")
    }
}