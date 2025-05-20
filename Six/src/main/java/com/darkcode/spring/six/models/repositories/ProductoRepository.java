package com.darkcode.spring.six.models.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.darkcode.spring.six.models.entities.Categoria;
import com.darkcode.spring.six.models.entities.Producto;
import com.darkcode.spring.six.models.entities.Producto.Estado;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {
    
    Optional<Producto> findByCodigo(String codigo);
    
    List<Producto> findByCategoria(Categoria categoria);
    
    List<Producto> findByEstado(Estado estado);
    
    boolean existsByCodigo(String codigo);
    
    List<Producto> findByCodigoContainingIgnoreCaseOrNombreContainingIgnoreCase(String codigo, String nombre);
} 