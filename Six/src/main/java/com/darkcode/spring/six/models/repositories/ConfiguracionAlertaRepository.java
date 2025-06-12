package com.darkcode.spring.six.models.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.darkcode.spring.six.models.entities.Categoria;
import com.darkcode.spring.six.models.entities.ConfiguracionAlerta;
import com.darkcode.spring.six.models.entities.Producto;

@Repository
public interface ConfiguracionAlertaRepository extends JpaRepository<ConfiguracionAlerta, Long> {
    
    // Buscar configuración general (sin categoría ni producto)
    @Query("SELECT c FROM ConfiguracionAlerta c WHERE c.categoria IS NULL AND c.producto IS NULL")
    Optional<ConfiguracionAlerta> findConfiguracionGeneral();
    
    // Buscar configuración por categoría
    List<ConfiguracionAlerta> findByCategoria(Categoria categoria);
    
    // Buscar configuración por producto
    Optional<ConfiguracionAlerta> findByProducto(Producto producto);
    
    // Verificar si existe configuración para un producto
    boolean existsByProducto(Producto producto);
    
    // Verificar si existe configuración para una categoría
    boolean existsByCategoria(Categoria categoria);
    
    // Obtener todas las configuraciones por categoría
    @Query("SELECT c FROM ConfiguracionAlerta c WHERE c.categoria IS NOT NULL")
    List<ConfiguracionAlerta> findAllByCategoria();
    
    // Obtener todas las configuraciones por producto
    @Query("SELECT c FROM ConfiguracionAlerta c WHERE c.producto IS NOT NULL")
    List<ConfiguracionAlerta> findAllByProducto();
} 