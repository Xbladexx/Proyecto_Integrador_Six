package com.darkcode.spring.six.models.repositories;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.darkcode.spring.six.models.entities.MovimientoStock;
import com.darkcode.spring.six.models.entities.MovimientoStock.MotivoMovimiento;
import com.darkcode.spring.six.models.entities.MovimientoStock.TipoMovimiento;
import com.darkcode.spring.six.models.entities.Usuario;
import com.darkcode.spring.six.models.entities.VarianteProducto;

@Repository
public interface MovimientoStockRepository extends JpaRepository<MovimientoStock, Long> {
    
    List<MovimientoStock> findByVariante(VarianteProducto variante);
    
    List<MovimientoStock> findByUsuario(Usuario usuario);
    
    List<MovimientoStock> findByTipo(TipoMovimiento tipo);
    
    List<MovimientoStock> findByMotivo(MotivoMovimiento motivo);
    
    List<MovimientoStock> findByFechaBetween(LocalDateTime fechaInicio, LocalDateTime fechaFin);

    List<MovimientoStock> findByVarianteIdOrderByFechaDesc(Long varianteId);
    
    List<MovimientoStock> findByUsuarioId(Long usuarioId);
} 