package com.darkcode.spring.six.dtos;

import java.math.BigDecimal;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductoVarianteDTO {
    private Long id;
    private String codigo;
    private String nombre;
    private String color;
    private String talla;
    private BigDecimal precio;
    private Long varianteId;
    private String sku;
    private List<String> tallasDisponibles;
    private Integer stock;
} 