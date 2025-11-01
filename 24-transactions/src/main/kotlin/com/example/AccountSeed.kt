package com.example

import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Bean
import org.springframework.boot.CommandLineRunner
import java.math.BigDecimal

@Configuration
class AccountSeed{

    @Bean
    fun seedAccount(accountRepo: AccountRepository) = CommandLineRunner{
        val account1 = AccountEntity(1, "user-1", BigDecimal(100))
        val account2 = AccountEntity(2, "user-2", BigDecimal(0))

        accountRepo.save(account1)
        accountRepo.save(account2)
    }
}