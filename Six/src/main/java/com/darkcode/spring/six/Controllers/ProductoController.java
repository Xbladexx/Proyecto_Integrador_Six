package com.darkcode.spring.six.Controllers;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.darkcode.spring.six.dtos.ProductoVarianteDTO;
import com.darkcode.spring.six.models.entities.Categoria;
import com.darkcode.spring.six.models.entities.Inventario;
import com.darkcode.spring.six.models.entities.MovimientoStock;
import com.darkcode.spring.six.models.entities.Producto;
import com.darkcode.spring.six.models.entities.Producto.Estado;
import com.darkcode.spring.six.models.entities.VarianteProducto;
import com.darkcode.spring.six.models.repositories.CategoriaRepository;
import com.darkcode.spring.six.models.repositories.InventarioRepository;
import com.darkcode.spring.six.models.repositories.MovimientoStockRepository;
import com.darkcode.spring.six.models.repositories.ProductoRepository;
import com.darkcode.spring.six.models.repositories.VarianteProductoRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/productos")
@RequiredArgsConstructor
@Slf4j
public class ProductoController {

    private final ProductoRepository productoRepository;
    private final CategoriaRepository categoriaRepository;
    private final VarianteProductoRepository varianteRepository;
    private final InventarioRepository inventarioRepository;
    private final MovimientoStockRepository movimientoStockRepository;
    
