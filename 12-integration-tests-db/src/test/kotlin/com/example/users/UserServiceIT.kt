package com.example.users

import org.springframework.test.context.ActiveProfiles
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.beans.factory.annotation.Autowired

import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.containers.PostgreSQLContainer

import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
class UserServiceIT(
    @Autowired private val userRepository: UserRepository,
    @Autowired private val userService: UserService
){
    companion object{
        @Container
        val pg = PostgreSQLContainer("postgres:15.5")

        @JvmStatic
        @DynamicPropertySource
        fun props(registry: DynamicPropertyRegistry){
            registry.add("spring.datasource.url", pg::getJdbcUrl)
            registry.add("spring.datasource.username", pg::getUsername)
            registry.add("spring.datasource.password", pg::getPassword)
            registry.add("spring.jpa.hibernate.ddl-auto") { "none" }
        }
    }

    @Test
    fun replaceSuccess(){
        val user = userRepository.saveAndFlush(UserEntity(null, "a", "a@a.com"))
        
        userService.replace(user.id!!, "b", "b@b.com")

        val updated = userRepository.findById(user.id!!).get()!!
        assertNotNull(updated)
        assertEquals("b", updated.name)
        assertEquals("b@b.com", updated.email)
    }

    @Test
    fun replaceFailing(){
        val user = userRepository.saveAndFlush(UserEntity(null, "a", "a@a.com"))
        
        assertFailsWith<IllegalStateException> { userService.replaceFailing(user.id!!, "b", "b@b.com") }

        val updated = userRepository.findById(user.id!!).get()!!
        assertNotNull(updated)
        assertEquals("a", updated.name)
        assertEquals("a@a.com", updated.email)
    }
}