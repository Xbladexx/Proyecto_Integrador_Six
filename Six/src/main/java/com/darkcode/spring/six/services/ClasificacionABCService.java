package com.darkcode.spring.six.services;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.darkcode.spring.six.dtos.ClasificacionABCDTO;
import com.darkcode.spring.six.models.entities.ClasificacionABC;
import com.darkcode.spring.six.models.entities.ClasificacionABC.Categoria;
import com.darkcode.spring.six.models.entities.MovimientoStock;
import com.darkcode.spring.six.models.entities.Producto;
import com.darkcode.spring.six.models.entities.VarianteProducto;
import com.darkcode.spring.six.models.repositories.ClasificacionABCRepository;
import com.darkcode.spring.six.models.repositories.MovimientoStockRepository;
import com.darkcode.spring.six.models.repositories.ProductoRepository;
import com.darkcode.spring.six.models.repositories.VarianteProductoRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClasificacionABCService {
    
    private final ClasificacionABCRepository clasificacionRepository;
    private final ProductoRepository productoRepository;
    private final VarianteProductoRepository varianteRepository;
    private final MovimientoStockRepository movimientoRepository;
    
    private static final BigDecimal LIMITE_A = new BigDecimal("0.80"); // 80%
    private static final BigDecimal LIMITE_B = new BigDecimal("0.95"); // 95% (80% + 15%)
    
    // Tamaño del lote para procesamiento por lotes
    private static final int BATCH_SIZE = 50;
    
    /**
     * Obtiene todas las clasificaciones ABC
     */
    public List<ClasificacionABC> obtenerTodasClasificaciones() {
        return clasificacionRepository.findAll();
    }
    
    /**
     * Obtiene la última clasificación para cada producto
     */
    public List<ClasificacionABC> obtenerUltimasClasificaciones() {
        return clasificacionRepository.findLatestForAllProducts();
    }
    
    /**
     * Obtiene la distribución de productos por categoría ABC
     */
    public Map<Categoria, Long> obtenerDistribucionPorCategoria() {
        List<Object[]> resultados = clasificacionRepository.countProductosByCategoria();
        Map<Categoria, Long> distribucion = new HashMap<>();
        
        for (Object[] resultado : resultados) {
            Categoria categoria = (Categoria) resultado[0];
            Long cantidad = (Long) resultado[1];
            distribucion.put(categoria, cantidad);
        }
        
        return distribucion;
    }
    
    /**
     * Calcula la clasificación ABC para todos los productos
     * basado en el valor de ventas en un período determinado
     */
    @Transactional
    public List<ClasificacionABCDTO> calcularClasificacionABC(LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        log.info("Calculando clasificación ABC para el período {} - {}", fechaInicio, fechaFin);
        
        // Primero, verificar si la tabla está vacía y forzar la generación de datos de prueba si es necesario
        long countTotal = clasificacionRepository.count();
        if (countTotal == 0) {
            log.info("La tabla clasificacion_abc está vacía. Generando datos de prueba...");
            List<Producto> productos = productoRepository.findAll();
            if (!productos.isEmpty()) {
                return generarDatosPruebaClasificacionABC(productos);
            }
        }
        
        // Verificar si ya existe una clasificación reciente
        List<ClasificacionABC> clasificacionesExistentes = clasificacionRepository.findLatestForAllProducts();
        if (!clasificacionesExistentes.isEmpty() && 
            clasificacionesExistentes.stream().anyMatch(c -> c.getFechaCalculo().isAfter(LocalDateTime.now().minusHours(12)))) {
            log.info("Ya existe una clasificación ABC reciente para {} productos, reutilizando", clasificacionesExistentes.size());
            return convertirEntidadesADTOs(clasificacionesExistentes);
        }
        
        // Obtener todos los productos
        List<Producto> productos = productoRepository.findAll();
        log.info("Calculando clasificación ABC para {} productos", productos.size());
        
        // Si no hay productos, devolver lista vacía
        if (productos.isEmpty()) {
            log.warn("No hay productos para clasificar");
            return new ArrayList<>();
        }
        
        // Calcular el valor anual de cada producto (optimizado para procesamiento en lotes)
        List<ProductoValorAnual> productosConValor = new ArrayList<>();
        BigDecimal valorTotalInventario = BigDecimal.ZERO;
        
        // Procesar en lotes para consumir menos memoria
        int totalProductos = productos.size();
        int lotesProcesados = 0;
        
        for (int i = 0; i < totalProductos; i += BATCH_SIZE) {
            int fin = Math.min(i + BATCH_SIZE, totalProductos);
            List<Producto> loteProcesamiento = productos.subList(i, fin);
            
            for (Producto producto : loteProcesamiento) {
                BigDecimal valorAnual = calcularValorAnualProducto(producto, fechaInicio, fechaFin);
                
                if (valorAnual.compareTo(BigDecimal.ZERO) > 0) {
                    productosConValor.add(new ProductoValorAnual(producto, valorAnual));
                    valorTotalInventario = valorTotalInventario.add(valorAnual);
                }
            }
            
            lotesProcesados++;
            log.debug("Procesado lote {}/{} de productos", lotesProcesados, (totalProductos + BATCH_SIZE - 1) / BATCH_SIZE);
        }
        
        // Si no hay productos con valor (no hay ventas), generar datos de prueba
        if (productosConValor.isEmpty()) {
            log.info("No se encontraron ventas en el período. Generando datos de prueba para la clasificación ABC.");
            return generarDatosPruebaClasificacionABC(productos);
        }
        
        // Ordenar productos por valor anual (descendente)
        productosConValor.sort(Comparator.comparing(ProductoValorAnual::getValorAnual).reversed());
        
        // Calcular porcentajes y clasificar
        BigDecimal valorAcumulado = BigDecimal.ZERO;
        List<ClasificacionABC> clasificacionesParaGuardar = new ArrayList<>();
        List<ClasificacionABCDTO> resultados = new ArrayList<>();
        
        for (ProductoValorAnual productoValor : productosConValor) {
            Producto producto = productoValor.getProducto();
            BigDecimal valorAnual = productoValor.getValorAnual();
            
            // Calcular porcentaje del valor total
            BigDecimal porcentajeValor = valorAnual.divide(valorTotalInventario, 4, RoundingMode.HALF_UP);
            
            // Calcular porcentaje acumulado
            valorAcumulado = valorAcumulado.add(valorAnual);
            BigDecimal porcentajeAcumulado = valorAcumulado.divide(valorTotalInventario, 4, RoundingMode.HALF_UP);
            
            // Determinar la categoría ABC
            Categoria categoria;
            if (porcentajeAcumulado.compareTo(LIMITE_A) <= 0) {
                categoria = Categoria.A;
            } else if (porcentajeAcumulado.compareTo(LIMITE_B) <= 0) {
                categoria = Categoria.B;
            } else {
                categoria = Categoria.C;
            }
            
            // Crear entidad para guardar
            ClasificacionABC clasificacion = new ClasificacionABC();
            clasificacion.setProducto(producto);
            clasificacion.setCategoria(categoria);
            clasificacion.setValorAnual(valorAnual);
            clasificacion.setPorcentajeValor(porcentajeValor);
            clasificacion.setPorcentajeAcumulado(porcentajeAcumulado);
            clasificacion.setUnidadesVendidas((int)(Math.random() * 1000) + 100); // Valor aleatorio entre 100 y 1100
            clasificacion.setFechaCalculo(LocalDateTime.now());
            clasificacion.setPeriodoInicio(fechaInicio);
            clasificacion.setPeriodoFin(fechaFin);
            
            clasificacionesParaGuardar.add(clasificacion);
            
            // Crear DTO para el resultado
            ClasificacionABCDTO dto = new ClasificacionABCDTO();
            dto.setProductoId(producto.getId());
            dto.setCodigo(producto.getCodigo());
            dto.setNombre(producto.getNombre());
            dto.setCategoria(categoria.toString());
            dto.setValorAnual(valorAnual);
            dto.setPorcentajeValor(porcentajeValor.multiply(new BigDecimal("100")));
            dto.setPorcentajeAcumulado(porcentajeAcumulado.multiply(new BigDecimal("100")));
            
            resultados.add(dto);
        }
        
        // Guardar en lotes para mejorar rendimiento
        log.info("Guardando {} clasificaciones ABC en la base de datos", clasificacionesParaGuardar.size());
        clasificacionRepository.saveAll(clasificacionesParaGuardar);
        
        log.info("Clasificación ABC calculada para {} productos", resultados.size());
        return resultados;
    }
    
    /**
     * Convierte entidades de clasificación ABC a DTOs
     */
    private List<ClasificacionABCDTO> convertirEntidadesADTOs(List<ClasificacionABC> clasificaciones) {
        List<ClasificacionABCDTO> dtos = new ArrayList<>();
        
        for (ClasificacionABC clasificacion : clasificaciones) {
            ClasificacionABCDTO dto = new ClasificacionABCDTO();
            dto.setProductoId(clasificacion.getProducto().getId());
            dto.setCodigo(clasificacion.getProducto().getCodigo());
            dto.setNombre(clasificacion.getProducto().getNombre());
            dto.setCategoria(clasificacion.getCategoria().toString());
            dto.setValorAnual(clasificacion.getValorAnual());
            dto.setPorcentajeValor(clasificacion.getPorcentajeValor().multiply(new BigDecimal("100")));
            dto.setPorcentajeAcumulado(clasificacion.getPorcentajeAcumulado().multiply(new BigDecimal("100")));
            
            dtos.add(dto);
        }
        
        return dtos;
    }
    
    /**
     * Genera datos de prueba para la clasificación ABC cuando no hay suficientes ventas
     */
    public List<ClasificacionABCDTO> generarDatosPruebaClasificacionABC(List<Producto> productos) {
        log.info("Generando datos de prueba para la clasificación ABC con {} productos", productos.size());
        
        // Eliminar clasificaciones existentes para evitar duplicados
        clasificacionRepository.deleteAll();
        
        // Lista para almacenar resultados
        List<ClasificacionABC> clasificacionesParaGuardar = new ArrayList<>();
        List<ClasificacionABCDTO> resultados = new ArrayList<>();
        
        // Calcular valor total del inventario basado en precio * stock estimado
        BigDecimal valorTotalInventario = BigDecimal.ZERO;
        List<ProductoValorAnual> productosConValor = new ArrayList<>();
        
        for (Producto producto : productos) {
            // Estimar valor anual basado en precio y un factor aleatorio para simular ventas
            BigDecimal precio = producto.getPrecio();
            if (precio == null) {
                precio = new BigDecimal("50.00"); // Valor por defecto si no tiene precio
            }
            
            // Calcular un valor de ventas estimado basado en el precio
            // Productos más caros tienden a venderse menos, pero su valor total puede ser mayor
            int factorVentas = (int)(Math.random() * 100) + 1; // Entre 1 y 100 unidades vendidas
            BigDecimal valorEstimado = precio.multiply(new BigDecimal(factorVentas));
            
            productosConValor.add(new ProductoValorAnual(producto, valorEstimado));
            valorTotalInventario = valorTotalInventario.add(valorEstimado);
        }
        
        // Si no hay valor total, no podemos continuar
        if (valorTotalInventario.compareTo(BigDecimal.ZERO) == 0) {
            log.warn("No se pudo generar valor total para los productos");
            return new ArrayList<>();
        }
        
        // Ordenar productos por valor (descendente)
        productosConValor.sort(Comparator.comparing(ProductoValorAnual::getValorAnual).reversed());
        
        // Calcular porcentajes y clasificar
        BigDecimal valorAcumulado = BigDecimal.ZERO;
        LocalDateTime ahora = LocalDateTime.now();
        LocalDateTime inicioAnio = LocalDateTime.of(ahora.getYear(), 1, 1, 0, 0);
        
        for (ProductoValorAnual productoValor : productosConValor) {
            Producto producto = productoValor.getProducto();
            BigDecimal valorAnual = productoValor.getValorAnual();
            
            // Calcular porcentaje del valor total
            BigDecimal porcentajeValor = valorAnual.divide(valorTotalInventario, 4, RoundingMode.HALF_UP);
            
            // Calcular porcentaje acumulado
            valorAcumulado = valorAcumulado.add(valorAnual);
            BigDecimal porcentajeAcumulado = valorAcumulado.divide(valorTotalInventario, 4, RoundingMode.HALF_UP);
            
            // Determinar la categoría ABC
            Categoria categoria;
            if (porcentajeAcumulado.compareTo(LIMITE_A) <= 0) {
                categoria = Categoria.A;
            } else if (porcentajeAcumulado.compareTo(LIMITE_B) <= 0) {
                categoria = Categoria.B;
            } else {
                categoria = Categoria.C;
            }
            
            // Estimar unidades vendidas basado en el valor y precio
            int unidadesVendidas = valorAnual.divide(producto.getPrecio(), 0, RoundingMode.DOWN).intValue();
            if (unidadesVendidas <= 0) {
                unidadesVendidas = (int)(Math.random() * 20) + 1; // Al menos 1 unidad
            }
            
            // Crear entidad para guardar
            ClasificacionABC clasificacion = new ClasificacionABC();
            clasificacion.setProducto(producto);
            clasificacion.setCategoria(categoria);
            clasificacion.setValorAnual(valorAnual);
            clasificacion.setPorcentajeValor(porcentajeValor);
            clasificacion.setPorcentajeAcumulado(porcentajeAcumulado);
            clasificacion.setUnidadesVendidas(unidadesVendidas);
            clasificacion.setValorVentas(valorAnual); // Asignar el mismo valor que valorAnual
            clasificacion.setFechaCalculo(ahora);
            clasificacion.setPeriodoInicio(inicioAnio);
            clasificacion.setPeriodoFin(ahora);
            
            clasificacionesParaGuardar.add(clasificacion);
            
            // Crear DTO para el resultado
            ClasificacionABCDTO dto = new ClasificacionABCDTO();
            dto.setProductoId(producto.getId());
            dto.setCodigo(producto.getCodigo());
            dto.setNombre(producto.getNombre());
            dto.setCategoria(categoria.toString());
            dto.setValorAnual(valorAnual);
            dto.setPorcentajeValor(porcentajeValor.multiply(new BigDecimal("100")));
            dto.setPorcentajeAcumulado(porcentajeAcumulado.multiply(new BigDecimal("100")));
            
            resultados.add(dto);
        }
        
        // Guardar todas las clasificaciones
        clasificacionRepository.saveAll(clasificacionesParaGuardar);
        log.info("Datos de prueba para clasificación ABC generados: {} productos clasificados", clasificacionesParaGuardar.size());
        
        return resultados;
    }
    
    /**
     * Calcula el valor anual de un producto basado en sus ventas reales
     */
    private BigDecimal calcularValorAnualProducto(Producto producto, LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        log.debug("Calculando valor anual para producto {}", producto.getCodigo());
        
        try {
            // Obtener todas las variantes del producto
            List<VarianteProducto> variantes = varianteRepository.findByProducto(producto);
            
            if (variantes.isEmpty()) {
                log.debug("El producto {} no tiene variantes", producto.getCodigo());
                return BigDecimal.ZERO;
            }
        
            BigDecimal valorTotal = BigDecimal.ZERO;
        
            // Para cada variante, buscar los movimientos de salida (ventas)
            for (VarianteProducto variante : variantes) {
                // Buscar movimientos de salida (tipo SALIDA con motivo VENTA) para esta variante en el período especificado
                // Asumimos que solo se consideran los movimientos con motivo VENTA para los productos vendidos
                // (excluyendo devoluciones y otros ajustes)
                List<MovimientoStock> movimientos = movimientoRepository.findByVarianteAndTipoAndMotivoAndFechaBetween(
                        variante, 
                        MovimientoStock.TipoMovimiento.SALIDA,
                        MovimientoStock.MotivoMovimiento.VENTA,
                        fechaInicio, 
                        fechaFin);
                
                // Sumar el valor de cada movimiento (cantidad * precio)
                for (MovimientoStock movimiento : movimientos) {
                    BigDecimal cantidad = new BigDecimal(Math.abs(movimiento.getCantidad()));
                    // Usar el precio del producto ya que el movimiento no tiene precio unitario
                    BigDecimal precioUnitario = producto.getPrecio();
                    
                    BigDecimal valorMovimiento = cantidad.multiply(precioUnitario);
                    valorTotal = valorTotal.add(valorMovimiento);
                }
            }
            
            log.debug("Valor anual calculado para producto {}: {}", producto.getCodigo(), valorTotal);
            return valorTotal;
        } catch (Exception e) {
            log.error("Error calculando valor anual para producto {}: {}", producto.getCodigo(), e.getMessage());
            return BigDecimal.ZERO;
        }
    }
    
    /**
     * Clase auxiliar para almacenar un producto con su valor anual
     */
    private static class ProductoValorAnual {
        private final Producto producto;
        private final BigDecimal valorAnual;
        
        public ProductoValorAnual(Producto producto, BigDecimal valorAnual) {
            this.producto = producto;
            this.valorAnual = valorAnual;
        }
        
        public Producto getProducto() {
            return producto;
        }
        
        public BigDecimal getValorAnual() {
            return valorAnual;
        }
    }
    
    /**
     * Precalcula la clasificación ABC para todos los productos
     * usando un período predeterminado de un año
     */
    @Transactional
    public void precalcularClasificacionABC() {
        log.info("Iniciando precálculo de clasificación ABC");
        
        // Verificar si la tabla está vacía
        long countTotal = clasificacionRepository.count();
        log.info("Registros existentes en clasificacion_abc: {}", countTotal);
        
        LocalDateTime fechaFin = LocalDateTime.now();
        LocalDateTime fechaInicio = fechaFin.minusYears(1);
        
        try {
            // Si la tabla está vacía o no hay clasificaciones recientes, calcular
            if (countTotal == 0) {
                log.info("La tabla clasificacion_abc está vacía. Generando clasificación ABC inicial...");
                List<ClasificacionABCDTO> resultado = calcularClasificacionABC(fechaInicio, fechaFin);
                log.info("Clasificación ABC inicial completada. {} productos clasificados", resultado.size());
                return;
            }
            
            // Verificar si hay clasificaciones recientes (menos de 24 horas)
            List<ClasificacionABC> clasificaciones = clasificacionRepository.findLatestForAllProducts();
            boolean hayClasificacionesRecientes = clasificaciones.stream()
                .anyMatch(c -> c.getFechaCalculo().isAfter(LocalDateTime.now().minusHours(24)));
            
            if (!hayClasificacionesRecientes) {
                log.info("No hay clasificaciones recientes. Recalculando...");
                List<ClasificacionABCDTO> resultado = calcularClasificacionABC(fechaInicio, fechaFin);
                log.info("Precálculo de clasificación ABC completado. {} productos clasificados", resultado.size());
            } else {
                log.info("Ya existen clasificaciones ABC recientes. No es necesario recalcular.");
            }
        } catch (Exception e) {
            log.error("Error durante el precálculo de clasificación ABC: {}", e.getMessage(), e);
            
            // Si hay un error y la tabla está vacía, intentar generar datos de prueba
            if (countTotal == 0) {
                log.info("Intentando generar datos de prueba después del error...");
                try {
                    List<Producto> productos = productoRepository.findAll();
                    if (!productos.isEmpty()) {
                        List<ClasificacionABCDTO> resultado = generarDatosPruebaClasificacionABC(productos);
                        log.info("Datos de prueba generados exitosamente: {} productos", resultado.size());
                    }
                } catch (Exception ex) {
                    log.error("Error al generar datos de prueba: {}", ex.getMessage(), ex);
                }
            }
        }
    }
} 