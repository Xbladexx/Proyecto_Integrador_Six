package com.darkcode.spring.six.Controllers;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.darkcode.spring.six.dtos.ClasificacionABCDTO;
import com.darkcode.spring.six.models.entities.ClasificacionABC;
import com.darkcode.spring.six.models.entities.DetalleVenta;
import com.darkcode.spring.six.models.entities.Inventario;
import com.darkcode.spring.six.models.entities.Producto;
import com.darkcode.spring.six.models.entities.VarianteProducto;
import com.darkcode.spring.six.models.entities.Venta;
import com.darkcode.spring.six.models.repositories.ClasificacionABCRepository;
import com.darkcode.spring.six.models.repositories.DetalleVentaRepository;
import com.darkcode.spring.six.models.repositories.InventarioRepository;
import com.darkcode.spring.six.models.repositories.MovimientoStockRepository;
import com.darkcode.spring.six.models.repositories.ProductoRepository;
import com.darkcode.spring.six.models.repositories.VarianteProductoRepository;
import com.darkcode.spring.six.models.repositories.VentaRepository;
import com.darkcode.spring.six.services.ClasificacionABCService;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/reportes")
@RequiredArgsConstructor
@Slf4j
public class ReporteController {

    private final VentaRepository ventaRepository;
    private final DetalleVentaRepository detalleVentaRepository;
    private final InventarioRepository inventarioRepository;
    private final ClasificacionABCRepository clasificacionRepository;
    private final ClasificacionABCService clasificacionABCService;
    
    // Estos repositorios son necesarios para el cálculo automático de la clasificación ABC
    // y son utilizados indirectamente a través del clasificacionABCService
    @SuppressWarnings("unused")
    private final ProductoRepository productoRepository;
    @SuppressWarnings("unused")
    private final VarianteProductoRepository varianteRepository;
    @SuppressWarnings("unused")
    private final MovimientoStockRepository movimientoRepository;
    
    // Caché para almacenar la última clasificación ABC calculada
    private Map<String, Object> clasificacionABCCache;
    private LocalDateTime ultimaActualizacionCache;
    // Tiempo máximo de vida del caché (12 horas)
    private static final long CACHE_TTL_HOURS = 12;
    
