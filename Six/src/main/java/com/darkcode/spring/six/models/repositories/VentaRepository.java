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
    
    List<Venta> findByEstado(String estado);
    
    List<Venta> findByEstadoAndFechaBetween(String estado, LocalDateTime inicio, LocalDateTime fin);
    
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
     * Obtiene las 20 ventas completadas más recientes, ordenadas por fecha de más reciente a más antigua
     */
    List<Venta> findTop20ByEstadoOrderByFechaDesc(String estado);
    
    /**
     * Cuenta el número de ventas realizadas en el mes actual
     */
    @Query("SELECT COUNT(v) FROM Venta v WHERE YEAR(v.fecha) = YEAR(CURRENT_DATE) AND MONTH(v.fecha) = MONTH(CURRENT_DATE) AND v.estado = 'COMPLETADA'")
    Long countVentasMesActual();
    
    /**
     * Obtiene el total de ventas de los últimos 7 días agrupados por día de la semana
     * Devuelve [día de la semana (1-7), total]
     */
    @Query(value = "SELECT DAYOFWEEK(v.fecha) as dia, SUM(v.total) as total " +
           "FROM ventas v " +
           "WHERE v.fecha >= DATE_SUB(CURDATE(), INTERVAL 7 DAY) " +
           "AND v.estado = 'COMPLETADA' " +
           "GROUP BY DAYOFWEEK(v.fecha) " +
           "ORDER BY dia ASC", nativeQuery = true)
    List<Object[]> findVentasUltimos7Dias();
    
    /**
     * Obtiene el total de ventas de los últimos 12 meses agrupados por mes
     * Devuelve [mes (1-12), total]
     */
    @Query(value = "SELECT MONTH(v.fecha) as mes, SUM(v.total) as total " +
           "FROM ventas v " +
           "WHERE v.fecha >= DATE_SUB(CURDATE(), INTERVAL 12 MONTH) " +
           "AND v.estado = 'COMPLETADA' " +
           "GROUP BY MONTH(v.fecha) " +
           "ORDER BY mes ASC", nativeQuery = true)
    List<Object[]> findVentasUltimos12Meses();
    
    /**
     * Obtiene el total de ventas de los últimos 5 años agrupados por año
     * Devuelve [año, total]
     */
    @Query(value = "SELECT YEAR(v.fecha) as ano, SUM(v.total) as total " +
           "FROM ventas v " +
           "WHERE v.fecha >= DATE_SUB(CURDATE(), INTERVAL 5 YEAR) " +
           "AND v.estado = 'COMPLETADA' " +
           "GROUP BY YEAR(v.fecha) " +
           "ORDER BY ano ASC", nativeQuery = true)
    List<Object[]> findVentasUltimos5Anos();
} 