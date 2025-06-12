package com.darkcode.spring.six.models.repositories;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.darkcode.spring.six.models.entities.LoteProducto;
import com.darkcode.spring.six.models.entities.Proveedor;
import com.darkcode.spring.six.models.entities.VarianteProducto;

@Repository
public interface LoteProductoRepository extends JpaRepository<LoteProducto, Long> {
    
    List<LoteProducto> findByVariante(VarianteProducto variante);
    
    List<LoteProducto> findByVarianteId(Long varianteId);
    
    // Buscar lotes por número de lote
    List<LoteProducto> findByNumeroLote(String numeroLote);
    
    // Buscar lotes próximos a vencer (en los próximos 30 días)
    @Query("SELECT l FROM LoteProducto l WHERE l.fechaVencimiento <= :fechaLimite AND l.fechaVencimiento >= :hoy AND l.cantidadActual > 0")
    List<LoteProducto> findLotesProximosAVencer(@Param("hoy") LocalDate hoy, @Param("fechaLimite") LocalDate fechaLimite);
    
    // Buscar lotes vencidos
    @Query("SELECT l FROM LoteProducto l WHERE l.fechaVencimiento < :hoy AND l.cantidadActual > 0")
    List<LoteProducto> findLotesVencidos(@Param("hoy") LocalDate hoy);
    
    // Buscar lotes disponibles (con stock) para una variante específica
    @Query("SELECT l FROM LoteProducto l WHERE l.variante.id = :varianteId AND l.cantidadActual > 0 ORDER BY l.fechaEntrada ASC")
    List<LoteProducto> findLotesDisponiblesByVarianteIdOrderByFechaEntradaAsc(@Param("varianteId") Long varianteId);
    
    // Buscar lotes disponibles no vencidos para una variante específica (para PEPS)
    @Query("SELECT l FROM LoteProducto l WHERE l.variante.id = :varianteId AND l.cantidadActual > 0 AND (l.fechaVencimiento IS NULL OR l.fechaVencimiento >= :hoy) ORDER BY l.fechaEntrada ASC")
    List<LoteProducto> findLotesDisponiblesNoVencidosByVarianteIdOrderByFechaEntradaAsc(
            @Param("varianteId") Long varianteId, 
            @Param("hoy") LocalDate hoy);
    
    // Buscar todos los lotes ordenados por fecha de registro descendente
    List<LoteProducto> findAllByOrderByFechaEntradaDesc();
    
    // Buscar lotes por estado
    List<LoteProducto> findByEstado(String estado);
    
    // Buscar lotes por estado y fecha de devolución entre un rango
    List<LoteProducto> findByEstadoAndFechaDevolucionBetween(String estado, LocalDateTime fechaDesde, LocalDateTime fechaHasta);
    
    // Buscar lotes por proveedor y estado distinto de un valor
    List<LoteProducto> findByProveedorAndEstadoNot(Proveedor proveedor, String estado);
} 