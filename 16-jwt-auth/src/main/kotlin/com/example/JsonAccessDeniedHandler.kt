package com.example

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.web.access.AccessDeniedHandler
import org.springframework.stereotype.Component
import java.net.URI

@Component
class JsonAccessDeniedHandler(
    private val om: ObjectMapper
) : AccessDeniedHandler{
    override fun handle(
        req: HttpServletRequest,
        res: HttpServletResponse,
        ex: AccessDeniedException
    ){
        val pd = ProblemDetail.forStatusAndDetail(
            HttpStatus.FORBIDDEN,
            "Insufficient permissions"
        ).apply {
            title = "Forbidden"
            instance = URI.create(req.requestURI)
        }

        res.status = HttpStatus.FORBIDDEN.value()
        res.contentType = "application/problem+json"
        res.characterEncoding = "UTF-8"
        om.writeValue(res.outputStream, pd)
    }
}