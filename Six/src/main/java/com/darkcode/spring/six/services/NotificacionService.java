package com.darkcode.spring.six.services;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.darkcode.spring.six.dtos.AlertaDTO;
import com.darkcode.spring.six.models.entities.Alerta;
import com.darkcode.spring.six.models.entities.ConfiguracionAlerta;
import com.darkcode.spring.six.models.repositories.ConfiguracionAlertaRepository;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class NotificacionService {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    
    @Autowired
    private ConfiguracionAlertaRepository configuracionRepository;
    
    /**
     * Envía una notificación de alerta en tiempo real a través de WebSocket
     */
    @Async
    public void enviarNotificacionAlerta(AlertaDTO alerta) {
        log.info("Enviando notificación de alerta en tiempo real: {}", alerta.getId());
        
        // Verificar si las notificaciones push están habilitadas
        ConfiguracionAlerta config = configuracionRepository.findConfiguracionGeneral()
                .orElseGet(ConfiguracionAlerta::new);
        
        if (!config.isNotificacionesPushHabilitadas()) {
            log.info("Las notificaciones push están deshabilitadas. No se enviará la notificación.");
            return;
        }
        
        // Si solo se deben enviar alertas críticas, verificar la prioridad
        if (config.isSoloAlertasCriticas() && 
            !alerta.getPrioridad().equals(Alerta.PrioridadAlerta.ALTA) && 
            !alerta.getPrioridad().equals(Alerta.PrioridadAlerta.CRITICA)) {
            log.info("Solo se envían alertas críticas y esta alerta no es crítica.");
            return;
        }
        
        try {
            Map<String, Object> mensaje = new HashMap<>();
            mensaje.put("tipo", "NUEVA_ALERTA");
            mensaje.put("alerta", alerta);
            
            messagingTemplate.convertAndSend("/topic/alertas", mensaje);
            log.info("Notificación de alerta enviada correctamente");
        } catch (Exception e) {
            log.error("Error al enviar notificación de alerta", e);
        }
    }
    
    /**
     * Envía una notificación por email
     */
    @Async
    public void enviarNotificacionEmail(AlertaDTO alerta) {
        log.info("Enviando notificación de alerta por email: {}", alerta.getId());
        
        // Verificar si las notificaciones por email están habilitadas
        ConfiguracionAlerta config = configuracionRepository.findConfiguracionGeneral()
                .orElseGet(ConfiguracionAlerta::new);
        
        if (!config.isNotificacionesEmailHabilitadas() || config.getEmailsNotificacion() == null) {
            log.info("Las notificaciones por email están deshabilitadas o no hay destinatarios configurados.");
            return;
        }
        
        // Si solo se deben enviar alertas críticas, verificar la prioridad
        if (config.isSoloAlertasCriticas() && 
            !alerta.getPrioridad().equals(Alerta.PrioridadAlerta.ALTA) && 
            !alerta.getPrioridad().equals(Alerta.PrioridadAlerta.CRITICA)) {
            log.info("Solo se envían alertas críticas por email y esta alerta no es crítica.");
            return;
        }
        
        // Aquí iría la lógica para enviar el email
        // Por ahora solo lo registramos en el log
        log.info("Simulando envío de email para alerta {} a destinatarios: {}", 
                 alerta.getId(), config.getEmailsNotificacion());
    }
} 