package com.darkcode.spring.six.dtos;

import java.util.List;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class DashboardEmpleadoDTO {
    private Double ventasDiarias; // Total de ventas del día
    private Integer cantidadVentas; // Número de ventas realizadas
    private Long productosVendidos; // Cantidad de productos vendidos
    private Long clientesAtendidos; // Número de clientes atendidos
    private List<VentaRecienteDTO> ventasRecientes; // Lista de ventas recientes
} 