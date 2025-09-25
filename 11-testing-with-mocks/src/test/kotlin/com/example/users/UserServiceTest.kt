package com.example.users

import io.mockk.every
import io.mockk.verify
import io.mockk.verifyOrder
import io.mockk.mockk
import io.mockk.slot
import io.mockk.confirmVerified

import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertEquals


class UserServiceTest{
    private val repo: UserRepository = mockk()
    private val userService = UserService(repo)

    @Test
    fun `given email taken when create then throw exception`(){
        every { repo.existsByEmail("a@a.com") } returns true

        assertFailsWith<IllegalStateException>{
            userService.create("a", "a@a.com")
        }
        verify(exactly = 1){ repo.existsByEmail("a@a.com") }
        verify(exactly = 0) { repo.save(any()) } // any/match DSL-scoped function

        // ensures *no other* calls (like save(...)) happened
        confirmVerified(repo)
    }

    @Test
    fun `given free email when create then return user`(){        
        every { repo.existsByEmail("a@a.com") } returns false
        every { repo.save(any()) } answers { arg<User>(0).copy(id = 1L) } 

        val saved = userService.create("a", "a@a.com")

        assertEquals(1L, saved.id)
        verify(exactly = 1) { repo.existsByEmail("a@a.com") }
        verify(exactly = 1) { repo.save(match{ it.name == "a" && it.email == "a@a.com" })}
        confirmVerified(repo)
    }

    @Test
    fun `given free email when create then verifyOrder`(){
        val capt = slot<User>()
        every { repo.existsByEmail("a@a.com") } returns false
        every { repo.save(capture(capt)) } answers { capt.captured.copy(id = 42L) } 

        val saved = userService.create("a", "a@a.com")

        assertEquals("a", capt.captured.name)
        assertEquals("a@a.com", capt.captured.email)
        assertEquals(42L, saved.id)
        verifyOrder { 
            repo.existsByEmail(any())   
            repo.save(any())         
        }
        confirmVerified(repo)
    }

    @Test
    fun `given not found id when load then throw exception`(){
        every { repo.findById(any())} returns null

        val actualException = assertFailsWith<NoSuchElementException>{
            userService.load(1L)
        }

        assertEquals("user 1 not found", actualException.message)
    }

    @Test
    fun `given found id when load then return user`(){
        every { repo.findById(1L)} returns User(1L, "a", "a@a.com")

        val user = userService.load(1L)
        
        assertEquals(1L, user.id)
        assertEquals("a", user.name)
        assertEquals("a@a.com", user.email)
    }
    
}