package com.example.validation.core

import org.springframework.stereotype.Service

@Service
class GroupService{
    private val groups:MutableList<Group> = mutableListOf()
    
    fun create(group: Group): Group{
        val nextId = this.getMaxId() + 1
        val created = group.copy(id = nextId)
        groups += created

        return created
    }

    private fun getMaxId(): Long = groups.maxByOrNull{it.id}?.id ?: 0
}