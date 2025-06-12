package com.darkcode.spring.six.models.entities;

import java.math.BigDecimal;
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
@Table(name = "clasificacion_abc")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClasificacionABC {
    
    public enum Categoria {
        A, // Alto valor (80% del valor, 20% de los ítems)
        B, // Valor medio (15% del valor, 30% de los ítems)
        C  // Bajo valor (5% del valor, 50% de los ítems)
    }
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Categoria categoria;
    
    @Column(name = "valor_anual", nullable = false)
    private BigDecimal valorAnual;
    
    @Column(name = "porcentaje_valor", nullable = false)
    private BigDecimal porcentajeValor;
    
    @Column(name = "porcentaje_acumulado", nullable = false)
    private BigDecimal porcentajeAcumulado;
    
    @Column(name = "unidades_vendidas", nullable = false)
    private Integer unidadesVendidas = 0;
    
    @Column(name = "valor_ventas", nullable = false)
    private BigDecimal valorVentas = BigDecimal.ZERO;
    
    @Column(name = "fecha_calculo", nullable = false)
    private LocalDateTime fechaCalculo = LocalDateTime.now();
    
    @Column(name = "fecha_clasificacion", nullable = false)
    private LocalDateTime fechaClasificacion = LocalDateTime.now();
    
    @Column(name = "periodo_inicio")
    private LocalDateTime periodoInicio;
    
    @Column(name = "periodo_fin")
    private LocalDateTime periodoFin;
} 