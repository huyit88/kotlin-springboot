package com.example.pricing

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assertions.assertThrows

class PriceCalculatorTest{
    @Test
    fun `calculate with net amount`(){
        assertTrue(10.0 == PriceCalculator(1.0).total(5.0))
    }

    @Test
    fun `throw exception when netAmount is smaller 0`(){
        assertThrows(IllegalArgumentException::class.java){
            PriceCalculator(1.0).total(0.0)
        }
    }
}
