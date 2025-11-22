package com.example.book

interface BookRepository {
    fun save(book: Book): Book
    fun findById(id: Long): Book?
}