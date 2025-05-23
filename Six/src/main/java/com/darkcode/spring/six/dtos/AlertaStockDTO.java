package com.darkcode.spring.six.dtos;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AlertaStockDTO {
    private Long id;
    private String producto;
    private String variante;
    private Integer stockActual;
    private Integer stockMinimo;
    private String estado;
} 