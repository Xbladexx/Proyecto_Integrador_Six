package com.darkcode.spring.six.Controllers;

import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.darkcode.spring.six.dtos.InventarioDTO;
import com.darkcode.spring.six.models.entities.Inventario;
import com.darkcode.spring.six.models.repositories.InventarioRepository;
import com.darkcode.spring.six.services.InventarioService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/inventario")
@RequiredArgsConstructor
@Slf4j
public class InventarioController {

    private final InventarioRepository inventarioRepository;
    private final InventarioService inventarioService;
    
    @GetMapping
    public ResponseEntity<List<Inventario>> obtenerTodos() {
        log.info("Obteniendo inventario completo");
        List<Inventario> inventario = inventarioRepository.findAll();
        return ResponseEntity.ok(inventario);
    }
    
    @GetMapping("/detalles")
    public ResponseEntity<List<InventarioDTO>> obtenerDetallesInventario() {
        log.info("Obteniendo detalles completos del inventario");
        List<InventarioDTO> detalles = inventarioService.obtenerDetallesInventario();
        return ResponseEntity.ok(detalles);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Inventario> obtenerPorId(@PathVariable Long id) {
        log.info("Obteniendo registro de inventario con ID: {}", id);
        Optional<Inventario> inventario = inventarioRepository.findById(id);
        return inventario.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
    
    @GetMapping("/variante/{varianteId}")
    public ResponseEntity<Inventario> obtenerPorVarianteId(@PathVariable Long varianteId) {
        log.info("Obteniendo inventario para la variante con ID: {}", varianteId);
        Optional<Inventario> inventario = inventarioRepository.findByVarianteId(varianteId);
        return inventario.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
    
    @PostMapping
    public ResponseEntity<Inventario> crear(@RequestBody Inventario inventario) {
        log.info("Creando nuevo registro de inventario para variante ID: {}", 
                inventario.getVariante() != null ? inventario.getVariante().getId() : null);
        if (inventario.getId() != null) {
            return ResponseEntity.badRequest().build();
        }
        Inventario nuevoInventario = inventarioRepository.save(inventario);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevoInventario);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<Inventario> actualizar(@PathVariable Long id, @RequestBody Inventario inventario) {
        log.info("Actualizando registro de inventario con ID: {}", id);
        if (!inventarioRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        inventario.setId(id);
        Inventario inventarioActualizado = inventarioRepository.save(inventario);
        return ResponseEntity.ok(inventarioActualizado);
    }
    
    @PutMapping("/{id}/stock/{cantidad}")
    public ResponseEntity<Inventario> actualizarStock(@PathVariable Long id, @PathVariable Integer cantidad) {
        log.info("Actualizando stock del inventario con ID: {} a cantidad: {}", id, cantidad);
        Optional<Inventario> inventarioOpt = inventarioRepository.findById(id);
        if (inventarioOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        Inventario inventario = inventarioOpt.get();
        inventario.setStock(cantidad);
        Inventario inventarioActualizado = inventarioRepository.save(inventario);
        return ResponseEntity.ok(inventarioActualizado);
    }
    
    // Endpoints para gestionar el stock desde la interfaz
    @PostMapping("/variante/{varianteId}/aumentar")
    public ResponseEntity<?> aumentarStock(
            @PathVariable Long varianteId,
            @RequestParam Integer cantidad,
            @RequestParam(required = false) String motivo,
            @RequestParam(required = false, defaultValue = "1") Long usuarioId) {
        
        log.info("Solicitud para aumentar stock de variante ID: {} en {} unidades", varianteId, cantidad);
        
        try {
            Inventario inventarioActualizado = inventarioService.aumentarStock(
                    varianteId, cantidad, usuarioId, motivo, "Ajuste desde interfaz de usuario");
            
            return ResponseEntity.ok(inventarioActualizado);
        } catch (Exception e) {
            log.error("Error al aumentar stock", e);
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    @PostMapping("/variante/{varianteId}/disminuir")
    public ResponseEntity<?> disminuirStock(
            @PathVariable Long varianteId,
            @RequestParam Integer cantidad,
            @RequestParam(required = false) String motivo,
            @RequestParam(required = false, defaultValue = "1") Long usuarioId) {
        
        log.info("Solicitud para disminuir stock de variante ID: {} en {} unidades", varianteId, cantidad);
        
        try {
            Inventario inventarioActualizado = inventarioService.disminuirStock(
                    varianteId, cantidad, usuarioId, motivo, "Ajuste desde interfaz de usuario");
            
            return ResponseEntity.ok(inventarioActualizado);
        } catch (Exception e) {
            log.error("Error al disminuir stock", e);
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    // Endpoints para gestionar el stock desde el inventario
    @PostMapping("/{inventarioId}/aumentar")
    public ResponseEntity<?> aumentarStockPorInventario(
            @PathVariable Long inventarioId,
            @RequestParam Integer cantidad,
            @RequestParam(required = false) String motivo,
            @RequestParam(required = false, defaultValue = "1") Long usuarioId) {
        
        log.info("Solicitud para aumentar stock de inventario ID: {} en {} unidades", inventarioId, cantidad);
        
        try {
            Optional<Inventario> inventarioOpt = inventarioRepository.findById(inventarioId);
            if (inventarioOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(com.darkcode.spring.six.dtos.RespuestaDTO.error("Inventario no encontrado"));
            }
            
            Inventario inventario = inventarioOpt.get();
            Long varianteId = inventario.getVariante().getId();
            
            Inventario inventarioActualizado = inventarioService.aumentarStock(
                    varianteId, cantidad, usuarioId, motivo, "Ajuste desde interfaz de usuario");
            
            return ResponseEntity.ok(com.darkcode.spring.six.dtos.RespuestaDTO.exito(
                    "Stock aumentado correctamente", 
                    inventarioActualizado));
        } catch (Exception e) {
            log.error("Error al aumentar stock", e);
            return ResponseEntity.badRequest()
                    .body(com.darkcode.spring.six.dtos.RespuestaDTO.error(e.getMessage()));
        }
    }
    
    @PostMapping("/{inventarioId}/disminuir")
    public ResponseEntity<?> disminuirStockPorInventario(
            @PathVariable Long inventarioId,
            @RequestParam Integer cantidad,
            @RequestParam(required = false) String motivo,
            @RequestParam(required = false, defaultValue = "1") Long usuarioId) {
        
        log.info("Solicitud para disminuir stock de inventario ID: {} en {} unidades", inventarioId, cantidad);
        
        try {
            Optional<Inventario> inventarioOpt = inventarioRepository.findById(inventarioId);
            if (inventarioOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(com.darkcode.spring.six.dtos.RespuestaDTO.error("Inventario no encontrado"));
            }
            
            Inventario inventario = inventarioOpt.get();
            Long varianteId = inventario.getVariante().getId();
            
            Inventario inventarioActualizado = inventarioService.disminuirStock(
                    varianteId, cantidad, usuarioId, motivo, "Ajuste desde interfaz de usuario");
            
            return ResponseEntity.ok(com.darkcode.spring.six.dtos.RespuestaDTO.exito(
                    "Stock reducido correctamente", 
                    inventarioActualizado));
        } catch (Exception e) {
            log.error("Error al disminuir stock", e);
            return ResponseEntity.badRequest()
                    .body(com.darkcode.spring.six.dtos.RespuestaDTO.error(e.getMessage()));
        }
    }
} 