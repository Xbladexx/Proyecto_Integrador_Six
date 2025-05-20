package com.darkcode.spring.six.dtos;

import java.math.BigDecimal;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VentaDTO {
    private String nombreCliente;
    private String dniCliente;
    private BigDecimal subtotal;
    private BigDecimal igv;
    private BigDecimal total;
    private List<DetalleVentaDTO> items;
} 