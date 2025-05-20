package com.darkcode.spring.six.services;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.darkcode.spring.six.dtos.UsuarioDTO;
import com.darkcode.spring.six.models.entities.Usuario;
import com.darkcode.spring.six.models.repositories.UsuarioRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class UsuarioService {
    
    private final UsuarioRepository usuarioRepository;
    
    /**
     * Obtiene una lista de todos los usuarios convertidos a DTO para la interfaz de usuario
     * @return Lista de DTOs con la información de usuarios
     */
    public List<UsuarioDTO> obtenerTodosUsuariosDTO() {
        List<Usuario> usuarios = usuarioRepository.findAll();
        
        return usuarios.stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Obtiene un usuario por su ID
     */
    public Optional<Usuario> obtenerPorId(Long id) {
        return usuarioRepository.findById(id);
    }
    
    /**
     * Guarda o actualiza un usuario
     */
    @Transactional
    public Usuario guardarUsuario(Usuario usuario) {
        log.info("Guardando usuario: {}", usuario.getNombre());
        return usuarioRepository.save(usuario);
    }
    
    /**
     * Crea un nuevo usuario con generación automática de contraseña
     */
    @Transactional
    public Usuario crearUsuarioConContraseña(Usuario usuario) {
        log.info("Creando nuevo usuario con contraseña autogenerada: {}", usuario.getNombre());
        
        try {
            // Validar datos mínimos
            if (usuario.getNombre() == null || usuario.getNombre().isEmpty()) {
                throw new IllegalArgumentException("El nombre del usuario es obligatorio");
            }
            
            // Normalizar el rol si es necesario
            if (usuario.getRol() == null || usuario.getRol().isEmpty()) {
                usuario.setRol("EMPLEADO");
                log.info("Rol establecido a EMPLEADO por defecto");
            } else if (usuario.getRol().toUpperCase().contains("ADMIN")) {
                usuario.setRol("ADMIN");
                log.info("Rol normalizado a ADMIN");
            } else {
                usuario.setRol("EMPLEADO");
                log.info("Rol normalizado a EMPLEADO");
            }
            
            // Generar teléfono automáticamente (9 dígitos comenzando con 9)
            if (usuario.getTelefono() == null || usuario.getTelefono().isEmpty()) {
                String telefono = generarTelefono();
                usuario.setTelefono(telefono);
                log.info("Teléfono generado automáticamente: {}", telefono);
            }
            
            // Siempre generamos un nuevo nombre de usuario según el rol (ADM/EMP + 5 números)
            // Ignoramos cualquier valor existente que se haya proporcionado
            String nombreUsuarioGenerado = generarNombreUsuarioSegunRol(usuario.getRol());
            if (nombreUsuarioGenerado == null || nombreUsuarioGenerado.isEmpty()) {
                log.error("Error crítico: no se pudo generar un nombre de usuario válido");
                throw new IllegalStateException("No se pudo generar un nombre de usuario válido");
            }
            usuario.setUsuario(nombreUsuarioGenerado);
            log.info("Nombre de usuario establecido a: {}", nombreUsuarioGenerado);
            
            // SIEMPRE generamos una contraseña, incluso si se proporcionó una
            // Esto garantiza que nunca sea nula
            String contraseñaGenerada = generarContraseñaAleatoria();
            if (contraseñaGenerada == null || contraseñaGenerada.isEmpty()) {
                log.error("Error crítico: no se pudo generar una contraseña válida");
                throw new IllegalStateException("No se pudo generar una contraseña válida");
            }
            log.info("Contraseña generada automáticamente para {}: {}", usuario.getUsuario(), contraseñaGenerada);
            usuario.setPassword(contraseñaGenerada);
            
            // Aseguramos que el usuario esté activo al crearse
            usuario.setActivo(true);
            
            // Establecemos la fecha de creación
            usuario.setFechaCreacion(LocalDateTime.now());
            
            // Validación final antes de guardar
            if (usuario.getUsuario() == null || usuario.getUsuario().isEmpty()) {
                log.error("Error crítico: el campo usuario sigue siendo nulo después de todas las validaciones");
                throw new IllegalStateException("El campo usuario no puede ser nulo al guardar");
            }
            
            // Guardar el usuario
            Usuario usuarioGuardado = usuarioRepository.save(usuario);
            log.info("Usuario guardado en la base de datos con ID: {}, usuario: {}", 
                    usuarioGuardado.getId(), usuarioGuardado.getUsuario());
            
            return usuarioGuardado;
        } catch (IllegalArgumentException e) {
            log.error("Error de validación al crear usuario: {}", e.getMessage(), e);
            throw e;
        } catch (DataIntegrityViolationException e) {
            log.error("Error de integridad de datos al crear usuario: {}", e.getMessage(), e);
            throw new IllegalArgumentException("Error al crear el usuario: posible duplicación de datos");
        } catch (RuntimeException e) {
            log.error("Error de ejecución al crear usuario: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * Genera un número de teléfono de 9 dígitos que comienza con 9
     */
    private String generarTelefono() {
        try {
            Random random = new Random();
            StringBuilder telefono = new StringBuilder("9"); // Comienza con 9
            
            // Añadir 8 dígitos aleatorios más para completar 9 dígitos
            for (int i = 0; i < 8; i++) {
                telefono.append(random.nextInt(10)); // Dígitos del 0 al 9
            }
            
            String resultado = telefono.toString();
            log.info("Teléfono generado: {} (longitud: {})", resultado, resultado.length());
            
            // Verificación de seguridad: garantizar exactamente 9 dígitos
            if (resultado.length() != 9) {
                log.warn("El teléfono generado no tiene 9 dígitos, ajustando...");
                if (resultado.length() < 9) {
                    // Si es menor, añadir ceros al final hasta completar 9 dígitos
                    while (resultado.length() < 9) {
                        resultado += "0";
                    }
                } else {
                    // Si es mayor, truncar a 9 dígitos
                    resultado = resultado.substring(0, 9);
                }
                log.info("Teléfono ajustado: {} (longitud: {})", resultado, resultado.length());
            }
            
            // Verificación final: asegurar que comienza con 9
            if (!resultado.startsWith("9")) {
                resultado = "9" + resultado.substring(1);
                log.info("Teléfono corregido para comenzar con 9: {}", resultado);
            }
            
            return resultado;
        } catch (Exception e) {
            log.error("Error generando teléfono: {}", e.getMessage(), e);
            // En caso de cualquier error, devolver un número fijo válido
            return "900000000";
        }
    }
    
    /**
     * Genera un nombre de usuario según el rol (ADM/EMP + 5 números)
     * @return El nombre de usuario generado
     */
    private String generarNombreUsuarioSegunRol(String rol) {
        try {
            // Determinar prefijo según rol
            String prefijo;
            if (rol != null && rol.toUpperCase().contains("ADMIN")) {
                prefijo = "ADM";
                log.info("Generando usuario con prefijo ADM para rol: {}", rol);
            } else {
                prefijo = "EMP";
                log.info("Generando usuario con prefijo EMP para rol: {}", rol);
            }
            
            // Generar números aleatorios para completar
            // Como el prefijo ocupa 3 caracteres, podemos generar 7 números (pero usamos 5 para el formato solicitado)
            Random random = new Random();
            int numeroAleatorio = 10000 + random.nextInt(90000); // Número entre 10000 y 99999
            
            // Construir nombre de usuario (garantizando máximo 10 caracteres)
            String nombreUsuarioBase = prefijo + numeroAleatorio;
            if (nombreUsuarioBase.length() > 10) {
                nombreUsuarioBase = nombreUsuarioBase.substring(0, 10);
                log.info("Nombre de usuario truncado a 10 caracteres: {}", nombreUsuarioBase);
            }
            
            log.info("Nombre de usuario base generado: {}", nombreUsuarioBase);
            
            // Verificar si ya existe y generar alternativas si es necesario
            String nombreUsuario = nombreUsuarioBase;
            int contador = 1;
            
            while (usuarioRepository.existsByUsuario(nombreUsuario) && contador < 100) {
                log.info("El usuario {} ya existe, generando alternativa", nombreUsuario);
                // Si ya existe, intentamos con otro número
                numeroAleatorio = 10000 + random.nextInt(90000);
                nombreUsuario = prefijo + numeroAleatorio;
                if (nombreUsuario.length() > 10) {
                    nombreUsuario = nombreUsuario.substring(0, 10);
                }
                contador++;
            }
            
            if (contador >= 100) {
                log.error("No se pudo generar un nombre de usuario único después de múltiples intentos");
                // En lugar de lanzar excepción, generamos un nombre con timestamp que será único
                String fallbackUsuario = (rol != null && rol.toUpperCase().contains("ADMIN") ? "ADM" : "EMP") + 
                       (System.currentTimeMillis() % 9999999); // Usar módulo para limitar a 7 dígitos máximo
                if (fallbackUsuario.length() > 10) {
                    fallbackUsuario = fallbackUsuario.substring(0, 10);
                }
                return fallbackUsuario;
            }
            
            log.info("Usuario final generado: {}", nombreUsuario);
            return nombreUsuario;
        } catch (IllegalArgumentException | NullPointerException e) {
            log.error("Error específico generando nombre de usuario: {}", e.getMessage(), e);
            // Backup: generar un nombre de usuario simple pero único
            String fallbackUsuario = (rol != null && rol.toUpperCase().contains("ADMIN") ? "ADM" : "EMP") + 
                    (System.currentTimeMillis() % 9999999); // Limitar a 7 dígitos máximo
            if (fallbackUsuario.length() > 10) {
                fallbackUsuario = fallbackUsuario.substring(0, 10);
            }
            log.info("Generado nombre de usuario alternativo: {}", fallbackUsuario);
            return fallbackUsuario;
        } catch (RuntimeException e) {
            log.error("Error inesperado generando nombre de usuario: {}", e.getMessage(), e);
            // Backup simple en caso de cualquier otro error
            String fallbackUsuario = "EMP" + (System.currentTimeMillis() % 9999999);
            if (fallbackUsuario.length() > 10) {
                fallbackUsuario = fallbackUsuario.substring(0, 10);
            }
            log.info("Generado nombre de usuario alternativo de emergencia: {}", fallbackUsuario);
            return fallbackUsuario;
        } catch (Exception e) {
            log.error("Error fatal generando nombre de usuario: {}", e.getMessage(), e);
            // Última línea de defensa, siempre debe devolver un valor
            String fallbackUsuario = "TMP" + (System.currentTimeMillis() % 9999999);
            if (fallbackUsuario.length() > 10) {
                fallbackUsuario = fallbackUsuario.substring(0, 10);
            }
            return fallbackUsuario;
        }
    }
    
    /**
     * Genera una contraseña aleatoria
     */
    private String generarContraseñaAleatoria() {
        String caracteres = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%";
        StringBuilder contraseña = new StringBuilder();
        
        // Generamos una contraseña de 8 caracteres
        Random random = new Random();
        for (int i = 0; i < 8; i++) {
            int index = random.nextInt(caracteres.length());
            contraseña.append(caracteres.charAt(index));
        }
        
        return contraseña.toString();
    }
    
    /**
     * Activa un usuario
     */
    @Transactional
    public void activarUsuario(Long id) {
        log.info("Activando usuario con ID: {}", id);
        Optional<Usuario> usuarioOpt = usuarioRepository.findById(id);
        
        if (usuarioOpt.isPresent()) {
            Usuario usuario = usuarioOpt.get();
            usuario.setActivo(true);
            usuarioRepository.save(usuario);
        } else {
            log.error("No se pudo activar el usuario con ID {} porque no existe", id);
            throw new RuntimeException("Usuario no encontrado");
        }
    }
    
    /**
     * Desactiva un usuario
     */
    @Transactional
    public void desactivarUsuario(Long id) {
        log.info("Desactivando usuario con ID: {}", id);
        Optional<Usuario> usuarioOpt = usuarioRepository.findById(id);
        
        if (usuarioOpt.isPresent()) {
            Usuario usuario = usuarioOpt.get();
            usuario.setActivo(false);
            usuarioRepository.save(usuario);
        } else {
            log.error("No se pudo desactivar el usuario con ID {} porque no existe", id);
            throw new RuntimeException("Usuario no encontrado");
        }
    }
    
    /**
     * Elimina un usuario
     */
    @Transactional
    public void eliminarUsuario(Long id) {
        log.info("Eliminando usuario con ID: {}", id);
        if (usuarioRepository.existsById(id)) {
            usuarioRepository.deleteById(id);
        } else {
            log.error("No se pudo eliminar el usuario con ID {} porque no existe", id);
            throw new RuntimeException("Usuario no encontrado");
        }
    }
    
    /**
     * Resetea la contraseña de un usuario
     */
    @Transactional
    public void resetearPassword(Long id, String nuevaPassword) {
        log.info("Reseteando contraseña para usuario con ID: {}", id);
        Optional<Usuario> usuarioOpt = usuarioRepository.findById(id);
        
        if (usuarioOpt.isPresent()) {
            Usuario usuario = usuarioOpt.get();
            usuario.setPassword(nuevaPassword);
            usuarioRepository.save(usuario);
        } else {
            log.error("No se pudo resetear la contraseña del usuario con ID {} porque no existe", id);
            throw new RuntimeException("Usuario no encontrado");
        }
    }
    
    /**
     * Convierte una entidad Usuario a un DTO
     */
    private UsuarioDTO convertirADTO(Usuario usuario) {
        UsuarioDTO dto = new UsuarioDTO();
        dto.setId(usuario.getId());
        dto.setNombre(usuario.getNombre());
        dto.setUsuario(usuario.getUsuario());
        dto.setEmail(usuario.getEmail());
        dto.setRol(usuario.getRol());
        dto.setActivo(usuario.isActivo());
        dto.setUltimoAcceso(usuario.getUltimoAcceso());
        dto.setFechaCreacion(usuario.getFechaCreacion());
        dto.setNotas(usuario.getNotas());
        
        return dto;
    }
} 