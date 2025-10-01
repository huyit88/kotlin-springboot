package com.example

import org.springframework.web.bind.annotation.*
import jakarta.servlet.http.HttpServletRequest
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import java.net.URI

@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice
class UserExceptionHandler{
    @ExceptionHandler(UserError.EmailTaken::class)
    fun handleEmailTaken(ex: UserError.EmailTaken, req: HttpServletRequest): ProblemDetail{
        val pd = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.message)
        pd.title = "Email conflict"
        pd.setProperty("code", "USER_EMAIL_TAKEN")
        pd.setProperty("email", ex.email)
        pd.instance = URI.create(req.requestURI)
        return pd
    }

    @ExceptionHandler(UserError.UserNotFound::class)
    fun handleEmailTaken(ex: UserError.UserNotFound, req: HttpServletRequest): ProblemDetail{
        val pd = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.message)
        pd.title = "User not found"
        pd.setProperty("code", "USER_NOT_FOUND")
        pd.setProperty("id", ex.id)
        pd.instance = URI.create(req.requestURI)
        return pd
    }
}