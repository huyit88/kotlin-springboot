package com.example.users

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Page

interface UserRepository : JpaRepository<UserEntity, Long>{
    fun findByEmail(email: String): UserEntity?
    fun findByNameContainingIgnoreCase(name: String): List<UserEntity>
    fun findAllByNameContainingIgnoreCase(name: String, pageable: Pageable): Page<UserEntity>
}