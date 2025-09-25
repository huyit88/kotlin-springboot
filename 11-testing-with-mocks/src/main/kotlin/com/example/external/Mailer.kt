package com.example.external

interface Mailer{
    fun send(to: String, subject: String, body: String): Boolean
}