package com.darkcode.spring.six.Controllers;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.darkcode.spring.six.models.entities.Inventario;
import com.darkcode.spring.six.models.entities.MovimientoStock;
import com.darkcode.spring.six.models.repositories.MovimientoStockRepository;
import com.darkcode.spring.six.services.InventarioService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/movimientos")
@RequiredArgsConstructor
@Slf4j
public class MovimientoStockController {

    private final MovimientoStockRepository movimientoRepository;
    private final InventarioService inventarioService;
    
    @GetMapping
    public ResponseEntity<List<MovimientoStock>> obtenerTodos() {
        log.info("Obteniendo lista de movimientos de stock");
        List<MovimientoStock> movimientos = movimientoRepository.findAll();
        return ResponseEntity.ok(movimientos);
    }
    
    @GetMapping("/variante/{varianteId}")
    public ResponseEntity<List<MovimientoStock>> obtenerPorVariante(@PathVariable Long varianteId) {
        log.info("Obteniendo movimientos de stock para la variante con ID: {}", varianteId);
        List<MovimientoStock> movimientos = movimientoRepository.findByVarianteIdOrderByFechaDesc(varianteId);
        return ResponseEntity.ok(movimientos);
    }
    
    @PostMapping("/entrada")
    public ResponseEntity<Inventario> registrarEntrada(
            @RequestParam Long varianteId,
            @RequestParam int cantidad,
            @RequestParam Long usuarioId,
            @RequestParam(required = false) String referencia,
            @RequestParam(required = false) String notas) {
        
        log.info("Registrando entrada de stock: Variante ID {}, Cantidad {}", varianteId, cantidad);
        
        try {
            Inventario inventario = inventarioService.aumentarStock(varianteId, cantidad, usuarioId, referencia, notas);
            return ResponseEntity.ok(inventario);
        } catch (IllegalArgumentException e) {
            log.error("Error al registrar entrada de stock: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error inesperado al registrar entrada de stock", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @PostMapping("/salida")
    public ResponseEntity<Inventario> registrarSalida(
            @RequestParam Long varianteId,
            @RequestParam int cantidad,
            @RequestParam Long usuarioId,
            @RequestParam(required = false) String referencia,
            @RequestParam(required = false) String notas) {
        
        log.info("Registrando salida de stock: Variante ID {}, Cantidad {}", varianteId, cantidad);
        
        try {
            Inventario inventario = inventarioService.disminuirStock(varianteId, cantidad, usuarioId, referencia, notas);
            return ResponseEntity.ok(inventario);
        } catch (IllegalArgumentException e) {
            log.error("Error al registrar salida de stock: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error inesperado al registrar salida de stock", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @PostMapping("/ajuste")
    public ResponseEntity<Inventario> registrarAjuste(
            @RequestParam Long varianteId,
            @RequestParam int nuevoStock,
            @RequestParam Long usuarioId,
            @RequestParam(required = false) String referencia,
            @RequestParam(required = false) String notas) {
        
        log.info("Registrando ajuste de stock: Variante ID {}, Nuevo stock {}", varianteId, nuevoStock);
        
        try {
            Inventario inventario = inventarioService.ajustarStock(varianteId, nuevoStock, usuarioId, referencia, notas);
            return ResponseEntity.ok(inventario);
        } catch (IllegalArgumentException e) {
            log.error("Error al registrar ajuste de stock: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error inesperado al registrar ajuste de stock", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
} 