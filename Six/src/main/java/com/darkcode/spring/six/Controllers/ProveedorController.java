package com.darkcode.spring.six.Controllers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.darkcode.spring.six.models.entities.Proveedor;
import com.darkcode.spring.six.services.ProveedorService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/proveedores")
@RequiredArgsConstructor
@Slf4j
public class ProveedorController {
    
    private final ProveedorService proveedorService;
    
    /**
     * Obtiene todos los proveedores
     */
    @GetMapping
    public ResponseEntity<List<Proveedor>> getAllProveedores() {
        List<Proveedor> proveedores = proveedorService.findAll();
        return ResponseEntity.ok(proveedores);
    }
    
    /**
     * Obtiene un proveedor por su ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getProveedorById(@PathVariable Long id) {
        try {
            Proveedor proveedor = proveedorService.findById(id);
            return ResponseEntity.ok(proveedor);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", "Proveedor no encontrado");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }
    
    /**
     * Busca proveedores por nombre
     */
    @GetMapping("/buscar")
    public ResponseEntity<List<Proveedor>> buscarProveedores(@RequestParam(required = false) String nombre,
                                                          @RequestParam(required = false) String ruc) {
        List<Proveedor> proveedores;
        
        if (nombre != null && !nombre.isEmpty()) {
            proveedores = proveedorService.findByNombreContaining(nombre);
        } else if (ruc != null && !ruc.isEmpty()) {
            proveedores = proveedorService.findByRucContaining(ruc);
        } else {
            proveedores = proveedorService.findAll();
        }
        
        return ResponseEntity.ok(proveedores);
    }
    
    /**
     * Crea un nuevo proveedor
     */
    @PostMapping
    public ResponseEntity<?> createProveedor(@RequestBody Proveedor proveedor) {
        try {
            Proveedor nuevoProveedor = proveedorService.save(proveedor);
            return ResponseEntity.status(HttpStatus.CREATED).body(nuevoProveedor);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", "Error al crear el proveedor: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }
    
    /**
     * Actualiza un proveedor existente
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateProveedor(@PathVariable Long id, @RequestBody Proveedor proveedor) {
        try {
            proveedor.setId(id);
            Proveedor proveedorActualizado = proveedorService.update(proveedor);
            return ResponseEntity.ok(proveedorActualizado);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", "Error al actualizar el proveedor: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }
    
    /**
     * Cambia el estado de activación de un proveedor
     */
    @PutMapping("/{id}/estado")
    public ResponseEntity<?> cambiarEstadoProveedor(@PathVariable Long id, @RequestBody Map<String, Boolean> estado) {
        try {
            Boolean activo = estado.get("activo");
            if (activo == null) {
                Map<String, String> response = new HashMap<>();
                response.put("error", "El parámetro 'activo' es requerido");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            Proveedor proveedor = proveedorService.findById(id);
            proveedor.setActivo(activo);
            Proveedor proveedorActualizado = proveedorService.update(proveedor);
            return ResponseEntity.ok(proveedorActualizado);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", "Error al cambiar el estado del proveedor: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }
    
    /**
     * Elimina un proveedor
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProveedor(@PathVariable Long id) {
        try {
            proveedorService.deleteById(id);
            Map<String, String> response = new HashMap<>();
            response.put("mensaje", "Proveedor eliminado correctamente");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", "Error al eliminar el proveedor: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }
} 