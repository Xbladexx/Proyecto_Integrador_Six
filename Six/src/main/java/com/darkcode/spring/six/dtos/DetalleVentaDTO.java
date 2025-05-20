package com.darkcode.spring.six.dtos;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DetalleVentaDTO {
    private Long varianteId;
    private String codigo;
    private String nombre;
    private String color;
    private String talla;
    private Integer cantidad;
    private BigDecimal precioUnitario;
    private BigDecimal subtotal;
} 