package com.example

import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import org.springframework.http.ResponseEntity
import org.springframework.http.HttpHeaders
import java.net.URI
import java.nio.file.*
import org.springframework.core.io.FileSystemResource
import org.springframework.core.io.Resource
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody


@RestController
@RequestMapping("/api/files")
class FileController{
    @PostMapping
    fun createFile(@RequestParam("file") file: MultipartFile):ResponseEntity<String>{
        val uploadsDir = Paths.get("uploads")
        if (!Files.exists(uploadsDir)) {
            Files.createDirectories(uploadsDir)
        }
        val path = Path.of("uploads/${file.originalFilename}")
        Files.copy(file.inputStream, path)
        return ResponseEntity.created(URI.create("/files/${file.originalFilename}")).build()
    }

    @PostMapping("/many")
    fun uploadMany(@RequestParam("files") files: List<MultipartFile>):List<String>{
        val uploadsDir = Paths.get("uploads")
        if (!Files.exists(uploadsDir)) {
            Files.createDirectories(uploadsDir)
        }
        val uploadedFiles = files.map{file->
            val path = Path.of("uploads/${file.originalFilename}")
            Files.copy(file.inputStream, path)
            file.originalFilename
        }
        return uploadedFiles
    }

    @GetMapping("/{name}")
    fun download(@PathVariable name: String): ResponseEntity<Resource>{
        val path = Path.of("uploads/$name")
        val resource = FileSystemResource(path)
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=$name")
            .body(resource)
    }

    @GetMapping("/stream/{name}")
    fun streamLargeFile(@PathVariable name: String): ResponseEntity<StreamingResponseBody>{
        val path = Path.of("uploads/$name")
        val stream = StreamingResponseBody { out ->
            Files.newInputStream(path).use { it.copyTo(out) }
        }
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=$name")
            .body(stream)
    }


}