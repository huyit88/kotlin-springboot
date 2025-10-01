package com.example

import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import jakarta.servlet.http.HttpServletRequest
import java.net.URI

@Order(Ordered.LOWEST_PRECEDENCE)
@RestControllerAdvice
class GlobalExceptionHandler{

    @ExceptionHandler(NoSuchElementException::class)
    fun handleNotFound(ex: NoSuchElementException, req: HttpServletRequest): ProblemDetail{
        val pd = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.message ?: "Not Found")
        pd.title = "Resource not found"
        pd.instance = URI.create(req.requestURI)
        return pd
    }

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleBadRequest(ex: IllegalArgumentException, req: HttpServletRequest): ProblemDetail{
        val pd = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.message ?: "Bad request")
        pd.title = "Validation error"
        pd.instance = URI.create(req.requestURI)
        return pd
    }

    @ExceptionHandler(Exception::class)
    fun handleOtherError(ex: Exception, req: HttpServletRequest): ProblemDetail{
        val pd = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, "Internal error")
        pd.title = "Internal error"
        pd.instance = URI.create(req.requestURI)
        req.getHeader("X-request-id")?.also{ pd.setProperty("requestId", it) }
        return pd
    }
}