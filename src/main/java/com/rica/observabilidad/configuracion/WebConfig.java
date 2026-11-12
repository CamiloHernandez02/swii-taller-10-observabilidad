package com.rica.observabilidad.configuracion;

import com.rica.observabilidad.correlacion.CorrelacionIdInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    private final CorrelacionIdInterceptor correlacionIdInterceptor;

    public WebConfig(CorrelacionIdInterceptor correlacionIdInterceptor) {
        this.correlacionIdInterceptor = correlacionIdInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(correlacionIdInterceptor);
    }
}
