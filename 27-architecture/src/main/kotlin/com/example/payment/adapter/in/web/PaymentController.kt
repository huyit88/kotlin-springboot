package com.example.payment.adapter.in.web

import com.example.payment.domain.PaymentProcessor
import com.example.payment.domain.PaymentResult
import org.springframework.web.bind.annotation.*
import java.math.BigDecimal

@RestController
@RequestMapping("/api")
class PaymentController(
    private val paymentProcessor: PaymentProcessor
) {
    @PostMapping("/payments")
    fun pay(@RequestParam amount: BigDecimal): PaymentResult {
        return paymentProcessor.process(amount)
    }
}