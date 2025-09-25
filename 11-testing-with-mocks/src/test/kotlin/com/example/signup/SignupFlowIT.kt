package com.example.signup

import io.mockk.every
import io.mockk.verify
import com.ninjasquad.springmockk.MockkBean
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.beans.factory.annotation.Autowired
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.test.assertFalse
import com.example.external.Mailer

import org.springframework.boot.SpringBootConfiguration
import org.springframework.context.annotation.ComponentScan

@SpringBootConfiguration
@ComponentScan("com.example.signup")   // only this feature
class SignupTestApp
/**
If not specify, it will scan whole project and we have to mock all bean 
in order to avoiding compile error
*/

@SpringBootTest(classes = [SignupTestApp::class], webEnvironment = SpringBootTest.WebEnvironment.NONE)
class SignupFlowIT(
    @Autowired private val service: SignupService
){
    @MockkBean lateinit var mailer: Mailer
    
    @Test
    fun `mailer oki`(){
        every{ mailer.send("a@a.com", "Hello a", any()) } returns true

        val res = service.register("a", "a@a.com")

        assertTrue(res)
        verify(exactly = 1){ mailer.send("a@a.com", "Hello a", any()) }
    }

    @Test
    fun `mailer false`(){
        every{ mailer.send("a@a.com", "Hello a", any()) } returns false

        val res = service.register("a", "a@a.com")
        
        assertFalse(res)
        verify(exactly = 1){ mailer.send("a@a.com", "Hello a", any()) }
    }
}