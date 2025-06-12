package com.darkcode.spring.six.services;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;

import com.darkcode.spring.six.models.entities.Inventario;
import com.darkcode.spring.six.models.entities.LoteProducto;
import com.darkcode.spring.six.models.entities.MovimientoStock;
import com.darkcode.spring.six.models.entities.MovimientoStock.MotivoMovimiento;
import com.darkcode.spring.six.models.entities.MovimientoStock.TipoMovimiento;
import com.darkcode.spring.six.models.entities.Proveedor;
import com.darkcode.spring.six.models.entities.Usuario;
import com.darkcode.spring.six.models.entities.VarianteProducto;
import com.darkcode.spring.six.models.repositories.InventarioRepository;
import com.darkcode.spring.six.models.repositories.LoteProductoRepository;
import com.darkcode.spring.six.models.repositories.MovimientoStockRepository;
import com.darkcode.spring.six.models.repositories.ProveedorRepository;
import com.darkcode.spring.six.models.repositories.UsuarioRepository;
import com.darkcode.spring.six.models.repositories.VarianteProductoRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class LoteProductoService {
    
    private final LoteProductoRepository loteRepository;
    private final VarianteProductoRepository varianteRepository;
    private final InventarioRepository inventarioRepository;
    private final MovimientoStockRepository movimientoRepository;
    private final UsuarioRepository usuarioRepository;
    private final ProveedorRepository proveedorRepository;
    private final AlertaService alertaService;
    
    // Definir umbrales de stock
    private static final int STOCK_CRITICO = 3;
    
    /**
     * Obtiene todos los lotes
     */
    public List<LoteProducto> obtenerTodosLotes() {
        return loteRepository.findAll();
    }
    
    /**
     * Obtiene un lote por su ID
     */
    public Optional<LoteProducto> obtenerPorId(Long id) {
        return loteRepository.findById(id);
    }
    
    /**
     * Obtiene los lotes de una variante específica
     */
    public List<LoteProducto> obtenerLotesPorVariante(Long varianteId) {
        return loteRepository.findByVarianteId(varianteId);
    }
    
    /**
     * Obtiene los lotes disponibles (con stock) de una variante específica ordenados por fecha de entrada (PEPS)
     */
    public List<LoteProducto> obtenerLotesDisponiblesPorVariante(Long varianteId) {
        return loteRepository.findLotesDisponiblesByVarianteIdOrderByFechaEntradaAsc(varianteId);
    }
    
    /**
     * Obtiene los lotes disponibles no vencidos de una variante específica ordenados por fecha de entrada (PEPS)
     */
    public List<LoteProducto> obtenerLotesDisponiblesNoVencidosPorVariante(Long varianteId) {
        return loteRepository.findLotesDisponiblesNoVencidosByVarianteIdOrderByFechaEntradaAsc(varianteId, LocalDate.now());
    }
    
    /**
     * Obtiene los lotes próximos a vencer (en los próximos 30 días)
     */
    public List<LoteProducto> obtenerLotesProximosAVencer() {
        LocalDate hoy = LocalDate.now();
        LocalDate fechaLimite = hoy.plusDays(30);
        return loteRepository.findLotesProximosAVencer(hoy, fechaLimite);
    }
    
    /**
     * Obtiene los lotes vencidos con stock
     */
    public List<LoteProducto> obtenerLotesVencidos() {
        return loteRepository.findLotesVencidos(LocalDate.now());
    }
    
    /**
     * Registra un nuevo lote
     */
    @Transactional
    public LoteProducto registrarLote(Long varianteId, String numeroLote, Integer cantidad, 
            BigDecimal costoUnitario, LocalDate fechaFabricacion, LocalDate fechaVencimiento, 
            Long proveedorId, String observaciones, Long usuarioId) {
        
        // Validaciones básicas
        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor que cero");
        }
        
        if (costoUnitario.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El costo unitario debe ser mayor que cero");
        }
        
        // Buscar la variante
        VarianteProducto variante = varianteRepository.findById(varianteId)
                .orElseThrow(() -> new IllegalArgumentException("Variante no encontrada"));
        
        // Buscar el usuario
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        
        // Buscar el proveedor si se proporciona un ID
        Proveedor proveedor = null;
        if (proveedorId != null) {
            proveedor = proveedorRepository.findById(proveedorId)
                    .orElseThrow(() -> new IllegalArgumentException("Proveedor no encontrado"));
        }
        
        // Generar número de lote si no se proporciona
        if (numeroLote == null || numeroLote.isEmpty()) {
            numeroLote = generarNumeroLote();
        }
        
        // Crear y guardar el lote
        LoteProducto lote = new LoteProducto();
        lote.setVariante(variante);
        lote.setNumeroLote(numeroLote);
        lote.setCantidadInicial(cantidad);
        lote.setCantidadActual(cantidad);
        lote.setCostoUnitario(costoUnitario);
        lote.setFechaEntrada(LocalDateTime.now());
        lote.setFechaFabricacion(fechaFabricacion);
        lote.setFechaVencimiento(fechaVencimiento);
        lote.setProveedor(proveedor);
        lote.setObservaciones(observaciones);
        
        LoteProducto loteGuardado = loteRepository.save(lote);
        
        // Actualizar el inventario
        Inventario inventario = inventarioRepository.findByVarianteId(varianteId)
                .orElseThrow(() -> new IllegalArgumentException("Inventario no encontrado"));
        
        int stockAnterior = inventario.getStock();
        int stockNuevo = stockAnterior + cantidad;
        
        inventario.setStock(stockNuevo);
        inventario.setUltimaActualizacion(LocalDateTime.now());
        
        // Actualizar estado de la alerta según el stock resultante
        String estadoAnterior = "";
        String estadoNuevo = "";
        
        if (stockAnterior <= STOCK_CRITICO) {
            estadoAnterior = "Crítico";
        } else if (stockAnterior <= inventario.getStockMinimo()) {
            estadoAnterior = "Bajo";
        } else {
            estadoAnterior = "Normal";
        }
        
        if (stockNuevo <= STOCK_CRITICO) {
            estadoNuevo = "Crítico";
        } else if (stockNuevo <= inventario.getStockMinimo()) {
            estadoNuevo = "Bajo";
        } else {
            estadoNuevo = "Resuelto";
        }
        
        inventarioRepository.save(inventario);
        
        // Actualizar alertas de inventario para esta variante
        int alertasActualizadas = alertaService.actualizarEstadoAlertasPorStock(
                variante, stockNuevo, inventario.getStockMinimo());
        
        // Registrar el movimiento de stock
        MovimientoStock movimiento = new MovimientoStock();
        movimiento.setVariante(variante);
        movimiento.setTipo(TipoMovimiento.ENTRADA);
        movimiento.setMotivo(MotivoMovimiento.REPOSICION);
        movimiento.setMotivoDetalle("Entrada de lote: " + numeroLote);
        movimiento.setCantidad(cantidad);
        movimiento.setUsuario(usuario);
        movimiento.setObservaciones(observaciones);
        movimiento.setFecha(LocalDateTime.now());
        movimientoRepository.save(movimiento);
        
        log.info("Lote registrado: Variante ID {} - Lote {} - Cantidad {} - Stock anterior {} - Stock nuevo {} - Estado anterior: {} - Estado nuevo: {} - Alertas actualizadas: {}", 
                varianteId, numeroLote, cantidad, stockAnterior, stockNuevo, estadoAnterior, estadoNuevo, alertasActualizadas);
        
        return loteGuardado;
    }
    
    /**
     * Disminuye el stock de un producto utilizando el método PEPS (Primero en Entrar, Primero en Salir)
     */
    @Transactional
    public List<LoteProducto> disminuirStockPEPS(Long varianteId, int cantidad, Long usuarioId, String referencia, String notas) {
        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor que cero");
        }
        
        // Buscar la variante
        VarianteProducto variante = varianteRepository.findById(varianteId)
                .orElseThrow(() -> new IllegalArgumentException("Variante no encontrada"));
        
        // Buscar el inventario
        Inventario inventario = inventarioRepository.findByVarianteId(varianteId)
                .orElseThrow(() -> new IllegalArgumentException("Inventario no encontrado"));
        
        // Verificar si hay suficiente stock
        if (inventario.getStock() < cantidad) {
            throw new IllegalArgumentException("No hay suficiente stock disponible");
        }
        
        // Buscar el usuario
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        
        // Obtener los lotes disponibles ordenados por fecha de entrada (PEPS)
        List<LoteProducto> lotesDisponibles = loteRepository.findLotesDisponiblesNoVencidosByVarianteIdOrderByFechaEntradaAsc(
                varianteId, LocalDate.now());
        
        if (lotesDisponibles.isEmpty()) {
            throw new IllegalArgumentException("No hay lotes disponibles para esta variante");
        }
        
        int cantidadRestante = cantidad;
        List<LoteProducto> lotesAfectados = new ArrayList<>();
        
        // Recorrer los lotes por orden PEPS hasta satisfacer la cantidad requerida
        for (LoteProducto lote : lotesDisponibles) {
            if (cantidadRestante <= 0) {
                break;
            }
            
            int cantidadDisponibleEnLote = lote.getCantidadActual();
            int cantidadARestar = Math.min(cantidadDisponibleEnLote, cantidadRestante);
            
            // Actualizar el lote
            lote.setCantidadActual(cantidadDisponibleEnLote - cantidadARestar);
            lote.setFechaUltimaSalida(LocalDateTime.now());
            loteRepository.save(lote);
            
            cantidadRestante -= cantidadARestar;
            lotesAfectados.add(lote);
            
            log.info("Salida de lote: Lote ID {} - Cantidad {} - Restante en lote {}", 
                    lote.getId(), cantidadARestar, lote.getCantidadActual());
        }
        
        // Si después de recorrer todos los lotes aún queda cantidad por satisfacer, lanzar error
        if (cantidadRestante > 0) {
            throw new IllegalArgumentException("No hay suficiente stock en los lotes disponibles");
        }
        
        // Actualizar el inventario
        int stockAnterior = inventario.getStock();
        int stockNuevo = stockAnterior - cantidad;
        
        inventario.setStock(stockNuevo);
        inventario.setUltimaActualizacion(LocalDateTime.now());
        inventarioRepository.save(inventario);
        
        // Registrar el movimiento de stock
        MovimientoStock movimiento = new MovimientoStock();
        movimiento.setVariante(variante);
        movimiento.setTipo(TipoMovimiento.SALIDA);
        movimiento.setMotivo(MotivoMovimiento.VENTA);
        if (referencia != null && !referencia.isEmpty()) {
            movimiento.setMotivoDetalle(referencia);
        }
        movimiento.setCantidad(cantidad);
        movimiento.setUsuario(usuario);
        movimiento.setObservaciones(notas);
        movimiento.setFecha(LocalDateTime.now());
        movimientoRepository.save(movimiento);
        
        log.info("Stock disminuido (PEPS): Variante ID {} - Cantidad {} - Stock anterior {} - Stock nuevo {}", 
                varianteId, cantidad, stockAnterior, stockNuevo);
        
        return lotesAfectados;
    }
    
    /**
     * Genera un número de lote único basado en la fecha actual y un UUID
     */
    private String generarNumeroLote() {
        LocalDate hoy = LocalDate.now();
        String fechaParte = String.format("%d%02d%02d", hoy.getYear(), hoy.getMonthValue(), hoy.getDayOfMonth());
        String uuidParte = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return "LOT-" + fechaParte + "-" + uuidParte;
    }
    
    /**
     * Obtiene el historial de lotes con información detallada
     */
    public List<Map<String, Object>> obtenerHistorialLotes() {
        List<LoteProducto> lotes = loteRepository.findAllByOrderByFechaEntradaDesc();
        List<Map<String, Object>> resultado = new ArrayList<>();
        
        for (LoteProducto lote : lotes) {
            Map<String, Object> loteInfo = new HashMap<>();
            
            // Información del lote
            loteInfo.put("id", lote.getId());
            loteInfo.put("numeroLote", lote.getNumeroLote());
            loteInfo.put("cantidadInicial", lote.getCantidadInicial());
            loteInfo.put("cantidadActual", lote.getCantidadActual());
            loteInfo.put("costoUnitario", lote.getCostoUnitario());
            loteInfo.put("fechaEntrada", lote.getFechaEntrada());
            loteInfo.put("fechaFabricacion", lote.getFechaFabricacion());
            loteInfo.put("fechaVencimiento", lote.getFechaVencimiento());
            loteInfo.put("observaciones", lote.getObservaciones());
            
            // Estado del lote
            String estado = lote.getEstado();
            loteInfo.put("estado", estado != null ? estado : "COMPLETO");
            
            // Información del producto
            if (lote.getVariante() != null) {
                VarianteProducto variante = lote.getVariante();
                loteInfo.put("productoId", variante.getId());
                loteInfo.put("sku", variante.getSku());
                loteInfo.put("talla", variante.getTalla());
                loteInfo.put("color", variante.getColor());
                
                if (variante.getProducto() != null) {
                    loteInfo.put("nombreProducto", variante.getProducto().getNombre());
                    
                    if (variante.getProducto().getCategoria() != null) {
                        loteInfo.put("categoria", variante.getProducto().getCategoria().getNombre());
                    }
                }
            }
            
            // Información del proveedor
            if (lote.getProveedor() != null) {
                loteInfo.put("proveedorId", lote.getProveedor().getId());
                loteInfo.put("proveedorNombre", lote.getProveedor().getNombre());
                loteInfo.put("proveedorRuc", lote.getProveedor().getRuc());
            }
            
            resultado.add(loteInfo);
        }
        
        return resultado;
    }
    
    /**
     * Genera un código de lote basado en la fecha actual y un identificador único
     */
    private String generarCodigoLote(VarianteProducto variante, Long id) {
        return "LOT-" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + 
                "-" + variante.getProducto().getId() + "-" + id;
    }
    
    /**
     * Busca un usuario por su nombre de usuario
     * @param username Nombre de usuario
     * @return Usuario encontrado o vacío
     */
    public Optional<Usuario> buscarUsuarioPorNombre(String username) {
        return usuarioRepository.findByUsuario(username);
    }
    
    /**
     * Procesa la devolución de un lote
     * @param loteId ID del lote a devolver
     * @param detallesIds Lista de IDs de detalles seleccionados para devolución
     * @param motivo Motivo de la devolución
     * @param comentarios Comentarios adicionales
     * @param usuarioId ID del usuario que procesa la devolución
     * @return Lote procesado
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public LoteProducto procesarDevolucionLote(Long loteId, List<Integer> detallesIds, String motivo, String comentarios, Long usuarioId) {
        try {
            // Buscar el lote
            LoteProducto lote = loteRepository.findById(loteId)
                    .orElseThrow(() -> new IllegalArgumentException("Lote no encontrado con ID: " + loteId));
            
            // Verificar que el lote no haya sido devuelto antes
            if ("DEVUELTO".equals(lote.getEstado())) {
                throw new IllegalStateException("El lote ya ha sido devuelto anteriormente");
            }
            
            // Buscar el usuario
            Usuario usuario = usuarioRepository.findById(usuarioId)
                    .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
            
            // Asegurar que el lote tenga una variante válida
            if (lote.getVariante() == null) {
                log.error("El lote no tiene una variante de producto asociada. Lote ID: {}", loteId);
                throw new IllegalStateException("El lote no tiene una variante de producto asociada");
            }
            
            // Obtener la variante del producto
            VarianteProducto variante = lote.getVariante();
            
            // Preparar el lote con cantidades válidas
            prepararLoteParaDevolucion(lote);
            
            // Cantidad a usar para la devolución (la actual)
            Integer cantidadActual = lote.getCantidadActual();
            log.info("Procesando devolución del lote #{} con cantidad: {}", loteId, cantidadActual);
            
            // Actualizar el inventario en una transacción separada
            try {
                log.info("Iniciando actualización de inventario para devolución de lote #{} con cantidad: {}", loteId, cantidadActual);
                actualizarInventarioParaDevolucion(variante.getId(), cantidadActual, loteId);
            } catch (Exception e) {
                log.error("Error al actualizar inventario para lote #{}: {}. Continuando con la devolución.", loteId, e.getMessage());
            }
            
            // Actualizar estado del lote
            lote.setEstado("DEVUELTO");
            lote.setFechaDevolucion(LocalDateTime.now());
            lote.setMotivoDevolucion(motivo != null ? motivo : "No especificado");
            lote.setComentariosDevolucion(comentarios != null ? comentarios : "");
            
            // No modificar la cantidad actual del lote, solo marcar como devuelto
            // lote.setCantidadActual(0); -- Comentado para mantener la cantidad original
            
            // Registrar movimiento de stock en una transacción separada
            try {
                registrarMovimientoParaDevolucion(variante, usuario, cantidadActual, lote.getNumeroLote(), loteId, motivo, comentarios);
            } catch (Exception e) {
                log.error("Error al registrar movimiento: {}. Continuando con la devolución.", e.getMessage());
            }
            
            // Guardar y devolver el lote actualizado
            lote = loteRepository.save(lote);
            log.info("Devolución completada exitosamente para lote #{}", loteId);
            
            return lote;
        } catch (Exception e) {
            log.error("Error general al procesar devolución de lote #{}: {}", loteId, e.getMessage(), e);
            throw e; // Re-lanzar para que sea manejada por el controlador
        }
    }
    
    /**
     * Prepara un lote para devolución asegurando cantidades válidas
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void prepararLoteParaDevolucion(LoteProducto lote) {
        // Guardamos la cantidad inicial para usarla como cantidad a devolver
        Integer cantidadInicial = lote.getCantidadInicial();
        
        // Asegurar que el lote tenga cantidades válidas
        if (cantidadInicial == null || cantidadInicial <= 0) {
            // Si no hay cantidad inicial válida, intentamos usar la cantidad actual
            if (lote.getCantidadActual() != null && lote.getCantidadActual() > 0) {
                cantidadInicial = lote.getCantidadActual();
                lote.setCantidadInicial(cantidadInicial);
                log.info("Usando cantidad actual ({}) como cantidad inicial para lote #{}", cantidadInicial, lote.getId());
            } else {
                // Si no hay cantidad válida, usamos 1 como valor por defecto
                cantidadInicial = 1;
                lote.setCantidadInicial(cantidadInicial);
                log.info("Forzando cantidad inicial válida (1) para lote #{}", lote.getId());
            }
        }
        
        // Establecemos la cantidad actual para la devolución igual a la cantidad inicial
        // De esta forma, devolvemos toda la cantidad del lote
        lote.setCantidadActual(cantidadInicial);
        log.info("Estableciendo cantidad actual igual a cantidad inicial ({}) para lote #{}", cantidadInicial, lote.getId());
        
        // Guardar lote con cantidades válidas
        loteRepository.save(lote);
        log.info("Lote guardado con cantidades válidas: cantidadActual={}, cantidadInicial={}", 
                lote.getCantidadActual(), lote.getCantidadInicial());
    }
    
    /**
     * Actualiza el inventario para la devolución de un lote
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void actualizarInventarioParaDevolucion(Long varianteId, int cantidad, Long loteId) {
        // Buscar el registro de inventario para esta variante
        Optional<Inventario> inventarioOpt = inventarioRepository.findByVarianteId(varianteId);
        
        if (!inventarioOpt.isPresent()) {
            log.warn("No se encontró registro de inventario para la variante ID: {}. Omitiendo actualización de inventario.", 
                    varianteId);
            return;
        }
        
        // Al devolver un lote, SE DEBE REDUCIR el stock del inventario,
        // ya que estamos sacando el producto del inventario y devolviéndolo al proveedor
        Inventario inventario = inventarioOpt.get();
        
        // Obtener el stock actual
        int stockActual = inventario.getStock();
        
        // Calcular el nuevo stock después de la devolución (restar la cantidad devuelta)
        int nuevoStock = stockActual - cantidad;
        
        // Si el nuevo stock sería negativo, ajustarlo a cero y registrar advertencia
        if (nuevoStock < 0) {
            log.warn("La devolución del lote #{} resultaría en stock negativo. Ajustando stock a 0.", loteId);
            nuevoStock = 0;
        }
        
        log.info("Procesando devolución de lote. Lote ID: {}, Cantidad: {}, Stock anterior: {}, Nuevo stock: {}", 
                loteId, cantidad, stockActual, nuevoStock);
                
        // Actualizar el stock en el inventario
        inventario.setStock(nuevoStock);
        inventario.setUltimaActualizacion(LocalDateTime.now());
        
        // Guardar los cambios en el inventario
        inventarioRepository.save(inventario);
        
        log.info("Inventario actualizado correctamente para devolución de lote. Variante ID: {}. Stock anterior: {}, Stock nuevo: {}", 
                varianteId, stockActual, nuevoStock);
    }
    
    /**
     * Registra un movimiento de stock para la devolución de un lote
     * Este movimiento es solo informativo, no afecta el inventario real
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void registrarMovimientoParaDevolucion(VarianteProducto variante, Usuario usuario, int cantidad, 
            String numeroLote, Long loteId, String motivo, String comentarios) {
        // Registrar movimiento de stock con validación de datos
        MovimientoStock movimiento = new MovimientoStock();
        movimiento.setFecha(LocalDateTime.now());
        
        // Usamos SALIDA como tipo de movimiento para la devolución de lote
        // aunque no afecte al inventario real, esto indica que el producto sale del sistema
        movimiento.setTipo(MovimientoStock.TipoMovimiento.SALIDA);
        
        // Set de la cantidad validada
        movimiento.setCantidad(cantidad);
        
        // Asignar la variante del lote al movimiento
        movimiento.setVariante(variante);
        movimiento.setUsuario(usuario);
        movimiento.setMotivo(MovimientoStock.MotivoMovimiento.DEVOLUCION_LOTE);
        
        // Asegurar que el detalle no sea nulo
        String detalleMotivo = "Devolución de lote #" + 
            (numeroLote != null ? numeroLote : loteId) + 
            ". Motivo: " + (motivo != null ? motivo : "No especificado");
        movimiento.setMotivoDetalle(detalleMotivo);
        
        // Establecer referencia no nula
        movimiento.setReferencia("LOTE-" + loteId);
        
        // Establecer notas y observaciones
        String notasTexto = comentarios != null ? comentarios : "";
        movimiento.setNotas(notasTexto);
        movimiento.setObservaciones("Devolución procesada el " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
        
        // Guardar el movimiento de stock
        movimientoRepository.save(movimiento);
        log.info("Movimiento de stock informativo registrado exitosamente para la devolución del lote #{} con cantidad: {}", 
                loteId, cantidad);
    }
    
    /**
     * Obtiene las devoluciones de lotes
     * @param desde Fecha desde la que filtrar (opcional)
     * @param hasta Fecha hasta la que filtrar (opcional)
     * @return Lista de devoluciones de lotes
     */
    public List<Map<String, Object>> obtenerDevolucionesLotes(LocalDate desde, LocalDate hasta) {
        // Buscar lotes devueltos
        List<LoteProducto> lotes;
        
        if (desde != null && hasta != null) {
            // Convertir LocalDate a LocalDateTime para el inicio y fin del día
            LocalDateTime fechaDesde = desde.atStartOfDay();
            LocalDateTime fechaHasta = hasta.atTime(23, 59, 59);
            lotes = loteRepository.findByEstadoAndFechaDevolucionBetween("DEVUELTO", fechaDesde, fechaHasta);
        } else {
            lotes = loteRepository.findByEstado("DEVUELTO");
        }
        
        // Ordenar por fecha descendente
        lotes.sort((l1, l2) -> {
            if (l2.getFechaDevolucion() == null) return -1;
            if (l1.getFechaDevolucion() == null) return 1;
            return l2.getFechaDevolucion().compareTo(l1.getFechaDevolucion());
        });
        
        // Convertir a mapas con la información necesaria
        List<Map<String, Object>> resultado = new ArrayList<>();
        
        for (LoteProducto lote : lotes) {
            Map<String, Object> devolucionMap = new HashMap<>();
            
            // Información básica del lote
            devolucionMap.put("id", lote.getId());
            devolucionMap.put("codigo", lote.getNumeroLote());
            devolucionMap.put("fechaRecepcion", lote.getFechaEntrada());
            devolucionMap.put("fechaDevolucion", lote.getFechaDevolucion());
            devolucionMap.put("motivoDevolucion", lote.getMotivoDevolucion());
            devolucionMap.put("comentarios", lote.getComentariosDevolucion());
            devolucionMap.put("estado", lote.getEstado());
            
            // Información del lote para la tabla
            Map<String, Object> loteInfo = new HashMap<>();
            loteInfo.put("id", lote.getId());
            loteInfo.put("codigo", lote.getNumeroLote() != null ? lote.getNumeroLote() : "LOT-" + lote.getId());
            devolucionMap.put("lote", loteInfo);
            
            // Información del proveedor
            if (lote.getProveedor() != null) {
                Map<String, Object> proveedorMap = new HashMap<>();
                proveedorMap.put("id", lote.getProveedor().getId());
                proveedorMap.put("nombre", lote.getProveedor().getNombre());
                proveedorMap.put("ruc", lote.getProveedor().getRuc());
                devolucionMap.put("proveedor", proveedorMap);
            } else {
                // Proveedor predeterminado si no hay información
                Map<String, Object> proveedorMap = new HashMap<>();
                proveedorMap.put("id", 0);
                proveedorMap.put("nombre", "No especificado");
                proveedorMap.put("ruc", "");
                devolucionMap.put("proveedor", proveedorMap);
            }
            
            // Información de productos
            List<Map<String, Object>> productos = new ArrayList<>();
            if (lote.getVariante() != null && lote.getVariante().getProducto() != null) {
                Map<String, Object> productoMap = new HashMap<>();
                productoMap.put("id", lote.getVariante().getProducto().getId());
                productoMap.put("nombre", lote.getVariante().getProducto().getNombre());
                productoMap.put("sku", lote.getVariante().getSku());
                productoMap.put("cantidad", lote.getCantidadInicial() != null ? lote.getCantidadInicial() : 0);
                productoMap.put("precioUnitario", lote.getCostoUnitario() != null ? lote.getCostoUnitario() : BigDecimal.ZERO);
                productos.add(productoMap);
            }
            devolucionMap.put("productos", productos);
            
            // Cantidad total devuelta
            int cantidad = lote.getCantidadInicial() != null ? lote.getCantidadInicial() : 0;
            devolucionMap.put("cantidad", cantidad);
            
            // Información del usuario que procesó la devolución
            String motivoDetalle = "Devolución de lote #" + (lote.getNumeroLote() != null ? lote.getNumeroLote() : lote.getId());
            List<MovimientoStock> movimientos = movimientoRepository.findByMotivoDetalleContainingAndFechaGreaterThanEqual(
                    motivoDetalle, lote.getFechaDevolucion() != null ? lote.getFechaDevolucion().minusMinutes(1) : LocalDateTime.now().minusDays(30));
            
            if (!movimientos.isEmpty()) {
                MovimientoStock movimiento = movimientos.get(0);
                if (movimiento.getUsuario() != null) {
                    Map<String, Object> usuarioMap = new HashMap<>();
                    usuarioMap.put("id", movimiento.getUsuario().getId());
                    usuarioMap.put("nombre", movimiento.getUsuario().getNombre());
                    devolucionMap.put("usuario", usuarioMap);
                    devolucionMap.put("responsable", movimiento.getUsuario().getNombre());
                }
            }
            
            // Calcular el total
            BigDecimal total = BigDecimal.ZERO;
            if (lote.getCostoUnitario() != null && lote.getCantidadInicial() != null) {
                total = lote.getCostoUnitario().multiply(BigDecimal.valueOf(lote.getCantidadInicial()));
            }
            devolucionMap.put("total", total);
            devolucionMap.put("valor", total);
            
            resultado.add(devolucionMap);
        }
        
        return resultado;
    }
    
    /**
     * Obtiene los lotes de un proveedor para devolución
     * @param proveedorId ID del proveedor
     * @return Lista de lotes del proveedor
     */
    public List<Map<String, Object>> obtenerLotesPorProveedorParaDevolucion(Long proveedorId) {
        // Buscar el proveedor
        Proveedor proveedor = proveedorRepository.findById(proveedorId)
                .orElseThrow(() -> new IllegalArgumentException("Proveedor no encontrado con ID: " + proveedorId));
        
        // Buscar lotes del proveedor que no estén devueltos
        List<LoteProducto> lotes = loteRepository.findByProveedorAndEstadoNot(proveedor, "DEVUELTO");
        
        // Convertir a mapas con la información necesaria
        List<Map<String, Object>> resultado = new ArrayList<>();
        
        for (LoteProducto lote : lotes) {
            Map<String, Object> loteMap = new HashMap<>();
            
            // Información básica del lote
            loteMap.put("id", lote.getId());
            loteMap.put("codigo", lote.getNumeroLote());
            loteMap.put("fechaRecepcion", lote.getFechaEntrada());
            loteMap.put("estado", lote.getEstado());
            
            // Información del producto
            if (lote.getVariante() != null && lote.getVariante().getProducto() != null) {
                loteMap.put("producto", lote.getVariante().getProducto().getNombre());
                loteMap.put("productoId", lote.getVariante().getProducto().getId());
                loteMap.put("sku", lote.getVariante().getSku());
            }
            
            // Calcular el total
            BigDecimal total = BigDecimal.ZERO;
            if (lote.getCostoUnitario() != null && lote.getCantidadInicial() != null) {
                total = lote.getCostoUnitario().multiply(BigDecimal.valueOf(lote.getCantidadInicial()));
            }
            loteMap.put("total", total);
            
            resultado.add(loteMap);
        }
        
        return resultado;
    }
    
    /**
     * Guarda un lote sin usar transacción (para casos de emergencia)
     */
    public LoteProducto guardarLoteSinTransaccion(LoteProducto lote) {
        log.info("Guardando lote #{} sin transacción", lote.getId());
        return loteRepository.save(lote);
    }
} 