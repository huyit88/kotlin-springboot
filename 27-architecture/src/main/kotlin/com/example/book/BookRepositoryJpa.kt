package com.example.book

import jakarta.annotation.PostConstruct
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface BookJpaRepository : JpaRepository<Book, Long>

@Repository
class BookRepositoryJpa(
    private val jpaRepository: BookJpaRepository
) : BookRepository {
    override fun save(book: Book): Book = jpaRepository.save(book)
    override fun findById(id: Long): Book? = jpaRepository.findById(id).orElse(null)
    
    @PostConstruct
    fun init() {
        save(Book(title = "Clean Code", author = "Robert Martin"))
        save(Book(title = "Effective Java", author = "Joshua Bloch"))
    }
}