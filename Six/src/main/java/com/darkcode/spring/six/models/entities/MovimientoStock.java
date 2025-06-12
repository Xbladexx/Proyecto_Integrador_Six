package com.darkcode.spring.six.models.entities;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@Table(name = "movimientos_stock")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MovimientoStock {
    
    public enum TipoMovimiento {
        ENTRADA, SALIDA
    }
    
    public enum MotivoMovimiento {
        REPOSICION, DETERIORADO, VENTA, AJUSTE, DEVOLUCION_VENTA, DEVOLUCION_LOTE, OTRO
    }
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "variante_id", nullable = false)
    private VarianteProducto variante;
    
    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;
    
    @Column(nullable = false)
    private Integer cantidad;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoMovimiento tipo;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MotivoMovimiento motivo;
    
    @Column(length = 255)
    private String motivoDetalle;
    
    @Column(nullable = false)
    private LocalDateTime fecha = LocalDateTime.now();
    
    @Column(length = 100)
    private String referencia;
    
    @Column(length = 500)
    private String notas;
    
    @Column(length = 500)
    private String observaciones;
} 