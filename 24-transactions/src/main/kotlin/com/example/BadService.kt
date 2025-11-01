package com.example

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal

@Service
class BadService(
    val accountRepo: AccountRepository,
){
    fun outerCallInner(id: Long, amount: BigDecimal){
        innerWrite(id, amount)
    }


    @Transactional
    fun innerWrite(id: Long, amount: BigDecimal){
        val account = accountRepo.findById(id).orElseThrow{error("account $id not found")}
        val update = account.copy(balance = account.balance.plus(amount))
        accountRepo.save(update)
        throw error("throw error to trigger rollback")
    }
}