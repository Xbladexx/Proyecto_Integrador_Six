package com.darkcode.spring.six.services;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.darkcode.spring.six.models.entities.Venta;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class WebSocketService {
    
    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper;
    
    /**
     * Envía una notificación de nueva venta a todos los clientes conectados
     * @param venta La venta nueva a notificar
     */
    public void notificarNuevaVenta(Venta venta) {
        try {
            // Crear mensaje con tipo y datos
            String mensaje = objectMapper.writeValueAsString(
                    new WebSocketMessage<>("NUEVA_VENTA", venta)
            );
            
            // Enviar a todos los clientes suscritos al topic de ventas
            messagingTemplate.convertAndSend("/topic/ventas", mensaje);
            log.info("Notificación WebSocket enviada: nueva venta {}", venta.getCodigo());
        } catch (Exception e) {
            log.error("Error al enviar notificación WebSocket de nueva venta", e);
        }
    }
    
    /**
     * Envía una notificación de actualización de venta a todos los clientes conectados
     * @param venta La venta actualizada a notificar
     */
    public void notificarActualizacionVenta(Venta venta) {
        try {
            // Crear mensaje con tipo y datos
            String mensaje = objectMapper.writeValueAsString(
                    new WebSocketMessage<>("ACTUALIZAR_VENTA", venta)
            );
            
            // Enviar a todos los clientes suscritos al topic de ventas
            messagingTemplate.convertAndSend("/topic/ventas", mensaje);
            log.info("Notificación WebSocket enviada: actualización de venta {}", venta.getCodigo());
        } catch (Exception e) {
            log.error("Error al enviar notificación WebSocket de actualización de venta", e);
        }
    }
    
    /**
     * Clase para estructurar los mensajes WebSocket
     */
    private static class WebSocketMessage<T> {
        @JsonProperty("tipo")
        private String tipo;
        
        @JsonProperty("datos")
        private T datos;
        
        public WebSocketMessage(String tipo, T datos) {
            this.tipo = tipo;
            this.datos = datos;
        }
    }
} 