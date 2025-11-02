package com.example

import org.springframework.core.MethodParameter
import org.springframework.http.MediaType
import org.springframework.http.converter.HttpMessageConverter
import org.springframework.http.server.ServerHttpRequest
import org.springframework.http.server.ServerHttpResponse
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice


@ControllerAdvice(annotations = [RestController::class])
class DeprecationV1 : ResponseBodyAdvice<Any> {

        override fun supports(returnType: MethodParameter, converterType: Class<out HttpMessageConverter<*>>): Boolean {
            return true
        }

        override fun beforeBodyWrite(
            body: Any?,
            returnType: MethodParameter,
            selectedContentType: MediaType,
            selectedConverterType: Class<out HttpMessageConverter<*>>,
            request: ServerHttpRequest,
            response: ServerHttpResponse
        ): Any? {
            if(request.uri.path.contains("/v1/")){
                response.headers.apply{
                    add("Deprecation", "true")
                    add("Sunset", "2026-01-01")
                    add("Link", "</policy>; rel=\"deprecation\"")
                }
            }            
            return body
        }
    }