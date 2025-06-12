package com.darkcode.spring.six.models.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.darkcode.spring.six.models.entities.Usuario;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    
    Optional<Usuario> findByNombre(String nombre);
    
    Optional<Usuario> findByNombreAndPassword(String nombre, String password);
    
    Optional<Usuario> findByUsuario(String usuario);
    
    Optional<Usuario> findByUsuarioAndPassword(String usuario, String password);
    
    boolean existsByNombre(String nombre);
    
    boolean existsByUsuario(String usuario);
    
    // Método para buscar usuarios por rol
    Optional<Usuario> findByRol(String rol);
    
    // Método para buscar usuarios por rol (lista)
    List<Usuario> findAllByRol(String rol);
} 