package com.darkcode.spring.six.Controllers;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.darkcode.spring.six.dtos.DetalleVentaDTO;
import com.darkcode.spring.six.dtos.VentaDTO;
import com.darkcode.spring.six.models.entities.Cliente;
import com.darkcode.spring.six.models.entities.DetalleVenta;
import com.darkcode.spring.six.models.entities.Inventario;
import com.darkcode.spring.six.models.entities.Usuario;
import com.darkcode.spring.six.models.entities.VarianteProducto;
import com.darkcode.spring.six.models.entities.Venta;
import com.darkcode.spring.six.models.repositories.ClienteRepository;
import com.darkcode.spring.six.models.repositories.DetalleVentaRepository;
import com.darkcode.spring.six.models.repositories.InventarioRepository;
import com.darkcode.spring.six.models.repositories.UsuarioRepository;
import com.darkcode.spring.six.models.repositories.VarianteProductoRepository;
import com.darkcode.spring.six.models.repositories.VentaRepository;

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
        
        // Definir los días de la semana en español
        String[] labels = {"Lun", "Mar", "Mié", "Jue", "Vie", "Sáb", "Dom"};
        BigDecimal[] values = new BigDecimal[7];
        
        // Inicializar los valores a cero
        for (int i = 0; i < 7; i++) {
            values[i] = BigDecimal.ZERO;
        }
        
        // Llenar con los datos reales
        for (Object[] venta : ventasDiarias) {
            Integer diaSemana = (Integer) venta[0]; // 1 = Lunes, 7 = Domingo
            BigDecimal total = (BigDecimal) venta[1];
            
            // Ajustar el índice (diaSemana-1 para convertir 1-7 a 0-6)
            values[diaSemana-1] = total;
        }
        
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
            labels[i] = String.valueOf(anoActual - 4 + i);
            values[i] = BigDecimal.ZERO;
        }
        
        // Llenar con los datos reales
        for (Object[] venta : ventasAnuales) {
            Integer ano = (Integer) venta[0];
            BigDecimal total = (BigDecimal) venta[1];
            
            // Buscar el índice correspondiente a este año
            for (int i = 0; i < 5; i++) {
                if (Integer.parseInt(labels[i]) == ano) {
                    values[i] = total;
                    break;
                }
            }
        }
        
        resultado.put("labels", labels);
        resultado.put("values", values);
        
        return resultado;
    }
} 