package com.example

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import jakarta.persistence.LockModeType

interface AccountRepository : JpaRepository<AccountEntity, Long>{
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM AccountEntity a WHERE a.id = :id")
    fun findByIdWithLock(@Param("id") id: Long): AccountEntity?
}