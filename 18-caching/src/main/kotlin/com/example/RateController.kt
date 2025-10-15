package com.example

import org.springframework.web.bind.annotation.*
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity


@RestController
@RequestMapping("/api/rates")
class RateController(
    val currencyService: CurrencyService
){
    @GetMapping("{from}/{to}")
    fun getRate(@PathVariable from: String, @PathVariable to: String): Map<String,Any>{
        val rate = currencyService.getRate(from, to)
        return mapOf(
            "from" to from,
            "to" to to,
            "rate" to rate
            )
    }

    @GetMapping("/random")
    fun getRateRandom(): Double{
        return currencyService.getRandomRate()
    }

    @PutMapping("{from}/{to}")
    fun setRate(@PathVariable from: String, @PathVariable to: String, @RequestParam rate: Double): Map<String,Any>{
        currencyService.setRate(from, to, rate)  
        return mapOf(
            "from" to from,
            "to" to to,
            "rate" to rate
            )      
    }

    @DeleteMapping("{from}/{to}")
    fun deleteRate(@PathVariable from: String, @PathVariable to: String): ResponseEntity<String>{
        currencyService.deleteRate(from, to)  
        return  ResponseEntity.noContent().build()
    }
}