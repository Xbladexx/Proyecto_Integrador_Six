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
import com.darkcode.spring.six.services.InventarioService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/alertas-stock")
@RequiredArgsConstructor
@Slf4j
public class AlertaStockController {

    private final InventarioService inventarioService;
    private static final int STOCK_CRITICO = 3;

    @GetMapping("/critico")
    public ResponseEntity<List<AlertaStockDTO>> obtenerAlertasCriticas() {
        try {
            log.info("Obteniendo alertas de stock crítico (3 unidades o menos)");
            
            // Obtener inventario con stock crítico usando el servicio
            List<Inventario> inventarioCritico = StreamSupport
                .stream(inventarioService.obtenerStockBajo().spliterator(), false)
                .filter(inv -> inv.getStock() <= STOCK_CRITICO)
                .collect(Collectors.toList());

            if (inventarioCritico.isEmpty()) {
                log.info("No se encontraron productos con stock crítico");
                return ResponseEntity.ok(List.of());
            }

            List<AlertaStockDTO> alertas = inventarioCritico.stream()
                .filter(inv -> inv.getVariante() != null && inv.getVariante().getProducto() != null)
                .map(inv -> {
                    try {
                        String nombreProducto = inv.getVariante().getProducto().getNombre();
                        String color = inv.getVariante().getColor();
                        String talla = inv.getVariante().getTalla();
                        
                        return AlertaStockDTO.builder()
                            .id(inv.getId())
                            .producto(nombreProducto)
                            .variante(String.format("%s, %s", color, talla))
                            .stockActual(inv.getStock())
                            .stockMinimo(STOCK_CRITICO)
                            .estado("critical")
                            .build();
                    } catch (Exception e) {
                        log.error("Error al procesar alerta para inventario ID {}: {}", inv.getId(), e.getMessage());
                        return null;
                    }
                })
                .filter(alerta -> alerta != null)
                .collect(Collectors.toList());

            log.info("Se encontraron {} productos con stock crítico", alertas.size());
            return ResponseEntity.ok(alertas);
            
        } catch (Exception e) {
            log.error("Error al obtener alertas de stock crítico: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
} 