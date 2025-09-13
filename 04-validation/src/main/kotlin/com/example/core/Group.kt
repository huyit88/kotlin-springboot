package com.example.validation.core

data class Group(
    val id: Long,
    val owener: User,
    val members: List<String>
)