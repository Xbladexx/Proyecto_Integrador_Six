package com.darkcode.spring.six.Controllers;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.darkcode.spring.six.dtos.DetalleVentaDTO;
import com.darkcode.spring.six.dtos.VentaDTO;
import com.darkcode.spring.six.models.entities.Alerta;
import com.darkcode.spring.six.models.entities.Cliente;
import com.darkcode.spring.six.models.entities.DetalleVenta;
import com.darkcode.spring.six.models.entities.Inventario;
import com.darkcode.spring.six.models.entities.MovimientoStock;
import com.darkcode.spring.six.models.entities.Producto;
import com.darkcode.spring.six.models.entities.Usuario;
import com.darkcode.spring.six.models.entities.VarianteProducto;
import com.darkcode.spring.six.models.entities.Venta;
import com.darkcode.spring.six.models.repositories.ClienteRepository;
import com.darkcode.spring.six.models.repositories.DetalleVentaRepository;
import com.darkcode.spring.six.models.repositories.InventarioRepository;
import com.darkcode.spring.six.models.repositories.MovimientoStockRepository;
import com.darkcode.spring.six.models.repositories.UsuarioRepository;
import com.darkcode.spring.six.models.repositories.VarianteProductoRepository;
import com.darkcode.spring.six.models.repositories.VentaRepository;
import com.darkcode.spring.six.services.AlertaService;
import com.darkcode.spring.six.services.WebSocketService;

import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/ventas")
@RequiredArgsConstructor
@Slf4j
public class VentaController {

