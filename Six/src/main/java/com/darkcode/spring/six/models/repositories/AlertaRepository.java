package com.darkcode.spring.six.models.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.darkcode.spring.six.models.entities.Alerta;
import com.darkcode.spring.six.models.entities.Usuario;
import com.darkcode.spring.six.models.entities.VarianteProducto;

@Repository
public interface AlertaRepository extends JpaRepository<Alerta, Long> {
    
    List<Alerta> findByLeida(boolean leida);
    
    List<Alerta> findByTipo(String tipo);
    
    List<Alerta> findByUsuario(Usuario usuario);
    
    List<Alerta> findByVariante(VarianteProducto variante);
    
    long countByLeida(boolean leida);
} 