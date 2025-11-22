package com.example.payment.adapter.out

import com.example.payment.domain.port.PaymentGateway
import org.springframework.stereotype.Component
import java.math.BigDecimal
import kotlin.random.Random

@Component
class FakePaymentGateway : PaymentGateway {
    override fun charge(amount: BigDecimal): Boolean {
        return Random.nextBoolean()
    }
}