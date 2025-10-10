package com.example

import org.springframework.web.bind.annotation.*
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException
import java.time.Instant


@RestController
@RequestMapping("/auth")
class AuthController(
    val authenticationManager: AuthenticationManager,
    val jwtProvider: JwtProvider
){
    @PostMapping("/login")
    fun login(@RequestBody req: AuthReq, @RequestParam ttlSeconds: Long?): Map<String,String>{
        try {
            val auth = authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken(req.username, req.password)
            )
            val user = auth.principal as UserDetails
            val roles = user.authorities.map { it.authority.removePrefix("ROLE_") }
            val jwt = if(ttlSeconds == null) jwtProvider.createToken(user.username, roles)
                else jwtProvider.createToken(user.username, roles, Instant.now(), ttlSeconds!!)                
            return mapOf("accessToken" to jwt)
        } catch (ex: Exception){
            throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid username or password")
        }
    }
}

data class AuthReq(val username: String, val password: String)