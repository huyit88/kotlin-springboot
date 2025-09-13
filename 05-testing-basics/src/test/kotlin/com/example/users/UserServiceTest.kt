package com.example.users

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assertions.assertNotNull

class InMemoryRepo : UserRepository{
    private var autoId = 1L;
    private val users: MutableList<User> = mutableListOf()

    override fun save(user: User): User{
        val created = user.copy(autoId++)
        users += created

        return created
    }

    override fun findById(id: Long): User?{
        return users.find {id == it.id}
    }
}

class UserServiceTest{
    @Test
    fun `should generate id when create new user`(){
        val inMemoryRepo = InMemoryRepo()
        val service = UserService(inMemoryRepo)

        val created = service.create("name", "a@gmail.com")

        assertTrue(created.id > 0L)
    }

    @Test
    fun `should return user when finding with correct id`(){
        val inMemoryRepo = InMemoryRepo()
        val service = UserService(inMemoryRepo)

        val created = service.create("name", "a@gmail.com")

        assertNotNull(service.findById(created.id))
    }
}