package com.example.validation

import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestBody

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import jakarta.validation.Valid
import com.example.validation.dtos.CreateGroupReq
import com.example.validation.core.Group
import com.example.validation.core.User
import com.example.validation.core.GroupService
import java.net.URI

@RestController
@RequestMapping("/api/groups")
class GroupController(val gsv: GroupService){
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@RequestBody @Valid req: CreateGroupReq): ResponseEntity<Map<String, Group>>{
        val owner = req.owner
        val group = Group(0L, User(1L, owner.name, owner.email), req.members)
        val created = gsv.create(group)
        val location = URI.create("api/group/${created.id}")
        return ResponseEntity.created(location).body(mapOf("group" to created))
    }
}