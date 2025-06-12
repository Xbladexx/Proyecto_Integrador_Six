package com.darkcode.spring.six.dtos;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventarioDTO {
    private Long id;
    private Long varianteId;
    private Long productoId;
    private String sku;
    private String nombreProducto;
    private String nombreCategoria;
    private String color;
    private String talla;
    private Integer stock;
    private BigDecimal precio;
} 