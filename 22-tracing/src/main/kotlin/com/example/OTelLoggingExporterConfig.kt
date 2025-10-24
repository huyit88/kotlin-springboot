package com.example.tracing

import io.opentelemetry.api.OpenTelemetry
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator
import io.opentelemetry.context.propagation.ContextPropagators
import io.opentelemetry.sdk.OpenTelemetrySdk
import io.opentelemetry.sdk.trace.SdkTracerProvider
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor
import io.opentelemetry.sdk.trace.export.SpanExporter
import io.opentelemetry.sdk.trace.samplers.Sampler
import io.opentelemetry.exporter.logging.LoggingSpanExporter
import jakarta.annotation.PreDestroy
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OTelLoggingExporterConfig {

  private lateinit var provider: SdkTracerProvider

  @Bean
  fun loggingSpanExporter(): SpanExporter =
    LoggingSpanExporter.create() // prints spans to console

  @Bean
  fun openTelemetry(exporter: SpanExporter): OpenTelemetry {
    provider = SdkTracerProvider.builder()
      .setSampler(Sampler.alwaysOn())
      .addSpanProcessor(SimpleSpanProcessor.create(exporter))
      .build()

    return OpenTelemetrySdk.builder()
      .setTracerProvider(provider)
      .setPropagators(ContextPropagators.create(W3CTraceContextPropagator.getInstance()))
      .buildAndRegisterGlobal()
  }

  @PreDestroy
  fun shutdown() {
    // flush & close gracefully
    provider.shutdown()
  }
}
