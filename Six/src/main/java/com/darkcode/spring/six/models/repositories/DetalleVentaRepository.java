package com.darkcode.spring.six.models.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.darkcode.spring.six.models.entities.DetalleVenta;
import com.darkcode.spring.six.models.entities.VarianteProducto;
import com.darkcode.spring.six.models.entities.Venta;

@Repository
public interface DetalleVentaRepository extends JpaRepository<DetalleVenta, Long> {
    
    List<DetalleVenta> findByVenta(Venta venta);
    
    List<DetalleVenta> findByVariante(VarianteProducto variante);
    
    @Query("SELECT d.variante.id, SUM(d.cantidad) as total FROM DetalleVenta d " +
           "GROUP BY d.variante.id ORDER BY total DESC")
    List<Object[]> findTopSellingProducts();
    
    @Query("SELECT SUM(d.cantidad) FROM DetalleVenta d WHERE d.venta.fecha >= CURRENT_DATE AND d.venta.usuario.id = :usuarioId")
    Long countProductosVendidosHoyByUsuario(@Param("usuarioId") Long usuarioId);
    
    @Query("SELECT d FROM DetalleVenta d WHERE d.venta.fecha >= CURRENT_DATE AND d.venta.usuario.id = :usuarioId ORDER BY d.venta.fecha DESC")
    List<DetalleVenta> findVentasRecientesByUsuario(@Param("usuarioId") Long usuarioId);
    
    @Query("SELECT d FROM DetalleVenta d WHERE d.venta.usuario.id = :usuarioId ORDER BY d.venta.fecha DESC")
    List<DetalleVenta> findAllVentasByUsuario(@Param("usuarioId") Long usuarioId);
} 