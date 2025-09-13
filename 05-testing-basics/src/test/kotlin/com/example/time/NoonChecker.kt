package com.example.time

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset

class NoonCheckerTest{
    @Test
    fun `return true with noon time`(){
        val fixedClock = Clock.fixed(
            Instant.parse("2025-01-01T12:00:00Z"),
            ZoneOffset.UTC
            )
        val checker = NoonChecker(fixedClock)
        assertTrue(checker.isUtcNoon())
    }

    @Test
    fun `return false with other time`(){
        val fixedClock = Clock.fixed(
            Instant.parse("2025-01-01T11:00:00Z"),
            ZoneOffset.UTC
            )
        val checker = NoonChecker(fixedClock)
        assertFalse(checker.isUtcNoon())
    }
}