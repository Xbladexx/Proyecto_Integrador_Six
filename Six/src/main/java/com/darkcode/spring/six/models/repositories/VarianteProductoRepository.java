package com.darkcode.spring.six.models.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.darkcode.spring.six.models.entities.Producto;
import com.darkcode.spring.six.models.entities.VarianteProducto;

@Repository
public interface VarianteProductoRepository extends JpaRepository<VarianteProducto, Long> {
    
    List<VarianteProducto> findByProducto(Producto producto);
    
    Optional<VarianteProducto> findBySku(String sku);
    
    List<VarianteProducto> findByProductoAndColor(Producto producto, String color);
    
    List<VarianteProducto> findByProductoAndTalla(Producto producto, String talla);
    
    boolean existsBySku(String sku);
} 