    private final ClienteRepository clienteRepository;
    private final VentaRepository ventaRepository;
    private final DetalleVentaRepository detalleVentaRepository;
    private final VarianteProductoRepository varianteRepository;
    private final InventarioRepository inventarioRepository;
    private final UsuarioRepository usuarioRepository;
    private final WebSocketService webSocketService;
    private final MovimientoStockRepository movimientoStockRepository;
    private final AlertaService alertaService;

    
    /**
     * Endpoint para verificar la conexión a la base de datos
     */
    @GetMapping("/verificar-conexion")
    public ResponseEntity<?> verificarConexion() {
        try {
            log.info("Verificando conexión a la base de datos");
            
            // Verificar si podemos acceder a la base de datos
            long cantidadVentas = ventaRepository.count();
            log.info("Conexión exitosa. Total de ventas en la base de datos: {}", cantidadVentas);
            
            // Crear respuesta con información útil
            Map<String, Object> respuesta = new HashMap<>();
            respuesta.put("estado", "OK");
            respuesta.put("mensaje", "Conexión exitosa a la base de datos");
            respuesta.put("totalVentas", cantidadVentas);
            
            return ResponseEntity.ok(respuesta);
        } catch (Exception e) {
            log.error("Error al verificar conexión a la base de datos", e);
            
            Map<String, Object> respuesta = new HashMap<>();
            respuesta.put("estado", "ERROR");
            respuesta.put("mensaje", "Error al conectar con la base de datos: " + e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(respuesta);
        }
    }
    
    /**
     * Obtiene todas las ventas con filtro opcional por fechas
     */
    @GetMapping
    public ResponseEntity<?> obtenerVentas(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate hasta) {
        try {
            log.info("Obteniendo ventas con filtros - desde: {}, hasta: {}", desde, hasta);
            
            List<Venta> ventas;
            
            if (desde != null && hasta != null) {
                // Convertir LocalDate a LocalDateTime para el inicio y fin del día
                LocalDateTime fechaDesde = desde.atStartOfDay();
                LocalDateTime fechaHasta = hasta.atTime(23, 59, 59);
                log.info("Buscando ventas entre {} y {}", fechaDesde, fechaHasta);
                ventas = ventaRepository.findByFechaBetween(fechaDesde, fechaHasta);
            } else {
                ventas = ventaRepository.findAll();
            }
            
            // Ordenar por fecha descendente (más recientes primero)
            ventas.sort((v1, v2) -> v2.getFecha().compareTo(v1.getFecha()));
            
            // Cargar detalles para cada venta
            List<Map<String, Object>> ventasConDetalles = new ArrayList<>();
            
            for (Venta venta : ventas) {
                Map<String, Object> ventaMap = new HashMap<>();
                ventaMap.put("id", venta.getId());
                ventaMap.put("codigo", venta.getCodigo());
                ventaMap.put("fecha", venta.getFecha());
                ventaMap.put("subtotal", venta.getSubtotal());
                ventaMap.put("igv", venta.getIgv());
                ventaMap.put("total", venta.getTotal());
                ventaMap.put("estado", venta.getEstado());
                
                // Información del cliente
                if (venta.getCliente() != null) {
                    Map<String, Object> clienteMap = new HashMap<>();
                    clienteMap.put("id", venta.getCliente().getId());
                    clienteMap.put("nombre", venta.getCliente().getNombre());
                    ventaMap.put("cliente", clienteMap);
                }
                
                // Obtener detalles de la venta
                List<DetalleVenta> detalles = detalleVentaRepository.findByVenta(venta);
                List<Map<String, Object>> detallesMap = new ArrayList<>();
                
                for (DetalleVenta detalle : detalles) {
                    Map<String, Object> detalleMap = new HashMap<>();
                    detalleMap.put("id", detalle.getId());
                    detalleMap.put("cantidad", detalle.getCantidad());
                    detalleMap.put("precioUnitario", detalle.getPrecioUnitario());
                    detalleMap.put("subtotal", detalle.getSubtotal());
                    
                    // Información de la variante y producto
                    if (detalle.getVariante() != null) {
                        Map<String, Object> varianteMap = new HashMap<>();
                        varianteMap.put("id", detalle.getVariante().getId());
                        varianteMap.put("color", detalle.getVariante().getColor());
                        varianteMap.put("talla", detalle.getVariante().getTalla());
                        
                        if (detalle.getVariante().getProducto() != null) {
                            Map<String, Object> productoMap = new HashMap<>();
                            productoMap.put("id", detalle.getVariante().getProducto().getId());
                            productoMap.put("nombre", detalle.getVariante().getProducto().getNombre());
                            productoMap.put("codigo", detalle.getVariante().getProducto().getCodigo());
                            varianteMap.put("producto", productoMap);
                        }
                        
                        detalleMap.put("variante", varianteMap);
                    }
                    
                    detallesMap.add(detalleMap);
                }
                
                ventaMap.put("detalles", detallesMap);
                ventaMap.put("totalProductos", detalles.stream().mapToInt(DetalleVenta::getCantidad).sum());
                
                ventasConDetalles.add(ventaMap);
            }
            
            return ResponseEntity.ok(ventasConDetalles);
        } catch (Exception e) {
            log.error("Error al obtener ventas", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al obtener ventas: " + e.getMessage());
        }
    }
    
    /**
     * Obtiene detalles de una venta específica
     */
    @GetMapping("/{ventaId}")
    public ResponseEntity<?> obtenerVentaPorId(@PathVariable Long ventaId) {
        try {
            Optional<Venta> ventaOpt = ventaRepository.findById(ventaId);
            if (ventaOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            Venta venta = ventaOpt.get();
            List<DetalleVenta> detalles = detalleVentaRepository.findByVenta(venta);
            
            Map<String, Object> resultado = new HashMap<>();
            resultado.put("venta", venta);
            resultado.put("detalles", detalles);
            
            return ResponseEntity.ok(resultado);
        } catch (Exception e) {
            log.error("Error al obtener venta por ID", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al obtener venta: " + e.getMessage());
        }
    }
    
    /**
     * Obtiene un detalle específico de venta
     */
    @GetMapping("/detalle/{detalleId}")
    public ResponseEntity<?> obtenerDetallePorId(@PathVariable Long detalleId) {
        try {
            log.info("Buscando detalle de venta con ID: {}", detalleId);
            Optional<DetalleVenta> detalleOpt = detalleVentaRepository.findById(detalleId);
            
            if (detalleOpt.isEmpty()) {
                log.warn("No se encontró el detalle de venta con ID: {}", detalleId);
                return ResponseEntity.notFound().build();
            }
            
            DetalleVenta detalle = detalleOpt.get();
            log.info("Detalle de venta encontrado: {}", detalle);
            
            // Crear un mapa con información más detallada para la respuesta
            Map<String, Object> respuesta = new HashMap<>();
            respuesta.put("id", detalle.getId());
            respuesta.put("cantidad", detalle.getCantidad());
            respuesta.put("precioUnitario", detalle.getPrecioUnitario());
            respuesta.put("subtotal", detalle.getSubtotal());
            
            // Añadir información de la variante si está disponible
            if (detalle.getVariante() != null) {
                VarianteProducto variante = detalle.getVariante();
                Map<String, Object> varianteInfo = new HashMap<>();
                varianteInfo.put("id", variante.getId());
                varianteInfo.put("sku", variante.getSku());
                varianteInfo.put("talla", variante.getTalla());
                varianteInfo.put("color", variante.getColor());
                varianteInfo.put("imagenUrl", variante.getImagenUrl());
                
                // Añadir información del producto si está disponible
                if (variante.getProducto() != null) {
                    Producto producto = variante.getProducto();
                    Map<String, Object> productoInfo = new HashMap<>();
                    productoInfo.put("id", producto.getId());
                    productoInfo.put("codigo", producto.getCodigo());
                    productoInfo.put("nombre", producto.getNombre());
                    productoInfo.put("descripcion", producto.getDescripcion());
                    productoInfo.put("precio", producto.getPrecio());
                    
                    // Añadir categoría si está disponible
                    if (producto.getCategoria() != null) {
                        Map<String, Object> categoriaInfo = new HashMap<>();
                        categoriaInfo.put("id", producto.getCategoria().getId());
                        categoriaInfo.put("nombre", producto.getCategoria().getNombre());
                        productoInfo.put("categoria", categoriaInfo);
                    }
                    
                    varianteInfo.put("producto", productoInfo);
                }
                
                respuesta.put("variante", varianteInfo);
            }
            
            // Añadir información de la venta si está disponible
            if (detalle.getVenta() != null) {
                Map<String, Object> ventaInfo = new HashMap<>();
                ventaInfo.put("id", detalle.getVenta().getId());
                ventaInfo.put("codigo", detalle.getVenta().getCodigo());
                ventaInfo.put("fecha", detalle.getVenta().getFecha());
                ventaInfo.put("estado", detalle.getVenta().getEstado());
                
                respuesta.put("venta", ventaInfo);
            }
            
            return ResponseEntity.ok(respuesta);
        } catch (Exception e) {
            log.error("Error al obtener detalle de venta por ID", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al obtener detalle de venta: " + e.getMessage());
        }
    }
    
    // Clase privada para los datos de venta
    private static class VentaData {
        String clienteNombre;
        String clienteInicial;
        LocalDateTime fecha;
        BigDecimal total;
        List<String> productos = new ArrayList<>();
    }

    @PostMapping
    @Transactional
    public ResponseEntity<?> registrarVenta(@RequestBody VentaDTO ventaDTO, HttpSession session) {
        try {
            log.info("Registrando nueva venta para cliente: {}, DNI: {}", ventaDTO.getNombreCliente(), ventaDTO.getDniCliente());
            
            // 1. Verificar que hay items en la venta
            if (ventaDTO.getItems() == null || ventaDTO.getItems().isEmpty()) {
                return ResponseEntity.badRequest().body("La venta debe contener al menos un producto");
            }
            
            // 2. Obtener o crear el cliente
            Cliente cliente;
            Optional<Cliente> clienteExistente = clienteRepository.findByDni(ventaDTO.getDniCliente());
            
            if (clienteExistente.isPresent()) {
                cliente = clienteExistente.get();
                log.info("Cliente encontrado con ID: {}", cliente.getId());
            } else {
                cliente = new Cliente();
                cliente.setNombre(ventaDTO.getNombreCliente());
                cliente.setDni(ventaDTO.getDniCliente());
                
                // Configurar fecha y hora en zona horaria de Perú sin milisegundos
                ZoneId zonaPeruana = ZoneId.of("America/Lima");
                LocalDateTime fechaHoraPeruana = LocalDateTime.now(zonaPeruana).withNano(0);
                cliente.setFechaRegistro(fechaHoraPeruana);
                
                cliente = clienteRepository.save(cliente);
                log.info("Nuevo cliente creado con ID: {} y fecha de registro: {}", cliente.getId(), fechaHoraPeruana);
            }
            
            // 3. Obtener el usuario actual desde la sesión
            String nombreUsuario = (String) session.getAttribute("usuario");
            if (nombreUsuario == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Usuario no autenticado");
            }
            
            Optional<Usuario> usuarioOpt = usuarioRepository.findByUsuario(nombreUsuario);
            if (usuarioOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Usuario no encontrado");
            }
            
            Usuario usuario = usuarioOpt.get();
            
            // 4. Crear la venta
            Venta venta = new Venta();
            venta.setCodigo("V-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
            venta.setCliente(cliente);
            venta.setUsuario(usuario);
            venta.setFecha(LocalDateTime.now());
            venta.setSubtotal(ventaDTO.getSubtotal());
            venta.setIgv(ventaDTO.getIgv());
            venta.setTotal(ventaDTO.getTotal());
            venta.setEstado("COMPLETADA");
            venta.setMetodoPago("EFECTIVO"); // Por defecto
            
            venta = ventaRepository.save(venta);
            log.info("Venta creada con ID: {} y código: {}", venta.getId(), venta.getCodigo());
            
            // 5. Procesar cada detalle de venta
            Set<DetalleVenta> detalles = new HashSet<>();
            for (DetalleVentaDTO detalleDTO : ventaDTO.getItems()) {
                log.info("Procesando detalle de venta para varianteId: {}, cantidad: {}", 
                         detalleDTO.getVarianteId(), detalleDTO.getCantidad());
                
                // Verificar que la variante existe
                Optional<VarianteProducto> varianteOpt = varianteRepository.findById(detalleDTO.getVarianteId());
                if (varianteOpt.isEmpty()) {
                    log.error("Variante no encontrada con ID: {}", detalleDTO.getVarianteId());
                    throw new RuntimeException("Variante no encontrada con ID: " + detalleDTO.getVarianteId());
                }
                
                VarianteProducto variante = varianteOpt.get();
                log.info("Variante encontrada: color={}, talla={}, producto={}",
                         variante.getColor(), variante.getTalla(), variante.getProducto().getNombre());
                
                // Verificar stock disponible
                Optional<Inventario> inventarioOpt = inventarioRepository.findByVarianteId(variante.getId());
                if (inventarioOpt.isEmpty()) {
                    log.error("No se encontró inventario para la variante ID: {}", variante.getId());
                    throw new RuntimeException("No hay inventario registrado para el producto: " + variante.getProducto().getNombre());
                }
                
                Inventario inventario = inventarioOpt.get();
                log.info("Stock actual: {}, cantidad solicitada: {}", inventario.getStock(), detalleDTO.getCantidad());
                
                if (inventario.getStock() < detalleDTO.getCantidad()) {
                    log.error("Stock insuficiente. Disponible: {}, Solicitado: {}", inventario.getStock(), detalleDTO.getCantidad());
                    throw new RuntimeException("Stock insuficiente para el producto: " + variante.getProducto().getNombre() 
                                              + " - " + variante.getColor() 
                                              + " (Talla: " + variante.getTalla() 
                                              + "). Disponible: " + inventario.getStock());
                }
                
                // Actualizar el inventario
                inventario.setStock(inventario.getStock() - detalleDTO.getCantidad());
                inventario.setUltimaActualizacion(LocalDateTime.now());
                inventarioRepository.save(inventario);
                log.info("Inventario actualizado. Nuevo stock: {}", inventario.getStock());
                
                // Registrar movimiento de stock para la venta
                MovimientoStock movimiento = new MovimientoStock();
                movimiento.setVariante(variante);
                movimiento.setTipo(MovimientoStock.TipoMovimiento.SALIDA);
                movimiento.setMotivo(MovimientoStock.MotivoMovimiento.VENTA);
                movimiento.setMotivoDetalle("Venta: " + venta.getCodigo());
                movimiento.setCantidad(detalleDTO.getCantidad());
                movimiento.setUsuario(usuario);
                movimiento.setObservaciones("Venta registrada");
                movimiento.setFecha(LocalDateTime.now());
                movimientoStockRepository.save(movimiento);
                log.info("Movimiento de stock registrado para la venta: {}, variante: {}, cantidad: {}", 
                        venta.getCodigo(), variante.getId(), detalleDTO.getCantidad());
                
                // Crear el detalle de venta
                DetalleVenta detalle = new DetalleVenta();
                detalle.setVenta(venta);
                detalle.setVariante(variante);
                detalle.setCantidad(detalleDTO.getCantidad());
                detalle.setPrecioUnitario(detalleDTO.getPrecioUnitario());
                detalle.setSubtotal(detalleDTO.getSubtotal());
                
                detalleVentaRepository.save(detalle);
                detalles.add(detalle);
                
                log.info("Detalle de venta registrado con éxito para variante ID: {}", 
                        variante.getId());
            }
            
            venta.setDetalles(detalles);
            
            // Notificar por WebSocket sobre la nueva venta
            webSocketService.notificarNuevaVenta(venta);
            
            // Llamar al servicio de alerta
            alertaService.notificarStockInsuficiente(detalles);
            
            return ResponseEntity.ok().body(venta.getCodigo());
            
        } catch (RuntimeException e) {
            log.error("Error al procesar la venta: " + e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Error al procesar la venta: " + e.getMessage());
        } catch (Exception e) {
            log.error("Error interno al procesar la venta", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error interno al procesar la venta: " + e.getMessage());
        }
    }
    
    /**
     * Obtiene un resumen de ventas para el dashboard
     */
    @PostMapping("/resumen")
    public ResponseEntity<?> obtenerResumenVentas() {
        try {
            // Obtener ventas del día
            List<Venta> ventasHoy = ventaRepository.findVentasHoy();
            Double totalVentasHoy = ventaRepository.getTotalVentasHoy();
            
            // Obtener los productos más vendidos
            List<Object[]> productosTopVentas = detalleVentaRepository.findTopSellingProducts();
            
            Map<String, Object> resumen = new HashMap<>();
            resumen.put("ventasHoy", ventasHoy.size());
            resumen.put("totalVentasHoy", totalVentasHoy != null ? totalVentasHoy : 0);
            resumen.put("productosTopVentas", productosTopVentas);
            
            return ResponseEntity.ok(resumen);
        } catch (RuntimeException e) {
            log.error("Error al obtener resumen de ventas: " + e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Error al obtener resumen de ventas: " + e.getMessage());
        } catch (Exception e) {
            log.error("Error interno al obtener resumen de ventas", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error interno al obtener resumen de ventas: " + e.getMessage());
        }
    }

    /**
     * Obtiene datos del dashboard para el empleado actual
     */
    @PostMapping("/dashboard-empleado")
    public ResponseEntity<?> obtenerDatosDashboardEmpleado(HttpSession session) {
        try {
            // Obtener id de usuario de la sesión
            Long usuarioId = (Long) session.getAttribute("usuarioId");
            if (usuarioId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Usuario no autenticado");
            }
            
            // Crear mapa para almacenar los datos del dashboard
            Map<String, Object> dashboardData = new HashMap<>();
            
            // Obtener total de ventas de hoy
            Double totalVentas = ventaRepository.getTotalVentasHoyByUsuario(usuarioId);
            dashboardData.put("ventasDiarias", totalVentas != null ? totalVentas : 0.0);
            
            // Obtener cantidad de ventas
            List<Venta> ventasHoy = ventaRepository.findVentasHoyByUsuario(usuarioId);
            dashboardData.put("cantidadVentas", ventasHoy.size());
            
            // Obtener productos vendidos hoy
            Long productosVendidos = detalleVentaRepository.countProductosVendidosHoyByUsuario(usuarioId);
            dashboardData.put("productosVendidos", productosVendidos != null ? productosVendidos : 0);
            
            // Obtener clientes atendidos
            Long clientesAtendidos = ventaRepository.countClientesAtendidosByUsuario(usuarioId);
            dashboardData.put("clientesAtendidos", clientesAtendidos != null ? clientesAtendidos : 0);
            
            // Obtener ventas recientes del empleado, agrupadas por ID de venta
            List<Venta> ventas = ventaRepository.findTop5ByUsuarioIdOrderByFechaDesc(usuarioId);
            if (ventas.size() > 5) {
                ventas = ventas.subList(0, 5);
            }
            
            Map<Long, VentaData> ventasPorId = new HashMap<>();
            
            // Obtener todos los detalles de estas ventas
            for (Venta venta : ventas) {
                List<DetalleVenta> detalles = detalleVentaRepository.findByVenta(venta);
                VentaData ventaData;
                
                if (ventasPorId.containsKey(venta.getId())) {
                    ventaData = ventasPorId.get(venta.getId());
                } else {
                    ventaData = new VentaData();
                    ventaData.clienteNombre = venta.getCliente().getNombre();
                    ventaData.fecha = venta.getFecha();
                    ventaData.total = venta.getTotal();
                    
                    // Obtener iniciales del cliente
                    String nombre = venta.getCliente().getNombre();
                    String[] nombrePartes = nombre.split(" ");
                    String iniciales = "";
                    if (nombrePartes.length > 0) {
                        iniciales += nombrePartes[0].substring(0, 1);
                        if (nombrePartes.length > 1) {
                            iniciales += nombrePartes[nombrePartes.length - 1].substring(0, 1);
                        }
                    }
                    ventaData.clienteInicial = iniciales.toUpperCase();
                    
                    ventasPorId.put(venta.getId(), ventaData);
                }
                
                // Añadir productos a la venta
                for (DetalleVenta detalle : detalles) {
                    VarianteProducto variante = detalle.getVariante();
                    String descripcion = variante.getProducto().getNombre() + " - Talla " + 
                                        variante.getTalla() + ", " + variante.getColor();
                    ventaData.productos.add(descripcion);
                }
            }
            
            // Convertir el mapa de objetos tipados a la estructura esperada por el frontend
            List<Map<String, Object>> ventasRecientes = new ArrayList<>();
            ventasPorId.values().forEach(ventaData -> {
                Map<String, Object> map = new HashMap<>();
                map.put("clienteNombre", ventaData.clienteNombre);
                map.put("clienteInicial", ventaData.clienteInicial);
                map.put("fecha", ventaData.fecha);
                map.put("total", ventaData.total);
                map.put("productos", ventaData.productos);
                ventasRecientes.add(map);
            });
            
            // Ordenar por fecha descendente sin necesidad de cast
            ventasRecientes.sort((v1, v2) -> {
                LocalDateTime fecha1 = (LocalDateTime) v1.get("fecha");
                LocalDateTime fecha2 = (LocalDateTime) v2.get("fecha");
                return fecha2.compareTo(fecha1);
            });
            
            dashboardData.put("ventasRecientes", ventasRecientes);
            
            return ResponseEntity.ok(dashboardData);
        } catch (Exception e) {
            log.error("Error al obtener datos del dashboard del empleado", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al obtener datos del dashboard: " + e.getMessage());
        }
    }
    
    /**
     * Obtiene las ventas recientes para el dashboard del administrador
     */
    @GetMapping("/ventas-recientes-admin")
    public ResponseEntity<?> obtenerVentasRecientesAdmin() {
        try {
            log.info("Obteniendo ventas recientes para el dashboard de administrador");
            
            // Obtener todas las ventas más recientes, ordenadas por fecha descendente
            List<Venta> ventasRecientes = ventaRepository.findTop20ByOrderByFechaDesc();
            
            // Contar el total de ventas del mes actual
            Long ventasMes = ventaRepository.countVentasMesActual();
            
            // Preparar los datos de respuesta
            Map<Long, VentaData> ventasPorId = new HashMap<>();
            
            // Obtener todos los detalles de estas ventas
            for (Venta venta : ventasRecientes) {
                List<DetalleVenta> detalles = detalleVentaRepository.findByVenta(venta);
                
                // Solo procesar ventas que tengan detalles (productos)
                if (detalles == null || detalles.isEmpty()) {
                    continue;
                }
                
                VentaData ventaData;
                
                if (ventasPorId.containsKey(venta.getId())) {
                    ventaData = ventasPorId.get(venta.getId());
                } else {
                    ventaData = new VentaData();
                    ventaData.clienteNombre = venta.getCliente().getNombre();
                    ventaData.fecha = venta.getFecha();
                    ventaData.total = venta.getTotal();
                    
                    // Obtener iniciales del cliente
                    String nombre = venta.getCliente().getNombre();
                    String[] nombrePartes = nombre.split(" ");
                    String iniciales = "";
                    if (nombrePartes.length > 0) {
                        iniciales += nombrePartes[0].substring(0, 1);
                        if (nombrePartes.length > 1) {
                            iniciales += nombrePartes[nombrePartes.length - 1].substring(0, 1);
                        }
                    }
                    ventaData.clienteInicial = iniciales.toUpperCase();
                    
                    ventasPorId.put(venta.getId(), ventaData);
                }
                
                // Añadir productos a la venta
                for (DetalleVenta detalle : detalles) {
                    VarianteProducto variante = detalle.getVariante();
                    String descripcion = variante.getProducto().getNombre() + " - Talla " + 
                                        variante.getTalla() + ", " + variante.getColor();
                    ventaData.productos.add(descripcion);
                }
            }
            
            // Convertir el mapa de objetos a una lista
            List<Map<String, Object>> ventasFormateadas = new ArrayList<>();
            ventasPorId.values().forEach(ventaData -> {
                // Solo incluir ventas que tengan productos
                if (ventaData.productos.isEmpty()) {
                    return;
                }
                
                Map<String, Object> map = new HashMap<>();
                map.put("clienteNombre", ventaData.clienteNombre);
                map.put("clienteInicial", ventaData.clienteInicial);
                map.put("fecha", ventaData.fecha);
                map.put("total", ventaData.total);
                map.put("productos", ventaData.productos);
                ventasFormateadas.add(map);
            });
            
            // Asegurar que las ventas estén ordenadas por fecha más reciente primero
            ventasFormateadas.sort((v1, v2) -> {
                LocalDateTime fecha1 = (LocalDateTime) v1.get("fecha");
                LocalDateTime fecha2 = (LocalDateTime) v2.get("fecha");
                return fecha2.compareTo(fecha1); // Orden descendente: más reciente primero
            });
            
            Map<String, Object> resultado = new HashMap<>();
            resultado.put("ventasRecientes", ventasFormateadas);
            resultado.put("totalVentasMes", ventasMes != null ? ventasMes : 0);
            
            return ResponseEntity.ok(resultado);
        } catch (Exception e) {
            log.error("Error al obtener ventas recientes para dashboard admin", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al obtener ventas recientes: " + e.getMessage());
        }
    }
    
    /**
     * Obtiene datos de ventas por periodo para el gráfico del dashboard
     */
    @GetMapping("/ventas-por-periodo")
    public ResponseEntity<?> obtenerVentasPorPeriodo() {
        try {
            log.info("Obteniendo datos de ventas por periodo para el gráfico");
            
            Map<String, Object> resultado = new HashMap<>();
            
            // Datos para el periodo diario (últimos 7 días)
            List<Object[]> ventasDiarias = ventaRepository.findVentasUltimos7Dias();
            Map<String, Object> datosVentasDiarias = procesarDatosVentasDiarias(ventasDiarias);
            resultado.put("daily", datosVentasDiarias);
            
            // Datos para el periodo mensual (últimos 12 meses)
            List<Object[]> ventasMensuales = ventaRepository.findVentasUltimos12Meses();
            Map<String, Object> datosVentasMensuales = procesarDatosVentasMensuales(ventasMensuales);
            resultado.put("monthly", datosVentasMensuales);
            
            // Datos para el periodo anual (últimos 5 años)
            List<Object[]> ventasAnuales = ventaRepository.findVentasUltimos5Anos();
            Map<String, Object> datosVentasAnuales = procesarDatosVentasAnuales(ventasAnuales);
            resultado.put("yearly", datosVentasAnuales);
            
            return ResponseEntity.ok(resultado);
        } catch (Exception e) {
            log.error("Error al obtener datos de ventas por periodo", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al obtener datos de ventas por periodo: " + e.getMessage());
        }
    }
    
    /**
     * Procesa los datos de ventas diarias para el formato esperado por el frontend
     */
    private Map<String, Object> procesarDatosVentasDiarias(List<Object[]> ventasDiarias) {
        Map<String, Object> resultado = new HashMap<>();
        
        // Inicializar arrays para los días de la semana (1=Domingo, 2=Lunes, ..., 7=Sábado)
        String[] diasSemana = {"Dom", "Lun", "Mar", "Mié", "Jue", "Vie", "Sáb"};
        List<String> labels = new ArrayList<>(Arrays.asList(diasSemana));
        
        // Inicializar valores en cero para todos los días
        BigDecimal[] valoresVentas = new BigDecimal[7];
        Arrays.fill(valoresVentas, BigDecimal.ZERO);
        
        // Procesar resultados de la consulta
        for (Object[] venta : ventasDiarias) {
            int diaSemana = ((Number) venta[0]).intValue();
            BigDecimal total = (BigDecimal) venta[1];
            
            // Ajustar índice (1-7 a 0-6)
            int indice = diaSemana - 1;
            
            // Asignar el valor al día correspondiente
            if (indice >= 0 && indice < 7) {
                valoresVentas[indice] = total;
        }
        }
        
        // Convertir BigDecimal a valores que puede manejar JSON
        List<Object> values = Arrays.stream(valoresVentas)
                .map(bd -> bd.doubleValue())
                .collect(Collectors.toList());
        
        resultado.put("labels", labels);
        resultado.put("values", values);
        
        return resultado;
    }
    
    /**
     * Procesa los datos de ventas mensuales para el formato esperado por el frontend
     */
    private Map<String, Object> procesarDatosVentasMensuales(List<Object[]> ventasMensuales) {
        Map<String, Object> resultado = new HashMap<>();
        
        // Definir los meses en español
        String[] labels = {"Ene", "Feb", "Mar", "Abr", "May", "Jun", "Jul", "Ago", "Sep", "Oct", "Nov", "Dic"};
        BigDecimal[] values = new BigDecimal[12];
        
        // Inicializar los valores a cero
        for (int i = 0; i < 12; i++) {
            values[i] = BigDecimal.ZERO;
        }
        
        // Llenar con los datos reales
        for (Object[] venta : ventasMensuales) {
            Integer mes = (Integer) venta[0]; // 1 = Enero, 12 = Diciembre
            BigDecimal total = (BigDecimal) venta[1];
            
            // Ajustar el índice (mes-1 para convertir 1-12 a 0-11)
            values[mes-1] = total;
        }
        
        resultado.put("labels", labels);
        resultado.put("values", values);
        
        return resultado;
    }
    
    /**
     * Procesa los datos de ventas anuales para el formato esperado por el frontend
     */
    private Map<String, Object> procesarDatosVentasAnuales(List<Object[]> ventasAnuales) {
        Map<String, Object> resultado = new HashMap<>();
        
        // Obtener año actual y los 4 anteriores
        int anoActual = LocalDateTime.now().getYear();
        String[] labels = new String[5];
        BigDecimal[] values = new BigDecimal[5];
        
        // Inicializar los valores a cero y establecer las etiquetas de años
        for (int i = 0; i < 5; i++) {
            int año = anoActual - 4 + i;
            labels[i] = String.valueOf(año);
            values[i] = BigDecimal.ZERO;
        }
        
        // Llenar con los datos reales
        for (Object[] venta : ventasAnuales) {
            Integer ano = (Integer) venta[0];
            BigDecimal total = (BigDecimal) venta[1];
            
            // Buscar el índice correspondiente a este año
            for (int i = 0; i < 5; i++) {
                int añoLabel = anoActual - 4 + i;
                if (añoLabel == ano) {
                    values[i] = total;
                    break;
                }
            }
            
        }
        
        resultado.put("labels", labels);
        resultado.put("values", values);
        
        return resultado;
    }

    /**
     * Actualiza el estado de una venta
     */
    @PutMapping("/{ventaId}/estado")
    public ResponseEntity<?> actualizarEstadoVenta(@PathVariable Long ventaId, @RequestBody Map<String, String> request) {
        try {
            Optional<Venta> ventaOpt = ventaRepository.findById(ventaId);
            if (ventaOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            Venta ventaExistente = ventaOpt.get();
            String estado = request.get("estado");
            
            // Actualizar el estado
            ventaExistente.setEstado(estado);
            Venta ventaActualizada = ventaRepository.save(ventaExistente);
            
            // Notificar por WebSocket la actualización de la venta
            webSocketService.notificarActualizacionVenta(ventaActualizada);
            
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Error al actualizar el estado de la venta", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al actualizar el estado de la venta: " + e.getMessage());
        }
    }

    /**
     * Endpoint para procesar la devolución completa de una venta
     */
    @PostMapping("/devolucion/{ventaId}")
    @Transactional
    public ResponseEntity<?> procesarDevolucionVenta(@PathVariable Long ventaId, HttpSession session) {
        try {
            log.info("Procesando devolución de venta con ID: {}", ventaId);
            
            // Verificar si la venta existe
            Optional<Venta> ventaOpt = ventaRepository.findById(ventaId);
            if (ventaOpt.isEmpty()) {
                log.error("No se pudo encontrar la venta con ID: {}", ventaId);
                return ResponseEntity.notFound().build();
            }
            
            Venta venta = ventaOpt.get();
            
            // Verificar si la venta ya fue devuelta
            if ("DEVUELTA".equals(venta.getEstado())) {
                log.warn("La venta con ID: {} ya ha sido devuelta", ventaId);
                return ResponseEntity.badRequest().body("Esta venta ya ha sido devuelta anteriormente");
            }
            
            // Obtener los detalles de la venta
            List<DetalleVenta> detalles = detalleVentaRepository.findByVenta(venta);
            if (detalles.isEmpty()) {
                log.warn("La venta con ID: {} no tiene detalles para devolver", ventaId);
                return ResponseEntity.badRequest().body("No hay productos para devolver en esta venta");
            }
            
            // Obtener el usuario actual
            String username = (String) session.getAttribute("usuario");
            log.info("Procesando devolución con usuario de sesión: {}", username);
            if (username == null) {
                log.error("No hay sesión de usuario activa para procesar la devolución");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Debe iniciar sesión para realizar esta operación");
            }
            
            Optional<Usuario> usuarioOpt = usuarioRepository.findByUsuario(username);
            if (usuarioOpt.isEmpty()) {
                log.error("No se pudo encontrar el usuario: {}", username);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al obtener información del usuario");
            }
            
            Usuario usuario = usuarioOpt.get();
            
            // Procesar la devolución de cada producto
            for (DetalleVenta detalle : detalles) {
                // Obtener la variante del producto
                VarianteProducto variante = detalle.getVariante();
                if (variante != null) {
                    // Buscar el inventario de esta variante
                    Optional<Inventario> inventarioOpt = inventarioRepository.findByVariante(variante);
                    
                    if (inventarioOpt.isPresent()) {
                        Inventario inventario = inventarioOpt.get();
                        
                        // Actualizar el stock (devolver los productos al inventario)
                        int stockActual = inventario.getStock();
                        int cantidadDevuelta = detalle.getCantidad();
                        int nuevoStock = stockActual + cantidadDevuelta;
                        
                        log.info("Actualizando stock para variante {}: {} + {} = {}", 
                            variante.getId(), stockActual, cantidadDevuelta, nuevoStock);
                        
                        inventario.setStock(nuevoStock);
                        inventarioRepository.save(inventario);
                        
                        // Registrar el movimiento de stock
                        MovimientoStock movimiento = new MovimientoStock();
                        movimiento.setVariante(variante);
                        movimiento.setFecha(LocalDateTime.now());
                        movimiento.setCantidad(cantidadDevuelta);
                        movimiento.setTipo(MovimientoStock.TipoMovimiento.ENTRADA);
                        movimiento.setMotivo(MovimientoStock.MotivoMovimiento.OTRO);
                        movimiento.setMotivoDetalle("Devolución de venta #" + venta.getCodigo());
                        movimiento.setUsuario(usuario);
                        
                        movimientoStockRepository.save(movimiento);
                        
                        log.info("Devueltos {} unidades del producto {} al inventario", cantidadDevuelta, variante.getProducto().getNombre());
                    } else {
                        log.warn("No se encontró inventario para la variante con ID: {}", variante.getId());
                    }
                } else {
                    log.warn("Detalle de venta ID: {} sin variante asociada", detalle.getId());
                }
            }
            
            // Actualizar estado de la venta
            venta.setEstado("DEVUELTA");
            ventaRepository.save(venta);
            
            // Notificar a través de WebSocket
            Map<String, Object> notificacion = new HashMap<>();
            notificacion.put("tipo", "VENTA_DEVUELTA");
            notificacion.put("ventaId", venta.getId());
            notificacion.put("codigo", venta.getCodigo());
            
            // Usar WebSocketMessage para enviar la notificación
            webSocketService.notificarActualizacionVenta(venta);
            
            // Crear alerta en el sistema
            Alerta alerta = new Alerta();
            alerta.setTitulo("Devolución de venta");
            alerta.setMensaje("Se ha procesado la devolución completa de la venta #" + venta.getCodigo());
            alerta.setTipo(Alerta.TipoAlerta.SISTEMA);
            alerta.setPrioridad(Alerta.PrioridadAlerta.MEDIA);
            alerta.setFechaCreacion(LocalDateTime.now());
            alerta.setLeida(false);
            alerta.setAccionRequerida("Verificar detalles de la devolución en el sistema");
            
            alertaService.crearAlerta(alerta);
            
            // Preparar respuesta
            Map<String, Object> respuesta = new HashMap<>();
            respuesta.put("mensaje", "Devolución procesada correctamente");
            respuesta.put("ventaId", venta.getId());
            respuesta.put("estado", venta.getEstado());
            
            return ResponseEntity.ok(respuesta);
        } catch (Exception e) {
            log.error("Error al procesar devolución de venta: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al procesar la devolución: " + e.getMessage());
        }
    }

    /**
     * Obtiene todas las devoluciones con filtro opcional por fechas
     */
    @GetMapping("/devoluciones")
    public ResponseEntity<?> obtenerDevoluciones(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate hasta) {
        try {
            log.info("Obteniendo devoluciones con filtros - desde: {}, hasta: {}", desde, hasta);
            
            List<Venta> ventas;
            
            if (desde != null && hasta != null) {
                // Convertir LocalDate a LocalDateTime para el inicio y fin del día
                LocalDateTime fechaDesde = desde.atStartOfDay();
                LocalDateTime fechaHasta = hasta.atTime(23, 59, 59);
                log.info("Buscando devoluciones entre {} y {}", fechaDesde, fechaHasta);
                ventas = ventaRepository.findByEstadoAndFechaBetween("DEVUELTA", fechaDesde, fechaHasta);
            } else {
                ventas = ventaRepository.findByEstado("DEVUELTA");
            }
            
            // Ordenar por fecha descendente (más recientes primero)
            ventas.sort((v1, v2) -> v2.getFecha().compareTo(v1.getFecha()));
            
            // Cargar detalles para cada devolución
            List<Map<String, Object>> devolucionesConDetalles = new ArrayList<>();
            
            for (Venta venta : ventas) {
                List<DetalleVenta> detalles = detalleVentaRepository.findByVenta(venta);
                
                // Crear un mapa para almacenar los datos de la devolución
                Map<String, Object> devolucionMap = new HashMap<>();
                devolucionMap.put("id", venta.getId());
                devolucionMap.put("codigo", venta.getCodigo());
                devolucionMap.put("fechaVenta", venta.getFecha());
                
                // Obtener fecha de devolución (buscamos el último movimiento de tipo ENTRADA relacionado con esta venta)
                List<MovimientoStock> movimientos = movimientoStockRepository.findByMotivoDetalleContaining("Devolución de venta #" + venta.getCodigo());
                if (!movimientos.isEmpty()) {
                    // Ordenar movimientos por fecha, el más reciente primero
                    movimientos.sort((m1, m2) -> m2.getFecha().compareTo(m1.getFecha()));
                    devolucionMap.put("fechaDevolucion", movimientos.get(0).getFecha());
                    
                    // Obtener usuario que procesó la devolución
                    if (movimientos.get(0).getUsuario() != null) {
                        devolucionMap.put("usuarioId", movimientos.get(0).getUsuario().getId());
                        devolucionMap.put("usuarioNombre", movimientos.get(0).getUsuario().getNombre());
                    }
                } else {
                    // Si no hay movimientos, usamos la fecha de la venta como fecha de devolución
                    devolucionMap.put("fechaDevolucion", venta.getFecha());
                }
                
                devolucionMap.put("subtotal", venta.getSubtotal());
                devolucionMap.put("igv", venta.getIgv());
                devolucionMap.put("total", venta.getTotal());
                devolucionMap.put("estado", venta.getEstado());
                
                // Agregar información del cliente si está disponible
                if (venta.getCliente() != null) {
                    Map<String, Object> clienteMap = new HashMap<>();
                    clienteMap.put("id", venta.getCliente().getId());
                    clienteMap.put("nombre", venta.getCliente().getNombre());
                    clienteMap.put("dni", venta.getCliente().getDni());
                    devolucionMap.put("cliente", clienteMap);
                }
                
                // Procesar los detalles de venta
                List<Map<String, Object>> detallesArray = new ArrayList<>();
                for (DetalleVenta detalle : detalles) {
                    Map<String, Object> detalleMap = new HashMap<>();
                    detalleMap.put("id", detalle.getId());
                    detalleMap.put("cantidad", detalle.getCantidad());
                    detalleMap.put("precioUnitario", detalle.getPrecioUnitario());
                    detalleMap.put("subtotal", detalle.getSubtotal());
                    
                    // Agregar información de la variante si está disponible
                    if (detalle.getVariante() != null) {
                        Map<String, Object> varianteMap = new HashMap<>();
                        varianteMap.put("id", detalle.getVariante().getId());
                        varianteMap.put("color", detalle.getVariante().getColor());
                        varianteMap.put("talla", detalle.getVariante().getTalla());
                        varianteMap.put("sku", detalle.getVariante().getSku());
                        
                        // Agregar información del producto si está disponible
                        if (detalle.getVariante().getProducto() != null) {
                            Map<String, Object> productoMap = new HashMap<>();
                            Producto producto = detalle.getVariante().getProducto();
                            productoMap.put("id", producto.getId());
                            productoMap.put("nombre", producto.getNombre());
                            productoMap.put("codigo", producto.getCodigo());
                            
                            // Por defecto, usar una imagen predeterminada
                            productoMap.put("imagen", "/img/producto-default.png");
                            
                            varianteMap.put("producto", productoMap);
                        }
                        
                        detalleMap.put("variante", varianteMap);
                    }
                    
                    detallesArray.add(detalleMap);
                }
                
                devolucionMap.put("detalles", detallesArray);
                devolucionesConDetalles.add(devolucionMap);
            }
            
            return ResponseEntity.ok(devolucionesConDetalles);
        } catch (Exception e) {
            log.error("Error al obtener devoluciones", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al obtener devoluciones: " + e.getMessage());
        }
    }

    /**
     * Obtiene los detalles completos de una devolución por su ID
     * @param id ID de la devolución
     * @return Detalles completos de la devolución
     */
    @GetMapping("/devolucion/{id}/detalles")
    public ResponseEntity<?> obtenerDetallesDevolucion(@PathVariable Long id) {
        try {
            log.info("Obteniendo detalles de devolución con ID: {}", id);
            
            // Buscar la venta
            Optional<Venta> ventaOpt = ventaRepository.findById(id);
            if (ventaOpt.isEmpty()) {
                log.warn("No se encontró la devolución con ID: {}", id);
                return ResponseEntity.notFound().build();
            }
            
            Venta venta = ventaOpt.get();
            if (!"DEVUELTA".equals(venta.getEstado())) {
                log.warn("La venta con ID: {} no es una devolución", id);
                return ResponseEntity.badRequest().body("Esta venta no es una devolución");
            }
            
            // Obtener los detalles de la venta
            List<DetalleVenta> detalles = detalleVentaRepository.findByVenta(venta);
            
            // Crear un mapa con todos los datos relevantes
            Map<String, Object> respuesta = new HashMap<>();
            respuesta.put("id", venta.getId());
            respuesta.put("codigo", venta.getCodigo());
            respuesta.put("fechaVenta", venta.getFecha());
            respuesta.put("estado", venta.getEstado());
            respuesta.put("subtotal", venta.getSubtotal());
            respuesta.put("igv", venta.getIgv());
            respuesta.put("total", venta.getTotal());
            
            // Añadir información del cliente si está disponible
            if (venta.getCliente() != null) {
                Map<String, Object> clienteMap = new HashMap<>();
                clienteMap.put("id", venta.getCliente().getId());
                clienteMap.put("nombre", venta.getCliente().getNombre());
                clienteMap.put("dni", venta.getCliente().getDni());
                respuesta.put("cliente", clienteMap);
            }
            
            // Añadir información del usuario que registró la venta
            if (venta.getUsuario() != null) {
                Map<String, Object> usuarioMap = new HashMap<>();
                usuarioMap.put("id", venta.getUsuario().getId());
                usuarioMap.put("nombre", venta.getUsuario().getNombre());
                respuesta.put("usuario", usuarioMap);
            }
            
            // Obtener fecha de devolución (último movimiento de ENTRADA relacionado)
            List<MovimientoStock> movimientos = movimientoStockRepository.findByMotivoDetalleContaining("Devolución de venta #" + venta.getCodigo());
            if (!movimientos.isEmpty()) {
                // Ordenar por fecha más reciente primero
                movimientos.sort((m1, m2) -> m2.getFecha().compareTo(m1.getFecha()));
                respuesta.put("fechaDevolucion", movimientos.get(0).getFecha());
                
                // Añadir información del usuario que procesó la devolución
                if (movimientos.get(0).getUsuario() != null) {
                    respuesta.put("usuarioDevolucionId", movimientos.get(0).getUsuario().getId());
                    respuesta.put("usuarioDevolucionNombre", movimientos.get(0).getUsuario().getNombre());
                }
            }
            
            // Transformar los detalles para incluir información del producto
            List<Map<String, Object>> detallesMap = new ArrayList<>();
            for (DetalleVenta detalle : detalles) {
                Map<String, Object> detalleMap = new HashMap<>();
                detalleMap.put("id", detalle.getId());
                detalleMap.put("cantidad", detalle.getCantidad());
                detalleMap.put("precioUnitario", detalle.getPrecioUnitario());
                detalleMap.put("subtotal", detalle.getSubtotal());
                
                // Añadir información del producto
                if (detalle.getVariante() != null && detalle.getVariante().getProducto() != null) {
                    Map<String, Object> productoMap = new HashMap<>();
                    Producto producto = detalle.getVariante().getProducto();
                    productoMap.put("id", producto.getId());
                    productoMap.put("nombre", producto.getNombre());
                    productoMap.put("codigo", producto.getCodigo());
                    detalleMap.put("producto", productoMap);
                }
                
                detallesMap.add(detalleMap);
            }
            respuesta.put("detalles", detallesMap);
            
            return ResponseEntity.ok(respuesta);
        } catch (Exception e) {
            log.error("Error al obtener detalles de devolución", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al obtener detalles de devolución: " + e.getMessage());
        }
    }
} 