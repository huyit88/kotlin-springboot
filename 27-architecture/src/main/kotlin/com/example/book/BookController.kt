package com.example.book

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.net.URI

@RestController
@RequestMapping("/api")
class BookController(
    private val bookService: BookService
) {
    @GetMapping("/books/{id}")
    fun getBook(@PathVariable id: Long): ResponseEntity<Book> {
        val book = bookService.getBookById(id)
        return if (book != null) {
            ResponseEntity.ok(book)
        } else {
            ResponseEntity.notFound().build()
        }
    }

    @PostMapping("/books")
    fun createBook(@RequestBody book: Book): ResponseEntity<Book> {
        val savedBook = bookService.createBook(book)
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .location(URI.create("/api/books/${savedBook.id}"))
            .body(savedBook)
    }
}