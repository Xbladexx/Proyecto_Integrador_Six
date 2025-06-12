package com.darkcode.spring.six.models.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.darkcode.spring.six.models.entities.Proveedor;

@Repository
public interface ProveedorRepository extends JpaRepository<Proveedor, Long> {
    
    /**
     * Busca proveedores por estado activo
     */
    List<Proveedor> findByActivoTrue();
    
    /**
     * Busca proveedores por estado inactivo
     */
    List<Proveedor> findByActivoFalse();
    
    /**
     * Busca un proveedor por su RUC
     */
    Optional<Proveedor> findByRuc(String ruc);
    
    /**
     * Verifica si existe un proveedor con el RUC dado
     */
    boolean existsByRuc(String ruc);
    
    /**
     * Busca proveedores cuyo nombre contiene el texto proporcionado (ignorando mayúsculas/minúsculas)
     */
    List<Proveedor> findByNombreContainingIgnoreCase(String nombre);
    
    /**
     * Busca proveedores cuyo RUC contiene el texto proporcionado (ignorando mayúsculas/minúsculas)
     */
    List<Proveedor> findByRucContainingIgnoreCase(String ruc);
} 