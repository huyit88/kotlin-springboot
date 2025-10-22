package com.example

import org.springframework.stereotype.Service
import io.micrometer.core.annotation.Timed

@Service
class AppService{
    @Timed("search.latency")
    fun search(q: String){
        Thread.sleep((100L..500L).random())
    }
}