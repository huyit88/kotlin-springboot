package com.example

import io.github.resilience4j.bulkhead.annotation.Bulkhead
import org.springframework.stereotype.Service

@Service
class ThumbnailService{
    @Bulkhead(name = "thumb", type = Bulkhead.Type.SEMAPHORE)
    fun render(id: String): String{
        Thread.sleep(500)
        return "ok: $id"
    }
}