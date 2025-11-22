package com.example.payment.domain

import com.example.payment.domain.port.PaymentGateway
import java.math.BigDecimal

class PaymentProcessor(
    private val paymentGateway: PaymentGateway
) {
    fun process(amount: BigDecimal): PaymentResult {
        val success = paymentGateway.charge(amount)
        return PaymentResult(
            status = if (success) "SUCCESS" else "FAILED",
            amount = amount
        )
    }
}