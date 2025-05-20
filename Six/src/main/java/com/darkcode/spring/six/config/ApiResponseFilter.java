package com.darkcode.spring.six.config;

import java.io.IOException;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@Slf4j
public class ApiResponseFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain)
            throws ServletException, IOException {
        
        // Solo procesar solicitudes a la API
        String path = request.getRequestURI();
        if (path.startsWith("/api/")) {
            log.info("Procesando solicitud API: {}", path);
            
            // Asegurarse de que la respuesta tenga el tipo de contenido adecuado
            response.setContentType("application/json;charset=UTF-8");
            
            // Configurar headers CORS
            response.setHeader("Access-Control-Allow-Origin", "*");
            response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, PATCH");
            response.setHeader("Access-Control-Allow-Headers", "*");
            
            // Continuar con la cadena de filtros
            filterChain.doFilter(request, response);
        } else {
            // Para otras rutas, simplemente continuar
            filterChain.doFilter(request, response);
        }
    }
} 