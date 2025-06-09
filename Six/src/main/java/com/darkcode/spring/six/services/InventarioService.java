package com.darkcode.spring.six.services;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.darkcode.spring.six.dtos.InventarioDTO;
import com.darkcode.spring.six.models.entities.Inventario;
import com.darkcode.spring.six.models.entities.MovimientoStock;
import com.darkcode.spring.six.models.entities.MovimientoStock.MotivoMovimiento;
import com.darkcode.spring.six.models.entities.MovimientoStock.TipoMovimiento;
import com.darkcode.spring.six.models.entities.Usuario;
import com.darkcode.spring.six.models.entities.VarianteProducto;
import com.darkcode.spring.six.models.repositories.InventarioRepository;
import com.darkcode.spring.six.models.repositories.MovimientoStockRepository;
import com.darkcode.spring.six.models.repositories.UsuarioRepository;
import com.darkcode.spring.six.models.repositories.VarianteProductoRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventarioService {
    
    private final InventarioRepository inventarioRepository;
    private final VarianteProductoRepository varianteRepository;
    private final MovimientoStockRepository movimientoRepository;
    private final UsuarioRepository usuarioRepository;
    
    /**
     * Obtiene todos los registros de inventario
     */
    public List<Inventario> obtenerTodoInventario() {
        return inventarioRepository.findAll();
    }
    
    /**
     * Obtiene detalles completos del inventario combinando datos de varias entidades
     * @return Lista de DTOs con la información combinada
     */
    public List<InventarioDTO> obtenerDetallesInventario() {
        List<Inventario> inventario = inventarioRepository.findAll();
        
        return inventario.stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Convierte una entidad Inventario a un DTO con información combinada
     */
    private InventarioDTO convertirADTO(Inventario inventario) {
        VarianteProducto variante = inventario.getVariante();
        
        InventarioDTO dto = new InventarioDTO();
        dto.setId(inventario.getId());
        dto.setVarianteId(variante.getId());
        dto.setSku(variante.getSku());
        dto.setNombreProducto(variante.getProducto().getNombre());
        dto.setNombreCategoria(variante.getProducto().getCategoria().getNombre());
        dto.setColor(variante.getColor());
        dto.setTalla(variante.getTalla());
        dto.setStock(inventario.getStock());
        dto.setPrecio(variante.getProducto().getPrecio());
        
        return dto;
    }
    
    /**
     * Obtiene un registro de inventario por ID
     */
    public Optional<Inventario> obtenerPorId(Long id) {
        return inventarioRepository.findById(id);
    }
    
    /**
     * Obtiene un registro de inventario por ID de variante
     */
    public Optional<Inventario> obtenerPorVarianteId(Long varianteId) {
        return inventarioRepository.findByVarianteId(varianteId);
    }
    
    /**
     * Obtiene el inventario con stock por debajo del mínimo
     */
    public Iterable<Inventario> obtenerStockBajo() {
        return inventarioRepository.findByStockLessThanEqualStockMinimo();
    }
    
    /**
     * Obtiene la distribución de productos por categoría
     * @return Lista de arrays [nombre_categoria, cantidad]
     */
    public List<Object[]> obtenerDistribucionPorCategoria() {
        log.info("Obteniendo distribución de productos por categoría desde el servicio");
        
        List<Object[]> resultados = inventarioRepository.obtenerDistribucionPorCategoria();
        
        log.info("Se encontraron {} categorías con productos", resultados.size());
        return resultados;
    }
    
    /**
     * Obtiene la distribución de unidades en stock por categoría
     * @return Lista de arrays [nombre_categoria, total_unidades]
     */
    public List<Object[]> obtenerDistribucionUnidadesPorCategoria() {
        log.info("Obteniendo distribución de unidades en stock por categoría desde el servicio");
        
        List<Object[]> resultados = inventarioRepository.obtenerDistribucionUnidadesPorCategoria();
        
        log.info("Se encontraron {} categorías con unidades en stock", resultados.size());
        return resultados;
    }
    
    /**
     * Aumenta el stock de un producto
     * @param varianteId ID de la variante
     * @param cantidad Cantidad a aumentar
     * @param usuarioId ID del usuario que realiza la operación
     * @param referencia Referencia de la operación (opcional)
     * @param notas Notas adicionales (opcional)
     */
    @Transactional
    public Inventario aumentarStock(Long varianteId, int cantidad, Long usuarioId, String referencia, String notas) {
        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor que cero");
        }
        
        // Buscar la variante
        VarianteProducto variante = varianteRepository.findById(varianteId)
                .orElseThrow(() -> new IllegalArgumentException("Variante no encontrada"));
        
        // Buscar el inventario
        Inventario inventario = inventarioRepository.findByVarianteId(varianteId)
                .orElseThrow(() -> new IllegalArgumentException("Inventario no encontrado"));
        
        // Buscar el usuario
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        
        // Registrar el movimiento
        int stockAnterior = inventario.getStock();
        int stockNuevo = stockAnterior + cantidad;
        
        // Actualizar el inventario
        inventario.setStock(stockNuevo);
        inventario.setUltimaActualizacion(LocalDateTime.now());
        inventarioRepository.save(inventario);
        
        // Registrar el movimiento de stock
        MovimientoStock movimiento = new MovimientoStock();
        movimiento.setVariante(variante);
        movimiento.setTipo(TipoMovimiento.ENTRADA);
        movimiento.setMotivo(MotivoMovimiento.REPOSICION);
        if (referencia != null && !referencia.isEmpty()) {
            movimiento.setMotivoDetalle(referencia);
        }
        movimiento.setCantidad(cantidad);
        movimiento.setUsuario(usuario);
        movimiento.setObservaciones(notas);
        movimiento.setFecha(LocalDateTime.now());
        movimientoRepository.save(movimiento);
        
        log.info("Stock aumentado: Variante ID {} - Cantidad {} - Stock anterior {} - Stock nuevo {}", 
                varianteId, cantidad, stockAnterior, stockNuevo);
        
        return inventario;
    }
    
    /**
     * Disminuye el stock de un producto
     * @param varianteId ID de la variante
     * @param cantidad Cantidad a disminuir
     * @param usuarioId ID del usuario que realiza la operación
     * @param referencia Referencia de la operación (opcional)
     * @param notas Notas adicionales (opcional)
     */
    @Transactional
    public Inventario disminuirStock(Long varianteId, int cantidad, Long usuarioId, String referencia, String notas) {
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
        
        // Registrar el movimiento
        int stockAnterior = inventario.getStock();
        int stockNuevo = stockAnterior - cantidad;
        
        // Actualizar el inventario
        inventario.setStock(stockNuevo);
        inventario.setUltimaActualizacion(LocalDateTime.now());
        inventarioRepository.save(inventario);
        
        // Registrar el movimiento de stock
        MovimientoStock movimiento = new MovimientoStock();
        movimiento.setVariante(variante);
        movimiento.setTipo(TipoMovimiento.SALIDA);
        movimiento.setMotivo(MotivoMovimiento.OTRO);
        if (referencia != null && !referencia.isEmpty()) {
            movimiento.setMotivoDetalle(referencia);
        }
        movimiento.setCantidad(cantidad);
        movimiento.setUsuario(usuario);
        movimiento.setObservaciones(notas);
        movimiento.setFecha(LocalDateTime.now());
        movimientoRepository.save(movimiento);
        
        log.info("Stock disminuido: Variante ID {} - Cantidad {} - Stock anterior {} - Stock nuevo {}", 
                varianteId, cantidad, stockAnterior, stockNuevo);
        
        return inventario;
    }
    
    /**
     * Actualiza el stock de un producto
     * @param varianteId ID de la variante
     * @param nuevoStock Nuevo valor de stock
     * @param usuarioId ID del usuario que realiza la operación
     * @param referencia Referencia de la operación (opcional)
     * @param notas Notas adicionales (opcional)
     */
    @Transactional
    public Inventario ajustarStock(Long varianteId, int nuevoStock, Long usuarioId, String referencia, String notas) {
        if (nuevoStock < 0) {
            throw new IllegalArgumentException("El stock no puede ser negativo");
        }
        
        // Buscar la variante
        VarianteProducto variante = varianteRepository.findById(varianteId)
                .orElseThrow(() -> new IllegalArgumentException("Variante no encontrada"));
        
        // Buscar el inventario
        Inventario inventario = inventarioRepository.findByVarianteId(varianteId)
                .orElseThrow(() -> new IllegalArgumentException("Inventario no encontrado"));
        
        // Buscar el usuario
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        
        // Registrar el movimiento
        int stockAnterior = inventario.getStock();
        int stockNuevo = nuevoStock;
        
        // Determinar el tipo de movimiento
        TipoMovimiento tipoMovimiento;
        if (stockNuevo > stockAnterior) {
            tipoMovimiento = TipoMovimiento.ENTRADA;
        } else {
            tipoMovimiento = TipoMovimiento.SALIDA;
        }
        
        // Actualizar el inventario
        inventario.setStock(stockNuevo);
        inventario.setUltimaActualizacion(LocalDateTime.now());
        inventarioRepository.save(inventario);
        
        // Solo registrar movimiento si hay cambio en el stock
        if (stockNuevo != stockAnterior) {
            // Registrar el movimiento de stock
            MovimientoStock movimiento = new MovimientoStock();
            movimiento.setVariante(variante);
            movimiento.setTipo(tipoMovimiento);
            movimiento.setMotivo(MotivoMovimiento.AJUSTE);
            if (referencia != null && !referencia.isEmpty()) {
                movimiento.setMotivoDetalle(referencia);
            }
            movimiento.setCantidad(Math.abs(stockNuevo - stockAnterior));
            movimiento.setUsuario(usuario);
            movimiento.setObservaciones(notas);
            movimiento.setFecha(LocalDateTime.now());
            movimientoRepository.save(movimiento);
        }
        
        log.info("Stock ajustado: Variante ID {} - Stock anterior {} - Stock nuevo {}", 
                varianteId, stockAnterior, stockNuevo);
        
        return inventario;
    }
} 