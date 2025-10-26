package com.example

import org.springframework.web.bind.annotation.*
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode
import org.springframework.http.ResponseEntity

@RestController
@RequestMapping("/api")
class AppController(
    val weatherService: FlakyWeatherService,
    val catalogService: CatalogService,
    val reportService: ReportService,
    val thumbnailService: ThumbnailService
    ){
    @GetMapping("/weather/{city}")
    fun getWeather(@PathVariable city: String): String{
        return weatherService.get(city)
    }

    @GetMapping("/sku/{sku}")
    fun getCatalog(@PathVariable sku: String): String{
        return catalogService.fetchSku(sku)
    }

    @GetMapping("/report/async")
    fun report(): String{
        return reportService.load().get()
    }

    @GetMapping("/thumb/{id}")
    fun thumb(@PathVariable id: String): ResponseEntity<String>{
        return try{
            ResponseEntity.ok(thumbnailService.render(id))            
        }catch(ex: Exception){
            println(ex.message)
            ResponseEntity("busy", HttpStatusCode.valueOf(HttpStatus.TOO_MANY_REQUESTS.value()))      
        }
    }
}