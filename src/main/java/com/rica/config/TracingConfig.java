package com.rica.config;

import com.rica.tracing.CorrelationIdFilter;
import com.rica.tracing.CorrelationIdInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class TracingConfig implements WebMvcConfigurer {
    
    private final CorrelationIdInterceptor correlationIdInterceptor;

    public TracingConfig(CorrelationIdInterceptor correlationIdInterceptor) {
        this.correlationIdInterceptor = correlationIdInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(correlationIdInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns("/actuator/**");
    }
}
