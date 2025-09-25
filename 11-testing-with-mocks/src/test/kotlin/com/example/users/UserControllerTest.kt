package com.example.users

import com.ninjasquad.springmockk.MockkBean
import io.mockk.verify
import io.mockk.every
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

import kotlin.test.Test

@WebMvcTest(UserController::class)
class UserControllerWebTest(
    @Autowired private val mockMvc: MockMvc
){
    @MockkBean private lateinit var service: UserService

    @Test
    fun getById_ok(){
        every{ service.getById(1L)} returns User(1L, "a", "a@a.com")

        mockMvc.perform(get("/api/users/1"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.name").value("a"))

        verify(exactly = 1){ service.getById(1L) }
    }
}