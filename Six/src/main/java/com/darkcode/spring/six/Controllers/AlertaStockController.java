package com.darkcode.spring.six.Controllers;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.darkcode.spring.six.dtos.AlertaStockDTO;
import com.darkcode.spring.six.models.entities.Inventario;
import com.darkcode.spring.six.models.repositories.InventarioRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/alertas-stock")
@RequiredArgsConstructor
public class AlertaStockController {
    
    private final InventarioRepository inventarioRepository;
    
    @GetMapping("/critico")
    public ResponseEntity<List<AlertaStockDTO>> obtenerAlertasCriticas() {
        log.info("Obteniendo alertas de stock crítico");
        Iterable<Inventario> inventariosBajoStock = inventarioRepository.findByStockLessThanEqualStockMinimo();
        
        List<AlertaStockDTO> alertas = StreamSupport.stream(inventariosBajoStock.spliterator(), false)
            .map(inventario -> {
                return AlertaStockDTO.builder()
                    .id(inventario.getId())
                    .producto(inventario.getVariante().getProducto().getNombre())
                    .variante(String.format("%s, %s", 
                        inventario.getVariante().getColor(), 
                        inventario.getVariante().getTalla()))
                    .stockActual(inventario.getStock())
                    .stockMinimo(inventario.getStockMinimo())
                    .estado(inventario.getStock() <= 3 ? "critical" : "low")
                    .build();
            })
            .collect(Collectors.toList());
            
        return ResponseEntity.ok(alertas);
    }
} 