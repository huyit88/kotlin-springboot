package com.example.migration.users

import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/users")
class UserController(
    val jdbcTemplate: JdbcTemplate
){
    @GetMapping("/count")
      fun countUsers(): Map<String, Long> =
          mapOf("count" to jdbcTemplate.queryForObject("select count(*) from users", Long::class.java)!!)
      
    @PostMapping
    fun create(@RequestBody req: UserDto): Map<String,Any>{
        val sql = "INSERT INTO users (name, email) VALUES (?, ?)"
        val rowsAffected = jdbcTemplate.update(sql, req.name, req.email)
        return mapOf(
            "rows" to rowsAffected,
            "message" to "User created with JDBC"
        )
    }
}