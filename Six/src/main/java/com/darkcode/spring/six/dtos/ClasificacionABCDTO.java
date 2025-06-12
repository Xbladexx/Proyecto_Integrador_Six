package com.darkcode.spring.six.dtos;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClasificacionABCDTO {
    private Long productoId;
    private String codigo;
    private String nombre;
    private String categoria; // A, B, o C
    private BigDecimal valorAnual;
    private BigDecimal porcentajeValor;
    private BigDecimal porcentajeAcumulado;
} 