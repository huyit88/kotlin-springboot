package com.example

import org.springframework.web.bind.annotation.*
import java.math.BigDecimal

@RestController
@RequestMapping("/api")
class AccountController(
    val accountService: AccountService,
    val badService: BadService,
    val goodService: GoodService,
){
    @PostMapping("/tx/transfer")
    fun transfer(@RequestParam from: Long, @RequestParam to: Long, @RequestParam amount: BigDecimal){
        accountService.transfer(from, to, amount)
    }

    @PostMapping("/tx/template")
    fun transferWithTemplate(@RequestParam from: Long, @RequestParam to: Long, @RequestParam amount: BigDecimal){
        accountService.transferWithTemplate(from, to, amount)
    }

    @PostMapping("/tx/bad")
    fun badCall(@RequestParam id: Long, @RequestParam amount: BigDecimal){
        badService.outerCallInner(id, amount)
    }

    @PostMapping("/tx/good")
    fun goodCall(@RequestParam id: Long, @RequestParam amount: BigDecimal){
        goodService.outerCallInner(id, amount)
    }

    @PostMapping("/tx/deposit/lock")
    fun depositLock(@RequestParam id: Long, @RequestParam amount: BigDecimal){
        accountService.depositLock(id, amount)
    }

    @PostMapping("/tx/deposit/plain")
    fun depositPlain(@RequestParam id: Long, @RequestParam amount: BigDecimal){
        accountService.depositPlain(id, amount)
    }

    @GetMapping("/accounts/{id}")
    fun getAccount(@PathVariable id: Long): AccountDto{
        val accountEntity = accountService.getAccount(id)
        return AccountDto(
            id = accountEntity.id ?: throw IllegalStateException("Account ID is null"),
            owner = accountEntity.owner,
            balance = accountEntity.balance
        )
    }
}

data class AccountDto(val id: Long, val owner: String, val balance: BigDecimal)