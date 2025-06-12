package com.darkcode.spring.six.services;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.darkcode.spring.six.dtos.AlertaDTO;
import com.darkcode.spring.six.dtos.ConfiguracionAlertaDTO;
import com.darkcode.spring.six.models.entities.Alerta;
import com.darkcode.spring.six.models.entities.Alerta.PrioridadAlerta;
import com.darkcode.spring.six.models.entities.Alerta.TipoAlerta;
import com.darkcode.spring.six.models.entities.ConfiguracionAlerta;
import com.darkcode.spring.six.models.entities.DetalleVenta;
import com.darkcode.spring.six.models.entities.Inventario;
import com.darkcode.spring.six.models.entities.Producto;
import com.darkcode.spring.six.models.entities.VarianteProducto;
import com.darkcode.spring.six.models.repositories.AlertaRepository;
import com.darkcode.spring.six.models.repositories.CategoriaRepository;
import com.darkcode.spring.six.models.repositories.ConfiguracionAlertaRepository;
import com.darkcode.spring.six.models.repositories.InventarioRepository;
import com.darkcode.spring.six.models.repositories.ProductoRepository;
import com.darkcode.spring.six.models.repositories.UsuarioRepository;
import com.darkcode.spring.six.models.repositories.VarianteProductoRepository;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class AlertaService {

    @Autowired
    private AlertaRepository alertaRepository;
    
    @Autowired
    private ConfiguracionAlertaRepository configuracionAlertaRepository;
    
    @Autowired
    private InventarioRepository inventarioRepository;
    
    @Autowired
    private ProductoRepository productoRepository;
    
    @Autowired
    private VarianteProductoRepository varianteRepository;
    
    @Autowired
    private CategoriaRepository categoriaRepository;
    
    @Autowired
    private UsuarioRepository usuarioRepository;
    
    @Autowired
    private NotificacionService notificacionService;
    
    /**
     * Crea una nueva alerta
     */
    @Transactional
    public Alerta crearAlerta(Alerta alerta) {
        log.info("Creando nueva alerta: {}", alerta);
        
        // Guardar la alerta en la base de datos
        Alerta alertaGuardada = alertaRepository.save(alerta);
        
        // Convertir a DTO para enviar notificación
        AlertaDTO alertaDTO = convertirADTO(alertaGuardada);
        
        // Enviar notificaciones (en hilos separados)
        notificacionService.enviarNotificacionAlerta(alertaDTO);
        notificacionService.enviarNotificacionEmail(alertaDTO);
        
        return alertaGuardada;
    }
    
    /**
     * Crea una alerta de stock bajo
     */
    @Transactional
    public Alerta crearAlertaStockBajo(VarianteProducto variante, Integer stockActual, Integer umbral) {
        log.info("Creando alerta de stock bajo para variante: {}, stock actual: {}, umbral: {}", 
                variante.getId(), stockActual, umbral);
        
        Alerta alerta = new Alerta();
        alerta.setTipo(TipoAlerta.STOCK_BAJO);
        alerta.setPrioridad(PrioridadAlerta.MEDIA);
        alerta.setVariante(variante);
        alerta.setProducto(variante.getProducto());
        alerta.setStockActual(stockActual);
        alerta.setUmbral(umbral);
        
        String nombreProducto = variante.getProducto().getNombre();
        String color = variante.getColor();
        String talla = variante.getTalla();
        
        alerta.setTitulo("Stock bajo: " + nombreProducto);
        alerta.setMensaje("El producto " + nombreProducto + " (" + color + ", " + talla + ") " +
                        "tiene un stock bajo de " + stockActual + " unidades (umbral: " + umbral + ")");
        alerta.setAccionRequerida("Considere reponer el inventario de este producto.");
        
        return alertaRepository.save(alerta);
    }
    
    /**
     * Crea una alerta de stock crítico
     */
    @Transactional
    public Alerta crearAlertaStockCritico(VarianteProducto variante, Integer stockActual, Integer umbral) {
        log.info("Creando alerta de stock crítico para variante: {}, stock actual: {}, umbral: {}", 
                variante.getId(), stockActual, umbral);
        
        Alerta alerta = new Alerta();
        alerta.setTipo(TipoAlerta.STOCK_CRITICO);
        alerta.setPrioridad(PrioridadAlerta.ALTA);
        alerta.setVariante(variante);
        alerta.setProducto(variante.getProducto());
        alerta.setStockActual(stockActual);
        alerta.setUmbral(umbral);
        
        String nombreProducto = variante.getProducto().getNombre();
        String color = variante.getColor();
        String talla = variante.getTalla();
        
        alerta.setTitulo("STOCK CRÍTICO: " + nombreProducto);
        alerta.setMensaje("El stock del producto " + nombreProducto + " (" + color + ", " + talla + ") " +
                        "ha alcanzado un nivel crítico. Stock actual: " + stockActual + " unidades (umbral crítico: " + umbral + ").");
        alerta.setAccionRequerida("Es urgente reponer este producto para evitar quiebres de stock.");
        
        return alertaRepository.save(alerta);
    }
    
    /**
     * Convierte una entidad Alerta a AlertaDTO
     */
    private AlertaDTO convertirADTO(Alerta alerta) {
        AlertaDTO dto = new AlertaDTO();
        dto.setId(alerta.getId());
        dto.setTipo(alerta.getTipo());
        dto.setTitulo(alerta.getTitulo());
        dto.setMensaje(alerta.getMensaje());
        dto.setFechaCreacion(alerta.getFechaCreacion());
        dto.setFechaLectura(alerta.getFechaLectura());
        dto.setLeida(alerta.isLeida());
        dto.setPrioridad(alerta.getPrioridad());
        dto.setStockActual(alerta.getStockActual());
        dto.setUmbral(alerta.getUmbral());
        dto.setAccionRequerida(alerta.getAccionRequerida());
        
        // Información del producto si existe
        if (alerta.getProducto() != null) {
            dto.setProductoId(alerta.getProducto().getId());
            dto.setProductoNombre(alerta.getProducto().getNombre());
            dto.setProductoSku(alerta.getProducto().getCodigo());
        }
        
        // Información de la variante si existe
        if (alerta.getVariante() != null) {
            VarianteProducto variante = alerta.getVariante();
            dto.setVarianteId(variante.getId());
            dto.setVarianteColor(variante.getColor());
            dto.setVarianteTalla(variante.getTalla());
            
            // Dar prioridad al SKU de la variante si existe
            if (variante.getSku() != null && !variante.getSku().isEmpty()) {
                dto.setProductoSku(variante.getSku());
            } else if (alerta.getProducto() != null) {
                // Si no hay SKU de variante, usar el código del producto + talla + color
                String codigoProducto = alerta.getProducto().getCodigo();
                if (codigoProducto != null && !codigoProducto.isEmpty()) {
                    String talla = variante.getTalla() != null ? variante.getTalla() : "";
                    String color = variante.getColor() != null ? variante.getColor() : "";
                    dto.setProductoSku(codigoProducto + "-" + talla + "-" + color);
                }
            }
            
            if (variante.getImagenUrl() != null) {
                dto.setProductoImagen(variante.getImagenUrl());
            }
        }
        
        return dto;
    }
    
    /**
     * Obtiene todas las alertas paginadas
     */
    public Page<AlertaDTO> obtenerTodasLasAlertas(Pageable pageable) {
        log.info("Obteniendo todas las alertas paginadas");
        return alertaRepository.findAll(pageable)
                .map(this::convertirADTO);
    }
    
    /**
     * Obtiene todas las alertas no leídas
     */
    public List<AlertaDTO> obtenerAlertasNoLeidas() {
        log.info("Obteniendo todas las alertas no leídas");
        return alertaRepository.findByLeidaFalse().stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Obtiene alertas por tipo
     */
    public List<AlertaDTO> obtenerAlertasPorTipo(TipoAlerta tipo) {
        log.info("Obteniendo alertas por tipo: {}", tipo);
        return alertaRepository.findByTipo(tipo).stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Marca una alerta como leída
     */
    @Transactional
    public boolean marcarComoLeida(Long alertaId) {
        log.info("Marcando alerta como leída: {}", alertaId);
        Optional<Alerta> alertaOpt = alertaRepository.findById(alertaId);
        if (alertaOpt.isPresent()) {
            Alerta alerta = alertaOpt.get();
            alerta.setLeida(true);
            alerta.setFechaLectura(LocalDateTime.now());
            alertaRepository.save(alerta);
            return true;
        }
        return false;
    }
    
    /**
     * Marca varias alertas como leídas
     */
    @Transactional
    public int marcarComoLeidas(List<Long> alertaIds) {
        log.info("Marcando alertas como leídas: {}", alertaIds);
        return alertaRepository.marcarComoLeidas(alertaIds, LocalDateTime.now());
    }
    
    /**
     * Marca todas las alertas como leídas
     */
    @Transactional
    public int marcarTodasComoLeidas() {
        log.info("Marcando todas las alertas como leídas");
        return alertaRepository.marcarTodasComoLeidas(LocalDateTime.now());
    }
    
    /**
     * Elimina alertas leídas antiguas
     */
    @Transactional
    public int eliminarAlertasLeidasAntiguas(int dias) {
        log.info("Eliminando alertas leídas con más de {} días", dias);
        LocalDateTime fechaLimite = LocalDateTime.now().minusDays(dias);
        return alertaRepository.eliminarAlertasLeidasAnteriores(fechaLimite);
    }
    
    /**
     * Obtiene la configuración general de alertas
     */
    public ConfiguracionAlerta obtenerConfiguracionGeneral() {
        log.info("Obteniendo configuración general de alertas");
        return configuracionAlertaRepository.findConfiguracionGeneral()
                .orElseGet(() -> {
                    // Si no existe, crear una configuración por defecto
                    ConfiguracionAlerta config = new ConfiguracionAlerta();
                    return configuracionAlertaRepository.save(config);
                });
    }
    
    /**
     * Guarda la configuración general de alertas
     */
    @Transactional
    public ConfiguracionAlerta guardarConfiguracionGeneral(ConfiguracionAlertaDTO configuracionDTO) {
        log.info("Guardando configuración general de alertas");
        
        ConfiguracionAlerta config = configuracionAlertaRepository.findConfiguracionGeneral()
                .orElseGet(ConfiguracionAlerta::new);
        
        // Actualizar campos
        config.setAlertasStockHabilitadas(configuracionDTO.isAlertasStockHabilitadas());
        config.setAlertasVentasHabilitadas(configuracionDTO.isAlertasVentasHabilitadas());
        config.setUmbralStockBajo(configuracionDTO.getUmbralStockBajo());
        config.setUmbralStockCritico(configuracionDTO.getUmbralStockCritico());
        config.setPedidosAutomaticosHabilitados(configuracionDTO.isPedidosAutomaticosHabilitados());
        config.setUmbralPedidoAutomatico(configuracionDTO.getUmbralPedidoAutomatico());
        config.setCantidadPedidoAutomatico(configuracionDTO.getCantidadPedidoAutomatico());
        config.setNotificacionesEmailHabilitadas(configuracionDTO.isNotificacionesEmailHabilitadas());
        config.setNotificacionesPushHabilitadas(configuracionDTO.isNotificacionesPushHabilitadas());
        
        // Actualizar lista de emails
        if (configuracionDTO.getEmailsNotificacion() != null && !configuracionDTO.getEmailsNotificacion().isEmpty()) {
            config.setEmailsNotificacion(String.join(",", configuracionDTO.getEmailsNotificacion()));
        }
        
        config.setEnviarDigestoDiario(configuracionDTO.isEnviarDigestoDiario());
        config.setSoloAlertasCriticas(configuracionDTO.isSoloAlertasCriticas());
        config.setFechaActualizacion(LocalDateTime.now());
        
        return configuracionAlertaRepository.save(config);
    }
    
    /**
     * Notifica sobre stock insuficiente después de una venta
     * @param detalles Conjunto de detalles de venta
     * @return Lista de alertas generadas
     */
    @Transactional
    public List<AlertaDTO> notificarStockInsuficiente(Set<DetalleVenta> detalles) {
        log.info("Verificando stock bajo para {} detalles de venta", detalles.size());
        
        List<AlertaDTO> alertasGeneradas = new ArrayList<>();
        ConfiguracionAlerta configuracionGeneral = obtenerConfiguracionGeneral();
        
        if (!configuracionGeneral.isAlertasStockHabilitadas()) {
            log.info("Las alertas de stock están deshabilitadas en la configuración");
            return alertasGeneradas;
        }
        
        for (DetalleVenta detalle : detalles) {
            VarianteProducto variante = detalle.getVariante();
            if (variante == null) continue;
            
            // Buscar inventario para esta variante
            Optional<Inventario> inventarioOpt = inventarioRepository.findByVarianteId(variante.getId());
            if (inventarioOpt.isEmpty()) continue;
            
            Inventario inventario = inventarioOpt.get();
            int stockActual = inventario.getStock();
            
            // Buscar configuración específica para el producto
            Producto producto = variante.getProducto();
            if (producto == null) continue;
            
            ConfiguracionAlerta configuracionAplicable = determinarConfiguracionAplicable(producto, configuracionGeneral);
            
            // Verificar stock crítico
            if (stockActual < configuracionAplicable.getUmbralStockCritico()) {
                // Verificar si ya existe una alerta de stock crítico para esta variante
                List<Alerta> alertasCriticas = alertaRepository.findByTipoAndLeidaFalse(TipoAlerta.STOCK_CRITICO);
                boolean existeAlerta = alertasCriticas.stream()
                        .anyMatch(a -> a.getVariante() != null && a.getVariante().getId().equals(variante.getId()));
                
                if (!existeAlerta) {
                    Alerta alerta = crearAlertaStockCritico(variante, stockActual, configuracionAplicable.getUmbralStockCritico());
                    alertasGeneradas.add(convertirADTO(alerta));
                    log.info("Alerta de stock crítico generada para variante {} con stock {}", variante.getId(), stockActual);
                }
            } 
            // Verificar stock bajo (solo si no es crítico)
            else if (stockActual < configuracionAplicable.getUmbralStockBajo()) {
                // Verificar si ya existe una alerta de stock bajo para esta variante
                List<Alerta> alertasBajas = alertaRepository.findByTipoAndLeidaFalse(TipoAlerta.STOCK_BAJO);
                boolean existeAlerta = alertasBajas.stream()
                        .anyMatch(a -> a.getVariante() != null && a.getVariante().getId().equals(variante.getId()));
                
                if (!existeAlerta) {
                    Alerta alerta = crearAlertaStockBajo(variante, stockActual, configuracionAplicable.getUmbralStockBajo());
                    alertasGeneradas.add(convertirADTO(alerta));
                    log.info("Alerta de stock bajo generada para variante {} con stock {}", variante.getId(), stockActual);
                }
            }
        }
        
        log.info("Se generaron {} alertas de stock después de la venta", alertasGeneradas.size());
        return alertasGeneradas;
    }
    
    /**
     * Determina la configuración de alerta aplicable para un producto específico
     */
    private ConfiguracionAlerta determinarConfiguracionAplicable(Producto producto, ConfiguracionAlerta configuracionGeneral) {
        // Buscar configuración específica para el producto
        Optional<ConfiguracionAlerta> configProductoOpt = configuracionAlertaRepository.findByProducto(producto);
        
        // Si no hay configuración para el producto, buscar por categoría
        if (configProductoOpt.isPresent()) {
            return configProductoOpt.get();
        } else if (producto.getCategoria() != null) {
            List<ConfiguracionAlerta> configCategorias = configuracionAlertaRepository.findByCategoria(producto.getCategoria());
            if (!configCategorias.isEmpty()) {
                return configCategorias.get(0);
            }
        }
        
        // Si no hay configuración específica, usar la general
        return configuracionGeneral;
    }
    
    /**
     * Verificar stock bajo y generar alertas automáticamente
     */
    @Transactional
    public List<AlertaDTO> verificarStockBajo() {
        log.info("Verificando stock bajo para generar alertas");
        
        ConfiguracionAlerta configuracionGeneral = obtenerConfiguracionGeneral();
        
        if (!configuracionGeneral.isAlertasStockHabilitadas()) {
            log.info("Las alertas de stock están deshabilitadas en la configuración");
            return new ArrayList<>();
        }
        
        // Obtener todos los inventarios
        List<Inventario> inventarios = inventarioRepository.findAll();
        List<AlertaDTO> alertasGeneradas = new ArrayList<>();
        
        for (Inventario inventario : inventarios) {
            VarianteProducto variante = inventario.getVariante();
            
            if (variante == null) {
                continue;
            }
            
            Producto producto = variante.getProducto();
            
            if (producto == null) {
                continue;
            }
            
            int stockActual = inventario.getStock();
            
            // Buscar configuración aplicable para este producto
            ConfiguracionAlerta configuracionAplicable = determinarConfiguracionAplicable(producto, configuracionGeneral);
            
            // Verificar stock crítico
            if (stockActual < configuracionAplicable.getUmbralStockCritico()) {
                // Verificar si ya existe una alerta de stock crítico para esta variante
                List<Alerta> alertasCriticas = alertaRepository.findByTipoAndLeidaFalse(TipoAlerta.STOCK_CRITICO);
                boolean existeAlerta = alertasCriticas.stream()
                        .anyMatch(a -> a.getVariante() != null && a.getVariante().getId().equals(variante.getId()));
                
                if (!existeAlerta) {
                    Alerta alerta = crearAlertaStockCritico(variante, stockActual, configuracionAplicable.getUmbralStockCritico());
                    alertasGeneradas.add(convertirADTO(alerta));
                }
            } 
            // Verificar stock bajo (solo si no es crítico)
            else if (stockActual < configuracionAplicable.getUmbralStockBajo()) {
                // Verificar si ya existe una alerta de stock bajo para esta variante
                List<Alerta> alertasBajas = alertaRepository.findByTipoAndLeidaFalse(TipoAlerta.STOCK_BAJO);
                boolean existeAlerta = alertasBajas.stream()
                        .anyMatch(a -> a.getVariante() != null && a.getVariante().getId().equals(variante.getId()));
                
                if (!existeAlerta) {
                    Alerta alerta = crearAlertaStockBajo(variante, stockActual, configuracionAplicable.getUmbralStockBajo());
                    alertasGeneradas.add(convertirADTO(alerta));
                }
            }
        }
        
        log.info("Se generaron {} alertas de stock", alertasGeneradas.size());
        return alertasGeneradas;
    }
    
    /**
     * Actualiza el estado de las alertas de stock para una variante específica
     * @param variante La variante del producto
     * @param stockNuevo El nuevo stock después de agregar el lote
     * @param stockMinimo El stock mínimo configurado para esta variante
     * @return El número de alertas actualizadas
     */
    @Transactional
    public int actualizarEstadoAlertasPorStock(VarianteProducto variante, int stockNuevo, int stockMinimo) {
        log.info("Actualizando estado de alertas para variante ID: {} con stock nuevo: {}", variante.getId(), stockNuevo);
        
        // Constantes para los umbrales
        final int STOCK_CRITICO = 3; // Mismo valor usado en los controladores
        
        // Obtener todas las alertas de stock para esta variante
        List<Alerta> alertasExistentes = alertaRepository.findByVariante(variante).stream()
                .filter(a -> a.getTipo() == TipoAlerta.STOCK_BAJO || a.getTipo() == TipoAlerta.STOCK_CRITICO)
                .collect(Collectors.toList());
        
        if (alertasExistentes.isEmpty()) {
            log.info("No hay alertas existentes para actualizar");
            return 0;
        }
        
        int alertasActualizadas = 0;
        
        for (Alerta alerta : alertasExistentes) {
            String estadoAnterior = alerta.getTipo() == TipoAlerta.STOCK_CRITICO ? "Crítico" : "Bajo";
            
            // Determinar el nuevo estado según el stock actual
            if (stockNuevo <= STOCK_CRITICO) {
                // El stock sigue siendo crítico, no cambia
                if (alerta.getTipo() != TipoAlerta.STOCK_CRITICO) {
                    alerta.setTipo(TipoAlerta.STOCK_CRITICO);
                    alerta.setStockActual(stockNuevo);
                    alerta.setPrioridad(PrioridadAlerta.CRITICA);
                    alerta.setLeida(false); // Marcar como no leída para que aparezca como nueva
                    alerta.setTitulo("Stock CRÍTICO: " + variante.getProducto().getNombre());
                    alerta.setMensaje("El stock del producto " + variante.getProducto().getNombre() + 
                            " (" + variante.getColor() + ", " + variante.getTalla() + ") ha alcanzado un nivel crítico. " + 
                            "Stock actual: " + stockNuevo + " unidades (umbral crítico: " + STOCK_CRITICO + ").");
                    alertaRepository.save(alerta);
                    alertasActualizadas++;
                    log.info("Alerta ID {} actualizada: {} -> Crítico", alerta.getId(), estadoAnterior);
                }
            } else if (stockNuevo <= stockMinimo) {
                // El stock es bajo pero no crítico
                if (alerta.getTipo() != TipoAlerta.STOCK_BAJO) {
                    alerta.setTipo(TipoAlerta.STOCK_BAJO);
                    alerta.setStockActual(stockNuevo);
                    alerta.setPrioridad(PrioridadAlerta.ALTA);
                    alerta.setLeida(false); // Marcar como no leída para que aparezca como nueva
                    alerta.setTitulo("Stock bajo: " + variante.getProducto().getNombre());
                    alerta.setMensaje("El producto " + variante.getProducto().getNombre() + 
                            " (" + variante.getColor() + ", " + variante.getTalla() + ") tiene un stock bajo de " + 
                            stockNuevo + " unidades (umbral: " + stockMinimo + ")");
                    alertaRepository.save(alerta);
                    alertasActualizadas++;
                    log.info("Alerta ID {} actualizada: {} -> Bajo", alerta.getId(), estadoAnterior);
                }
            } else {
                // El stock ahora es normal, marcar la alerta como resuelta
                log.info("Eliminando alerta ID {} porque el stock ahora es normal: {}", alerta.getId(), stockNuevo);
                alertaRepository.delete(alerta);
                alertasActualizadas++;
            }
        }
        
        log.info("Actualizadas {} alertas para la variante ID: {}", alertasActualizadas, variante.getId());
        return alertasActualizadas;
    }
} 