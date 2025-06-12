package com.darkcode.spring.six.dtos;

import java.time.LocalDateTime;

import com.darkcode.spring.six.models.entities.Alerta.PrioridadAlerta;
import com.darkcode.spring.six.models.entities.Alerta.TipoAlerta;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AlertaDTO {
    private Long id;
    private TipoAlerta tipo;
    private String titulo;
    private String mensaje;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaLectura;
    private boolean leida;
    private PrioridadAlerta prioridad;
    private Long productoId;
    private String productoNombre;
    private String productoSku;
    private String productoImagen;
    private Long varianteId;
    private String varianteColor;
    private String varianteTalla;
    private Integer stockActual;
    private Integer umbral;
    private String accionRequerida;
    
    // Constructor adicional con campos principales
    public AlertaDTO(Long id, TipoAlerta tipo, String titulo, String mensaje, 
                     LocalDateTime fechaCreacion, boolean leida, PrioridadAlerta prioridad) {
        this.id = id;
        this.tipo = tipo;
        this.titulo = titulo;
        this.mensaje = mensaje;
        this.fechaCreacion = fechaCreacion;
        this.leida = leida;
        this.prioridad = prioridad;
    }
} 