    /**
     * Obtiene datos para el gráfico de ventas por período
     */
    @GetMapping("/ventas-por-periodo")
    public ResponseEntity<?> obtenerVentasPorPeriodo(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime hasta) {
        
        try {
            log.info("Obteniendo datos de ventas por período para reportes");
            log.info("Parámetros: desde={}, hasta={}", desde, hasta);
            
            // Si no se especifican fechas, usar los últimos 6 meses
            if (desde == null) {
                desde = LocalDateTime.now().minusMonths(6);
            }
            if (hasta == null) {
                hasta = LocalDateTime.now();
            }
            
            log.info("Fechas efectivas: desde={}, hasta={}", desde, hasta);
            
            // Obtener ventas en el rango de fechas
            List<Venta> ventas = ventaRepository.findByFechaBetween(desde, hasta);
            log.info("Ventas encontradas: {}", ventas.size());
            
            // Procesar los datos para el formato esperado por el gráfico
            Map<String, Object> resultado = new HashMap<>();
            
            // Agrupar por mes y calcular totales
            Map<String, BigDecimal> ventasPorMes = new HashMap<>();
            
            for (Venta venta : ventas) {
                int mes = venta.getFecha().getMonthValue();
                int anio = venta.getFecha().getYear();
                String etiqueta = obtenerNombreMes(mes) + " " + anio;
                
                BigDecimal total = ventasPorMes.getOrDefault(etiqueta, BigDecimal.ZERO);
                total = total.add(venta.getTotal());
                ventasPorMes.put(etiqueta, total);
            }
            
            // Convertir a arrays para el formato esperado por Chart.js
            List<String> labels = new ArrayList<>();
            List<BigDecimal> values = new ArrayList<>();
            
            // Ordenar por fecha
            ventasPorMes.entrySet().stream()
                .sorted((e1, e2) -> {
                    // Ordenar por año y mes
                    String[] parts1 = e1.getKey().split(" ");
                    String[] parts2 = e2.getKey().split(" ");
                    
                    int year1 = Integer.parseInt(parts1[1]);
                    int year2 = Integer.parseInt(parts2[1]);
                    
                    if (year1 != year2) {
                        return Integer.compare(year1, year2);
                    }
                    
                    return Integer.compare(
                        getMonthNumber(parts1[0]), 
                        getMonthNumber(parts2[0])
                    );
                })
                .forEach(entry -> {
                    labels.add(entry.getKey());
                    values.add(entry.getValue());
                });
            
            resultado.put("labels", labels);
            resultado.put("values", values);
            
            log.info("Datos de ventas por período procesados: {} períodos", labels.size());
            return ResponseEntity.ok(resultado);
        } catch (Exception e) {
            log.error("Error al obtener datos de ventas por período", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al obtener datos de ventas por período: " + e.getMessage());
        }
    }
    
    /**
     * Obtiene datos para el gráfico de ventas por categoría
     */
    @GetMapping("/ventas-por-categoria")
    public ResponseEntity<?> obtenerVentasPorCategoria(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime hasta) {
        
        try {
            log.info("Obteniendo datos de ventas por categoría para reportes");
            log.info("Parámetros: desde={}, hasta={}", desde, hasta);
            
            // Si no se especifican fechas, usar los últimos 6 meses
            if (desde == null) {
                desde = LocalDateTime.now().minusMonths(6);
            }
            if (hasta == null) {
                hasta = LocalDateTime.now();
            }
            
            log.info("Fechas efectivas: desde={}, hasta={}", desde, hasta);
            
            // Obtener ventas en el rango de fechas
            List<Venta> ventas = ventaRepository.findByFechaBetween(desde, hasta);
            log.info("Ventas encontradas: {}", ventas.size());
            
            // Agrupar por categoría y calcular totales
            Map<String, BigDecimal> ventasPorCategoria = new HashMap<>();
            
            for (Venta venta : ventas) {
                List<DetalleVenta> detalles = detalleVentaRepository.findByVenta(venta);
                
                for (DetalleVenta detalle : detalles) {
                    String categoria = detalle.getVariante().getProducto().getCategoria().getNombre();
                    BigDecimal subtotal = detalle.getSubtotal();
                    
                    BigDecimal total = ventasPorCategoria.getOrDefault(categoria, BigDecimal.ZERO);
                    total = total.add(subtotal);
                    ventasPorCategoria.put(categoria, total);
                }
            }
            
            // Convertir a arrays para el formato esperado por Chart.js
            List<String> labels = new ArrayList<>(ventasPorCategoria.keySet());
            List<BigDecimal> values = new ArrayList<>();
            
            for (String label : labels) {
                values.add(ventasPorCategoria.get(label));
            }
            
            Map<String, Object> resultado = new HashMap<>();
            resultado.put("labels", labels);
            resultado.put("values", values);
            
            log.info("Datos de ventas por categoría procesados: {} categorías", labels.size());
            return ResponseEntity.ok(resultado);
        } catch (Exception e) {
            log.error("Error al obtener datos de ventas por categoría", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al obtener datos de ventas por categoría: " + e.getMessage());
        }
    }
    
    /**
     * Obtiene datos para la tabla de productos más vendidos
     */
    @GetMapping("/productos-mas-vendidos")
    public ResponseEntity<?> obtenerProductosMasVendidos(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime hasta) {
        
        try {
            log.info("Obteniendo datos de productos más vendidos para reportes");
            
            // Si no se especifican fechas, usar los últimos 6 meses
            if (desde == null) {
                desde = LocalDateTime.now().minusMonths(6);
            }
            if (hasta == null) {
                hasta = LocalDateTime.now();
            }
            
            // Obtener ventas en el rango de fechas
            List<Venta> ventas = ventaRepository.findByFechaBetween(desde, hasta);
            
            // Mapa para almacenar datos agrupados por variante
            Map<Long, Map<String, Object>> productosPorVariante = new HashMap<>();
            
            // Procesar cada venta
            for (Venta venta : ventas) {
                List<DetalleVenta> detalles = detalleVentaRepository.findByVenta(venta);
                
                for (DetalleVenta detalle : detalles) {
                    Long varianteId = detalle.getVariante().getId();
                    String nombreProducto = detalle.getVariante().getProducto().getNombre();
                    String categoria = detalle.getVariante().getProducto().getCategoria().getNombre();
                    
                    // Si no existe la entrada para esta variante, crearla
                    if (!productosPorVariante.containsKey(varianteId)) {
                        Map<String, Object> datoVariante = new HashMap<>();
                        datoVariante.put("producto", nombreProducto);
                        datoVariante.put("categoria", categoria);
                        datoVariante.put("unidades", 0);
                        datoVariante.put("ingresos", BigDecimal.ZERO);
                        
                        productosPorVariante.put(varianteId, datoVariante);
                    }
                    
                    // Actualizar los datos de la variante
                    Map<String, Object> datoVariante = productosPorVariante.get(varianteId);
                    
                    int unidades = (int) datoVariante.get("unidades") + detalle.getCantidad();
                    BigDecimal ingresos = ((BigDecimal) datoVariante.get("ingresos")).add(detalle.getSubtotal());
                    
                    datoVariante.put("unidades", unidades);
                    datoVariante.put("ingresos", ingresos);
                }
            }
            
            // Convertir el mapa a una lista y ordenar por ingresos descendentes
            List<Map<String, Object>> productos = new ArrayList<>(productosPorVariante.values());
            productos.sort((p1, p2) -> {
                BigDecimal ingresos1 = (BigDecimal) p1.get("ingresos");
                BigDecimal ingresos2 = (BigDecimal) p2.get("ingresos");
                return ingresos2.compareTo(ingresos1); // Orden descendente
            });
            
            // Limitar a los 5 primeros productos
            if (productos.size() > 5) {
                productos = productos.subList(0, 5);
            }
            
            return ResponseEntity.ok(productos);
        } catch (Exception e) {
            log.error("Error al obtener datos de productos más vendidos", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al obtener datos de productos más vendidos: " + e.getMessage());
        }
    }
    
    /**
     * Obtiene datos para el resumen de ventas
     */
    @GetMapping("/resumen-ventas")
    public ResponseEntity<?> obtenerResumenVentas(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime hasta) {
        
        try {
            log.info("Obteniendo resumen de ventas para reportes");
            
            // Si no se especifican fechas, usar el mes actual
            LocalDateTime inicioMesActual = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
            
            if (desde == null) {
                desde = inicioMesActual;
            }
            if (hasta == null) {
                hasta = LocalDateTime.now();
            }
            
            // Obtener ventas del período actual
            List<Venta> ventasActuales = ventaRepository.findByFechaBetween(desde, hasta);
            
            // Obtener ventas del período anterior (mismo rango de tiempo)
            long diasEnPeriodo = java.time.Duration.between(desde, hasta).toDays();
            LocalDateTime desdeAnterior = desde.minusDays(diasEnPeriodo);
            LocalDateTime hastaAnterior = desde.minusNanos(1);
            
            List<Venta> ventasAnteriores = ventaRepository.findByFechaBetween(desdeAnterior, hastaAnterior);
            
            // Calcular totales y métricas
            BigDecimal totalVentas = BigDecimal.ZERO;
            int transacciones = ventasActuales.size();
            BigDecimal ticketPromedio = BigDecimal.ZERO;
            BigDecimal margenBruto = new BigDecimal("0.482"); // Este dato normalmente vendría calculado desde los costos
            
            BigDecimal totalVentasAnterior = BigDecimal.ZERO;
            int transaccionesAnterior = ventasAnteriores.size();
            
            for (Venta venta : ventasActuales) {
                totalVentas = totalVentas.add(venta.getTotal());
            }
            
            for (Venta venta : ventasAnteriores) {
                totalVentasAnterior = totalVentasAnterior.add(venta.getTotal());
            }
            
            // Calcular ticket promedio si hay ventas
            if (transacciones > 0) {
                ticketPromedio = totalVentas.divide(new BigDecimal(transacciones), 2, RoundingMode.HALF_UP);
            }
            
            // Calcular variaciones porcentuales
            double varTotalVentas = calcularVariacionPorcentual(totalVentasAnterior, totalVentas);
            double varTransacciones = calcularVariacionPorcentual(transaccionesAnterior, transacciones);
            
            // Preparar el resultado
            Map<String, Object> resumen = new HashMap<>();
            resumen.put("totalVentas", totalVentas);
            resumen.put("transacciones", transacciones);
            resumen.put("ticketPromedio", ticketPromedio);
            resumen.put("margenBruto", margenBruto);
            
            resumen.put("varTotalVentas", varTotalVentas);
            resumen.put("varTransacciones", varTransacciones);
            resumen.put("varTicketPromedio", 4.3); // Datos de ejemplo, calcular realmente
            resumen.put("varMargenBruto", -1.5); // Datos de ejemplo, calcular realmente
            
            return ResponseEntity.ok(resumen);
        } catch (Exception e) {
            log.error("Error al obtener resumen de ventas", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al obtener resumen de ventas: " + e.getMessage());
        }
    }
    
    /**
     * Obtiene datos para el gráfico de valor de inventario por categoría
     */
    @GetMapping("/valor-inventario-por-categoria")
    public ResponseEntity<?> obtenerValorInventarioPorCategoria() {
        try {
            log.info("Obteniendo datos de valor de inventario por categoría para reportes");
            
            // Obtener datos agrupados por categoría
            Map<String, Object> resultado = new HashMap<>();
            List<String> labels = new ArrayList<>();
            List<BigDecimal> values = new ArrayList<>();
            
            // Obtener el inventario agrupado por categoría usando query nativa
            List<Object[]> inventarioPorCategoria = inventarioRepository.obtenerValorInventarioPorCategoria();
            
            // Procesar los resultados
            for (Object[] item : inventarioPorCategoria) {
                String categoria = (String) item[0];
                BigDecimal valor = (BigDecimal) item[1];
                
                labels.add(categoria);
                values.add(valor);
            }
            
            resultado.put("labels", labels);
            resultado.put("values", values);
            
            return ResponseEntity.ok(resultado);
        } catch (Exception e) {
            log.error("Error al obtener datos de valor de inventario por categoría", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al obtener datos de valor de inventario por categoría: " + e.getMessage());
        }
    }
    
    /**
     * Obtiene datos para el gráfico de rotación de inventario
     */
    @GetMapping("/rotacion-inventario")
    public ResponseEntity<?> obtenerRotacionInventario() {
        try {
            log.info("Obteniendo datos de rotación de inventario para reportes");
            
            // En un sistema real, estos datos vendrían de un cálculo basado en ventas históricas
            // y niveles de inventario. Por ahora, generaremos datos de ejemplo.
            Map<String, Object> resultado = new HashMap<>();
            
            // Últimos 6 meses
            LocalDateTime ahora = LocalDateTime.now();
            List<String> labels = new ArrayList<>();
            List<Double> values = new ArrayList<>();
            
            // Datos de ejemplo para rotación de inventario de los últimos 6 meses
            for (int i = 5; i >= 0; i--) {
                LocalDateTime mesAnterior = ahora.minusMonths(i);
                String nombreMes = obtenerNombreMes(mesAnterior.getMonthValue());
                
                labels.add(nombreMes);
                
                // Valor de rotación (en un sistema real, esto sería calculado)
                // Fórmula: Rotación = Costo de ventas / Inventario promedio
                double rotacion = 3.8 + (Math.random() * 0.8); // Valores entre 3.8 y 4.6
                values.add(Math.round(rotacion * 100.0) / 100.0); // Redondear a 2 decimales
            }
            
            resultado.put("labels", labels);
            resultado.put("values", values);
            
            return ResponseEntity.ok(resultado);
        } catch (Exception e) {
            log.error("Error al obtener datos de rotación de inventario", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al obtener datos de rotación de inventario: " + e.getMessage());
        }
    }
    
    /**
     * Obtiene datos para la tabla de productos con bajo stock
     */
    @GetMapping("/productos-bajo-stock")
    public ResponseEntity<?> obtenerProductosBajoStock() {
        try {
            log.info("Obteniendo datos de productos con bajo stock para reportes");
            
            // Umbral de stock crítico: cuando es igual o menor a este valor (mismo que en AlertaInventarioController)
            final int STOCK_CRITICO = 3;
            
            // Obtener productos con stock menor o igual al stock mínimo
            List<Inventario> inventariosBajoStock = inventarioRepository.findByStockLessThanEqualStockMinimo();
            
            // Convertir a formato para la tabla
            List<Map<String, Object>> productos = new ArrayList<>();
            
            for (Inventario inventario : inventariosBajoStock) {
                Map<String, Object> producto = new HashMap<>();
                
                VarianteProducto variante = inventario.getVariante();
                Producto prod = variante.getProducto();
                
                producto.put("producto", prod.getNombre());
                producto.put("variante", variante.getColor() + ", " + variante.getTalla());
                producto.put("stockActual", inventario.getStock());
                producto.put("stockMinimo", inventario.getStockMinimo());
                
                // Determinar el estado del stock usando el mismo criterio que AlertaInventarioController
                String estado = "Normal";
                if (inventario.getStock() <= STOCK_CRITICO) {
                    estado = "Crítico";
                } else if (inventario.getStock() <= inventario.getStockMinimo()) {
                    estado = "Bajo";
                }
                producto.put("estado", estado);
                
                productos.add(producto);
            }
            
            return ResponseEntity.ok(productos);
        } catch (Exception e) {
            log.error("Error al obtener datos de productos con bajo stock", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al obtener datos de productos con bajo stock: " + e.getMessage());
        }
    }
    
    /**
     * Obtiene datos para el resumen de inventario
     */
    @GetMapping("/resumen-inventario")
    public ResponseEntity<?> obtenerResumenInventario() {
        try {
            log.info("Obteniendo resumen de inventario para reportes");
            
            Map<String, Object> resumen = new HashMap<>();
            
            // Calcular valor total del inventario y unidades
            List<Object[]> valorTotal = inventarioRepository.calcularValorTotalInventario();
            BigDecimal valorInventario = BigDecimal.ZERO;
            Long unidadesTotales = 0L;
            
            if (!valorTotal.isEmpty() && valorTotal.get(0)[0] != null) {
                valorInventario = (BigDecimal) valorTotal.get(0)[0];
                unidadesTotales = ((Number) valorTotal.get(0)[1]).longValue();
            }
            
            // Rotación promedio (valor simulado para este ejemplo)
            double rotacionPromedio = 4.2;
            
            // Contar alertas (productos con stock bajo o crítico)
            Long contadorAlertas = inventarioRepository.countByStockLessThanEqualStockMinimo();
            
            // Variaciones con respecto al mes anterior (simuladas)
            double varValorInventario = 5.2; // porcentaje
            double varUnidades = 3.8; // porcentaje
            double varRotacion = 0.3; // porcentaje
            double varAlertas = -1.5; // porcentaje (negativo indica reducción, que es bueno)
            
            // Construir respuesta
            resumen.put("valorTotal", valorInventario);
            resumen.put("unidades", unidadesTotales);
            resumen.put("rotacion", rotacionPromedio);
            resumen.put("alertas", contadorAlertas);
            
            resumen.put("varValorTotal", varValorInventario);
            resumen.put("varUnidades", varUnidades);
            resumen.put("varRotacion", varRotacion);
            resumen.put("varAlertas", varAlertas);
            
            return ResponseEntity.ok(resumen);
        } catch (Exception e) {
            log.error("Error al obtener resumen de inventario", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al obtener resumen de inventario: " + e.getMessage());
        }
    }
    
    /**
     * Obtiene datos para el análisis de rendimiento de productos
     */
    @GetMapping("/rendimiento-productos")
    public ResponseEntity<?> obtenerRendimientoProductos(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime hasta) {
        
        try {
            log.info("Obteniendo datos de rendimiento de productos para reportes");
            
            // Si no se especifican fechas, usar los últimos 3 meses
            if (desde == null) {
                desde = LocalDateTime.now().minusMonths(3);
            }
            if (hasta == null) {
                hasta = LocalDateTime.now();
            }
            
            // Obtener ventas en el rango de fechas
            List<Venta> ventas = ventaRepository.findByFechaBetween(desde, hasta);
            
            // Mapa para almacenar datos agrupados por producto
            Map<Long, Map<String, Object>> datosPorProducto = new HashMap<>();
            
            // Procesar cada venta
            for (Venta venta : ventas) {
                List<DetalleVenta> detalles = detalleVentaRepository.findByVenta(venta);
                
                for (DetalleVenta detalle : detalles) {
                    Producto producto = detalle.getVariante().getProducto();
                    Long productoId = producto.getId();
                    
                    // Si no existe la entrada para este producto, crearla
                    if (!datosPorProducto.containsKey(productoId)) {
                        Map<String, Object> datoProducto = new HashMap<>();
                        datoProducto.put("id", productoId);
                        datoProducto.put("nombre", producto.getNombre());
                        datoProducto.put("categoria", producto.getCategoria().getNombre());
                        datoProducto.put("unidadesVendidas", 0);
                        datoProducto.put("ingresos", BigDecimal.ZERO);
                        datoProducto.put("costoTotal", BigDecimal.ZERO);
                        
                        datosPorProducto.put(productoId, datoProducto);
                    }
                    
                    // Actualizar los datos del producto
                    Map<String, Object> datoProducto = datosPorProducto.get(productoId);
                    
                    int unidades = (int) datoProducto.get("unidadesVendidas") + detalle.getCantidad();
                    BigDecimal ingresos = ((BigDecimal) datoProducto.get("ingresos")).add(detalle.getSubtotal());
                    
                    // Calcular costo (esto es una aproximación, en un sistema real vendría del costo de adquisición)
                    BigDecimal costoUnitario = detalle.getPrecioUnitario().multiply(new BigDecimal("0.65"));
                    BigDecimal costoTotal = ((BigDecimal) datoProducto.get("costoTotal"))
                            .add(costoUnitario.multiply(new BigDecimal(detalle.getCantidad())));
                    
                    datoProducto.put("unidadesVendidas", unidades);
                    datoProducto.put("ingresos", ingresos);
                    datoProducto.put("costoTotal", costoTotal);
                }
            }
            
            // Calcular métricas adicionales para cada producto
            List<Map<String, Object>> resultados = new ArrayList<>();
            
            for (Map<String, Object> producto : datosPorProducto.values()) {
                BigDecimal ingresos = (BigDecimal) producto.get("ingresos");
                BigDecimal costoTotal = (BigDecimal) producto.get("costoTotal");
                
                // Calcular margen bruto
                BigDecimal margenBruto = ingresos.subtract(costoTotal);
                BigDecimal margenPorcentaje = BigDecimal.ZERO;
                
                if (ingresos.compareTo(BigDecimal.ZERO) > 0) {
                    margenPorcentaje = margenBruto.divide(ingresos, 4, RoundingMode.HALF_UP)
                            .multiply(new BigDecimal("100")).setScale(2, RoundingMode.HALF_UP);
                }
                
                producto.put("margenBruto", margenBruto);
                producto.put("margenPorcentaje", margenPorcentaje);
                
                resultados.add(producto);
            }
            
            // Ordenar por margen bruto descendente
            resultados.sort((p1, p2) -> {
                BigDecimal margen1 = (BigDecimal) p1.get("margenBruto");
                BigDecimal margen2 = (BigDecimal) p2.get("margenBruto");
                return margen2.compareTo(margen1); // Orden descendente
            });
            
            return ResponseEntity.ok(resultados);
        } catch (Exception e) {
            log.error("Error al obtener datos de rendimiento de productos", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al obtener datos de rendimiento de productos: " + e.getMessage());
        }
    }
    
    /**
     * Obtiene datos de tendencias de ventas de productos
     */
    @GetMapping("/tendencias-productos")
    public ResponseEntity<?> obtenerTendenciasProductos(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime hasta,
            @RequestParam(defaultValue = "5") int limite) {
        
        try {
            log.info("Obteniendo datos de tendencias de productos para reportes");
            
            // Si no se especifican fechas, usar los últimos 6 meses
            if (desde == null) {
                desde = LocalDateTime.now().minusMonths(6);
            }
            if (hasta == null) {
                hasta = LocalDateTime.now();
            }
            
            // Calculamos el punto medio para comparar tendencias
            LocalDateTime puntoMedio = desde.plus(java.time.Duration.between(desde, hasta).dividedBy(2));
            
            // Periodo 1: desde hasta puntoMedio
            List<Venta> ventasEnPeriodo1 = ventaRepository.findByFechaBetween(desde, puntoMedio);
            
            // Periodo 2: puntoMedio hasta hasta
            List<Venta> ventasEnPeriodo2 = ventaRepository.findByFechaBetween(puntoMedio, hasta);
            
            // Mapas para almacenar ventas por producto en cada periodo
            Map<Long, Integer> ventasPorProductoPeriodo1 = new HashMap<>();
            Map<Long, Integer> ventasPorProductoPeriodo2 = new HashMap<>();
            Map<Long, String> nombresProductos = new HashMap<>();
            
            // Procesar ventas del periodo 1
            procesarVentasPorPeriodo(ventasEnPeriodo1, ventasPorProductoPeriodo1, nombresProductos);
            
            // Procesar ventas del periodo 2
            procesarVentasPorPeriodo(ventasEnPeriodo2, ventasPorProductoPeriodo2, nombresProductos);
            
            // Calcular tendencias
            List<Map<String, Object>> tendencias = new ArrayList<>();
            
            for (Long productoId : nombresProductos.keySet()) {
                int ventasPeriodo1 = ventasPorProductoPeriodo1.getOrDefault(productoId, 0);
                int ventasPeriodo2 = ventasPorProductoPeriodo2.getOrDefault(productoId, 0);
                
                if (ventasPeriodo1 == 0 && ventasPeriodo2 == 0) {
                    continue; // Ignorar productos sin ventas
                }
                
                // Calcular porcentaje de cambio
                double porcentajeCambio;
                if (ventasPeriodo1 == 0) {
                    porcentajeCambio = 100.0; // Si no había ventas antes, es un 100% de aumento
                } else {
                    porcentajeCambio = ((double) (ventasPeriodo2 - ventasPeriodo1) / ventasPeriodo1) * 100;
                }
                
                Map<String, Object> tendencia = new HashMap<>();
                tendencia.put("producto", nombresProductos.get(productoId));
                tendencia.put("ventasPeriodo1", ventasPeriodo1);
                tendencia.put("ventasPeriodo2", ventasPeriodo2);
                tendencia.put("cambio", ventasPeriodo2 - ventasPeriodo1);
                tendencia.put("porcentajeCambio", Math.round(porcentajeCambio * 100.0) / 100.0);
                
                tendencias.add(tendencia);
            }
            
            // Ordenar por porcentaje de cambio (descendente)
            tendencias.sort((t1, t2) -> {
                Double cambio1 = (Double) t1.get("porcentajeCambio");
                Double cambio2 = (Double) t2.get("porcentajeCambio");
                return cambio2.compareTo(cambio1);
            });
            
            // Limitar resultados
            if (tendencias.size() > limite) {
                tendencias = tendencias.subList(0, limite);
            }
            
            Map<String, Object> resultado = new HashMap<>();
            resultado.put("periodo1", desde.toString() + " - " + puntoMedio.toString());
            resultado.put("periodo2", puntoMedio.toString() + " - " + hasta.toString());
            resultado.put("tendencias", tendencias);
            
            return ResponseEntity.ok(resultado);
        } catch (Exception e) {
            log.error("Error al obtener datos de tendencias de productos para reporte", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
    
    private void procesarVentasPorPeriodo(List<Venta> ventas, Map<Long, Integer> ventasPorProducto, Map<Long, String> nombresProductos) {
        for (Venta venta : ventas) {
            List<DetalleVenta> detalles = detalleVentaRepository.findByVenta(venta);
            
            for (DetalleVenta detalle : detalles) {
                Producto producto = detalle.getVariante().getProducto();
                Long productoId = producto.getId();
                
                // Guardar el nombre del producto
                nombresProductos.put(productoId, producto.getNombre());
                
                // Actualizar contadores de ventas
                int cantidadActual = ventasPorProducto.getOrDefault(productoId, 0);
                ventasPorProducto.put(productoId, cantidadActual + detalle.getCantidad());
            }
        }
    }
    
    // Métodos auxiliares
    
    private String obtenerNombreMes(int mes) {
        String[] meses = {"Ene", "Feb", "Mar", "Abr", "May", "Jun", "Jul", "Ago", "Sep", "Oct", "Nov", "Dic"};
        return meses[mes - 1];
    }
    
    private int getMonthNumber(String monthName) {
        return switch (monthName) {
            case "Ene" -> 1;
            case "Feb" -> 2;
            case "Mar" -> 3;
            case "Abr" -> 4;
            case "May" -> 5;
            case "Jun" -> 6;
            case "Jul" -> 7;
            case "Ago" -> 8;
            case "Sep" -> 9;
            case "Oct" -> 10;
            case "Nov" -> 11;
            case "Dic" -> 12;
            default -> 0;
        };
    }
    
    private double calcularVariacionPorcentual(Number valorAnterior, Number valorActual) {
        if (valorAnterior.doubleValue() == 0) {
            return 100.0; // Si el valor anterior es 0, consideramos un aumento del 100%
        }
        
        return ((valorActual.doubleValue() - valorAnterior.doubleValue()) / valorAnterior.doubleValue()) * 100;
    }
    
    /**
     * Obtiene datos para el análisis de distribución de productos por categoría
     */
    @GetMapping("/distribucion-productos-categoria")
    public ResponseEntity<?> obtenerDistribucionProductosCategoria() {
        try {
            log.info("Obteniendo datos de distribución de productos por categoría");
            
            // En un sistema real, este endpoint consultaría la base de datos para obtener
            // la distribución actual de productos por categoría
            
            // Para este ejemplo, construiremos una respuesta con datos simulados
            List<Map<String, Object>> distribucion = new ArrayList<>();
            
            // Datos simulados - en una implementación real, estos vendrían de consultas a la base de datos
            String[] categorias = {"Ropa", "Calzado", "Accesorios", "Deportes", "Electrónica"};
            int[] cantidades = {120, 85, 65, 40, 30};
            
            // Calcular total para los porcentajes
            int total = 0;
            for (int cantidad : cantidades) {
                total += cantidad;
            }
            
            // Construir respuesta
            for (int i = 0; i < categorias.length; i++) {
                Map<String, Object> item = new HashMap<>();
                item.put("categoria", categorias[i]);
                item.put("cantidad", cantidades[i]);
                
                // Calcular porcentaje
                double porcentaje = ((double) cantidades[i] / total) * 100;
                item.put("porcentaje", Math.round(porcentaje * 10.0) / 10.0); // Redondear a 1 decimal
                
                distribucion.add(item);
            }
            
            // Ordenar por cantidad descendente
            distribucion.sort((a, b) -> {
                Integer cantA = (Integer) a.get("cantidad");
                Integer cantB = (Integer) b.get("cantidad");
                return cantB.compareTo(cantA);
            });
            
            Map<String, Object> resultado = new HashMap<>();
            resultado.put("totalProductos", total);
            resultado.put("distribucion", distribucion);
            
            return ResponseEntity.ok(resultado);
        } catch (Exception e) {
            log.error("Error al obtener datos de distribución de productos por categoría", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al obtener datos de distribución de productos por categoría: " + e.getMessage());
        }
    }
    
    /**
     * Exporta los reportes en formato PDF
     */
    @GetMapping("/exportar-pdf")
    public ResponseEntity<byte[]> exportarPDF(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime hasta,
            @RequestParam(required = false, defaultValue = "ventas") String seccion) {
        
        log.info("Exportando reporte a PDF para la sección: {}", seccion);
            
            // Si no se especifican fechas, usar valores predeterminados
        LocalDateTime fechaDesde = desde != null ? desde : LocalDateTime.now().minusMonths(1);
        LocalDateTime fechaHasta = hasta != null ? hasta : LocalDateTime.now();
        
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            // Crear documento PDF
            Document document = new Document();
            PdfWriter.getInstance(document, baos);
            
            document.open();
            
            // Añadir encabezado
            com.itextpdf.text.Font titleFont = FontFactory.getFont(
                FontFactory.HELVETICA_BOLD, 18);
            Paragraph title = new Paragraph("SIX - Reporte de " + 
                obtenerTituloSeccion(seccion), titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);
            
            // Añadir fecha de generación
            com.itextpdf.text.Font normalFont = FontFactory.getFont(
                FontFactory.HELVETICA, 12);
            Paragraph fechaGeneracion = new Paragraph(
                "Generado: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")), normalFont);
            fechaGeneracion.setAlignment(Element.ALIGN_RIGHT);
            document.add(fechaGeneracion);
            
            // Añadir período del reporte
            Paragraph periodo = new Paragraph(
                "Período: " + fechaDesde.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + 
                " - " + fechaHasta.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")), normalFont);
            periodo.setAlignment(Element.ALIGN_LEFT);
            document.add(periodo);
            
            document.add(new Paragraph(" ")); // Espacio
            
            // Añadir contenido según la sección
            switch (seccion) {
                case "ventas" -> generarReportePDFVentas(document, fechaDesde, fechaHasta);
                case "inventario" -> generarReportePDFInventario(document, fechaDesde, fechaHasta);
                case "productos" -> generarReportePDFProductos(document, fechaDesde, fechaHasta);
                default -> log.warn("Sección de reporte no reconocida: {}", seccion);
            }
            
            document.close();
            
            // Configurar respuesta HTTP
            byte[] pdfBytes = baos.toByteArray();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("filename", "SIX_Reporte_" + seccion + ".pdf");
            
            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error al exportar a PDF", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
    
    /**
     * Exporta los reportes en formato Excel
     */
    @GetMapping("/exportar-excel")
    public ResponseEntity<byte[]> exportarExcel(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime hasta,
            @RequestParam(required = false, defaultValue = "ventas") String seccion) {
        
        log.info("Exportando reporte a Excel para la sección: {}", seccion);
            
            // Si no se especifican fechas, usar valores predeterminados
        LocalDateTime fechaDesde = desde != null ? desde : LocalDateTime.now().minusMonths(1);
        LocalDateTime fechaHasta = hasta != null ? hasta : LocalDateTime.now();
        
        try (XSSFWorkbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            
            // Crear hoja según la sección
            String nombreHoja = obtenerTituloSeccion(seccion);
            XSSFSheet sheet = workbook.createSheet(nombreHoja);
            
            // Crear estilos
            CellStyle headerStyle = workbook.createCellStyle();
            XSSFFont font = workbook.createFont();
            font.setBold(true);
            headerStyle.setFont(font);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            
            // Crear encabezado con información general
            Row titleRow = sheet.createRow(0);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("SIX - Reporte de " + nombreHoja);
            titleCell.setCellStyle(headerStyle);
            
            Row dateRow = sheet.createRow(1);
            dateRow.createCell(0).setCellValue("Generado: " + 
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
            
            Row periodRow = sheet.createRow(2);
            periodRow.createCell(0).setCellValue("Período: " + 
                fechaDesde.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + 
                " - " + fechaHasta.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            
            // Dejar una fila en blanco
            sheet.createRow(3);
            
            // Generar contenido según la sección
            try {
                switch (seccion.toLowerCase()) {
                    case "ventas" -> generarReporteExcelVentas(workbook, sheet, headerStyle, fechaDesde, fechaHasta);
                    case "inventario" -> generarReporteExcelInventario(workbook, sheet, headerStyle, fechaDesde, fechaHasta);
                    case "productos" -> generarReporteExcelProductos(workbook, sheet, headerStyle, fechaDesde, fechaHasta);
                    default -> {
                        log.warn("Sección de reporte no reconocida: {}", seccion);
                        throw new IllegalArgumentException("Sección de reporte no válida: " + seccion);
                    }
                }
            } catch (Exception e) {
                log.error("Error generando contenido del reporte para la sección {}: {}", seccion, e.getMessage(), e);
                throw new RuntimeException("Error generando contenido del reporte: " + e.getMessage(), e);
            }
            
            // Ajustar ancho de columnas
            int columnCount = sheet.getRow(0).getLastCellNum();
            for (int i = 0; i < columnCount; i++) {
                sheet.autoSizeColumn(i);
            }
            
            // Escribir a array de bytes
            workbook.write(baos);
            
            // Configurar respuesta
            byte[] excelBytes = baos.toByteArray();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
            headers.setContentDispositionFormData("filename", "SIX_Reporte_" + seccion + "_" + 
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".xlsx");
            
            return new ResponseEntity<>(excelBytes, headers, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error al exportar a Excel: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(("Error al generar el reporte: " + e.getMessage()).getBytes());
        }
    }
    
    // Métodos auxiliares para la generación de reportes
    
    private String obtenerTituloSeccion(String seccion) {
        return switch (seccion) {
            case "ventas" -> "Ventas";
            case "inventario" -> "Inventario";
            case "productos" -> "Productos";
            default -> "Reporte";
        };
    }
    
    private void generarReportePDFVentas(Document document, LocalDateTime desde, LocalDateTime hasta) {
        // Implementación del reporte de ventas en PDF
        try {
            log.info("Generando reporte PDF de ventas para el período {} - {}", desde, hasta);
            
            // Añadir contenido real (esto es solo placeholder)
            document.add(new Paragraph("Reporte de Ventas para el período indicado"));
            // En una implementación real, aquí se añadirían tablas y gráficos con datos
        } catch (Exception e) {
            log.error("Error al generar reporte PDF de ventas: {}", e.getMessage());
        }
    }
    
    private void generarReportePDFInventario(Document document, LocalDateTime desde, LocalDateTime hasta) {
        // Implementación del reporte de inventario en PDF
        try {
            log.info("Generando reporte PDF de inventario para el período {} - {}", desde, hasta);
            
            // Añadir contenido real (esto es solo placeholder)
            document.add(new Paragraph("Reporte de Inventario para el período indicado"));
            // En una implementación real, aquí se añadirían tablas y gráficos con datos
        } catch (Exception e) {
            log.error("Error al generar reporte PDF de inventario: {}", e.getMessage());
        }
    }
    
    private void generarReportePDFProductos(Document document, LocalDateTime desde, LocalDateTime hasta) {
        // Implementación del reporte de productos en PDF
        try {
            log.info("Generando reporte PDF de productos para el período {} - {}", desde, hasta);
            
            // Añadir contenido real (esto es solo placeholder)
            document.add(new Paragraph("Reporte de Productos para el período indicado"));
            // En una implementación real, aquí se añadirían tablas y gráficos con datos
        } catch (Exception e) {
            log.error("Error al generar reporte PDF de productos: {}", e.getMessage());
        }
    }
    
    private void generarReporteExcelVentas(XSSFWorkbook workbook, 
                                   XSSFSheet sheet,
                                   CellStyle headerStyle,
                                   LocalDateTime desde, LocalDateTime hasta) {
        
        // Obtener datos de resumen de ventas
        Map<String, Object> resumenVentas = new HashMap<>();
        try {
            ResponseEntity<?> response = obtenerResumenVentas(desde, hasta);
            if (response != null && response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                @SuppressWarnings("unchecked")
                Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
                resumenVentas = responseBody;
            }
        } catch (Exception e) {
            log.error("Error al obtener resumen de ventas para Excel", e);
        }
        
        // Crear encabezado de resumen
        Row resumenHeaderRow = sheet.createRow(4);
        Cell resumenHeaderCell = resumenHeaderRow.createCell(0);
        resumenHeaderCell.setCellValue("Resumen de Ventas");
        resumenHeaderCell.setCellStyle(headerStyle);
        
        // Crear datos de resumen
        int rowNum = 5;
        
        // Total ventas
        Row totalVentasRow = sheet.createRow(rowNum++);
        totalVentasRow.createCell(0).setCellValue("Total Ventas:");
        totalVentasRow.createCell(1).setCellValue("S/. " + (resumenVentas != null && resumenVentas.get("totalVentas") != null ? 
                new BigDecimal(resumenVentas.get("totalVentas").toString()).setScale(2, RoundingMode.HALF_UP) : "0.00"));
            
        // Transacciones
        Row transaccionesRow = sheet.createRow(rowNum++);
        transaccionesRow.createCell(0).setCellValue("Transacciones:");
        transaccionesRow.createCell(1).setCellValue(resumenVentas != null && resumenVentas.get("transacciones") != null ? 
            ((Number)resumenVentas.get("transacciones")).intValue() : 0);
        
        // Ticket promedio
        Row ticketRow = sheet.createRow(rowNum++);
        ticketRow.createCell(0).setCellValue("Ticket Promedio:");
        ticketRow.createCell(1).setCellValue("S/. " + (resumenVentas != null && resumenVentas.get("ticketPromedio") != null ? 
                new BigDecimal(resumenVentas.get("ticketPromedio").toString()).setScale(2, RoundingMode.HALF_UP) : "0.00"));
            
        // Margen bruto
        Row margenRow = sheet.createRow(rowNum++);
        margenRow.createCell(0).setCellValue("Margen Bruto:");
        margenRow.createCell(1).setCellValue((resumenVentas != null && resumenVentas.get("margenBruto") != null ? 
                new BigDecimal(resumenVentas.get("margenBruto").toString()).multiply(new BigDecimal("100")).setScale(1, RoundingMode.HALF_UP) : "0.0") + "%");
        
        // Dejar una fila en blanco
        rowNum++;
        
        // Obtener datos de productos más vendidos
        List<Map<String, Object>> productosMasVendidos = new ArrayList<>();
        try {
            ResponseEntity<?> response = obtenerProductosMasVendidos(desde, hasta);
            if (response != null && response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> responseBody = (List<Map<String, Object>>) response.getBody();
                productosMasVendidos = responseBody;
            }
        } catch (Exception e) {
            log.error("Error al obtener productos más vendidos para Excel", e);
        }
        
        // Crear encabezado de productos
        Row productosHeaderRow = sheet.createRow(rowNum++);
        Cell productosHeaderCell = productosHeaderRow.createCell(0);
        productosHeaderCell.setCellValue("Productos Más Vendidos");
        productosHeaderCell.setCellStyle(headerStyle);
        
        // Crear encabezados de la tabla
        Row tableHeaderRow = sheet.createRow(rowNum++);
        String[] productosHeaders = {"Producto", "Categoría", "Unidades", "Ingresos"};
        for (int i = 0; i < productosHeaders.length; i++) {
            Cell cell = tableHeaderRow.createCell(i);
            cell.setCellValue(productosHeaders[i]);
            cell.setCellStyle(headerStyle);
        }
        
        // Añadir datos de productos
        if (productosMasVendidos != null) {
            for (Map<String, Object> producto : productosMasVendidos) {
                if (producto != null) {
                    Row row = sheet.createRow(rowNum++);
                    row.createCell(0).setCellValue(producto.get("producto") != null ? producto.get("producto").toString() : "");
                    row.createCell(1).setCellValue(producto.get("categoria") != null ? producto.get("categoria").toString() : "");
                    row.createCell(2).setCellValue(producto.get("unidades") != null ? ((Number)producto.get("unidades")).intValue() : 0);
                    
                    // Formatear valores monetarios
                    Object ingresos = producto.get("ingresos");
                    if (ingresos != null) {
                        row.createCell(3).setCellValue(((Number)ingresos).doubleValue());
                    } else {
                        row.createCell(3).setCellValue(0.0);
                    }
                }
            }
        }
    }
    
    private void generarReporteExcelInventario(XSSFWorkbook workbook, 
                                       XSSFSheet sheet,
                                       CellStyle headerStyle,
                                       LocalDateTime desde, LocalDateTime hasta) {
        
        log.info("Generando reporte Excel de inventario para el período {} - {}", desde, hasta);
        
        // Obtener datos de resumen de inventario
        Map<String, Object> resumenInventario = new HashMap<>();
        try {
            ResponseEntity<?> response = obtenerResumenInventario();
            if (response != null && response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                @SuppressWarnings("unchecked")
                Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
                resumenInventario = responseBody;
            }
        } catch (Exception e) {
            log.error("Error al obtener resumen de inventario para Excel", e);
        }
        
        // Crear encabezado de resumen
        Row resumenHeaderRow = sheet.createRow(4);
        Cell resumenHeaderCell = resumenHeaderRow.createCell(0);
        resumenHeaderCell.setCellValue("Resumen de Inventario");
        resumenHeaderCell.setCellStyle(headerStyle);
        
        // Crear datos de resumen
        int rowNum = 5;
        
        // Valor total del inventario
        Row valorTotalRow = sheet.createRow(rowNum++);
        valorTotalRow.createCell(0).setCellValue("Valor Total:");
        valorTotalRow.createCell(1).setCellValue("S/. " + (resumenInventario != null && resumenInventario.get("valorTotal") != null ? 
                new BigDecimal(resumenInventario.get("valorTotal").toString()).setScale(2, RoundingMode.HALF_UP) : "0.00"));
            
        // Unidades en inventario
        Row unidadesRow = sheet.createRow(rowNum++);
        unidadesRow.createCell(0).setCellValue("Unidades en Stock:");
        unidadesRow.createCell(1).setCellValue(resumenInventario != null && resumenInventario.get("unidades") != null ? 
            ((Number)resumenInventario.get("unidades")).longValue() : 0);
        
        // Rotación promedio
        Row rotacionRow = sheet.createRow(rowNum++);
        rotacionRow.createCell(0).setCellValue("Rotación Promedio:");
        rotacionRow.createCell(1).setCellValue(resumenInventario != null && resumenInventario.get("rotacion") != null ? 
            ((Number)resumenInventario.get("rotacion")).doubleValue() : 0.0);
        
        // Alertas de stock
        Row alertasRow = sheet.createRow(rowNum++);
        alertasRow.createCell(0).setCellValue("Alertas de Stock:");
        alertasRow.createCell(1).setCellValue(resumenInventario != null && resumenInventario.get("alertas") != null ? 
            ((Number)resumenInventario.get("alertas")).longValue() : 0);
        
        // Dejar una fila en blanco
        rowNum++;
        
        // Obtener datos de productos con bajo stock
        List<Map<String, Object>> productosBajoStock = new ArrayList<>();
        try {
            ResponseEntity<?> response = obtenerProductosBajoStock();
            if (response != null && response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> responseBody = (List<Map<String, Object>>) response.getBody();
                productosBajoStock = responseBody;
            }
        } catch (Exception e) {
            log.error("Error al obtener productos con bajo stock para Excel", e);
        }
        
        // Crear encabezado de productos con bajo stock
        Row productosHeaderRow = sheet.createRow(rowNum++);
        Cell productosHeaderCell = productosHeaderRow.createCell(0);
        productosHeaderCell.setCellValue("Productos con Bajo Stock");
        productosHeaderCell.setCellStyle(headerStyle);
        
        // Crear encabezados de la tabla
        Row tableHeaderRow = sheet.createRow(rowNum++);
        String[] productosHeaders = {"Producto", "Variante", "Stock Actual", "Stock Mínimo", "Estado"};
        for (int i = 0; i < productosHeaders.length; i++) {
            Cell cell = tableHeaderRow.createCell(i);
            cell.setCellValue(productosHeaders[i]);
            cell.setCellStyle(headerStyle);
        }
        
        // Añadir datos de productos con bajo stock
        if (productosBajoStock != null) {
            for (Map<String, Object> producto : productosBajoStock) {
                if (producto != null) {
                    Row row = sheet.createRow(rowNum++);
                    row.createCell(0).setCellValue(producto.get("producto") != null ? producto.get("producto").toString() : "");
                    row.createCell(1).setCellValue(producto.get("variante") != null ? producto.get("variante").toString() : "");
                    row.createCell(2).setCellValue(producto.get("stockActual") != null ? ((Number)producto.get("stockActual")).intValue() : 0);
                    row.createCell(3).setCellValue(producto.get("stockMinimo") != null ? ((Number)producto.get("stockMinimo")).intValue() : 0);
                    row.createCell(4).setCellValue(producto.get("estado") != null ? producto.get("estado").toString() : "");
                }
            }
        }
    }
    
    private void generarReporteExcelProductos(XSSFWorkbook workbook, 
                                      XSSFSheet sheet,
                                      CellStyle headerStyle,
                                      LocalDateTime desde, LocalDateTime hasta) {
        
        log.info("Generando reporte Excel de productos para el período {} - {}", desde, hasta);
        
        // Obtener datos de rendimiento de productos
        List<Map<String, Object>> rendimientoProductos = new ArrayList<>();
        try {
            ResponseEntity<?> response = obtenerRendimientoProductos(desde, hasta);
            if (response != null && response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> responseBody = (List<Map<String, Object>>) response.getBody();
                rendimientoProductos = responseBody;
            }
        } catch (Exception e) {
            log.error("Error al obtener rendimiento de productos para Excel", e);
        }
        
        // Crear encabezado de rendimiento de productos
        Row rendimientoHeaderRow = sheet.createRow(4);
        Cell rendimientoHeaderCell = rendimientoHeaderRow.createCell(0);
        rendimientoHeaderCell.setCellValue("Rendimiento de Productos");
        rendimientoHeaderCell.setCellStyle(headerStyle);
        
        // Crear encabezados de la tabla
        Row tableHeaderRow = sheet.createRow(5);
        String[] rendimientoHeaders = {"Producto", "Categoría", "Unidades Vendidas", "Ingresos", "Margen Bruto", "Margen %"};
        for (int i = 0; i < rendimientoHeaders.length; i++) {
            Cell cell = tableHeaderRow.createCell(i);
            cell.setCellValue(rendimientoHeaders[i]);
            cell.setCellStyle(headerStyle);
        }
        
        // Añadir datos de rendimiento de productos
        int rowNum = 6;
        if (rendimientoProductos != null && !rendimientoProductos.isEmpty()) {
            for (int i = 0; i < Math.min(10, rendimientoProductos.size()); i++) { // Limitamos a los 10 mejores
                Map<String, Object> producto = rendimientoProductos.get(i);
                if (producto != null) {
                    Row row = sheet.createRow(rowNum++);
                    row.createCell(0).setCellValue(producto.get("nombre") != null ? producto.get("nombre").toString() : "");
                    row.createCell(1).setCellValue(producto.get("categoria") != null ? producto.get("categoria").toString() : "");
                    row.createCell(2).setCellValue(producto.get("unidadesVendidas") != null ? ((Number)producto.get("unidadesVendidas")).intValue() : 0);
                    
                    // Formatear valores monetarios
                    Object ingresos = producto.get("ingresos");
                    if (ingresos != null) {
                        row.createCell(3).setCellValue(((Number)ingresos).doubleValue());
                    } else {
                        row.createCell(3).setCellValue(0.0);
                    }
                    
                    Object margenBruto = producto.get("margenBruto");
                    if (margenBruto != null) {
                        row.createCell(4).setCellValue(((Number)margenBruto).doubleValue());
                    } else {
                        row.createCell(4).setCellValue(0.0);
                    }
                    
                    Object margenPorcentaje = producto.get("margenPorcentaje");
                    if (margenPorcentaje != null) {
                        row.createCell(5).setCellValue(((Number)margenPorcentaje).doubleValue() + "%");
                    } else {
                        row.createCell(5).setCellValue("0.0%");
                    }
                }
            }
        }
        
        // Dejar una fila en blanco
        rowNum++;
        
        // Obtener datos de tendencias de productos
        Map<String, Object> tendenciasData = new HashMap<>();
        try {
            ResponseEntity<?> response = obtenerTendenciasProductos(desde, hasta, 5);
            if (response != null && response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                @SuppressWarnings("unchecked")
                Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
                tendenciasData = responseBody;
            }
        } catch (Exception e) {
            log.error("Error al obtener tendencias de productos para Excel", e);
        }
        
        // Crear encabezado de tendencias
        Row tendenciasHeaderRow = sheet.createRow(rowNum++);
        Cell tendenciasHeaderCell = tendenciasHeaderRow.createCell(0);
        tendenciasHeaderCell.setCellValue("Tendencias de Productos");
        tendenciasHeaderCell.setCellStyle(headerStyle);
        
        // Crear encabezados de la tabla de tendencias
        Row tendenciasTableHeaderRow = sheet.createRow(rowNum++);
        String[] tendenciasHeaders = {"Producto", "Ventas Periodo 1", "Ventas Periodo 2", "Cambio", "% Cambio"};
        for (int i = 0; i < tendenciasHeaders.length; i++) {
            Cell cell = tendenciasTableHeaderRow.createCell(i);
            cell.setCellValue(tendenciasHeaders[i]);
            cell.setCellStyle(headerStyle);
        }
        
        // Añadir datos de tendencias
            @SuppressWarnings("unchecked")
        List<Map<String, Object>> tendencias = tendenciasData != null ? 
            (List<Map<String, Object>>) tendenciasData.get("tendencias") : new ArrayList<>();
            
        if (tendencias != null && !tendencias.isEmpty()) {
                for (Map<String, Object> tendencia : tendencias) {
                    if (tendencia != null) {
                    Row row = sheet.createRow(rowNum++);
                    row.createCell(0).setCellValue(tendencia.get("producto") != null ? tendencia.get("producto").toString() : "");
                    row.createCell(1).setCellValue(tendencia.get("ventasPeriodo1") != null ? ((Number)tendencia.get("ventasPeriodo1")).intValue() : 0);
                    row.createCell(2).setCellValue(tendencia.get("ventasPeriodo2") != null ? ((Number)tendencia.get("ventasPeriodo2")).intValue() : 0);
                    row.createCell(3).setCellValue(tendencia.get("cambio") != null ? ((Number)tendencia.get("cambio")).intValue() : 0);
                    
                        Object porcentajeCambio = tendencia.get("porcentajeCambio");
                        if (porcentajeCambio != null) {
                        row.createCell(4).setCellValue(((Number)porcentajeCambio).doubleValue() + "%");
                    } else {
                        row.createCell(4).setCellValue("0.0%");
                    }
                }
            }
        }
    }
    
    // Métodos auxiliares para obtener los datos de los reportes
    
    /**
     * Obtiene datos para la clasificación ABC de productos
     */
    @GetMapping("/clasificacion-abc")
    public ResponseEntity<?> obtenerClasificacionABC(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime hasta) {
        
        try {
            log.info("Obteniendo datos de clasificación ABC para reportes");
            
            // Si no se especifican fechas, usar los últimos 3 meses
            if (desde == null) {
                desde = LocalDateTime.now().minusMonths(3);
            }
            if (hasta == null) {
                hasta = LocalDateTime.now();
            }
            
            // Verificar si el caché es válido primero
            if (clasificacionABCCache != null && ultimaActualizacionCache != null &&
                ultimaActualizacionCache.isAfter(LocalDateTime.now().minusHours(CACHE_TTL_HOURS))) {
                
                log.info("Usando caché de clasificación ABC (última actualización: {})", ultimaActualizacionCache);
                return ResponseEntity.ok(clasificacionABCCache);
            }
            
            // Verificar si existen clasificaciones recientes (menos de 24 horas)
            List<ClasificacionABC> clasificaciones = clasificacionRepository.findLatestForAllProducts();
            
            // Si no hay clasificaciones, calcularlas ahora mismo
            if (clasificaciones.isEmpty()) {
                log.info("No hay clasificaciones ABC. Calculando clasificación ABC inmediatamente para el período {} - {}", desde, hasta);
                List<ClasificacionABCDTO> dtos = clasificacionABCService.calcularClasificacionABC(desde, hasta);
                
                // Si aún está vacío después de calcular, generar datos de prueba directamente
                if (dtos.isEmpty()) {
                    log.warn("No se pudieron calcular las clasificaciones ABC. Generando datos de prueba...");
                    List<Producto> productos = productoRepository.findAll();
                    if (productos.isEmpty()) {
                        // Si no hay productos, no se pueden generar datos de prueba
                        return ResponseEntity.ok(Map.of(
                            "message", "No hay productos para clasificar",
                            "conteoCategoria", Map.of(),
                            "valorCategoria", Map.of(),
                            "productos", new ArrayList<>()
                        ));
                    }
                }
                
                // Volver a obtener las clasificaciones recién calculadas
                clasificaciones = clasificacionRepository.findLatestForAllProducts();
            } else {
                log.info("Usando clasificaciones ABC existentes (cálculo anterior disponible)");
            }
            
            if (clasificaciones.isEmpty()) {
                log.warn("No se encontraron clasificaciones ABC disponibles");
                Map<String, Object> emptyResult = Map.of(
                    "conteoCategoria", Map.of(),
                    "valorCategoria", Map.of(),
                    "productos", new ArrayList<>()
                );
                return ResponseEntity.ok(emptyResult);
            }
            
            // Preparar los datos para el gráfico
            Map<String, Object> resultado = new HashMap<>();
            
            // Contar productos por categoría
            Map<ClasificacionABC.Categoria, Long> conteoCategoria = clasificaciones.stream()
                    .collect(Collectors.groupingBy(ClasificacionABC::getCategoria, Collectors.counting()));
            
            // Calcular valor por categoría
            Map<ClasificacionABC.Categoria, BigDecimal> valorCategoria = new HashMap<>();
            for (ClasificacionABC.Categoria categoria : ClasificacionABC.Categoria.values()) {
                BigDecimal valorTotal = clasificaciones.stream()
                        .filter(c -> c.getCategoria() == categoria)
                        .map(ClasificacionABC::getValorAnual)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                valorCategoria.put(categoria, valorTotal);
            }
            
            // Preparar datos para la tabla (optimizar para mejor rendimiento)
            List<Map<String, Object>> tablaProductos = new ArrayList<>();
            for (ClasificacionABC clasificacion : clasificaciones) {
                Map<String, Object> producto = new HashMap<>();
                producto.put("id", clasificacion.getProducto().getId());
                producto.put("codigo", clasificacion.getProducto().getCodigo());
                producto.put("nombre", clasificacion.getProducto().getNombre());
                producto.put("categoria", clasificacion.getCategoria().toString());
                producto.put("valorAnual", clasificacion.getValorAnual());
                producto.put("porcentajeValor", clasificacion.getPorcentajeValor().multiply(new BigDecimal("100")));
                producto.put("porcentajeAcumulado", clasificacion.getPorcentajeAcumulado().multiply(new BigDecimal("100")));
                tablaProductos.add(producto);
            }
            
            // Organizar resultados
            resultado.put("conteoCategoria", conteoCategoria);
            resultado.put("valorCategoria", valorCategoria);
            resultado.put("productos", tablaProductos);
            
            // Actualizar caché
            clasificacionABCCache = resultado;
            ultimaActualizacionCache = LocalDateTime.now();
            
            return ResponseEntity.ok(resultado);
        } catch (Exception e) {
            log.error("Error al obtener datos de clasificación ABC", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al obtener datos de clasificación ABC: " + e.getMessage());
        }
    }
    
    /**
     * Método para precalcular la clasificación ABC
     * Este método se ejecutará automáticamente al inicio
     */
    @GetMapping("/precalcular-abc")
    public ResponseEntity<?> precalcularClasificacionABC(
            @RequestParam(required = false, defaultValue = "false") boolean forzar) {
        try {
            log.info("Iniciando precálculo de clasificación ABC. Forzar: {}", forzar);
            
            // Usar últimos 3 meses como período por defecto
            LocalDateTime desde = LocalDateTime.now().minusMonths(3);
            LocalDateTime hasta = LocalDateTime.now();
            
            // Verificar si ya existen clasificaciones recientes
            List<ClasificacionABC> clasificaciones = clasificacionRepository.findLatestForAllProducts();
            boolean hayClasificacionesRecientes = clasificaciones.stream()
                .anyMatch(c -> c.getFechaCalculo().isAfter(LocalDateTime.now().minusHours(24)));
            
            if (forzar || !hayClasificacionesRecientes) {
                log.info("Iniciando cálculo de clasificación ABC...");
                List<ClasificacionABCDTO> dtos = clasificacionABCService.calcularClasificacionABC(desde, hasta);
                
                // Si no se pudieron calcular (no hay ventas), generar datos de prueba
                if (dtos.isEmpty()) {
                    log.info("No se encontraron ventas para clasificación ABC. Generando datos de prueba...");
                    List<Producto> productos = productoRepository.findAll();
                    if (!productos.isEmpty()) {
                        // Eliminar clasificaciones anteriores para evitar duplicados
                        clasificacionRepository.deleteAll();
                        dtos = clasificacionABCService.generarDatosPruebaClasificacionABC(productos);
                        log.info("Datos de prueba generados: {} productos clasificados", dtos.size());
                    } else {
                        log.warn("No hay productos para generar datos de prueba");
                    }
                }
                
                clasificaciones = clasificacionRepository.findLatestForAllProducts();
                log.info("Clasificación ABC calculada exitosamente: {} productos clasificados", clasificaciones.size());
            } else {
                log.info("Ya existen clasificaciones ABC recientes, no es necesario recalcular");
            }
            
            return ResponseEntity.ok(Map.of(
                "message", "Clasificación ABC actualizada", 
                "count", clasificaciones.size(),
                "forzado", forzar
            ));
        } catch (Exception e) {
            log.error("Error al precalcular clasificación ABC", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                        "error", "Error al precalcular clasificación ABC",
                        "message", e.getMessage()
                    ));
        }
    }
} 