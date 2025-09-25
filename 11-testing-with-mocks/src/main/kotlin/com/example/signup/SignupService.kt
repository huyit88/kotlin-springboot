package com.example.signup

import org.springframework.stereotype.Service
import com.example.external.Mailer

@Service
class SignupService(
    private val mailer: Mailer
){
    fun register(name: String, email: String): Boolean{
        return mailer.send(email, "Hello ${name}", "Access your profile now!")
    }
}