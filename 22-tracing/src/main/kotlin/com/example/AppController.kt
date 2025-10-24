package com.example

import org.springframework.web.bind.annotation.*
import io.micrometer.tracing.Tracer
import io.micrometer.tracing.Span
import org.slf4j.LoggerFactory
import kotlin.random.Random
import org.springframework.http.ResponseEntity

@RestController
@RequestMapping("/api")
class AppController(val tracer: Tracer){
    private val log = LoggerFactory.getLogger(AppController::class.java)

    @GetMapping("/ping")
    fun ping(): ResponseEntity<String>{
        log.info("handling /api/ping") 
        return ResponseEntity.ok("pong")
    }

    @GetMapping("/report")
    fun generateReport(@RequestParam(required = false) fail: Boolean): Map<String, String>{
        val span = tracer.nextSpan().name("generate-report").start()
        try{
            // Add tags
            span.tag("report.type", "daily")
            span.tag("rows", "42")
            
            // Simulate work (sleep 200-400ms)
            val sleepTime = (200..400).random()
            val rows = Random.nextInt(1_000, 5_000)
            span.tag("rows", rows.toString())

            // simulate work
            Thread.sleep(Random.nextLong(200, 400))

            if (fail) {
                throw IllegalStateException("simulated-report-failure")
            }

            log.info("report generated rows={}", rows)
            return mapOf("status" to "ok")
        }catch(e: Exception){
            span.error(e)
            log.error("report generation failed: {}", e.message)
            throw e
        }finally{
            span.end()
        }
    }

    @GetMapping("/compose")
    fun compose(): Map<String,Any>{
        stepOne()
        stepTwo()
        log.info("compose two step")  
        return mapOf(
            "status" to "ok", 
            "steps" to arrayOf("load-input","compute-output")
            )
    }

    private fun stepOne(){
        stepWithSpan("load-input"){
            Thread.sleep(Random.nextLong(200, 400))
            log.info("load-input") 
        } 
    }

    private fun stepTwo(){
        stepWithSpan("compute-output"){
            Thread.sleep(Random.nextLong(200, 400))
            log.info("compute-output") 
        } 
    }

    private fun <T> stepWithSpan(name: String, block: () -> T): T {
        val span = tracer.nextSpan().name(name).start()
        return try {
            tracer.withSpan(span).use { block() }
        } finally {
            span.end()
        }
    }
}