    @GetMapping
    public ResponseEntity<List<Producto>> obtenerTodos() {
        log.info("Obteniendo lista de productos");
        try {
            List<Producto> productos = productoRepository.findAll();
            
            // Prevenir referencias circulares
            productos.forEach(producto -> {
                if (producto.getCategoria() != null) {
                    producto.getCategoria().setProductos(null);
                }
                // Las variantes ya están anotadas con @JsonManagedReference y @JsonBackReference
                // para manejar la relación bidireccional correctamente, así que no necesitamos manipularlas aquí
                // Solo limpiamos las referencias que no son necesarias para mostrar los productos en la UI
                if (producto.getVariantes() != null) {
                    producto.getVariantes().forEach(variante -> {
                        // No eliminamos la referencia a producto para mantener la relación bidireccional
                        variante.setMovimientos(null);
                    });
                }
            });
            
            log.info("Se encontraron {} productos", productos.size());
            return ResponseEntity.ok(productos);
        } catch (Exception e) {
            log.error("Error al obtener productos", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Producto> obtenerPorId(@PathVariable Long id) {
        log.info("Obteniendo producto con ID: {}", id);
        try {
            Optional<Producto> productoOpt = productoRepository.findById(id);
            
            if (productoOpt.isPresent()) {
                Producto producto = productoOpt.get();
                
                // Prevenir referencias circulares
                if (producto.getCategoria() != null) {
                    producto.getCategoria().setProductos(null);
                }
                
                // Limpiar referencias en variantes y obtener información de inventario
                if (producto.getVariantes() != null) {
                    producto.getVariantes().forEach(variante -> {
                        variante.setMovimientos(null);
                        
                        // Obtener información de inventario para esta variante
                        Optional<Inventario> inventarioOpt = inventarioRepository.findByVarianteId(variante.getId());
                        if (inventarioOpt.isPresent()) {
                            // Podríamos añadir los datos de inventario a la variante a través de campos transientes
                            // o incluirlos de alguna manera en la respuesta
                            Inventario inventario = inventarioOpt.get();
                            log.info("Stock para variante ID {}: {}", variante.getId(), inventario.getStock());
                            
                            // En este ejemplo estamos creando un movimiento temporal para transportar el stock
                            // No es la solución ideal, pero funciona con la estructura actual
                            Set<MovimientoStock> movimientos = new HashSet<>();
                            MovimientoStock movimiento = new MovimientoStock();
                            movimiento.setCantidad(inventario.getStock());
                            movimiento.setTipo(MovimientoStock.TipoMovimiento.ENTRADA);
                            movimientos.add(movimiento);
                            variante.setMovimientos(movimientos);
                        }
                    });
                }
                
                return ResponseEntity.ok(producto);
            } else {
                log.warn("Producto con ID {} no encontrado", id);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("Error al obtener producto con ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @PostMapping
    public ResponseEntity<Producto> crear(@RequestBody Producto producto) {
        log.info("Creando nuevo producto: {}", producto.getNombre());
        log.info("Número de variantes recibidas: {}", producto.getVariantes() != null ? producto.getVariantes().size() : 0);
        
        if (producto.getId() != null) {
            return ResponseEntity.badRequest().build();
        }
        
        // Asegurarse de que la categoría exista o crearla
        if (producto.getCategoria() != null && producto.getCategoria().getId() == null) {
            String nombreCategoria = producto.getCategoria().getNombre();
            log.info("Buscando categoría con nombre: {}", nombreCategoria);
            
            // Buscar la categoría por nombre
            Optional<Categoria> categoriaExistente = categoriaRepository.findByNombre(nombreCategoria);
            
            if (categoriaExistente.isPresent()) {
                // Si existe, usar esa
                producto.setCategoria(categoriaExistente.get());
                log.info("Usando categoría existente con ID: {}", categoriaExistente.get().getId());
            } else {
                // Si no existe, crear una nueva
                Categoria nuevaCategoria = new Categoria();
                nuevaCategoria.setNombre(nombreCategoria);
                nuevaCategoria.setActivo(true);
                nuevaCategoria = categoriaRepository.save(nuevaCategoria);
                
                producto.setCategoria(nuevaCategoria);
                log.info("Creada nueva categoría con ID: {}", nuevaCategoria.getId());
            }
        }
        
        // Guardar el producto para que tenga un ID asignado
        Producto nuevoProducto = productoRepository.save(producto);
        
        // Procesar las variantes del producto, si existen
        if (producto.getVariantes() != null && !producto.getVariantes().isEmpty()) {
            log.info("Procesando {} variantes para el producto", producto.getVariantes().size());
            Set<VarianteProducto> variantesGuardadas = new HashSet<>();
            
            for (VarianteProducto variante : producto.getVariantes()) {
                // Asignar el producto a la variante
                variante.setProducto(nuevoProducto);
                
                // Generar un SKU único si no tiene uno
                if (variante.getSku() == null || variante.getSku().isEmpty()) {
                    String sku = nuevoProducto.getCodigo() + "-" + variante.getColor() + "-" + variante.getTalla();
                    variante.setSku(sku);
                }
                
                log.info("Guardando variante: color={}, talla={}, sku={}", variante.getColor(), variante.getTalla(), variante.getSku());
                VarianteProducto varianteGuardada = varianteRepository.save(variante);
                variantesGuardadas.add(varianteGuardada);
                
                // Actualizar o crear el inventario para esta variante
                Integer stockInicial = 0; // Valor por defecto
                
                // Intentar extraer el stock inicial si viene en los datos
                try {
                    if (variante.getMovimientos() != null && !variante.getMovimientos().isEmpty()) {
                        // Iterar por todos los movimientos para encontrar cualquier tipo válido
                        for (MovimientoStock movimiento : variante.getMovimientos()) {
                            // Si el movimiento tiene cantidad, usarla como stock inicial
                            if (movimiento.getCantidad() != null && movimiento.getCantidad() > 0) {
                                stockInicial = movimiento.getCantidad();
                                log.info("Stock inicial extraído de movimiento: {}", stockInicial);
                                
                                // Asegurarse de que tipo y motivo estén correctamente establecidos
                                if (movimiento.getTipo() == null) {
                                    movimiento.setTipo(MovimientoStock.TipoMovimiento.ENTRADA);
                                }
                                
                                if (movimiento.getMotivo() == null) {
                                    movimiento.setMotivo(MovimientoStock.MotivoMovimiento.REPOSICION);
                                }
                                
                                break;
                            }
                        }
                    }
                } catch (Exception e) {
                    log.warn("No se pudo extraer el stock inicial de la variante: {}", e.getMessage());
                }
                
                // Buscar si ya existe un registro de inventario para esta variante
                Optional<Inventario> inventarioExistente = inventarioRepository.findByVarianteId(varianteGuardada.getId());
                
                if (inventarioExistente.isPresent()) {
                    // Actualizar el inventario siempre, no solo para variantes nuevas
                    Inventario inventario = inventarioExistente.get();
                    inventario.setStock(stockInicial);
                    inventario.setUltimaActualizacion(LocalDateTime.now());
                    inventarioRepository.save(inventario);
                    log.info("Inventario actualizado para variante ID: {}, stock: {}", varianteGuardada.getId(), stockInicial);
                } else {
                    // Crear nuevo registro de inventario
                    Inventario inventario = new Inventario();
                    inventario.setVariante(varianteGuardada);
                    inventario.setStock(stockInicial);
                    inventario.setStockMinimo(5); // Valor por defecto
                    inventario.setStockMaximo(100); // Valor por defecto
                    inventario.setUltimaActualizacion(LocalDateTime.now());
                    inventarioRepository.save(inventario);
                    log.info("Inventario creado para variante ID: {}, stock: {}", varianteGuardada.getId(), stockInicial);
                }
            }
            
            // Actualizar el conjunto de variantes del producto
            nuevoProducto.setVariantes(variantesGuardadas);
        }
        
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevoProducto);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<Producto> actualizar(@PathVariable Long id, @RequestBody Producto producto) {
        log.info("Actualizando producto con ID: {}", id);
        log.info("Número de variantes recibidas: {}", producto.getVariantes() != null ? producto.getVariantes().size() : 0);
        
        if (!productoRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        
        // Asegurarse de que la categoría exista o crearla
        if (producto.getCategoria() != null && producto.getCategoria().getId() == null) {
            String nombreCategoria = producto.getCategoria().getNombre();
            log.info("Buscando categoría con nombre: {}", nombreCategoria);
            
            // Buscar la categoría por nombre
            Optional<Categoria> categoriaExistente = categoriaRepository.findByNombre(nombreCategoria);
            
            if (categoriaExistente.isPresent()) {
                // Si existe, usar esa
                producto.setCategoria(categoriaExistente.get());
                log.info("Usando categoría existente con ID: {}", categoriaExistente.get().getId());
            } else {
                // Si no existe, crear una nueva
                Categoria nuevaCategoria = new Categoria();
                nuevaCategoria.setNombre(nombreCategoria);
                nuevaCategoria.setActivo(true);
                nuevaCategoria = categoriaRepository.save(nuevaCategoria);
                
                producto.setCategoria(nuevaCategoria);
                log.info("Creada nueva categoría con ID: {}", nuevaCategoria.getId());
            }
        }
        
        // Obtener el producto existente para poder gestionar sus variantes
        Optional<Producto> productoExistenteOpt = productoRepository.findById(id);
        if (productoExistenteOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        // No necesitamos la variable productoExistente ya que no la estamos usando
        
        // Primero, guarda el producto actualizado
        producto.setId(id);
        Producto productoActualizado = productoRepository.save(producto);
        
        // Procesar las variantes del producto, si existen
        if (producto.getVariantes() != null && !producto.getVariantes().isEmpty()) {
            log.info("Procesando {} variantes para el producto actualizado", producto.getVariantes().size());
            Set<VarianteProducto> variantesGuardadas = new HashSet<>();
            
            for (VarianteProducto variante : producto.getVariantes()) {
                // Asignar el producto a la variante
                variante.setProducto(productoActualizado);
                
                // Determinar si la variante es nueva o existente
                boolean esVarianteNueva = variante.getId() == null;
                
                // Generar un SKU único si no tiene uno
                if (variante.getSku() == null || variante.getSku().isEmpty()) {
                    String sku = productoActualizado.getCodigo() + "-" + variante.getColor() + "-" + variante.getTalla();
                    variante.setSku(sku);
                }
                
                log.info("{}variante: color={}, talla={}, sku={}", 
                        esVarianteNueva ? "Creando nueva " : "Actualizando ", 
                        variante.getColor(), variante.getTalla(), variante.getSku());
                
                VarianteProducto varianteGuardada = varianteRepository.save(variante);
                variantesGuardadas.add(varianteGuardada);
                
                // Actualizar o crear el inventario para esta variante
                Integer stockInicial = 0; // Valor por defecto
                
                // Intentar extraer el stock inicial si viene en los datos
                try {
                    if (variante.getMovimientos() != null && !variante.getMovimientos().isEmpty()) {
                        // Iterar por todos los movimientos para encontrar cualquier tipo válido
                        for (MovimientoStock movimiento : variante.getMovimientos()) {
                            // Si el movimiento tiene cantidad, usarla como stock inicial
                            if (movimiento.getCantidad() != null && movimiento.getCantidad() > 0) {
                                stockInicial = movimiento.getCantidad();
                                log.info("Stock inicial extraído de movimiento: {}", stockInicial);
                                
                                // Asegurarse de que tipo y motivo estén correctamente establecidos
                                if (movimiento.getTipo() == null) {
                                    movimiento.setTipo(MovimientoStock.TipoMovimiento.ENTRADA);
                                }
                                
                                if (movimiento.getMotivo() == null) {
                                    movimiento.setMotivo(MovimientoStock.MotivoMovimiento.REPOSICION);
                                }
                                
                                break;
                            }
                        }
                    }
                } catch (Exception e) {
                    log.warn("No se pudo extraer el stock inicial de la variante: {}", e.getMessage());
                }
                
                // Buscar si ya existe un registro de inventario para esta variante
                Optional<Inventario> inventarioExistente = inventarioRepository.findByVarianteId(varianteGuardada.getId());
                
                if (inventarioExistente.isPresent()) {
                    // Actualizar el inventario siempre, no solo para variantes nuevas
                        Inventario inventario = inventarioExistente.get();
                        inventario.setStock(stockInicial);
                        inventario.setUltimaActualizacion(LocalDateTime.now());
                        inventarioRepository.save(inventario);
                        log.info("Inventario actualizado para variante ID: {}, stock: {}", varianteGuardada.getId(), stockInicial);
                } else {
                    // Crear nuevo registro de inventario
                    Inventario inventario = new Inventario();
                    inventario.setVariante(varianteGuardada);
                    inventario.setStock(stockInicial);
                    inventario.setStockMinimo(5); // Valor por defecto
                    inventario.setStockMaximo(100); // Valor por defecto
                    inventario.setUltimaActualizacion(LocalDateTime.now());
                    inventarioRepository.save(inventario);
                    log.info("Inventario creado para variante ID: {}, stock: {}", varianteGuardada.getId(), stockInicial);
                }
            }
            
            // Actualizar el conjunto de variantes del producto
            productoActualizado.setVariantes(variantesGuardadas);
        }
        
        return ResponseEntity.ok(productoActualizado);
    }
    
    @PatchMapping("/{id}")
    public ResponseEntity<Producto> actualizarParcial(@PathVariable Long id, @RequestBody Map<String, Object> campos) {
        log.info("Actualizando parcialmente producto con ID: {}", id);
        
        Optional<Producto> productoOpt = productoRepository.findById(id);
        if (productoOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        Producto producto = productoOpt.get();
        
        // Actualizar estado si viene en la petición
        if (campos.containsKey("estado")) {
            String estadoStr = (String) campos.get("estado");
            try {
                Estado estado = Estado.valueOf(estadoStr);
                producto.setEstado(estado);
                log.info("Actualizando estado de producto a: {}", estado);
            } catch (IllegalArgumentException e) {
                log.error("Estado no válido: {}", estadoStr);
                return ResponseEntity.badRequest().build();
            }
        }
        
        // Aquí se pueden agregar más campos para actualizar parcialmente
        
        Producto productoActualizado = productoRepository.save(producto);
        return ResponseEntity.ok(productoActualizado);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        log.info("Eliminando producto con ID: {}", id);
        
        // Verificar si el producto existe
        Optional<Producto> productoOpt = productoRepository.findById(id);
        if (productoOpt.isEmpty()) {
            log.error("Producto con ID {} no encontrado para eliminación", id);
            return ResponseEntity.notFound().build();
        }
        
        Producto producto = productoOpt.get();
        
            // Obtener las variantes del producto
            Set<VarianteProducto> variantes = producto.getVariantes();
            if (variantes != null && !variantes.isEmpty()) {
                log.info("Producto tiene {} variantes que también serán eliminadas", variantes.size());
                
                // Eliminar registros de inventario y movimientos relacionados con cada variante
                for (VarianteProducto variante : variantes) {
                    Long varianteId = variante.getId();
                    log.info("Procesando eliminación de variante ID: {}", varianteId);
                    
                    try {
                        log.info("Eliminando movimientos de stock para variante ID: {}", varianteId);
                        movimientoStockRepository.deleteByVarianteId(varianteId);
                        log.info("Movimientos de stock eliminados para variante ID: {}", varianteId);
                    
                    // 2. Luego eliminar el inventario
                        log.info("Eliminando inventario para variante ID: {}", varianteId);
                        Optional<Inventario> inventarioOpt = inventarioRepository.findByVarianteId(varianteId);
                    if (inventarioOpt.isPresent()) {
                        inventarioRepository.delete(inventarioOpt.get());
                            log.info("Inventario eliminado para variante ID: {}", varianteId);
                    }
                    
                    // 3. Finalmente eliminar la variante
                        log.info("Eliminando variante ID: {}", varianteId);
                        varianteRepository.deleteById(varianteId);
                        log.info("Variante ID: {} eliminada exitosamente", varianteId);
                    } catch (Exception e) {
                    log.error("Error al eliminar datos relacionados con la variante ID: {}", varianteId, e);
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
                    }
                }
            }
            
            // Finalmente eliminar el producto
        try {
            log.info("Eliminando producto ID: {}", id);
            productoRepository.deleteById(id);
            log.info("Producto ID: {} eliminado exitosamente", id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.error("Error al eliminar producto con ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
    
    /**
     * Elimina una variante de producto específica
     */
    @DeleteMapping("/variante/{id}")
    public ResponseEntity<Void> eliminarVariante(@PathVariable Long id) {
        log.info("Eliminando variante con ID: {}", id);
        
        // Verificar si la variante existe
        Optional<VarianteProducto> varianteOpt = varianteRepository.findById(id);
        if (varianteOpt.isEmpty()) {
            log.error("Variante con ID {} no encontrada para eliminación", id);
            return ResponseEntity.notFound().build();
        }
        
        VarianteProducto variante = varianteOpt.get();
        Long varianteId = variante.getId();
        
            try {
                log.info("Eliminando movimientos de stock para variante ID: {}", varianteId);
                movimientoStockRepository.deleteByVarianteId(varianteId);
                log.info("Movimientos de stock eliminados para variante ID: {}", varianteId);
            
            // 2. Luego eliminar el inventario
                log.info("Eliminando inventario para variante ID: {}", varianteId);
                Optional<Inventario> inventarioOpt = inventarioRepository.findByVarianteId(varianteId);
                if (inventarioOpt.isPresent()) {
                    inventarioRepository.delete(inventarioOpt.get());
                    log.info("Inventario eliminado para variante ID: {}", varianteId);
            }
            
            // 3. Finalmente eliminar la variante
                log.info("Eliminando variante ID: {}", varianteId);
                varianteRepository.deleteById(varianteId);
                log.info("Variante ID: {} eliminada exitosamente", varianteId);
            
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.error("Error al eliminar variante con ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    /**
     * Endpoint especial para la búsqueda de productos en la vista de ventas
     * Devuelve productos con sus variantes en un formato adecuado para la interfaz
     */
    @GetMapping("/buscar-ventas")
    public ResponseEntity<List<ProductoVarianteDTO>> buscarProductosParaVentas(
            @RequestParam(required = false) String query) {
        
        log.info("Buscando productos para ventas con query: {}", query);
        
        try {
            List<Producto> productos;
            
            // Si hay un término de búsqueda, filtrar los productos
            if (query != null && !query.trim().isEmpty()) {
                String searchTerm = "%" + query.toLowerCase() + "%";
                productos = productoRepository.findByCodigoContainingIgnoreCaseOrNombreContainingIgnoreCase(searchTerm, searchTerm);
                log.info("Búsqueda con término '{}' encontró {} productos", query, productos.size());
            } else {
                // Si no hay término, obtener todos los productos activos
                productos = productoRepository.findByEstado(Producto.Estado.ACTIVO);
                log.info("Búsqueda de todos los productos activos encontró {} productos", productos.size());
            }
            
            // Transformar a DTOs
            List<ProductoVarianteDTO> resultados = new ArrayList<>();
            
            for (Producto producto : productos) {
                log.info("Procesando producto: {} con {} variantes", producto.getNombre(), producto.getVariantes().size());
                
                for (VarianteProducto variante : producto.getVariantes()) {
                    // Obtener stock
                    Optional<Inventario> inventarioOpt = inventarioRepository.findByVarianteId(variante.getId());
                    Integer stock = inventarioOpt.map(Inventario::getStock).orElse(0);
                    
                    // Solo incluir variantes con stock
                    if (stock > 0) {
                        log.info("Variante con stock: id={}, color={}, talla={}, stock={}", 
                                variante.getId(), variante.getColor(), variante.getTalla(), stock);
                        
                        // Obtener todas las tallas disponibles para esta combinación de producto y color
                        List<String> tallasDisponibles = producto.getVariantes().stream()
                                .filter(v -> v.getColor().equals(variante.getColor()))
                                .map(VarianteProducto::getTalla)
                                .collect(Collectors.toList());
                        
                        ProductoVarianteDTO dto = new ProductoVarianteDTO();
                        dto.setId(producto.getId());
                        dto.setCodigo(producto.getCodigo());
                        dto.setNombre(producto.getNombre());
                        dto.setColor(variante.getColor());
                        dto.setPrecio(producto.getPrecio());
                        dto.setVarianteId(variante.getId());
                        dto.setSku(variante.getSku());
                        dto.setTalla(variante.getTalla()); // Añadir la talla específica de esta variante
                        dto.setTallasDisponibles(tallasDisponibles);
                        dto.setStock(stock);
                        
                        resultados.add(dto);
                    }
                }
            }
            
            // Eliminar duplicados (mismo producto y color)
            List<ProductoVarianteDTO> resultadosUnicos = resultados.stream()
                    .collect(Collectors.toMap(
                        dto -> dto.getId() + "-" + dto.getColor(),
                        dto -> dto,
                        (dto1, dto2) -> dto1 // En caso de duplicados, mantener el primero
                    ))
                    .values()
                    .stream()
                    .collect(Collectors.toList());
            
            log.info("Se encontraron {} productos con variantes para la venta", resultadosUnicos.size());
            return ResponseEntity.ok(resultadosUnicos);
        } catch (Exception e) {
            log.error("Error al buscar productos para ventas", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
} 