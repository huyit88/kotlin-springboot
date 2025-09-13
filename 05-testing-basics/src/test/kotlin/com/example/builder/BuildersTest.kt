package com.example.builder

import com.example.users.User
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertEquals

fun aUser(id: Long = 1, name: String = "Name", email: String = "a@gmail.com"): User = User(id, name, email)

class BuildersTest{
    @Test
    fun `default user value`(){
        val user = aUser()

        assertEquals(1L, user.id)
        assertEquals("Name", user.name)
        assertEquals("a@gmail.com", user.email)
    }
}