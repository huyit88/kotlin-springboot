package com.example.data.jpa.users

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface UserRepository : JpaRepository<UserEntity, Long>{
    fun findByEmail(email: String) : UserEntity?
    fun existsByEmail(email: String) : UserEntity?

    @Query("""
        select new com.example.data.jpa.users.UserView(u.id, u.name) 
        from UserEntity u 
        where u.email like concat('%', :domain, '%')
    """)
    fun findViewsByEmailDomain(@Param("domain") domain: String): List<UserView>
}