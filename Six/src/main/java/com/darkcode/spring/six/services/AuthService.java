package com.darkcode.spring.six.services;

import java.util.Objects;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.darkcode.spring.six.models.entities.Usuario;
import com.darkcode.spring.six.models.repositories.UsuarioRepository;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {
    
    public static final String ROL_ADMIN = "ADMIN";
    public static final String ROL_EMPLEADO = "EMPLEADO";
    
    private final UsuarioRepository usuarioRepository;
    
    /**
     * Autenticar un usuario con código de usuario y contraseña
     * @param usuario Código de usuario para login
     * @param password Contraseña
     * @return Usuario autenticado o null si las credenciales son inválidas
     */
    public Optional<Usuario> autenticar(String usuario, String password) {
        return usuarioRepository.findByUsuarioAndPassword(usuario, password);
    }
    
    /**
     * Actualiza la información del usuario en la base de datos
     * @param usuario Usuario a actualizar
     * @return Usuario actualizado
     */
    public Usuario actualizarUsuario(Usuario usuario) {
        return usuarioRepository.save(usuario);
    }
    
    /**
     * Verificar si un usuario tiene un rol específico
     * @param session Sesión HTTP
     * @param rol Rol a verificar
     * @return true si tiene el rol, false en caso contrario
     */
    public boolean tieneRol(HttpSession session, String rol) {
        String rolUsuario = (String) session.getAttribute("rol");
        return Objects.equals(rolUsuario, rol);
    }
    
    /**
     * Verificar si un usuario es administrador
     * @param session Sesión HTTP
     * @return true si es administrador, false en caso contrario
     */
    public boolean esAdmin(HttpSession session) {
        return tieneRol(session, ROL_ADMIN);
    }
    
    /**
     * Verificar si un usuario es empleado
     * @param session Sesión HTTP
     * @return true si es empleado, false en caso contrario
     */
    public boolean esEmpleado(HttpSession session) {
        return tieneRol(session, ROL_EMPLEADO);
    }
    
    /**
     * Verificar si un usuario está autenticado
     * @param session Sesión HTTP
     * @return true si está autenticado, false en caso contrario
     */
    public boolean estaAutenticado(HttpSession session) {
        return session.getAttribute("usuario") != null;
    }
} 