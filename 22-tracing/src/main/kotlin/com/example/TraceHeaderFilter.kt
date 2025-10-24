package com.example.tracing

import io.micrometer.tracing.Tracer
import org.slf4j.MDC
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse

@Component
class TraceHeaderFilter(
  private val tracer: Tracer
) : OncePerRequestFilter() {
  override fun doFilterInternal(
    request: HttpServletRequest,
    response: HttpServletResponse,
    chain: FilterChain
  ) {
    try {
      val traceId = tracer.currentSpan()?.context()?.traceId() ?: MDC.get("traceId")
      println("traceIdV2:" + traceId)
      if (!traceId.isNullOrBlank()) {
        response.addHeader("X-Trace-Id-v2", traceId)
      }
      chain.doFilter(request, response)
    } finally {
      
    }
  }
}
