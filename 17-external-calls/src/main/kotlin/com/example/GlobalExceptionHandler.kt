package com.example

import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import jakarta.servlet.http.HttpServletRequest
import java.net.URI
import java.util.concurrent.TimeoutException

@Order(Ordered.LOWEST_PRECEDENCE)
@RestControllerAdvice
class GlobalExceptionHandler{

    @ExceptionHandler(RemoteNotFound::class)
    fun handleRemoteNotFound(ex: RemoteNotFound, req: HttpServletRequest): ProblemDetail{
        val pd = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.message ?: "Not Found")
        pd.title = "Resource not found"
        pd.instance = URI.create(req.requestURI)
        return pd
    }

    @ExceptionHandler(RemoteFailure::class)
    fun handleRemoteFailure(ex: RemoteFailure, req: HttpServletRequest): ProblemDetail{
        val pd = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, ex.message ?: "Remote Failure")
        pd.title = "Remote Failure"
        pd.instance = URI.create(req.requestURI)
        return pd
    }

    @ExceptionHandler(TimeoutException::class)
    fun handleRemoteFailure(ex: TimeoutException, req: HttpServletRequest): ProblemDetail{
        val pd = ProblemDetail.forStatusAndDetail(HttpStatus.GATEWAY_TIMEOUT, "Upstream timeout")
        pd.title = "Gateway Timeout"
        pd.instance = URI.create(req.requestURI)
        return pd
    }

    @ExceptionHandler(IllegalStateException::class)
    fun handleOtherError(ex: IllegalStateException, req: HttpServletRequest): ProblemDetail{
        val pd = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, "Unhandled error")
        pd.title = "Unhandled error"
        pd.instance = URI.create(req.requestURI)
        return pd
    }
}