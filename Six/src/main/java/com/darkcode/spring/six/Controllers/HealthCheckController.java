package com.darkcode.spring.six.Controllers;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.darkcode.spring.six.models.repositories.CategoriaRepository;
import com.darkcode.spring.six.models.repositories.ProductoRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/health")
@RequiredArgsConstructor
@Slf4j
public class HealthCheckController {

    private final CategoriaRepository categoriaRepository;
    private final ProductoRepository productoRepository;

    @GetMapping
    public ResponseEntity<Map<String, Object>> health() {
        log.info("Verificando estado de la API");
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Verificar acceso a los repositorios
            long countCategorias = categoriaRepository.count();
            long countProductos = productoRepository.count();
            
            response.put("status", "UP");
            response.put("message", "API funcionando correctamente");
            response.put("categorias", countCategorias);
            response.put("productos", countProductos);
            
            log.info("API funcionando correctamente. Categorías: {}, Productos: {}", 
                    countCategorias, countProductos);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error al verificar estado de la API", e);
            
            response.put("status", "DOWN");
            response.put("message", "Error al verificar estado de la API: " + e.getMessage());
            
            return ResponseEntity.status(500).body(response);
        }
    }
} 