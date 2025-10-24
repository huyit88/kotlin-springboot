package com.example

import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerInterceptor
import org.springframework.web.servlet.ModelAndView
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import io.micrometer.tracing.Tracer
import org.slf4j.MDC

@Component
class TraceInterceptor(
    val tracer: Tracer
) : HandlerInterceptor{
    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
        // Logic to execute before the handler (controller method) is called
        val traceId = tracer.currentSpan()?.context()?.traceId() ?: MDC.get("traceId")
        println("traceId:" + traceId)
        if (!traceId.isNullOrBlank()) {
            response.addHeader("X-Trace-Id", traceId)
        }
        return true
    }

    override fun postHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any, modelAndView: ModelAndView?) {
        
    }

    override fun afterCompletion(request: HttpServletRequest, response: HttpServletResponse, handler: Any, ex: Exception?) {
        
    }
}