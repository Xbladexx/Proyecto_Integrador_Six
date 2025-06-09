package com.darkcode.spring.six.models.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.darkcode.spring.six.models.entities.Inventario;
import com.darkcode.spring.six.models.entities.VarianteProducto;

@Repository
public interface InventarioRepository extends JpaRepository<Inventario, Long> {
    
    Optional<Inventario> findByVariante(VarianteProducto variante);
    
    Optional<Inventario> findByVarianteId(Long varianteId);
    
    @Query("SELECT i FROM Inventario i WHERE i.stock <= i.stockMinimo")
    Iterable<Inventario> findLowStock();
    
    @Query("SELECT i FROM Inventario i WHERE i.stock = 0")
    Iterable<Inventario> findOutOfStock();
    
    @Query("SELECT i FROM Inventario i WHERE i.stock <= i.stockMinimo")
    List<Inventario> findByStockLessThanEqualStockMinimo();
    
    List<Inventario> findByUbicacion(String ubicacion);
    
    /**
     * Obtiene la distribución de productos por categoría
     * @return Lista de arrays [nombre_categoria, cantidad]
     */
    @Query(value = "SELECT c.nombre as categoria, COUNT(p.id) as cantidad " +
                   "FROM inventario i " +
                   "JOIN variantes_producto vp ON i.variante_id = vp.id " +
                   "JOIN productos p ON vp.producto_id = p.id " +
                   "JOIN categorias c ON p.categoria_id = c.id " +
                   "GROUP BY c.nombre " +
                   "ORDER BY cantidad DESC", nativeQuery = true)
    List<Object[]> obtenerDistribucionPorCategoria();
    
    /**
     * Obtiene la distribución de unidades en stock por categoría
     * @return Lista de arrays [nombre_categoria, total_unidades]
     */
    @Query(value = "SELECT c.nombre as categoria, SUM(i.stock) as total_unidades " +
                   "FROM inventario i " +
                   "JOIN variantes_producto vp ON i.variante_id = vp.id " +
                   "JOIN productos p ON vp.producto_id = p.id " +
                   "JOIN categorias c ON p.categoria_id = c.id " +
                   "GROUP BY c.nombre " +
                   "ORDER BY total_unidades DESC", nativeQuery = true)
    List<Object[]> obtenerDistribucionUnidadesPorCategoria();
    
    /**
     * Obtiene el valor del inventario por categoría
     * @return Lista de arrays [nombre_categoria, valor_total]
     */
    @Query(value = "SELECT c.nombre as categoria, SUM(i.stock * p.precio) as valor_total " +
                   "FROM inventario i " +
                   "JOIN variantes_producto vp ON i.variante_id = vp.id " +
                   "JOIN productos p ON vp.producto_id = p.id " +
                   "JOIN categorias c ON p.categoria_id = c.id " +
                   "GROUP BY c.nombre " +
                   "ORDER BY valor_total DESC", nativeQuery = true)
    List<Object[]> obtenerValorInventarioPorCategoria();
    
    /**
     * Calcula el valor total del inventario y la cantidad total de unidades
     * @return [valor_total, unidades_totales]
     */
    @Query(value = "SELECT SUM(i.stock * p.precio) as valor_total, SUM(i.stock) as unidades_totales " +
                   "FROM inventario i " +
                   "JOIN variantes_producto vp ON i.variante_id = vp.id " +
                   "JOIN productos p ON vp.producto_id = p.id", nativeQuery = true)
    List<Object[]> calcularValorTotalInventario();
    
    /**
     * Cuenta la cantidad de productos con stock menor o igual al stock mínimo
     * @return Cantidad de productos con stock bajo
     */
    @Query("SELECT COUNT(i) FROM Inventario i WHERE i.stock <= i.stockMinimo")
    Long countByStockLessThanEqualStockMinimo();
} 