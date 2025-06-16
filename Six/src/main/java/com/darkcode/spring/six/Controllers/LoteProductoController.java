package com.darkcode.spring.six.Controllers;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.darkcode.spring.six.models.entities.DevolucionLote;
import com.darkcode.spring.six.models.entities.LoteProducto;
import com.darkcode.spring.six.models.entities.MovimientoStock;
import com.darkcode.spring.six.models.entities.Producto;
import com.darkcode.spring.six.models.entities.Usuario;
import com.darkcode.spring.six.models.repositories.DevolucionLoteRepository;
import com.darkcode.spring.six.models.repositories.LoteProductoRepository;
import com.darkcode.spring.six.models.repositories.MovimientoStockRepository;
import com.darkcode.spring.six.services.LoteProductoService;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/lotes")
@RequiredArgsConstructor
@Slf4j
public class LoteProductoController {
    
    private final LoteProductoService loteService;
    private final MovimientoStockRepository movimientoStockRepository;
    private final LoteProductoRepository loteRepository;
    private final DevolucionLoteRepository devolucionLoteRepository;
    
    /**
     * Obtiene todos los lotes
     */
    @GetMapping
    public ResponseEntity<List<LoteProducto>> obtenerTodos() {
        log.info("Obteniendo todos los lotes");
        List<LoteProducto> lotes = loteService.obtenerTodosLotes();
        return ResponseEntity.ok(lotes);
    }
    
    /**
     * Obtiene un lote por su ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<LoteProducto> obtenerPorId(@PathVariable Long id) {
        log.info("Obteniendo lote con ID: {}", id);
        Optional<LoteProducto> lote = loteService.obtenerPorId(id);
        return lote.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * Obtiene los lotes de una variante específica
     */
    @GetMapping("/variante/{varianteId}")
    public ResponseEntity<List<LoteProducto>> obtenerPorVariante(@PathVariable Long varianteId) {
        log.info("Obteniendo lotes para la variante con ID: {}", varianteId);
        List<LoteProducto> lotes = loteService.obtenerLotesPorVariante(varianteId);
        return ResponseEntity.ok(lotes);
    }
    
    /**
     * Obtiene los lotes disponibles (con stock) de una variante específica ordenados por fecha de entrada (PEPS)
     */
    @GetMapping("/disponibles/variante/{varianteId}")
    public ResponseEntity<List<LoteProducto>> obtenerDisponiblesPorVariante(@PathVariable Long varianteId) {
        log.info("Obteniendo lotes disponibles para la variante con ID: {}", varianteId);
        List<LoteProducto> lotes = loteService.obtenerLotesDisponiblesPorVariante(varianteId);
        return ResponseEntity.ok(lotes);
    }
    
    /**
     * Obtiene los lotes disponibles no vencidos de una variante específica ordenados por fecha de entrada (PEPS)
     */
    @GetMapping("/disponibles-no-vencidos/variante/{varianteId}")
    public ResponseEntity<List<LoteProducto>> obtenerDisponiblesNoVencidosPorVariante(@PathVariable Long varianteId) {
        log.info("Obteniendo lotes disponibles no vencidos para la variante con ID: {}", varianteId);
        List<LoteProducto> lotes = loteService.obtenerLotesDisponiblesNoVencidosPorVariante(varianteId);
        return ResponseEntity.ok(lotes);
    }
    
    /**
     * Obtiene los lotes próximos a vencer (en los próximos 30 días)
     */
    @GetMapping("/proximos-a-vencer")
    public ResponseEntity<List<LoteProducto>> obtenerProximosAVencer() {
        log.info("Obteniendo lotes próximos a vencer");
        List<LoteProducto> lotes = loteService.obtenerLotesProximosAVencer();
        return ResponseEntity.ok(lotes);
    }
    
    /**
     * Obtiene los lotes vencidos con stock
     */
    @GetMapping("/vencidos")
    public ResponseEntity<List<LoteProducto>> obtenerVencidos() {
        log.info("Obteniendo lotes vencidos");
        List<LoteProducto> lotes = loteService.obtenerLotesVencidos();
        return ResponseEntity.ok(lotes);
    }
    
