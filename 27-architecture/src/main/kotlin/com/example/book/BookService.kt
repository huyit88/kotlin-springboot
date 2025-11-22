package com.example.book

import org.springframework.stereotype.Service

@Service
class BookService(private val repo: BookRepository) {
    fun getBookById(id: Long): Book? {
        return repo.findById(id)
    }
    
    fun createBook(book: Book): Book {
        return repo.save(book)
    }
}