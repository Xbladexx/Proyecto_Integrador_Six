package com.darkcode.spring.six.models.repositories;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.darkcode.spring.six.models.entities.Cliente;
import com.darkcode.spring.six.models.entities.Usuario;
import com.darkcode.spring.six.models.entities.Venta;

@Repository
public interface VentaRepository extends JpaRepository<Venta, Long> {
    
    Optional<Venta> findByCodigo(String codigo);
    
    List<Venta> findByUsuario(Usuario usuario);
    
    List<Venta> findByCliente(Cliente cliente);
    
    List<Venta> findByFechaBetween(LocalDateTime inicio, LocalDateTime fin);
    
    @Query("SELECT v FROM Venta v WHERE v.fecha >= CURRENT_DATE")
    List<Venta> findVentasHoy();
    
    @Query("SELECT SUM(v.total) FROM Venta v WHERE v.fecha >= CURRENT_DATE")
    Double getTotalVentasHoy();
    
    @Query("SELECT v FROM Venta v WHERE v.fecha >= CURRENT_DATE AND v.usuario.id = :usuarioId")
    List<Venta> findVentasHoyByUsuario(@Param("usuarioId") Long usuarioId);
    
    @Query("SELECT SUM(v.total) FROM Venta v WHERE v.fecha >= CURRENT_DATE AND v.usuario.id = :usuarioId")
    Double getTotalVentasHoyByUsuario(@Param("usuarioId") Long usuarioId);
    
    @Query("SELECT COUNT(DISTINCT v.cliente.id) FROM Venta v WHERE v.usuario.id = :usuarioId")
    Long countClientesAtendidosByUsuario(@Param("usuarioId") Long usuarioId);
    
    @Query("SELECT v FROM Venta v WHERE v.usuario.id = :usuarioId ORDER BY v.fecha DESC")
    List<Venta> findTop5ByUsuarioIdOrderByFechaDesc(@Param("usuarioId") Long usuarioId);
    
    // Nuevos métodos para el dashboard de administrador
    
    /**
     * Obtiene las 20 ventas más recientes, ordenadas por fecha de más reciente a más antigua
     */
    List<Venta> findTop20ByOrderByFechaDesc();
    
    /**
     * Cuenta el número de ventas realizadas en el mes actual
     */
    @Query("SELECT COUNT(v) FROM Venta v WHERE YEAR(v.fecha) = YEAR(CURRENT_DATE) AND MONTH(v.fecha) = MONTH(CURRENT_DATE)")
    Long countVentasMesActual();
} 