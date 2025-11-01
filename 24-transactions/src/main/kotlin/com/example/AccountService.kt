package com.example

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import org.springframework.transaction.support.TransactionTemplate

@Service
class AccountService(
    val accountRepo: AccountRepository,
    val auditLogService: AuditLogService,
    val transactionTemplate: TransactionTemplate,
){
    @Transactional
    fun depositLock(id: Long, amount: BigDecimal){
        val account = loadForUpdate(id)
        val update = account.copy(balance = account.balance.plus(amount))
        Thread.sleep(10_000)
        accountRepo.save(update)
    }

    @Transactional
    fun depositPlain(id: Long, amount: BigDecimal){
        val account = getAccount(id)
        val update = account.copy(balance = account.balance.plus(amount))
        accountRepo.save(update)
    }

    @Transactional
    fun loadForUpdate(id: Long): AccountEntity{
        return accountRepo.findByIdWithLock(id)
            ?: throw IllegalStateException("Account $id not found")
    }

    @Transactional
    fun transfer(fromId: Long, toId: Long, amount: BigDecimal){
        val accFrom = accountRepo.findById(fromId)
            .orElseThrow { IllegalStateException("Account $fromId not found") }
        val accTo = accountRepo.findById(toId)
            .orElseThrow { IllegalStateException("Account $toId not found") }
        auditLogService.log("TRANSFER_ATTEMPT", "transfer from $fromId to $toId : $amount")
        if(accFrom.balance < amount){
            throw IllegalStateException("Insufficient funds")
        }

        val accFromUpdate = accFrom.copy(balance = accFrom.balance.minus(amount))
        val accToUpdate = accTo.copy(balance = accTo.balance.plus(amount))

        accountRepo.save(accFromUpdate)
        accountRepo.save(accToUpdate)
    }

    fun transferWithTemplate(fromId: Long, toId: Long, amount: BigDecimal){
        transactionTemplate.execute {
            val accFrom = accountRepo.findById(fromId)
                .orElseThrow { IllegalStateException("Account $fromId not found") }
            val accTo = accountRepo.findById(toId)
                .orElseThrow { IllegalStateException("Account $toId not found") }
            
            if(accFrom.balance < amount){
                throw IllegalStateException("Insufficient funds")
            }

            val accFromUpdate = accFrom.copy(balance = accFrom.balance.minus(amount))
            val accToUpdate = accTo.copy(balance = accTo.balance.plus(amount))

            accountRepo.save(accFromUpdate)
            accountRepo.save(accToUpdate)
        }
        
        auditLogService.log("TRANSFER_ATTEMPT", "transfer from $fromId to $toId : $amount")
    }

    fun getAccount(id: Long): AccountEntity{
        return accountRepo.findById(id)
            .orElseThrow { IllegalStateException("Account $id not found") }
    }
}