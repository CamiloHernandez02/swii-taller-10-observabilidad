package com.rica.investigadores.observabilidad;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.UUID;

@Component
public class CorrelacionIdInterceptor implements HandlerInterceptor {

    private static final String CORRELATION_ID_HEADER = "X-Correlation-ID";
    private static final ThreadLocal<String> correlationId = new ThreadLocal<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String id = request.getHeader(CORRELATION_ID_HEADER);
        
        if (id == null || id.isEmpty()) {
            id = UUID.randomUUID().toString();
        }
        
        correlationId.set(id);
        response.setHeader(CORRELATION_ID_HEADER, id);
        
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        correlationId.remove();
    }

    public static String getCorrelationId() {
        return correlationId.get();
    }
}
