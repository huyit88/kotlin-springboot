package com.example

import org.springframework.stereotype.Service
import java.math.BigDecimal

@Service
class GoodService(
    val innerService: InnerService,
){
    fun outerCallInner(id: Long, amount: BigDecimal){
        innerService.innerWrite(id, amount)
    }
}