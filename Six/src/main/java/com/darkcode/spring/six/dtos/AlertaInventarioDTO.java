package com.darkcode.spring.six.dtos;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AlertaInventarioDTO {
    private Long id;
    private String estado;
    private String codigo;
    private String producto;
    private String talla;
    private String color;
    private Integer stockActual;
    private Integer stockMinimo;
    private Integer stockMaximo;
    private String categoria;
} 