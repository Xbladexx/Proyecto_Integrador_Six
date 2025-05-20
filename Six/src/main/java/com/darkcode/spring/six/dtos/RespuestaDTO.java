package com.darkcode.spring.six.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para estandarizar las respuestas de la API
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RespuestaDTO {
    private boolean exito;
    private String mensaje;
    private Object datos;
    
    /**
     * Constructor para respuestas de éxito
     */
    public static RespuestaDTO exito(String mensaje) {
        return RespuestaDTO.builder()
                .exito(true)
                .mensaje(mensaje)
                .build();
    }
    
    /**
     * Constructor para respuestas de éxito con datos
     */
    public static RespuestaDTO exito(String mensaje, Object datos) {
        return RespuestaDTO.builder()
                .exito(true)
                .mensaje(mensaje)
                .datos(datos)
                .build();
    }
    
    /**
     * Constructor para respuestas de error
     */
    public static RespuestaDTO error(String mensaje) {
        return RespuestaDTO.builder()
                .exito(false)
                .mensaje(mensaje)
                .build();
    }
} 