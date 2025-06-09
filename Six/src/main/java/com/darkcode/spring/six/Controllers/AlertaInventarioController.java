package com.darkcode.spring.six.Controllers;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.darkcode.spring.six.dtos.AlertaInventarioDTO;
import com.darkcode.spring.six.models.entities.Inventario;
import com.darkcode.spring.six.services.InventarioService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/alertas-inventario")
@RequiredArgsConstructor
@Slf4j
public class AlertaInventarioController {

    private final InventarioService inventarioService;
    
    // Umbral de stock crítico: cuando es igual o menor a este valor
    private static final int STOCK_CRITICO = 3;

    @GetMapping
    public ResponseEntity<List<AlertaInventarioDTO>> obtenerAlertasInventario() {
        try {
            log.info("Obteniendo todas las alertas de inventario");
            
            List<Inventario> inventarios = inventarioService.obtenerTodoInventario();
            
            List<AlertaInventarioDTO> alertas = inventarios.stream()
                .filter(inv -> inv.getVariante() != null && 
                             inv.getVariante().getProducto() != null && 
                             inv.getVariante().getProducto().getCategoria() != null)
                .map(inv -> {
                    String estado;
                    if (inv.getStock() <= STOCK_CRITICO) {
                        estado = "Crítico";
                    } else if (inv.getStock() <= inv.getStockMinimo()) {
                        estado = "Bajo";
                    } else {
                        estado = "Resuelto";
                    }

                    return AlertaInventarioDTO.builder()
                        .id(inv.getId())
                        .estado(estado)
                        .codigo(inv.getVariante().getSku())
                        .producto(inv.getVariante().getProducto().getNombre())
                        .talla(inv.getVariante().getTalla())
                        .color(inv.getVariante().getColor())
                        .stockActual(inv.getStock())
                        .stockMinimo(inv.getStockMinimo())
                        .stockMaximo(inv.getStockMaximo())
                        .categoria(inv.getVariante().getProducto().getCategoria().getNombre())
                        .build();
                })
                .collect(Collectors.toList());

            log.info("Se encontraron {} registros de inventario", alertas.size());
            return ResponseEntity.ok(alertas);
            
        } catch (Exception e) {
            log.error("Error al obtener alertas de inventario: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
} 