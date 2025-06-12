package com.darkcode.spring.six.Controllers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
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

import com.darkcode.spring.six.dtos.AlertaDTO;
import com.darkcode.spring.six.dtos.ConfiguracionAlertaDTO;
import com.darkcode.spring.six.models.entities.Alerta.TipoAlerta;
import com.darkcode.spring.six.models.entities.ConfiguracionAlerta;
import com.darkcode.spring.six.services.AlertaService;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/alertas")
@Slf4j
public class AlertaController {

    @Autowired
    private AlertaService alertaService;
    
    /**
     * Obtiene todas las alertas paginadas
     */
    @GetMapping
    public ResponseEntity<Page<AlertaDTO>> obtenerAlertas(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "fechaCreacion") String sort,
            @RequestParam(defaultValue = "DESC") String direction) {
        
        Sort.Direction dir = direction.equalsIgnoreCase("ASC") ? Sort.Direction.ASC : Sort.Direction.DESC;
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(dir, sort));
        
        Page<AlertaDTO> alertas = alertaService.obtenerTodasLasAlertas(pageRequest);
        return ResponseEntity.ok(alertas);
    }
    
    /**
     * Obtiene las alertas no leídas
     */
    @GetMapping("/no-leidas")
    public ResponseEntity<List<AlertaDTO>> obtenerAlertasNoLeidas() {
        List<AlertaDTO> alertas = alertaService.obtenerAlertasNoLeidas();
        return ResponseEntity.ok(alertas);
    }
    
    /**
     * Obtiene alertas por tipo
     */
    @GetMapping("/tipo/{tipo}")
    public ResponseEntity<List<AlertaDTO>> obtenerAlertasPorTipo(@PathVariable TipoAlerta tipo) {
        List<AlertaDTO> alertas = alertaService.obtenerAlertasPorTipo(tipo);
        return ResponseEntity.ok(alertas);
    }
    
    /**
     * Marca una alerta como leída
     */
    @PutMapping("/{alertaId}/marcar-leida")
    public ResponseEntity<?> marcarComoLeida(@PathVariable Long alertaId) {
        boolean resultado = alertaService.marcarComoLeida(alertaId);
        
        if (resultado) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Marca varias alertas como leídas
     */
    @PutMapping("/marcar-leidas")
    public ResponseEntity<?> marcarComoLeidas(@RequestBody List<Long> alertaIds) {
        int cantidad = alertaService.marcarComoLeidas(alertaIds);
        
        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("cantidadActualizada", cantidad);
        
        return ResponseEntity.ok(respuesta);
    }
    
    /**
     * Marca todas las alertas como leídas
     */
    @PutMapping("/marcar-todas-leidas")
    public ResponseEntity<?> marcarTodasComoLeidas() {
        int cantidad = alertaService.marcarTodasComoLeidas();
        
        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("cantidadActualizada", cantidad);
        
        return ResponseEntity.ok(respuesta);
    }
    
    /**
     * Elimina alertas leídas antiguas
     */
    @PostMapping("/eliminar-leidas-antiguas")
    public ResponseEntity<?> eliminarAlertasLeidasAntiguas(@RequestParam(defaultValue = "30") int dias) {
        int cantidad = alertaService.eliminarAlertasLeidasAntiguas(dias);
        
        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("cantidadEliminada", cantidad);
        
        return ResponseEntity.ok(respuesta);
    }
    
    /**
     * Obtiene la configuración general de alertas
     */
    @GetMapping("/configuracion")
    public ResponseEntity<ConfiguracionAlerta> obtenerConfiguracion() {
        ConfiguracionAlerta configuracion = alertaService.obtenerConfiguracionGeneral();
        return ResponseEntity.ok(configuracion);
    }
    
    /**
     * Guarda la configuración general de alertas
     */
    @PutMapping("/configuracion")
    public ResponseEntity<?> guardarConfiguracion(@RequestBody ConfiguracionAlertaDTO configuracionDTO) {
        try {
            ConfiguracionAlerta configuracion = alertaService.guardarConfiguracionGeneral(configuracionDTO);
            return ResponseEntity.ok(configuracion);
        } catch (Exception e) {
            log.error("Error al guardar configuración de alertas", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al guardar configuración: " + e.getMessage());
        }
    }
    
    /**
     * Verifica stock bajo y genera alertas automáticamente
     */
    @PostMapping("/verificar-stock")
    public ResponseEntity<?> verificarStockBajo() {
        List<AlertaDTO> alertasGeneradas = alertaService.verificarStockBajo();
        
        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("alertasGeneradas", alertasGeneradas.size());
        respuesta.put("alertas", alertasGeneradas);
        
        return ResponseEntity.ok(respuesta);
    }
} 