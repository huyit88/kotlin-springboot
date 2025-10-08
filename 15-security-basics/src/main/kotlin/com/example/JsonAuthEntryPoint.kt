package com.example 

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.stereotype.Component
import java.net.URI

@Component
class JsonAuthEntryPoint(
    private val om: ObjectMapper
): AuthenticationEntryPoint {
    override fun commence(
        req: HttpServletRequest,
        res: HttpServletResponse,
        authEx: AuthenticationException
    ){
        val pd = ProblemDetail.forStatusAndDetail(
            HttpStatus.UNAUTHORIZED, 
            "missing or invalid credentials")
            .apply{
                title = "Unauthorized"
                instance = URI.create(req.requestURI)
            }

        res.addHeader("WWW-Authenticate", "Basic realm=\"api\"")
        res.status = HttpStatus.UNAUTHORIZED.value()
        res.contentType = "application/problem+json"
        res.characterEncoding = "UTF-8"
        om.writeValue(res.outputStream, pd)
    }
}