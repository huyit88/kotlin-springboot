package com.example.payment.adapter.config

import com.example.payment.domain.PaymentProcessor
import com.example.payment.domain.port.PaymentGateway
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class PaymentConfig {
    @Bean
    fun paymentProcessor(paymentGateway: PaymentGateway): PaymentProcessor {
        return PaymentProcessor(paymentGateway)
    }
}

