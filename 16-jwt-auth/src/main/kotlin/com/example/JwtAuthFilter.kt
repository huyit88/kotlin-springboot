package com.example

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder

@Component
class JwtAuthFilter(
    private val jwtProvider: JwtProvider
): OncePerRequestFilter(){
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val header = request.getHeader("Authorization")
        val hasBearer = header != null && header.startsWith("Bearer ")
        if (!hasBearer) {
            filterChain.doFilter(request, response)
            return
        }

        val token = header.substring("Bearer ".length)
        try{
            val jwt = jwtProvider.verifyToken(token)
            val subject = jwt.subject
            val roles = jwt.getClaim("roles").asList(String::class.java) ?: emptyList()
            val authorities = roles.map{ SimpleGrantedAuthority("ROLE_$it") }

            val auth = UsernamePasswordAuthenticationToken(subject, null, authorities)
            SecurityContextHolder.getContext().authentication = auth
            filterChain.doFilter(request, response)
        }catch(ex: Exception){
            SecurityContextHolder.clearContext()
            response.status = HttpServletResponse.SC_UNAUTHORIZED
        }
    }
}