package com.example.catalog

import org.springframework.stereotype.Service

@Service
class CatalogService(private val catalogRepo: CatalogRepository) {
    fun get(id: Long): Catalog? {
        return catalogRepo.get(id)
    }
}