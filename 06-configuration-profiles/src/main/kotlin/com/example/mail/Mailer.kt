package com.example.mailer

import org.springframework.stereotype.Component
import org.springframework.context.annotation.Profile

interface Mailer{
    val name: String
}

@Component
@Profile("dev")
class InMemoryMailer : Mailer{
    override val name = "in-memory"
}

@Component
@Profile("prod")
class SmtpMailer : Mailer{
    override val name = "smtp"
}