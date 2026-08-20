package com.quickbite.gatewayservice.config;

import io.micrometer.tracing.Tracer;
import io.micrometer.tracing.brave.bridge.BraveTracer;
import io.micrometer.tracing.brave.bridge.BraveCurrentTraceContext;
import brave.Tracing;
import brave.propagation.StrictCurrentTraceContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TracingConfig {

    @Bean
    public Tracing braveTracing() {
        return Tracing.newBuilder()
                .currentTraceContext(new StrictCurrentTraceContext())
                .build();
    }

    @Bean
    public Tracer tracer(Tracing tracing) {
        return new BraveTracer(
                tracing.tracer(),
                new BraveCurrentTraceContext(tracing.currentTraceContext()),
                null
        );
    }
}