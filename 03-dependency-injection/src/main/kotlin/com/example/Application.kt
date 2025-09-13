package com.exmaple.dependencyinjection

import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import org.springframework.stereotype.Service
import org.springframework.stereotype.Repository
import org.springframework.context.annotation.Primary
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.http.ResponseEntity

@SpringBootApplication
class DependencyInjectionApplication

interface MessageFormatter{
    fun format(raw: String): String
}

@Component
@Primary
class PlainFormatter : MessageFormatter{
    override fun format(raw: String) = raw
}

@Component("shoutFormatter")
class ShoutFormatter: MessageFormatter{
    override fun format(raw: String) = raw.uppercase() + "!"
}

@Service
class GreetRepository{
    fun template() = "Hello %s"
}

@Repository
class GreetService(
    val repo: GreetRepository,
    val mf: MessageFormatter
){
    fun greet(name: String): String = mf.format(repo.template().format(name))
}

interface Rule{
    fun apply(s: String): String
}

@Component
@Order(1)
class TrimRule : Rule{
    override fun apply(s: String) = s.trim()
}

@Component
@Order(2)
class CollapseSpaceRule : Rule{
    override fun apply(s: String) = s.replace(Regex("\\s+"), " ")
}

@Component
@Order(3)
class UppercaseRule : Rule{
    override fun apply(s: String) = s.uppercase()
}

@Service
class NormalizationPipeline(val rules: List<Rule>, val repo: GreetRepository){
    fun greet(name: String): String{
        val normalized = rules.fold(name){ acc, el -> el.apply(acc) }
        return repo.template().format(normalized).uppercase()
    }
}

@Service
class FormatRegister(
    val mfs: Map<String, MessageFormatter>, 
    val repo: GreetRepository
){
    fun greet(strategy: String, name: String): String?{
        val fmt = mfs[strategy] ?: return null
        val msg = repo.template().format(name)
        return fmt.format(msg)
    }
}

fun main(args: Array<String>){
    runApplication<DependencyInjectionApplication>(*args);
}

@RestController
@RequestMapping("/api")
class GreetingController(
    val service: GreetService,
    val repo: GreetRepository,
    @Qualifier("shoutFormatter") val smf: MessageFormatter,
    val np: NormalizationPipeline,
    val registry: FormatRegister
){
    @GetMapping("/greet")
    fun greet(@RequestParam name: String) = mapOf("message" to service.greet(name))

    @GetMapping("/greet/loud")
    fun greetLoud(@RequestParam name: String) = mapOf("message" to smf.format(repo.template().format(name)))

    @GetMapping("/greet/normalize")
    fun greetNormalize(@RequestParam name: String) = mapOf("message" to np.greet(name))

    @GetMapping("/greet/by-name")
    fun greetByName(@RequestParam strategy: String, @RequestParam name: String) : ResponseEntity<Map<String,String>>{
        val result = registry.greet(strategy, name)
            ?: return ResponseEntity.badRequest()
                .body(mapOf("error" to "Unknown formatter: $strategy"))
        return ResponseEntity.ok(mapOf("message" to result))
    }

}

