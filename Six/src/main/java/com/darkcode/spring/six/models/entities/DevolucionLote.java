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
@Table(name = "devoluciones_lote")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DevolucionLote {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "lote_id", nullable = false)
    private LoteProducto lote;
    
    @Column(nullable = false)
    private Integer cantidad;
    
    @Column(nullable = false)
    private String estado;
    
    @Column(name = "fecha_devolucion", nullable = false)
    private LocalDateTime fechaDevolucion = LocalDateTime.now();
    
    @Column
    private String motivo;
    
    @Column(length = 1000)
    private String comentarios;
    
    @Column(name = "valor_total", nullable = false)
    private BigDecimal valorTotal;
    
    @ManyToOne
    @JoinColumn(name = "proveedor_id")
    private Proveedor proveedor;
    
    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;
} 