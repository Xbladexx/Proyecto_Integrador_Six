package com.darkcode.spring.six.models.repositories;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.darkcode.spring.six.models.entities.ClasificacionABC;
import com.darkcode.spring.six.models.entities.ClasificacionABC.Categoria;
import com.darkcode.spring.six.models.entities.Producto;

@Repository
public interface ClasificacionABCRepository extends JpaRepository<ClasificacionABC, Long> {
    
    List<ClasificacionABC> findByCategoria(Categoria categoria);
    
    Optional<ClasificacionABC> findByProductoAndFechaCalculoBetween(
            Producto producto, 
            LocalDateTime fechaInicio, 
            LocalDateTime fechaFin);
    
    // Obtener la última clasificación para un producto
    @Query("SELECT c FROM ClasificacionABC c WHERE c.producto.id = :productoId ORDER BY c.fechaCalculo DESC")
    List<ClasificacionABC> findLatestByProductoId(@Param("productoId") Long productoId);
    
    // Obtener la última clasificación para cada producto
    @Query("SELECT c FROM ClasificacionABC c WHERE c.fechaCalculo = " +
           "(SELECT MAX(c2.fechaCalculo) FROM ClasificacionABC c2 WHERE c2.producto = c.producto)")
    List<ClasificacionABC> findLatestForAllProducts();
    
    // Contar productos por categoría
    @Query("SELECT c.categoria, COUNT(c) FROM ClasificacionABC c WHERE c.fechaCalculo = " +
           "(SELECT MAX(c2.fechaCalculo) FROM ClasificacionABC c2 WHERE c2.producto = c.producto) " +
           "GROUP BY c.categoria")
    List<Object[]> countProductosByCategoria();
} 