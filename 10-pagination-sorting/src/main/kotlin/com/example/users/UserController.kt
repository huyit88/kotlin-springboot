package com.example.users

import org.springframework.web.bind.annotation.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Slice
import org.springframework.data.domain.Sort
import kotlin.math.min

@RestController
@RequestMapping("/api")
class UserController(
    val inMemoryRepo: InMemoryUserRepository,
    val userRepository: UserRepository
){
    private fun getComparator(sort: String): Comparator<User>{
        val (prop,dir) = sort.split(",").let{ it[0] to (it.getOrNull(1) ?: "asc")}
        return when(prop){
            "id" -> compareBy<User>{it.id}
            "name" -> compareBy<User>{it.name}
            else -> compareBy<User>{it.id}
        }.let{if (dir == "desc") it.reversed() else it}
    }

    private fun getSortedFilter(sort: List<String>): List<String>{
        val whiteListSorts = arrayOf("id", "name")
        var sortedFilter:List<String> = mutableListOf()
        sortedFilter += sort.map{it.lowercase()}.filter{whiteListSorts.contains(it.split(",").getOrNull(0))}
        if(!sortedFilter.contains("id,asc") && !sortedFilter.contains("id,desc")){
            sortedFilter += "id,asc"
        }
        return sortedFilter
    }

    @GetMapping("/memory/users")
    fun getInMemoryList(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        @RequestParam(defaultValue = "id,asc") sort: List<String>,
    ): Map<String,Any>{      
        val sortedFilter = getSortedFilter(sort)
        val sizeClamped = size.coerceIn(1, 50)
        val pageInRange = maxOf(page, 0)
        val comparator = sortedFilter.map{getComparator(it)}.reduce{acc,e -> acc.then(e)}
        val users = inMemoryRepo.findAll()    
        val sorted = users.sortedWith(comparator)

        val from = pageInRange * sizeClamped
        val to = min(from + sizeClamped, sorted.size)
        val totalElements = sorted.size
        val totalPages = if(totalElements == 0) 0 else (totalElements + sizeClamped - 1) / sizeClamped
        val hasNext = pageInRange + 1 < totalPages
        val content = if(from < sorted.size) sorted.subList(from, to) else emptyList()

        return mapOf(
            "content" to content,
            "page" to page,
            "size" to size,
            "totalElements" to users.size,
            "totalPages" to totalPages,
            "hasNext" to hasNext,
            "sort" to  sortedFilter
            )
    }

    @GetMapping("/users")
    fun getDbUsers(@RequestParam(defaultValue = "") q: String, pageable: Pageable): Page<UserResponse>{
        val pageRequest  = pageable.run{PageRequest.of(
            pageNumber, pageSize, sort.and(Sort.by(Sort.Order.asc("id")))
            )}
        
        val users: Page<UserEntity> = userRepository.findByNameContainingIgnoreCase(q, pageRequest)
        return users.map{it -> it.toResponse()}
    }

    @GetMapping("/users/slice")
    fun getDbUsersSlice(@RequestParam(defaultValue = "") q: String, pageable: Pageable): Slice<UserResponse>{
        val pageRequest  = pageable.run{PageRequest.of(
            pageNumber, pageSize, sort.and(Sort.by(Sort.Order.asc("id")))
            )}
        
        val users:Slice<UserEntity> = userRepository.findAllByNameContainingIgnoreCase(q, pageRequest)
        return users.map{it -> it.toResponse()}
    }
}