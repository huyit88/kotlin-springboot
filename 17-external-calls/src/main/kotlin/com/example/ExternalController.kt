package com.example

import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/ext")
class ExternalController(
    val echoService: EchoService,
    val userService: UserService,
    val statusService: StatusService,
    val slowService: SlowService
){
    @GetMapping("/echo")
    suspend fun echo(@RequestParam foo: String): Map<String, Any>{
        return echoService.getEcho(foo)
    }

    @GetMapping("/status/{code}")
    suspend fun getStatus(@PathVariable code: Int): Map<String,String>{
        val res = statusService.getStatus(code)
        return mapOf("result" to res)
    }

    @GetMapping("/slow")
    suspend fun getSlowData(@RequestParam sec: Long): Map<String,String>{
        val res = slowService.getSlow(sec)
        return mapOf("result" to res)
    }

    @PostMapping("/users")
    suspend fun getUsers(@RequestBody req: CreateReq): CreateOut{
        return userService.create(req)
    }
}

data class CreateReq(val name: String, val email: String)
data class CreateOut(val name: String, val email: String, val echoed: Boolean)