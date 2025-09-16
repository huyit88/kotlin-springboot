package com.example.migration.countries

import org.springframework.web.bind.annotation.*
import org.springframework.jdbc.core.JdbcTemplate

@RestController
@RequestMapping("/api/countries")
class CountryController(
    val jdbcTemplate: JdbcTemplate
){
    @GetMapping
    fun countries(): List<String> =
        jdbcTemplate.queryForList("select code from countries", String::class.java)

}