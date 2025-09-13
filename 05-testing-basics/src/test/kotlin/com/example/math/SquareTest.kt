package com.example.math

import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.api.Assertions.assertEquals

class SquareTest{
    @ParameterizedTest
    @CsvSource(
        "0, 0",
        "2, 4",
        "3, 9",
        "10, 100"
    )
    fun `square turn n times n`(n: Int, expected: Int){
        val square = Square()
        val actual = square.square(n)
        assertEquals(expected, actual)
    }
}