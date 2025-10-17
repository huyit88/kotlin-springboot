package com.example

import org.springframework.web.bind.annotation.*
import org.springframework.http.HttpStatus
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap
import org.springframework.http.ResponseEntity

@RestController
@RequestMapping("/api")
class AppController(
    val appService: AppService
){
    val concurrentMap = ConcurrentHashMap<Int, Int>()
    
    @GetMapping("/notify/{email}")
    @ResponseStatus(HttpStatus.ACCEPTED)
    fun notifyEmail(@PathVariable email: String){
        appService.sendEmail(email)
    }

    @PostMapping("/compute/{n}")
    fun compute(@PathVariable n: Int){
        appService.compute(n).thenAccept { result ->
            concurrentMap.put(n, result)
            println("Received: $result ${concurrentMap.get(n)}")
        }
        ResponseEntity.accepted().body("Dispatched n=$n")
    }

    @GetMapping("/result/{n}")
    fun getComputeResult(@PathVariable n: Int): ResponseEntity<Int>{
        if(concurrentMap.containsKey(n)) return ResponseEntity.accepted().body(concurrentMap.get(n))
        return ResponseEntity.notFound().build()
    }
}