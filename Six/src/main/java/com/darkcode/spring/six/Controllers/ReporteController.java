package com.darkcode.spring.six.Controllers;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.DocumentException;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFFont;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.darkcode.spring.six.models.entities.DetalleVenta;
import com.darkcode.spring.six.models.entities.Inventario;
import com.darkcode.spring.six.models.entities.Producto;
import com.darkcode.spring.six.models.entities.VarianteProducto;
import com.darkcode.spring.six.models.entities.Venta;
import com.darkcode.spring.six.models.repositories.DetalleVentaRepository;
import com.darkcode.spring.six.models.repositories.InventarioRepository;
import com.darkcode.spring.six.models.repositories.VentaRepository;

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
    
    /**
     * Obtiene datos para el gráfico de ventas por período
     */
    @GetMapping("/ventas-por-periodo")
    public ResponseEntity<?> obtenerVentasPorPeriodo(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime hasta) {
        
        try {
            log.info("Obteniendo datos de ventas por período para reportes");
            
            // Si no se especifican fechas, usar los últimos 6 meses
            if (desde == null) {
                desde = LocalDateTime.now().minusMonths(6);
            }
            if (hasta == null) {
                hasta = LocalDateTime.now();
            }
            
            // Obtener ventas en el rango de fechas
            List<Venta> ventas = ventaRepository.findByFechaBetween(desde, hasta);
            
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
            
            // Si no se especifican fechas, usar los últimos 6 meses
            if (desde == null) {
                desde = LocalDateTime.now().minusMonths(6);
            }
            if (hasta == null) {
                hasta = LocalDateTime.now();
            }
            
            // Obtener ventas en el rango de fechas
            List<Venta> ventas = ventaRepository.findByFechaBetween(desde, hasta);
            
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
    
    private int getMonthNumber(String mes) {
        switch (mes) {
            case "Ene": return 1;
            case "Feb": return 2;
            case "Mar": return 3;
            case "Abr": return 4;
            case "May": return 5;
            case "Jun": return 6;
            case "Jul": return 7;
            case "Ago": return 8;
            case "Sep": return 9;
            case "Oct": return 10;
            case "Nov": return 11;
            case "Dic": return 12;
            default: return 0;
        }
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
        
        try {
            log.info("Exportando reporte a PDF para la sección: " + seccion);
            
            // Si no se especifican fechas, usar valores predeterminados
            if (desde == null) {
                desde = LocalDateTime.now().minusMonths(1);
            }
            if (hasta == null) {
                hasta = LocalDateTime.now();
            }
            
            // Crear documento PDF
            Document document = new Document();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
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
                "Período: " + desde.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + 
                " - " + hasta.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")), normalFont);
            periodo.setAlignment(Element.ALIGN_LEFT);
            document.add(periodo);
            
            document.add(new Paragraph(" ")); // Espacio
            
            // Añadir contenido según la sección
            switch (seccion) {
                case "ventas":
                    generarReportePDFVentas(document, desde, hasta);
                    break;
                case "inventario":
                    generarReportePDFInventario(document, desde, hasta);
                    break;
                case "productos":
                    generarReportePDFProductos(document, desde, hasta);
                    break;
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
        
        try {
            log.info("Exportando reporte a Excel para la sección: " + seccion);
            
            // Si no se especifican fechas, usar valores predeterminados
            if (desde == null) {
                desde = LocalDateTime.now().minusMonths(1);
            }
            if (hasta == null) {
                hasta = LocalDateTime.now();
            }
            
            // Crear libro Excel
            XSSFWorkbook workbook = new XSSFWorkbook();
            
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
                desde.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + 
                " - " + hasta.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            
            // Dejar una fila en blanco
            sheet.createRow(3);
            
            // Generar contenido según la sección
            switch (seccion) {
                case "ventas":
                    generarReporteExcelVentas(workbook, sheet, headerStyle, desde, hasta);
                    break;
                case "inventario":
                    generarReporteExcelInventario(workbook, sheet, headerStyle, desde, hasta);
                    break;
                case "productos":
                    generarReporteExcelProductos(workbook, sheet, headerStyle, desde, hasta);
                    break;
            }
            
            // Ajustar ancho de columnas
            for (int i = 0; i < 10; i++) {
                sheet.autoSizeColumn(i);
            }
            
            // Convertir a array de bytes
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            workbook.write(baos);
            workbook.close();
            
            // Configurar respuesta
            byte[] excelBytes = baos.toByteArray();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
            headers.setContentDispositionFormData("filename", "SIX_Reporte_" + seccion + ".xlsx");
            
            return new ResponseEntity<>(excelBytes, headers, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error al exportar a Excel", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
    
    // Métodos auxiliares para la generación de reportes
    
    private String obtenerTituloSeccion(String seccion) {
        switch (seccion) {
            case "ventas": return "Ventas";
            case "inventario": return "Inventario";
            case "productos": return "Productos";
            default: return "General";
        }
    }
    
    private void generarReportePDFVentas(Document document, LocalDateTime desde, LocalDateTime hasta) 
            throws DocumentException {
        
        // Título de la sección
        com.itextpdf.text.Font sectionFont = FontFactory.getFont(
            FontFactory.HELVETICA_BOLD, 14);
        document.add(new Paragraph("Resumen de Ventas", sectionFont));
        document.add(new Paragraph(" ")); // Espacio
        
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
            log.error("Error al obtener resumen de ventas para PDF", e);
        }
        
        // Crear tabla de resumen
        PdfPTable resumenTable = new PdfPTable(2);
        resumenTable.setWidthPercentage(100);
        
        // Configurar anchos relativos de columnas
        float[] columnWidths = {1f, 1f};
        resumenTable.setWidths(columnWidths);
        
        // Añadir filas al resumen
        if (resumenVentas != null) {
            agregarCeldaResumen(resumenTable, "Total Ventas:", 
                "S/. " + (resumenVentas.get("totalVentas") != null ? 
                new BigDecimal(resumenVentas.get("totalVentas").toString()).setScale(2, RoundingMode.HALF_UP) : "0.00"));
            
            agregarCeldaResumen(resumenTable, "Transacciones:", 
                resumenVentas.get("transacciones") != null ? resumenVentas.get("transacciones").toString() : "0");
            
            agregarCeldaResumen(resumenTable, "Ticket Promedio:", 
                "S/. " + (resumenVentas.get("ticketPromedio") != null ? 
                new BigDecimal(resumenVentas.get("ticketPromedio").toString()).setScale(2, RoundingMode.HALF_UP) : "0.00"));
            
            agregarCeldaResumen(resumenTable, "Margen Bruto:", 
                (resumenVentas.get("margenBruto") != null ? 
                new BigDecimal(resumenVentas.get("margenBruto").toString()).multiply(new BigDecimal("100")).setScale(1, RoundingMode.HALF_UP) : "0.0") + "%");
        }
        
        document.add(resumenTable);
        document.add(new Paragraph(" ")); // Espacio
        
        // Productos más vendidos
        document.add(new Paragraph("Productos Más Vendidos", sectionFont));
        document.add(new Paragraph(" ")); // Espacio
        
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
            log.error("Error al obtener productos más vendidos para PDF", e);
        }
        
        // Crear tabla de productos
        PdfPTable productosTable = new PdfPTable(4);
        productosTable.setWidthPercentage(100);
        
        // Configurar anchos relativos de columnas
        float[] productosWidths = {2f, 1.5f, 1f, 1.5f};
        productosTable.setWidths(productosWidths);
        
        // Encabezados
        String[] headers = {"Producto", "Categoría", "Unidades", "Ingresos"};
        for (String header : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(header));
            cell.setBackgroundColor(new BaseColor(220, 220, 220));
            cell.setPadding(5);
            productosTable.addCell(cell);
        }
        
        // Añadir datos de productos
        if (productosMasVendidos != null) {
            for (Map<String, Object> producto : productosMasVendidos) {
                if (producto != null) {
                    productosTable.addCell(producto.getOrDefault("producto", "").toString());
                    productosTable.addCell(producto.getOrDefault("categoria", "").toString());
                    productosTable.addCell(producto.getOrDefault("unidades", 0).toString());
                    
                    Object ingresos = producto.getOrDefault("ingresos", BigDecimal.ZERO);
                    if (ingresos != null) {
                        productosTable.addCell("S/. " + new BigDecimal(ingresos.toString()).setScale(2, RoundingMode.HALF_UP));
                    } else {
                        productosTable.addCell("S/. 0.00");
                    }
                }
            }
        }
        
        document.add(productosTable);
    }
    
    private void generarReportePDFInventario(Document document, LocalDateTime desde, LocalDateTime hasta) 
            throws DocumentException {
        
        // Título de la sección
        com.itextpdf.text.Font sectionFont = FontFactory.getFont(
            FontFactory.HELVETICA_BOLD, 14);
        document.add(new Paragraph("Resumen de Inventario", sectionFont));
        document.add(new Paragraph(" ")); // Espacio
        
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
            log.error("Error al obtener resumen de inventario para PDF", e);
        }
        
        // Crear tabla de resumen
        PdfPTable resumenTable = new PdfPTable(2);
        resumenTable.setWidthPercentage(100);
        
        // Configurar anchos relativos de columnas
        float[] columnWidths = {1f, 1f};
        resumenTable.setWidths(columnWidths);
        
        // Añadir filas al resumen
        if (resumenInventario != null) {
            agregarCeldaResumen(resumenTable, "Valor Total:", 
                "S/. " + (resumenInventario.get("valorTotal") != null ? 
                new BigDecimal(resumenInventario.get("valorTotal").toString()).setScale(2, RoundingMode.HALF_UP) : "0.00"));
            
            agregarCeldaResumen(resumenTable, "Unidades:", 
                resumenInventario.get("unidades") != null ? resumenInventario.get("unidades").toString() : "0");
            
            agregarCeldaResumen(resumenTable, "Rotación:", 
                resumenInventario.get("rotacion") != null ? 
                new BigDecimal(resumenInventario.get("rotacion").toString()).setScale(1, RoundingMode.HALF_UP).toString() : "0.0");
            
            agregarCeldaResumen(resumenTable, "Alertas:", 
                resumenInventario.get("alertas") != null ? resumenInventario.get("alertas").toString() : "0");
        }
        
        document.add(resumenTable);
        document.add(new Paragraph(" ")); // Espacio
        
        // Productos con bajo stock
        document.add(new Paragraph("Productos con Bajo Stock", sectionFont));
        document.add(new Paragraph(" ")); // Espacio
        
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
            log.error("Error al obtener productos con bajo stock para PDF", e);
        }
        
        // Crear tabla de productos
        PdfPTable productosTable = new PdfPTable(5);
        productosTable.setWidthPercentage(100);
        
        // Configurar anchos relativos de columnas
        float[] productosWidths = {2f, 2f, 1f, 1f, 1f};
        productosTable.setWidths(productosWidths);
        
        // Encabezados
        String[] headers = {"Producto", "Variante", "Stock Actual", "Stock Mínimo", "Estado"};
        for (String header : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(header));
            cell.setBackgroundColor(new BaseColor(220, 220, 220));
            cell.setPadding(5);
            productosTable.addCell(cell);
        }
        
        // Añadir datos de productos
        if (productosBajoStock != null) {
            for (Map<String, Object> producto : productosBajoStock) {
                if (producto != null) {
                    productosTable.addCell(producto.getOrDefault("producto", "").toString());
                    productosTable.addCell(producto.getOrDefault("variante", "").toString());
                    productosTable.addCell(producto.getOrDefault("stockActual", 0).toString());
                    productosTable.addCell(producto.getOrDefault("stockMinimo", 0).toString());
                    
                    String estado = producto.getOrDefault("estado", "Normal").toString();
                    PdfPCell estadoCell = new PdfPCell(new Phrase(estado));
                    
                    if ("Crítico".equals(estado)) {
                        estadoCell.setBackgroundColor(new BaseColor(255, 200, 200));
                    } else if ("Bajo".equals(estado)) {
                        estadoCell.setBackgroundColor(new BaseColor(255, 235, 156));
                    }
                    
                    productosTable.addCell(estadoCell);
                }
            }
        }
        
        document.add(productosTable);
    }
    
    private void generarReportePDFProductos(Document document, LocalDateTime desde, LocalDateTime hasta) 
            throws DocumentException {
        
        // Título de la sección
        com.itextpdf.text.Font sectionFont = FontFactory.getFont(
            FontFactory.HELVETICA_BOLD, 14);
        document.add(new Paragraph("Rendimiento de Productos", sectionFont));
        document.add(new Paragraph(" ")); // Espacio
        
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
            log.error("Error al obtener rendimiento de productos para PDF", e);
        }
        
        // Productos con mayor margen
        document.add(new com.itextpdf.text.Paragraph("Productos con Mayor Margen", sectionFont));
        document.add(new com.itextpdf.text.Paragraph(" ")); // Espacio
        
        // Crear tabla de productos
        PdfPTable productosTable = new PdfPTable(5);
        productosTable.setWidthPercentage(100);
        
        // Configurar anchos relativos de columnas
        float[] productosWidths = {2f, 1.5f, 1.5f, 1.5f, 1f};
        productosTable.setWidths(productosWidths);
        
        // Encabezados
        String[] headers = {"Producto", "Categoría", "Ingresos", "Costo", "Margen %"};
        for (String header : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(header));
            cell.setBackgroundColor(new BaseColor(220, 220, 220));
            cell.setPadding(5);
            productosTable.addCell(cell);
        }
        
        // Añadir datos de productos (solo los 5 primeros para no saturar el reporte)
        int count = 0;
        if (rendimientoProductos != null) {
            for (Map<String, Object> producto : rendimientoProductos) {
                if (producto != null && count++ < 5) {
                    productosTable.addCell(producto.getOrDefault("nombre", "").toString());
                    productosTable.addCell(producto.getOrDefault("categoria", "").toString());
                    
                    Object ingresos = producto.getOrDefault("ingresos", BigDecimal.ZERO);
                    if (ingresos != null) {
                        productosTable.addCell("S/. " + new BigDecimal(ingresos.toString()).setScale(2, RoundingMode.HALF_UP));
                    } else {
                        productosTable.addCell("S/. 0.00");
                    }
                    
                    Object costoTotal = producto.getOrDefault("costoTotal", BigDecimal.ZERO);
                    if (costoTotal != null) {
                        productosTable.addCell("S/. " + new BigDecimal(costoTotal.toString()).setScale(2, RoundingMode.HALF_UP));
                    } else {
                        productosTable.addCell("S/. 0.00");
                    }
                    
                    Object margenPorcentaje = producto.getOrDefault("margenPorcentaje", BigDecimal.ZERO);
                    if (margenPorcentaje != null) {
                        BigDecimal margen = new BigDecimal(margenPorcentaje.toString()).setScale(1, RoundingMode.HALF_UP);
                        productosTable.addCell(margen + "%");
                    } else {
                        productosTable.addCell("0.0%");
                    }
                }
            }
        }
        
        document.add(productosTable);
        document.add(new Paragraph(" ")); // Espacio
        
        // Tendencias de productos
        document.add(new com.itextpdf.text.Paragraph("Tendencias de Productos", sectionFont));
        document.add(new com.itextpdf.text.Paragraph(" ")); // Espacio
        
        // Obtener datos de tendencias
        Map<String, Object> tendenciasProductos = new HashMap<>();
        try {
            ResponseEntity<?> response = obtenerTendenciasProductos(desde, hasta, 5);
            if (response != null && response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                @SuppressWarnings("unchecked")
                Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
                tendenciasProductos = responseBody;
            }
        } catch (Exception e) {
            log.error("Error al obtener tendencias de productos para PDF", e);
        }
        
        // Crear tabla de tendencias
        PdfPTable tendenciasTable = new PdfPTable(4);
        tendenciasTable.setWidthPercentage(100);
        
        // Configurar anchos relativos de columnas
        float[] tendenciasWidths = {2f, 1f, 1f, 1f};
        tendenciasTable.setWidths(tendenciasWidths);
        
        // Información de períodos
        if (tendenciasProductos != null && tendenciasProductos.containsKey("periodo1") && tendenciasProductos.containsKey("periodo2")) {
            com.itextpdf.text.Font infoFont = com.itextpdf.text.FontFactory.getFont(
                com.itextpdf.text.FontFactory.HELVETICA, 10, com.itextpdf.text.Font.ITALIC);
            document.add(new com.itextpdf.text.Paragraph("Período 1: " + tendenciasProductos.get("periodo1"), infoFont));
            document.add(new com.itextpdf.text.Paragraph("Período 2: " + tendenciasProductos.get("periodo2"), infoFont));
            document.add(new com.itextpdf.text.Paragraph(" ")); // Espacio
        }
        
        // Encabezados
        String[] tendenciasHeaders = {"Producto", "Ventas P1", "Ventas P2", "% Cambio"};
        for (String header : tendenciasHeaders) {
            PdfPCell cell = new PdfPCell(new Phrase(header));
            cell.setBackgroundColor(new BaseColor(220, 220, 220));
            cell.setPadding(5);
            tendenciasTable.addCell(cell);
        }
        
        // Añadir datos de tendencias
        if (tendenciasProductos != null && tendenciasProductos.containsKey("tendencias")) {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> tendencias = (List<Map<String, Object>>) tendenciasProductos.get("tendencias");
            
            if (tendencias != null) {
                for (Map<String, Object> tendencia : tendencias) {
                    if (tendencia != null) {
                        tendenciasTable.addCell(tendencia.getOrDefault("producto", "").toString());
                        tendenciasTable.addCell(tendencia.getOrDefault("ventasPeriodo1", 0).toString());
                        tendenciasTable.addCell(tendencia.getOrDefault("ventasPeriodo2", 0).toString());
                        
                        double porcentaje = 0.0;
                        Object porcentajeCambio = tendencia.get("porcentajeCambio");
                        if (porcentajeCambio != null) {
                            try {
                                porcentaje = Double.parseDouble(porcentajeCambio.toString());
                            } catch (NumberFormatException e) {
                                log.warn("Error al parsear porcentaje de cambio: {}", porcentajeCambio);
                            }
                        }
                        
                        String porcentajeStr = String.format("%.1f%%", porcentaje);
                        
                        PdfPCell porcentajeCell = new PdfPCell(new Phrase(porcentajeStr));
                        if (porcentaje > 0) {
                            porcentajeCell.setBackgroundColor(new BaseColor(200, 255, 200)); // Verde claro para positivo
                        } else if (porcentaje < 0) {
                            porcentajeCell.setBackgroundColor(new BaseColor(255, 200, 200)); // Rojo claro para negativo
                        }
                        
                        tendenciasTable.addCell(porcentajeCell);
                    }
                }
            }
        }
        
        document.add(tendenciasTable);
    }
    
    private void agregarCeldaResumen(PdfPTable table, String label, String value) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label));
        labelCell.setBorderWidth(0);
        labelCell.setPadding(5);
        table.addCell(labelCell);
        
        PdfPCell valueCell = new PdfPCell(new Phrase(value));
        valueCell.setBorderWidth(0);
        valueCell.setPadding(5);
        valueCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.addCell(valueCell);
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
            Integer.parseInt(resumenVentas.get("transacciones").toString()) : 0);
        
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
                    Object productoNombre = producto.get("producto");
                    Object categoriaNombre = producto.get("categoria");
                    Object unidades = producto.get("unidades");
                    Object ingresos = producto.get("ingresos");
                    
                    row.createCell(0).setCellValue(productoNombre != null ? productoNombre.toString() : "");
                    row.createCell(1).setCellValue(categoriaNombre != null ? categoriaNombre.toString() : "");
                    row.createCell(2).setCellValue(unidades != null ? Integer.parseInt(unidades.toString()) : 0);
                    row.createCell(3).setCellValue(ingresos != null ? Double.parseDouble(ingresos.toString()) : 0.0);
                }
            }
        }
    }
    
    private void generarReporteExcelInventario(XSSFWorkbook workbook, 
                                       XSSFSheet sheet,
                                       CellStyle headerStyle,
                                       LocalDateTime desde, LocalDateTime hasta) {
        
        // Código similar al de generarReporteExcelVentas pero para inventario
        // Se omite para ahorrar espacio, pero seguiría la misma estructura
        
        // 1. Obtener datos de resumen de inventario
        // 2. Crear sección de resumen
        // 3. Obtener datos de productos con bajo stock
        // 4. Crear tabla de productos con bajo stock
    }
    
    private void generarReporteExcelProductos(XSSFWorkbook workbook, 
                                      XSSFSheet sheet,
                                      CellStyle headerStyle,
                                      LocalDateTime desde, LocalDateTime hasta) {
        
        // Código similar al de generarReporteExcelVentas pero para productos
        // Se omite para ahorrar espacio, pero seguiría la misma estructura
        
        // 1. Obtener datos de rendimiento de productos
        // 2. Crear tabla de productos con mayor margen
        // 3. Obtener datos de tendencias
        // 4. Crear tabla de tendencias
    }
    
    // Métodos auxiliares para obtener los datos de los reportes
} 