package com.example

import org.springframework.web.bind.annotation.*
import io.micrometer.core.instrument.MeterRegistry
import org.slf4j.LoggerFactory

@RestController
@RequestMapping("/api")
class AppController(
    private val registry: MeterRegistry,
    private val appService: AppService
){
    private val counter = registry.counter("orders.created")
    private val log = LoggerFactory.getLogger(AppController::class.java)
    

    @PostMapping("/orders")
    fun createOrder(){
        counter.increment()
    }

    @GetMapping("/search")
    fun search(@RequestParam q: String){
        appService.search(q)
    }

    @PostMapping("/payments")
    fun payment(@RequestParam id: String, @RequestParam amount: Double){
        log.info("payment_created id={} amount={}", id, amount)
    }
}