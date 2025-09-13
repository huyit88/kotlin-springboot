package com.example.validation

import jakarta.validation.ConstraintViolationException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus

@RestControllerAdvice
class ValidationExceptionHandler {
    @ExceptionHandler(ConstraintViolationException::class)
    fun onConstraintViolation(ex: ConstraintViolationException): ResponseEntity<Map<String, Any>> {
        val errors = ex.constraintViolations.map {
            mapOf(
                "path" to it.propertyPath.toString(),   // e.g., "square.n"
                "message" to it.message                  // e.g., "must be greater than 0"
            )
        }
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(mapOf("status" to 400, "errors" to errors))
    }
}