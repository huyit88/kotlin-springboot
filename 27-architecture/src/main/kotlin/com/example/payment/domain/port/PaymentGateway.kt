package com.example.payment.domain.port

import java.math.BigDecimal

interface PaymentGateway { 
    fun charge(amount: BigDecimal): Boolean
}