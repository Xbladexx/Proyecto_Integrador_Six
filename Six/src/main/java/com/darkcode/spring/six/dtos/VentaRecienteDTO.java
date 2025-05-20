package com.darkcode.spring.six.dtos;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class VentaRecienteDTO {
    private String clienteNombre;
    private String clienteInicial;
    private String productoDescripcion;
    private BigDecimal monto;
    private LocalDateTime fecha;
    private List<String> productos = new ArrayList<>();
} 