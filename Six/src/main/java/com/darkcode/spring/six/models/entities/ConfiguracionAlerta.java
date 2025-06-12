package com.darkcode.spring.six.models.entities;

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
@Table(name = "configuracion_alertas")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConfiguracionAlerta {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    // Configuración general
    @Column(name = "alertas_stock_habilitadas", nullable = false)
    private boolean alertasStockHabilitadas = true;
    
    @Column(name = "alertas_ventas_habilitadas", nullable = false)
    private boolean alertasVentasHabilitadas = true;
    
    // Configuración de stock
    @Column(name = "umbral_stock_bajo", nullable = false)
    private Integer umbralStockBajo = 10;
    
    @Column(name = "umbral_stock_critico", nullable = false)
    private Integer umbralStockCritico = 5;
    
    // Configuración de pedidos automáticos
    @Column(name = "pedidos_automaticos_habilitados", nullable = false)
    private boolean pedidosAutomaticosHabilitados = false;
    
    @Column(name = "umbral_pedido_automatico", nullable = false)
    private Integer umbralPedidoAutomatico = 3;
    
    @Column(name = "cantidad_pedido_automatico", nullable = false)
    private Integer cantidadPedidoAutomatico = 10;
    
    // Configuración de notificaciones
    @Column(name = "notificaciones_email_habilitadas", nullable = false)
    private boolean notificacionesEmailHabilitadas = false;
    
    @Column(name = "notificaciones_push_habilitadas", nullable = false)
    private boolean notificacionesPushHabilitadas = true;
    
    @Column(name = "emails_notificacion", length = 500)
    private String emailsNotificacion;
    
    @Column(name = "enviar_digesto_diario", nullable = false)
    private boolean enviarDigestoDiario = false;
    
    @Column(name = "solo_alertas_criticas", nullable = false)
    private boolean soloAlertasCriticas = false;
    
    // Referencias a categoría o producto (opcional)
    @ManyToOne
    @JoinColumn(name = "categoria_id")
    private Categoria categoria;
    
    @ManyToOne
    @JoinColumn(name = "producto_id")
    private Producto producto;
    
    // Campos de auditoría
    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion = LocalDateTime.now();
    
    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;
    
    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;
    
    // Método para actualizar la fecha de actualización
    public void actualizarFechaActualizacion() {
        this.fechaActualizacion = LocalDateTime.now();
    }
} 