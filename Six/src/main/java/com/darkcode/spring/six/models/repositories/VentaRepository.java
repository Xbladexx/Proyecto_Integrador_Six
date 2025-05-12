package com.darkcode.spring.six.models.repositories;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
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
} 