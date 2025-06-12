package com.darkcode.spring.six.models.entities;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "lotes_producto")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoteProducto {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "variante_id", nullable = false)
    private VarianteProducto variante;
    
    @Column(nullable = false)
    private String numeroLote;
    
    @Column(nullable = false)
    private Integer cantidadInicial;
    
    @Column(nullable = false)
    private Integer cantidadActual;
    
    @Column(nullable = false)
    private BigDecimal costoUnitario;
    
    @Column(name = "fecha_entrada", nullable = false)
    private LocalDateTime fechaEntrada = LocalDateTime.now();
    
    @Column(name = "fecha_fabricacion")
    private LocalDate fechaFabricacion;
    
    @Column(name = "fecha_vencimiento")
    private LocalDate fechaVencimiento;
    
    @Column(name = "fecha_ultima_salida")
    private LocalDateTime fechaUltimaSalida;
    
    @ManyToOne
    @JoinColumn(name = "proveedor_id")
    private Proveedor proveedor;
    
    @Column
    private String observaciones;
    
    @Column(name = "estado")
    private String estado = "ACTIVO";
    
    @Column(name = "fecha_devolucion")
    private LocalDateTime fechaDevolucion;
    
    @Column(name = "motivo_devolucion")
    private String motivoDevolucion;
    
    @Column(name = "comentarios_devolucion", length = 1000)
    private String comentariosDevolucion;
    
    // Método para saber si el lote está próximo a deteriorarse (180 días)
    public boolean isProximoAVencer() {
        // Si no hay fecha de vencimiento, calculamos basado en la fecha de entrada
        if (fechaVencimiento == null) {
            // Para productos textiles, consideramos 180 días (6 meses) como tiempo de deterioro
            LocalDate fechaEstimadaDeterioracion = fechaEntrada.toLocalDate().plusDays(180);
            LocalDate fechaLimite = LocalDate.now().plusDays(30);
            return fechaEstimadaDeterioracion.isBefore(fechaLimite);
        }
        
        // Si hay fecha de vencimiento explícita, usamos esa
        LocalDate fechaLimite = LocalDate.now().plusDays(30);
        return fechaVencimiento.isBefore(fechaLimite);
    }
    
    // Método para saber si el lote está deteriorado
    public boolean isVencido() {
        // Si no hay fecha de vencimiento, calculamos basado en la fecha de entrada
        if (fechaVencimiento == null) {
            // Para productos textiles, consideramos 180 días (6 meses) como tiempo de deterioro
            LocalDate fechaEstimadaDeterioracion = fechaEntrada.toLocalDate().plusDays(180);
            return fechaEstimadaDeterioracion.isBefore(LocalDate.now());
        }
        
        // Si hay fecha de vencimiento explícita, usamos esa
        return fechaVencimiento.isBefore(LocalDate.now());
    }
} 