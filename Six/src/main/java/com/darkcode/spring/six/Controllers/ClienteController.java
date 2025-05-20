package com.darkcode.spring.six.Controllers;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.darkcode.spring.six.models.entities.Cliente;
import com.darkcode.spring.six.models.repositories.ClienteRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/clientes")
@RequiredArgsConstructor
@Slf4j
public class ClienteController {

    private final ClienteRepository clienteRepository;
    
    /**
     * Obtiene todos los clientes
     */
    @GetMapping
    public ResponseEntity<List<Cliente>> obtenerTodos() {
        log.info("Obteniendo lista de clientes");
        List<Cliente> clientes = clienteRepository.findAll();
        return ResponseEntity.ok(clientes);
    }
    
    /**
     * Busca un cliente por su DNI
     */
    @GetMapping("/buscar")
    public ResponseEntity<?> buscarPorDNI(@RequestParam String dni) {
        log.info("Buscando cliente con DNI: {}", dni);
        
        if (dni == null || dni.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("El DNI es requerido");
        }
        
        try {
            Optional<Cliente> clienteOpt = clienteRepository.findByDni(dni);
            
            if (clienteOpt.isPresent()) {
                Cliente cliente = clienteOpt.get();
                log.info("Cliente encontrado: {}", cliente.getNombre());
                return ResponseEntity.ok(cliente);
            } else {
                log.info("No se encontró cliente con DNI: {}", dni);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Cliente no encontrado");
            }
        } catch (Exception e) {
            log.error("Error al buscar cliente por DNI: {}", dni, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al buscar cliente: " + e.getMessage());
        }
    }
    
    /**
     * Crea un nuevo cliente con fecha y hora peruana sin milisegundos
     */
    @PostMapping
    public ResponseEntity<?> crearCliente(@RequestBody Cliente cliente) {
        log.info("Creando nuevo cliente: {}", cliente.getNombre());
        
        try {
            // Validar datos
            if (cliente.getDni() == null || cliente.getDni().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("El DNI es requerido");
            }
            
            if (cliente.getNombre() == null || cliente.getNombre().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("El nombre es requerido");
            }
            
            // Verificar si ya existe un cliente con ese DNI
            if (clienteRepository.existsByDni(cliente.getDni())) {
                return ResponseEntity.badRequest().body("Ya existe un cliente con ese DNI");
            }
            
            // Configurar fecha y hora en zona horaria de Perú sin milisegundos
            ZoneId zonaPeruana = ZoneId.of("America/Lima");
            LocalDateTime fechaHoraPeruana = LocalDateTime.now(zonaPeruana).withNano(0);
            cliente.setFechaRegistro(fechaHoraPeruana);
            
            Cliente clienteGuardado = clienteRepository.save(cliente);
            log.info("Cliente creado con ID: {} y fecha de registro: {}", clienteGuardado.getId(), fechaHoraPeruana);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(clienteGuardado);
        } catch (Exception e) {
            log.error("Error al crear cliente", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al crear cliente: " + e.getMessage());
        }
    }
} 