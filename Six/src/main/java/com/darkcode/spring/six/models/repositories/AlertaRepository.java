package com.darkcode.spring.six.models.repositories;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.darkcode.spring.six.models.entities.Alerta;
import com.darkcode.spring.six.models.entities.Alerta.PrioridadAlerta;
import com.darkcode.spring.six.models.entities.Alerta.TipoAlerta;
import com.darkcode.spring.six.models.entities.Producto;
import com.darkcode.spring.six.models.entities.Usuario;
import com.darkcode.spring.six.models.entities.VarianteProducto;

@Repository
public interface AlertaRepository extends JpaRepository<Alerta, Long> {
    
    // Buscar alertas por tipo
    List<Alerta> findByTipo(TipoAlerta tipo);
    
    // Buscar alertas por prioridad
    List<Alerta> findByPrioridad(PrioridadAlerta prioridad);
    
    // Buscar alertas no leídas
    List<Alerta> findByLeidaFalse();
    
    // Buscar alertas leídas
    List<Alerta> findByLeidaTrue();
    
    // Buscar alertas por usuario
    List<Alerta> findByUsuario(Usuario usuario);
    
    // Buscar alertas por producto
    List<Alerta> findByProducto(Producto producto);
    
    // Buscar alertas por variante
    List<Alerta> findByVariante(VarianteProducto variante);
    
    // Buscar alertas no leídas por tipo
    List<Alerta> findByTipoAndLeidaFalse(TipoAlerta tipo);
    
    // Buscar alertas por fecha de creación
    List<Alerta> findByFechaCreacionBetween(LocalDateTime inicio, LocalDateTime fin);
    
    // Obtener alertas paginadas
    Page<Alerta> findAll(Pageable pageable);
    
    // Obtener alertas paginadas por tipo
    Page<Alerta> findByTipo(TipoAlerta tipo, Pageable pageable);
    
    // Obtener alertas paginadas por usuario
    Page<Alerta> findByUsuario(Usuario usuario, Pageable pageable);
    
    // Contar alertas no leídas
    long countByLeidaFalse();
    
    // Contar alertas no leídas por tipo
    long countByTipoAndLeidaFalse(TipoAlerta tipo);
    
    // Contar alertas no leídas por usuario
    long countByUsuarioAndLeidaFalse(Usuario usuario);
    
    // Marcar todas las alertas como leídas
    @Modifying
    @Transactional
    @Query("UPDATE Alerta a SET a.leida = true, a.fechaLectura = :fechaLectura WHERE a.leida = false")
    int marcarTodasComoLeidas(@Param("fechaLectura") LocalDateTime fechaLectura);
    
    // Marcar alertas como leídas por ID
    @Modifying
    @Transactional
    @Query("UPDATE Alerta a SET a.leida = true, a.fechaLectura = :fechaLectura WHERE a.id IN :ids")
    int marcarComoLeidas(@Param("ids") List<Long> ids, @Param("fechaLectura") LocalDateTime fechaLectura);
    
    // Eliminar alertas leídas anteriores a una fecha
    @Modifying
    @Transactional
    @Query("DELETE FROM Alerta a WHERE a.leida = true AND a.fechaLectura < :fecha")
    int eliminarAlertasLeidasAnteriores(@Param("fecha") LocalDateTime fecha);
} 