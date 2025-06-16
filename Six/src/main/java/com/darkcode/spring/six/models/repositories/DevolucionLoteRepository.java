package com.darkcode.spring.six.models.repositories;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.darkcode.spring.six.models.entities.DevolucionLote;
import com.darkcode.spring.six.models.entities.LoteProducto;
import com.darkcode.spring.six.models.entities.Proveedor;
import com.darkcode.spring.six.models.entities.Usuario;

@Repository
public interface DevolucionLoteRepository extends JpaRepository<DevolucionLote, Long> {
    
    List<DevolucionLote> findByLote(LoteProducto lote);
    
    List<DevolucionLote> findByProveedor(Proveedor proveedor);
    
    List<DevolucionLote> findByUsuario(Usuario usuario);
    
    List<DevolucionLote> findByEstado(String estado);
    
    List<DevolucionLote> findByFechaDevolucionBetween(LocalDateTime fechaInicio, LocalDateTime fechaFin);
    
    List<DevolucionLote> findAllByOrderByFechaDevolucionDesc();
} 