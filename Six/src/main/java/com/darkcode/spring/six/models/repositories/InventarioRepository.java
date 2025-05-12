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
} 