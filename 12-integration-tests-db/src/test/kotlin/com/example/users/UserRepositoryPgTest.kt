package com.example.users

import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.test.context.ActiveProfiles
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort

import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.containers.PostgreSQLContainer

import jakarta.persistence.EntityManager

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

@Testcontainers
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class UserRepositoryPgTest(
    @Autowired private val userRepository: UserRepository
){
    @Autowired lateinit var entityManager: EntityManager

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
    fun testUserRepo(){
        assertEquals(0, userRepository.count())
    }

    @Test
    fun uniqueEmail(){
        val user = UserEntity(null, "a", "a@a.com")
        val anotherUser = UserEntity(null, "b", "a@a.com")
        userRepository.saveAndFlush(user)

        assertFailsWith<DataIntegrityViolationException>{
            userRepository.saveAndFlush(anotherUser)
        }
    }

    @Test
    fun caseSensitiveSearching(){
        val aUser = UserEntity(null, "Ann", "a@a.com")
        val bUser = UserEntity(null, "BANNer", "b@b.com")
        userRepository.save(aUser)
        userRepository.save(bUser)
        entityManager.flush()
        entityManager.clear()

        val list = userRepository.findByNameContainingIgnoreCase("ann")

        assertEquals(2, list.size)
        assertTrue(list.any{ it.email == "a@a.com"})
        assertTrue(list.any{ it.email == "b@b.com"})
    }

    @Test
    fun paginationWithStableSort(){
        val alexs = listOf(
            UserEntity(null, "Alex", "a1@a.com"),
            UserEntity(null, "Alex", "a2@a.com"),
            UserEntity(null, "Alex", "a3@a.com")
        )
        userRepository.saveAllAndFlush(alexs)
        val pageRequest = PageRequest.of(0, 5, Sort.by(Sort.Direction.ASC, "name").and(Sort.by(Sort.Direction.ASC, "id")))

        val page = userRepository.findAllByNameContainingIgnoreCase("alex", pageRequest)
        val content = page.content
        assertEquals(3, content.size)
        assertTrue(content.all { it.name == "Alex" })
        assertTrue(content.zipWithNext().all { it.first.id!! <= it.second.id!! })
    }
}