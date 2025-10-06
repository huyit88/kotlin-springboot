package com.example


import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springdoc.core.models.GroupedOpenApi
import org.springdoc.core.customizers.OpenApiCustomizer

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.info.Contact
import io.swagger.v3.oas.models.media.Content
import io.swagger.v3.oas.models.media.MediaType
import io.swagger.v3.oas.models.media.Schema
import io.swagger.v3.oas.models.responses.ApiResponse


@Configuration
class OpenApiConfiguration{
    @Bean
    fun apiInfo(): OpenAPI = OpenAPI().info(
            Info()
                .title("Sample Service API")
                .version("v1")
                .description("Endpoints for users, orders")
                .contact(Contact().name("API Owners").email("api@example.com")))
    
    @Bean
    fun groupUsers(addCommonErrors: OpenApiCustomizer): GroupedOpenApi{
        return GroupedOpenApi.builder()
            .group("users").pathsToMatch("/api/users/**")
            .addOpenApiCustomizer(addCommonErrors)
            .build()
    }

    @Bean
    fun groupOrders(addCommonErrors: OpenApiCustomizer): GroupedOpenApi{
        return GroupedOpenApi.builder()
            .group("orders").pathsToMatch("/api/orders/**")
            .addOpenApiCustomizer(addCommonErrors)
            .build()
    }

    @Bean
    fun groupPublic(addCommonErrors: OpenApiCustomizer): GroupedOpenApi{
        return GroupedOpenApi.builder()
            .group("public").pathsToMatch("/api/**")
            .addOpenApiCustomizer(addCommonErrors)
            .build()
    }

    @Bean
    fun addCommonErrors(): OpenApiCustomizer = OpenApiCustomizer { openApi ->
        openApi.paths?.values?.forEach { pathItem ->
            pathItem.readOperations().forEach { operation ->
                if (!operation.responses.containsKey("500")) {
                    val content = Content().addMediaType(
                        "application/problem+json",
                        MediaType().schema(Schema<Any>().`$ref`("#/components/schemas/ProblemDetail"))
                    )
                    operation.responses.addApiResponse(
                        "500",
                        ApiResponse().description("Internal error").content(content)
                    )
                }
            }
        }
    }
}