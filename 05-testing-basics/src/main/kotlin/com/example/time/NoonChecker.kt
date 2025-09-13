package com.example.time

import java.time.Clock
import java.time.ZoneOffset

class NoonChecker(
    val clock: Clock
){
    fun isUtcNoon(): Boolean{
        val hour = clock.instant().atZone(ZoneOffset.UTC).hour
        return hour == 12
    }
}