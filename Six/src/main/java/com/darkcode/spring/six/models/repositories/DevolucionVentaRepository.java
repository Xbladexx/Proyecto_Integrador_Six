package com.darkcode.spring.six.models.repositories;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.darkcode.spring.six.models.entities.DetalleVenta;
import com.darkcode.spring.six.models.entities.DevolucionVenta;
import com.darkcode.spring.six.models.entities.Usuario;
import com.darkcode.spring.six.models.entities.Venta;

@Repository
public interface DevolucionVentaRepository extends JpaRepository<DevolucionVenta, Long> {
    
    List<DevolucionVenta> findByVenta(Venta venta);
    
    List<DevolucionVenta> findByDetalleVenta(DetalleVenta detalleVenta);
    
    List<DevolucionVenta> findByUsuario(Usuario usuario);
    
    List<DevolucionVenta> findByFechaDevolucionBetween(LocalDateTime inicio, LocalDateTime fin);
    
    List<DevolucionVenta> findByVentaAndEstado(Venta venta, String estado);
    
    List<DevolucionVenta> findByCodigo(String codigo);
} 