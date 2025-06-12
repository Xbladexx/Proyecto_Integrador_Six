package com.darkcode.spring.six.models.repositories;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

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

    List<MovimientoStock> findByMotivoDetalleContaining(String texto);
    
    List<MovimientoStock> findByMotivoDetalleContainingAndFechaGreaterThanEqual(String texto, LocalDateTime fecha);

    List<MovimientoStock> findByVarianteIdOrderByFechaDesc(Long varianteId);
    
    List<MovimientoStock> findByUsuarioId(Long usuarioId);
    
    List<MovimientoStock> findByVarianteAndTipoAndFechaBetween(
            VarianteProducto variante, 
            TipoMovimiento tipo, 
            LocalDateTime fechaInicio, 
            LocalDateTime fechaFin);
    
    List<MovimientoStock> findByVarianteAndTipoAndMotivoAndFechaBetween(
            VarianteProducto variante, 
            TipoMovimiento tipo,
            MotivoMovimiento motivo,
            LocalDateTime fechaInicio, 
            LocalDateTime fechaFin);
    
    @Modifying
    @Transactional
    @Query("DELETE FROM MovimientoStock m WHERE m.variante.id = :varianteId")
    void deleteByVarianteId(@Param("varianteId") Long varianteId);
} 