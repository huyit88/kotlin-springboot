package com.example

import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.MethodArgumentNotValidException

import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.ConstraintViolationException
import java.net.URI

@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice
class ValidationExceptionHandler{

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationError(ex: MethodArgumentNotValidException, req: HttpServletRequest): ProblemDetail{
        val pd = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST)
        pd.title = "Request body validation failed"
        pd.detail = "One or more fields are invalid"
        pd.instance = URI.create(req.requestURI)

        val errors = ex.bindingResult.fieldErrors.map{fe->
            mapOf(
                "field" to fe.field,
                "message" to (fe.defaultMessage ?: "invalid"),
                "rejectedValue" to fe.rejectedValue
            )
        }
        pd.setProperty("errors", errors)
        return pd
    }

    @ExceptionHandler(ConstraintViolationException::class)
    fun handleValidationError(ex: ConstraintViolationException, req: HttpServletRequest): ProblemDetail{
        val pd = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST)
        pd.title = "Request parameter validation failed"
        pd.detail = "One or more parameters are invalid"
        pd.instance = URI.create(req.requestURI)

        val errors = ex.constraintViolations.map{v->
            mapOf(
                "path" to v.propertyPath.toString(),
                "message" to v.message
            )
        }
        pd.setProperty("errors", errors)
        return pd
    }
}