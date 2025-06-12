package com.darkcode.spring.six.Controllers;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.darkcode.spring.six.dtos.ClasificacionABCDTO;
import com.darkcode.spring.six.models.entities.ClasificacionABC;
import com.darkcode.spring.six.models.entities.ClasificacionABC.Categoria;
import com.darkcode.spring.six.services.ClasificacionABCService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/clasificacion-abc")
@RequiredArgsConstructor
@Slf4j
public class ClasificacionABCController {
    
    private final ClasificacionABCService clasificacionService;
    
    /**
     * Obtiene todas las clasificaciones ABC
     */
    @GetMapping
    public ResponseEntity<List<ClasificacionABC>> obtenerTodas() {
        log.info("Obteniendo todas las clasificaciones ABC");
        List<ClasificacionABC> clasificaciones = clasificacionService.obtenerTodasClasificaciones();
        return ResponseEntity.ok(clasificaciones);
    }
    
    /**
     * Obtiene la última clasificación para cada producto
     */
    @GetMapping("/ultimas")
    public ResponseEntity<List<ClasificacionABC>> obtenerUltimas() {
        log.info("Obteniendo las últimas clasificaciones ABC");
        List<ClasificacionABC> clasificaciones = clasificacionService.obtenerUltimasClasificaciones();
        return ResponseEntity.ok(clasificaciones);
    }
    
    /**
     * Obtiene la distribución de productos por categoría ABC
     */
    @GetMapping("/distribucion")
    public ResponseEntity<Map<Categoria, Long>> obtenerDistribucion() {
        log.info("Obteniendo distribución de productos por categoría ABC");
        Map<Categoria, Long> distribucion = clasificacionService.obtenerDistribucionPorCategoria();
        return ResponseEntity.ok(distribucion);
    }
    
    /**
     * Calcula la clasificación ABC para todos los productos
     * basado en el valor de ventas en un período determinado
     */
    @PostMapping("/calcular")
    public ResponseEntity<List<ClasificacionABCDTO>> calcularClasificacionABC(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaFin) {
        
        log.info("Calculando clasificación ABC para el período {} - {}", 
                fechaInicio.format(DateTimeFormatter.ISO_DATE_TIME),
                fechaFin.format(DateTimeFormatter.ISO_DATE_TIME));
        
        List<ClasificacionABCDTO> resultados = clasificacionService.calcularClasificacionABC(fechaInicio, fechaFin);
        return ResponseEntity.ok(resultados);
    }
} 