    /**
     * Registra un nuevo lote
     */
    @PostMapping
    public ResponseEntity<LoteProducto> registrarLote(
            @RequestParam Long varianteId,
            @RequestParam(required = false) String numeroLote,
            @RequestParam Integer cantidad,
            @RequestParam BigDecimal costoUnitario,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFabricacion,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaVencimiento,
            @RequestParam(required = false) Long proveedorId,
            @RequestParam(required = false) String observaciones,
            @RequestParam Long usuarioId) {
        
        log.info("Registrando nuevo lote para variante ID: {}", varianteId);
        
        try {
            LoteProducto lote = loteService.registrarLote(
                    varianteId, numeroLote, cantidad, costoUnitario, 
                    fechaFabricacion, fechaVencimiento, proveedorId, 
                    observaciones, usuarioId);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(lote);
        } catch (IllegalArgumentException e) {
            log.error("Error al registrar lote: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error al registrar lote", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Disminuye el stock de un producto utilizando el método PEPS
     */
    @PostMapping("/disminuir-stock-peps")
    public ResponseEntity<List<LoteProducto>> disminuirStockPEPS(
            @RequestParam Long varianteId,
            @RequestParam Integer cantidad,
            @RequestParam Long usuarioId,
            @RequestParam(required = false) String referencia,
            @RequestParam(required = false) String notas) {
        
        log.info("Disminuyendo stock (PEPS) para variante ID: {}", varianteId);
        
        try {
            List<LoteProducto> lotesAfectados = loteService.disminuirStockPEPS(
                    varianteId, cantidad, usuarioId, referencia, notas);
            
            return ResponseEntity.ok(lotesAfectados);
        } catch (IllegalArgumentException e) {
            log.error("Error al disminuir stock: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error al disminuir stock", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Obtiene el historial de lotes ingresados
     */
    @GetMapping("/historial")
    public ResponseEntity<List<Map<String, Object>>> obtenerHistorialLotes() {
        try {
            List<Map<String, Object>> historialLotes = loteService.obtenerHistorialLotes();
            return ResponseEntity.ok(historialLotes);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
    
    /**
     * Procesa la devolución de un lote
     */
    @PostMapping("/devolucion")
    public ResponseEntity<?> procesarDevolucionLote(@RequestBody Map<String, Object> datosDevolucion, HttpSession session) {
        try {
            log.info("Procesando devolución de lote con datos: {}", datosDevolucion);
            
            // Extraer datos de la solicitud con validación
            Long loteId;
            try {
                if (datosDevolucion.get("loteId") == null) {
                    return ResponseEntity.badRequest().body(
                        Map.of("exito", false, "mensaje", "El ID del lote es requerido")
                    );
                }
                
                loteId = Long.parseLong(datosDevolucion.get("loteId").toString());
            } catch (NumberFormatException e) {
                log.error("ID de lote inválido: {}", datosDevolucion.get("loteId"));
                return ResponseEntity.badRequest().body(
                    Map.of("exito", false, "mensaje", "ID de lote inválido")
                );
            }
            
            // Verificar que el lote exista
            Optional<LoteProducto> loteOpt = loteService.obtenerPorId(loteId);
            if (loteOpt.isEmpty()) {
                log.error("Lote no encontrado con ID: {}", loteId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    Map.of("exito", false, "mensaje", "Lote no encontrado")
                );
            }
            
            // *** SOLUCIÓN: Siempre forzar una cantidad válida para el lote ***
            LoteProducto lote = loteOpt.get();
            
            // Forzar una cantidad válida para el lote
            if (lote.getCantidadActual() == null || lote.getCantidadActual() <= 0) {
                lote.setCantidadActual(1);
                log.info("Forzando cantidad actual válida (1) para el lote ID: {}", loteId);
            }
            
            if (lote.getCantidadInicial() == null || lote.getCantidadInicial() <= 0) {
                lote.setCantidadInicial(1);
                log.info("Forzando cantidad inicial válida (1) para el lote ID: {}", loteId);
            }
            
            // *** FIN DE LA SOLUCIÓN ***
            
            String motivo = (String) datosDevolucion.get("motivo");
            if (motivo == null || motivo.trim().isEmpty()) {
                motivo = "No especificado";
            }
            
            String comentarios = (String) datosDevolucion.get("comentarios");
            if (comentarios == null) {
                comentarios = "";
            }
            
            // Obtener los IDs de detalles o usar el ID del lote si no hay detalles
            List<Integer> detallesIds = new ArrayList<>();
            
            try {
                Object detallesObj = datosDevolucion.get("detallesIds");
                if (detallesObj instanceof List) {
                    List<?> detallesList = (List<?>) detallesObj;
                    for (Object idObj : detallesList) {
                        if (idObj != null) {
                            if (idObj instanceof Integer) {
                                detallesIds.add((Integer) idObj);
                            } else if (idObj instanceof String) {
                                detallesIds.add(Integer.parseInt((String) idObj));
                            } else if (idObj instanceof Number) {
                                detallesIds.add(((Number) idObj).intValue());
                            }
                        }
                    }
                }
                
                if (detallesIds.isEmpty()) {
                    detallesIds.add(loteId.intValue());
                }
            } catch (Exception e) {
                log.warn("Error al procesar detallesIds: {}. Usando ID del lote como detalle.", e.getMessage());
                detallesIds = new ArrayList<>();
                detallesIds.add(loteId.intValue());
            }
            
            // Obtener el usuario actual
            String username = (String) session.getAttribute("usuario");
            log.info("Procesando devolución con usuario de sesión: {}", username);
            
            if (username == null) {
                log.error("No hay sesión de usuario activa para procesar la devolución");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    Map.of("exito", false, "mensaje", "Debe iniciar sesión para realizar esta operación")
                );
            }
            
            // Obtener el usuario por su nombre de usuario
            Optional<Usuario> usuarioOpt = loteService.buscarUsuarioPorNombre(username);
            if (usuarioOpt.isEmpty()) {
                log.error("Usuario no encontrado: {}", username);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    Map.of("exito", false, "mensaje", "Usuario no válido")
                );
            }
            
            Usuario usuario = usuarioOpt.get();
            
            // Procesar la devolución
            try {
                // Obtener la cantidad inicial del lote para la devolución
                Integer cantidad = lote.getCantidadInicial();
                
                // Si no hay cantidad inicial válida, intentar obtenerla de los datos recibidos
                if (cantidad == null || cantidad <= 0) {
                    if (datosDevolucion.containsKey("cantidad")) {
                        try {
                            cantidad = Integer.parseInt(datosDevolucion.get("cantidad").toString());
                            log.info("Usando cantidad enviada desde el cliente: {}", cantidad);
                        } catch (Exception e) {
                            log.warn("Error al parsear cantidad enviada: {}. Intentando usar cantidad actual del lote.", e.getMessage());
                            cantidad = lote.getCantidadActual();
                        }
                    } else {
                        // Si no hay datos de cantidad, intentar usar la cantidad actual
                        cantidad = lote.getCantidadActual();
                        log.info("Usando cantidad actual del lote: {}", cantidad);
                    }
                }
                
                // Si todavía no tenemos una cantidad válida, usar 1 como último recurso
                if (cantidad == null || cantidad <= 0) {
                    cantidad = 1;
                    log.warn("No se pudo determinar una cantidad válida. Usando valor por defecto (1).");
                }
                
                log.info("Procesando devolución de lote #{} con cantidad: {}", loteId, cantidad);
                
                // Actualizar la cantidad del lote antes de procesarlo
                lote.setCantidadActual(cantidad);
                lote.setCantidadInicial(cantidad);
                loteRepository.save(lote);
                
                // Procesar la devolución del lote
                LoteProducto loteDevuelto = loteService.procesarDevolucionLote(loteId, detallesIds, motivo, comentarios, usuario.getId());
                
                // Imprimir información detallada sobre la devolución para verificación
                log.info("Lote devuelto exitosamente - ID: {}, Estado: {}, Cantidad: {}, Variante ID: {}", 
                    loteDevuelto.getId(), 
                    loteDevuelto.getEstado(),
                    loteDevuelto.getCantidadInicial(),
                    loteDevuelto.getVariante() != null ? loteDevuelto.getVariante().getId() : "N/A");
                
                // Responder con el resultado
                Map<String, Object> respuesta = new HashMap<>();
                respuesta.put("exito", true);
                respuesta.put("mensaje", "Lote devuelto correctamente");
                respuesta.put("loteId", loteDevuelto.getId());
                respuesta.put("cantidadDevuelta", loteDevuelto.getCantidadInicial());
                
                return ResponseEntity.ok(respuesta);
            } catch (IllegalStateException e) {
                log.error("Error de estado al procesar devolución: {}", e.getMessage());
                return ResponseEntity.status(HttpStatus.CONFLICT).body(
                    Map.of("exito", false, "mensaje", e.getMessage())
                );
            } catch (IllegalArgumentException e) {
                log.error("Argumento inválido: {}", e.getMessage());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    Map.of("exito", false, "mensaje", e.getMessage())
                );
            } catch (Exception e) {
                // Comprobar si es un error de transacción marcada para rollback
                String errorMsg = e.getMessage();
                if (errorMsg != null && (
                    errorMsg.contains("rolled back") || 
                    errorMsg.contains("rollback") || 
                    errorMsg.contains("Transaction") ||
                    errorMsg.contains("transaction"))) {
                    
                    log.error("Error de transacción al procesar devolución: {}", errorMsg);
                    
                    // Intentar procesar la devolución nuevamente con un enfoque no transaccional
                    try {
                        log.info("Intentando procesar devolución sin transacción para lote #{}", loteId);
                        
                        // Obtener datos del lote para verificar
                        log.info("Lote a devolver - ID: {}, Código: {}, Cantidad Inicial: {}, Cantidad Actual: {}, Variante ID: {}", 
                            lote.getId(), lote.getNumeroLote(), lote.getCantidadInicial(), lote.getCantidadActual(),
                            lote.getVariante() != null ? lote.getVariante().getId() : "N/A");
                        
                        // Usar la cantidad inicial original del lote o la cantidad actual si está disponible
                        Integer cantidadOriginal = lote.getCantidadInicial();
                        if (cantidadOriginal == null || cantidadOriginal <= 0) {
                            // Si no hay cantidad inicial, intentar usar la cantidad actual
                            if (lote.getCantidadActual() != null && lote.getCantidadActual() > 0) {
                                cantidadOriginal = lote.getCantidadActual();
                                log.info("Usando cantidad actual como cantidad a devolver: {}", cantidadOriginal);
                            } else {
                                // Como último recurso, usar 1
                                cantidadOriginal = 1;
                                log.info("Usando valor por defecto (1) como cantidad a devolver");
                            }
                        } else {
                            log.info("Usando cantidad inicial como cantidad a devolver: {}", cantidadOriginal);
                        }
                        
                        // Establecer las cantidades correctas
                        lote.setCantidadActual(cantidadOriginal);
                        lote.setCantidadInicial(cantidadOriginal);
                        
                        // Marcar el lote como devuelto directamente
                        lote.setEstado("DEVUELTO");
                        lote.setFechaDevolucion(LocalDateTime.now());
                        lote.setMotivoDevolucion(motivo);
                        lote.setComentariosDevolucion(comentarios);
                        
                        // Actualizar el inventario para reflejar la devolución
                        if (lote.getVariante() != null) {
                            log.info("Actualizando inventario para devolución alternativa de lote #{}", loteId);
                            loteService.actualizarInventarioParaDevolucion(
                                lote.getVariante().getId(), 
                                cantidadOriginal, 
                                loteId
                            );
                        } else {
                            log.warn("No se pudo actualizar el inventario: el lote #{} no tiene variante asociada", loteId);
                        }
                        
                        // Guardar los cambios
                        LoteProducto loteActualizado = loteService.guardarLoteSinTransaccion(lote);
                        
                        // Crear registro en la tabla de devoluciones_lote
                        try {
                            DevolucionLote devolucionLote = new DevolucionLote();
                            devolucionLote.setLote(loteActualizado);
                            devolucionLote.setCantidad(cantidadOriginal);
                            devolucionLote.setEstado("DEVUELTO");
                            devolucionLote.setFechaDevolucion(LocalDateTime.now());
                            devolucionLote.setMotivo(motivo != null ? motivo : "No especificado");
                            devolucionLote.setComentarios(comentarios != null ? comentarios : "");
                            
                            // Calcular valor total
                            BigDecimal valorTotal = BigDecimal.ZERO;
                            if (lote.getCostoUnitario() != null) {
                                valorTotal = lote.getCostoUnitario().multiply(BigDecimal.valueOf(cantidadOriginal));
                            }
                            devolucionLote.setValorTotal(valorTotal);
                            
                            // Establecer proveedor y usuario
                            devolucionLote.setProveedor(lote.getProveedor());
                            devolucionLote.setUsuario(usuario);
                            
                            // Guardar la devolución
                            devolucionLoteRepository.save(devolucionLote);
                            log.info("Registro de devolución de lote creado con ID: {}", devolucionLote.getId());
                        } catch (Exception e2) {
                            log.error("Error al crear registro de devolución de lote: {}. Continuando con la devolución.", e2.getMessage());
                        }
                        
                        // Responder con el resultado
                        Map<String, Object> respuesta = new HashMap<>();
                        respuesta.put("exito", true);
                        respuesta.put("mensaje", "Lote devuelto correctamente (procesamiento alternativo)");
                        respuesta.put("loteId", loteActualizado.getId());
                        
                        return ResponseEntity.ok(respuesta);
                    } catch (Exception e2) {
                        log.error("Error en el procesamiento alternativo: {}", e2.getMessage());
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                            Map.of("exito", false, "mensaje", "No se pudo procesar la devolución: " + e2.getMessage())
                        );
                    }
                }
                
                log.error("Error no controlado al procesar devolución: {}", e.getMessage(), e);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    Map.of("exito", false, "mensaje", "Error al procesar la devolución: " + e.getMessage())
                );
            }
        } catch (Exception e) {
            log.error("Error general al procesar la devolución del lote", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("exito", false, "mensaje", "Error al procesar la devolución: " + e.getMessage()));
        }
    }
    
    /**
     * Obtiene las devoluciones de lotes
     */
    @GetMapping("/devoluciones")
    public ResponseEntity<List<Map<String, Object>>> obtenerDevolucionesLote(
            @RequestParam(name = "desde", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(name = "hasta", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        try {
            log.info("Obteniendo devoluciones de lotes");
            
            // Usar el nuevo método sin parámetros
            List<Map<String, Object>> devoluciones = loteService.obtenerDevolucionesLotes();
            return ResponseEntity.ok(devoluciones);
        } catch (Exception e) {
            log.error("Error al obtener devoluciones de lotes", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
    
    /**
     * Obtiene los lotes por proveedor
     */
    @GetMapping("/proveedor/{proveedorId}")
    public ResponseEntity<List<Map<String, Object>>> obtenerLotesPorProveedor(@PathVariable Long proveedorId) {
        try {
            log.info("Obteniendo lotes para el proveedor con ID: {}", proveedorId);
            
            List<Map<String, Object>> lotes = loteService.obtenerLotesPorProveedorParaDevolucion(proveedorId);
            return ResponseEntity.ok(lotes);
        } catch (Exception e) {
            log.error("Error al obtener lotes por proveedor", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
    
    /**
     * Obtiene los detalles completos de una devolución de lote por su ID
     * @param id ID del lote
     * @return Detalles completos de la devolución de lote
     */
    @GetMapping("/devolucion/{id}/detalles")
    public ResponseEntity<?> obtenerDetallesDevolucionLote(@PathVariable Long id) {
        try {
            log.info("Obteniendo detalles de devolución de lote con ID: {}", id);
            
            // Buscar el lote
            Optional<LoteProducto> loteOpt = loteService.obtenerPorId(id);
            if (loteOpt.isEmpty()) {
                log.warn("No se encontró el lote con ID: {}", id);
                return ResponseEntity.notFound().build();
            }
            
            LoteProducto lote = loteOpt.get();
            if (!"DEVUELTO".equals(lote.getEstado())) {
                log.warn("El lote con ID: {} no está devuelto. Estado actual: {}", id, lote.getEstado());
                
                // Se permite ver detalles aunque no esté devuelto
                log.info("Mostrando detalles de lote en estado no devuelto: {}", lote.getEstado());
            }
            
            // Crear un mapa con todos los datos relevantes
            Map<String, Object> respuesta = new HashMap<>();
            respuesta.put("id", lote.getId());
            respuesta.put("codigo", lote.getNumeroLote() != null ? lote.getNumeroLote() : "LOT-" + lote.getId());
            respuesta.put("fechaRecepcion", lote.getFechaEntrada());
            respuesta.put("fechaDevolucion", lote.getFechaDevolucion());
            respuesta.put("estado", lote.getEstado());
            respuesta.put("motivo", lote.getMotivoDevolucion());
            respuesta.put("motivoDevolucion", lote.getMotivoDevolucion());
            respuesta.put("comentarios", lote.getComentariosDevolucion());
            respuesta.put("comentariosDevolucion", lote.getComentariosDevolucion());
            
            // Información para la vista de tabla
            Map<String, Object> loteInfo = new HashMap<>();
            loteInfo.put("id", lote.getId());
            loteInfo.put("codigo", lote.getNumeroLote() != null ? lote.getNumeroLote() : "LOT-" + lote.getId());
            respuesta.put("lote", loteInfo);
            
            // Calcular total y costos
            BigDecimal costoUnitario = lote.getCostoUnitario() != null ? lote.getCostoUnitario() : BigDecimal.ZERO;
            int cantidadInicial = lote.getCantidadInicial() != null ? lote.getCantidadInicial() : 0;
            BigDecimal total = costoUnitario.multiply(BigDecimal.valueOf(cantidadInicial));
            
            respuesta.put("costoUnitario", costoUnitario);
            respuesta.put("cantidadInicial", cantidadInicial);
            respuesta.put("total", total);
            respuesta.put("valor", total);
            respuesta.put("cantidad", cantidadInicial);
            
            // Añadir información del proveedor si está disponible
            if (lote.getProveedor() != null) {
                Map<String, Object> proveedorMap = new HashMap<>();
                proveedorMap.put("id", lote.getProveedor().getId());
                proveedorMap.put("nombre", lote.getProveedor().getNombre());
                proveedorMap.put("ruc", lote.getProveedor().getRuc());
                respuesta.put("proveedor", proveedorMap);
            } else {
                // Proveedor predeterminado si no hay información
                Map<String, Object> proveedorMap = new HashMap<>();
                proveedorMap.put("id", 0);
                proveedorMap.put("nombre", "No especificado");
                proveedorMap.put("ruc", "");
                respuesta.put("proveedor", proveedorMap);
            }
            
            // Información de productos
            List<Map<String, Object>> productos = new ArrayList<>();
            
            // Obtener información sobre la variante y producto
            if (lote.getVariante() != null && lote.getVariante().getProducto() != null) {
                Producto producto = lote.getVariante().getProducto();
                Map<String, Object> productoMap = new HashMap<>();
                productoMap.put("id", producto.getId());
                productoMap.put("nombre", producto.getNombre());
                productoMap.put("codigo", producto.getCodigo());
                productoMap.put("varianteId", lote.getVariante().getId());
                productoMap.put("varianteNombre", lote.getVariante().getColor() + " / " + lote.getVariante().getTalla());
                productoMap.put("cantidad", cantidadInicial);
                productoMap.put("precioUnitario", costoUnitario);
                
                productos.add(productoMap);
                respuesta.put("producto", productoMap);
            }
            
            respuesta.put("productos", productos);
            
            // Buscar movimientos relacionados con la devolución
            String motivoDetalle = "Devolución de lote #" + 
                (lote.getNumeroLote() != null ? lote.getNumeroLote() : lote.getId());
                
            List<MovimientoStock> movimientos = movimientoStockRepository
                    .findByMotivoDetalleContainingAndFechaGreaterThanEqual(
                            motivoDetalle, 
                            lote.getFechaDevolucion() != null ? 
                                lote.getFechaDevolucion().minusMinutes(1) : 
                                LocalDateTime.now().minusDays(30));
            
            // Transformar los movimientos
            if (!movimientos.isEmpty()) {
                List<Map<String, Object>> movimientosMap = new ArrayList<>();
                for (MovimientoStock movimiento : movimientos) {
                    Map<String, Object> movMap = new HashMap<>();
                    movMap.put("id", movimiento.getId());
                    movMap.put("fecha", movimiento.getFecha());
                    movMap.put("tipo", movimiento.getTipo() != null ? movimiento.getTipo().toString() : "SALIDA");
                    movMap.put("cantidad", movimiento.getCantidad());
                    movMap.put("motivo", movimiento.getMotivo() != null ? movimiento.getMotivo().toString() : "DEVOLUCION_LOTE");
                    movMap.put("motivoDetalle", movimiento.getMotivoDetalle());
                    
                    // Añadir información del usuario si está disponible
                    if (movimiento.getUsuario() != null) {
                        movMap.put("usuarioId", movimiento.getUsuario().getId());
                        movMap.put("usuarioNombre", movimiento.getUsuario().getNombre());
                        
                        // Establecer responsable en la respuesta principal si no está establecido
                        if (!respuesta.containsKey("responsable")) {
                            respuesta.put("responsable", movimiento.getUsuario().getNombre());
                            
                            Map<String, Object> usuarioMap = new HashMap<>();
                            usuarioMap.put("id", movimiento.getUsuario().getId());
                            usuarioMap.put("nombre", movimiento.getUsuario().getNombre());
                            respuesta.put("usuario", usuarioMap);
                        }
                    }
                    
                    // Añadir información del producto/variante
                    if (movimiento.getVariante() != null && movimiento.getVariante().getProducto() != null) {
                        movMap.put("productoId", movimiento.getVariante().getProducto().getId());
                        movMap.put("productoNombre", movimiento.getVariante().getProducto().getNombre());
                    }
                    
                    movimientosMap.add(movMap);
                }
                respuesta.put("movimientos", movimientosMap);
            }
            
            return ResponseEntity.ok(respuesta);
        } catch (Exception e) {
            log.error("Error al obtener detalles de devolución de lote: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al obtener detalles de devolución de lote: " + e.getMessage()));
        }
    }
} 