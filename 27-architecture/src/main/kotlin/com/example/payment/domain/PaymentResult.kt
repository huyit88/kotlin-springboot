package com.example.payment.domain

import java.math.BigDecimal

data class PaymentResult(val status: String, val amount: BigDecimal)