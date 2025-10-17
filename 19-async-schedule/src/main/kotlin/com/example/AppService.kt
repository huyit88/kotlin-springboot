package com.example

import org.springframework.stereotype.Service
import org.springframework.scheduling.annotation.Async
import java.util.concurrent.CompletableFuture
import org.springframework.scheduling.annotation.Scheduled
import java.util.concurrent.atomic.AtomicBoolean


@Service
class AppService{    

    @Async
    fun sendEmail(email: String){
        Thread.sleep(1000)
        println("Dispatched to $email")
    }

    @Async
    fun compute(n: Int): CompletableFuture<Int> =
        CompletableFuture.supplyAsync { Thread.sleep(1000); n * n }

    @Scheduled(fixedRate = 5_000)
    fun heartBeat(){
        println("Heartbeat at <${(System.currentTimeMillis() / 1000)}> on <${Thread.currentThread().name}>")
    }

    val guard = AtomicBoolean(false)
    @Scheduled(cron = "0 * * * * *")
    fun cleanUp(){
        if (!guard.compareAndSet(false, true)) return
        try {
            println("START cleanup @ ${java.time.Instant.now()} on $t")
            Thread.sleep(2_000)
            println("End cleanup @ ${java.time.Instant.now()} on $t")
        } finally {
            guard.set(false)
        }        
    }
}