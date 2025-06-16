package com.darkcode.spring.six.models.entities;

import java.math.BigDecimal;
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
@Table(name = "devoluciones_venta")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DevolucionVenta {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private Integer cantidad;
    
    @Column
    private String codigo;
    
    @Column(nullable = false)
    private String estado;
    
    @Column(name = "fecha_devolucion", nullable = false)
    private LocalDateTime fechaDevolucion = LocalDateTime.now();
    
    @Column(name = "monto_devuelto", nullable = false)
    private BigDecimal montoDevuelto;
    
    @Column
    private String motivo;
    
    @ManyToOne
    @JoinColumn(name = "detalle_venta_id", nullable = false)
    private DetalleVenta detalleVenta;
    
    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;
    
    @ManyToOne
    @JoinColumn(name = "venta_id", nullable = false)
    private Venta venta;
    
    @Column(name = "monto_total", nullable = false)
    private BigDecimal montoTotal;
    
    @Column
    private String observaciones;
} 