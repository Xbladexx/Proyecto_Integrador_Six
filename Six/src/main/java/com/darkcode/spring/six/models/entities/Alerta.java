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
@Table(name = "alertas")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Alerta {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoAlerta tipo;
    
    @Column(nullable = false, length = 100)
    private String titulo;
    
    @Column(nullable = false, length = 500)
    private String mensaje;
    
    @Column(nullable = false)
    private LocalDateTime fechaCreacion = LocalDateTime.now();
    
    @Column
    private LocalDateTime fechaLectura;
    
    @Column(nullable = false)
    private boolean leida = false;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PrioridadAlerta prioridad;
    
    @ManyToOne
    @JoinColumn(name = "producto_id")
    private Producto producto;
    
    @ManyToOne
    @JoinColumn(name = "variante_id")
    private VarianteProducto variante;
    
    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;
    
    // Campos adicionales para contexto
    @Column(name = "stock_actual")
    private Integer stockActual;
    
    @Column(name = "umbral")
    private Integer umbral;
    
    @Column(name = "accion_requerida", length = 200)
    private String accionRequerida;
    
    // Enumeraciones
    public enum TipoAlerta {
        STOCK_BAJO,
        STOCK_CRITICO,
        PEDIDO_AUTOMATICO,
        PRODUCTO_SIN_MOVIMIENTO,
        SISTEMA
    }
    
    public enum PrioridadAlerta {
        BAJA,
        MEDIA,
        ALTA,
        CRITICA
    }
} 