package com.example.restapi

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity


@SpringBootApplication
class RestApiApplication 

data class Book(val id: Long, val title: String)

data class UpdateBookRequest(val title: String? = null)

@ResponseStatus(HttpStatus.NOT_FOUND)
class NotFound(id: Long) : RuntimeException("Book $id not found")

fun main(args: Array<String>){
    runApplication<RestApiApplication>(*args)
}

@RestController
@RequestMapping("api/books")
class ApiController{
    private val books = mutableListOf(Book(1, "Kotlin in action"))

    @GetMapping
    fun all() = books 

    @GetMapping("/{id}")
    fun byId(@PathVariable id: Long) = books.find{it.id == id} ?: throw NotFound(id)

    @PostMapping
    fun create(@RequestBody book: Book) : Book{
        val nextId = (books.maxOfOrNull { it.id ?: 0} ?: 0) + 1
        return book.copy(id = nextId).also{ books += it}
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(@PathVariable id: Long) = books.removeIf {it.id == id}

    @PutMapping("/{id}")
    fun put(@PathVariable id: Long, @RequestBody book: Book) : ResponseEntity<Book>{
        val index = books.indexOfFirst{it.id == id}
        if(index == -1) throw NotFound(id)

        val updated = books[index].copy(id = id)
        books[index] = updated
        return ResponseEntity.ok(updated)
    }

    @PatchMapping("/{id}")
    fun update(@PathVariable id: Long, @RequestBody req: UpdateBookRequest) : ResponseEntity<Book>{
        val index = books.indexOfFirst{it.id == id}
        if(index == -1) throw NotFound(id)

        if(req.title == null) return ResponseEntity.badRequest().build()
        
        val current = books[index]
        val updated = current.copy(title = req.title!!)

        if(updated == current){
            return ResponseEntity.noContent().build()
        }
        books[index] = updated
        return ResponseEntity.ok(updated)
    }
}