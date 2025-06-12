package com.darkcode.spring.six.dtos;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConfiguracionAlertaDTO {
    
    // Configuración general
    private boolean alertasStockHabilitadas = true;
    private boolean alertasVentasHabilitadas = true;
    
    // Configuración de stock
    private Integer umbralStockBajo = 10;
    private Integer umbralStockCritico = 5;
    
    // Configuración de pedidos automáticos
    private boolean pedidosAutomaticosHabilitados = false;
    private Integer umbralPedidoAutomatico = 3;
    private Integer cantidadPedidoAutomatico = 10;
    
    // Configuración de notificaciones
    private boolean notificacionesEmailHabilitadas = false;
    private boolean notificacionesPushHabilitadas = true;
    private List<String> emailsNotificacion;
    private boolean enviarDigestoDiario = false;
    private boolean soloAlertasCriticas = false;
    
    // Configuración por categoría
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ConfiguracionCategoria {
        private Long categoriaId;
        private String nombreCategoria;
        private Integer umbralStockBajo;
        private Integer umbralStockCritico;
        private boolean pedidosAutomaticosHabilitados;
        private Integer umbralPedidoAutomatico;
        private Integer cantidadPedidoAutomatico;
    }
    
    private List<ConfiguracionCategoria> configuracionesPorCategoria;
    
    // Configuración por producto
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ConfiguracionProducto {
        private Long productoId;
        private String nombreProducto;
        private Integer umbralStockBajo;
        private Integer umbralStockCritico;
        private boolean pedidosAutomaticosHabilitados;
        private Integer umbralPedidoAutomatico;
        private Integer cantidadPedidoAutomatico;
    }
    
    private List<ConfiguracionProducto> configuracionesPorProducto;
} 