package com.example.users

import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Slice
import org.springframework.data.domain.Page
import org.springframework.data.jpa.repository.JpaRepository

interface UserRepository : JpaRepository<UserEntity,Long>{
    fun findByNameContainingIgnoreCase(q: String, pageable: Pageable): Page<UserEntity>
    fun findAllByNameContainingIgnoreCase(q: String, pageable: Pageable): Slice<UserEntity>
}