package com.example.catalog

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api")
class CatalogController(private val catalogSvc: CatalogService) {
    @GetMapping("/catalog/{id}")
    fun get(@PathVariable id: Long): ResponseEntity<Catalog> {
        val catalog = catalogSvc.get(id)
        return if (catalog != null) {
            ResponseEntity.ok(catalog)
        } else {
            ResponseEntity.notFound().build()
        }
    }
}