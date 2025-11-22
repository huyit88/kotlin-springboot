package com.example.catalog

import jakarta.annotation.PostConstruct
import org.springframework.stereotype.Repository

@Repository
class CatalogRepository {
    private val storage = mutableMapOf<Long, Catalog>()
    
    @PostConstruct
    fun init() {
        storage[1L] = Catalog(1L, "Laptop")
        storage[2L] = Catalog(2L, "Mouse")
    }
    
    fun get(id: Long): Catalog? {
        return storage[id]
    }
}