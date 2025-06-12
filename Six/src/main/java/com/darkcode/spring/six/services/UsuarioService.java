package com.darkcode.spring.six.services;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.darkcode.spring.six.dtos.UsuarioDTO;
import com.darkcode.spring.six.models.entities.Usuario;
import com.darkcode.spring.six.models.repositories.UsuarioRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UsuarioService {
    
    private static final Logger log = LoggerFactory.getLogger(UsuarioService.class);
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
     * @return Usuario con ID generado y contraseña visible (solo en esta respuesta)
     */
    @Transactional
    public Usuario crearUsuarioConContraseña(Usuario usuario) {
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
            
            // IMPORTANTE: Forzar la generación del nombre de usuario con el prefijo correcto
            // Siempre generamos un nuevo nombre de usuario según el rol (ADM/EMP + 5 números)
            // Ignoramos cualquier valor existente que se haya proporcionado
            String nombreUsuarioGenerado = generarNombreUsuarioSegunRol(usuario.getRol());
            if (nombreUsuarioGenerado == null || nombreUsuarioGenerado.isEmpty()) {
                log.error("Error crítico: no se pudo generar un nombre de usuario válido");
                throw new IllegalStateException("No se pudo generar un nombre de usuario válido");
            }
            
            // Verificar que el nombre de usuario generado tenga el formato correcto
            if (!nombreUsuarioGenerado.matches("^(ADM|EMP)\\d{5}$")) {
                log.error("El nombre de usuario generado no cumple con el formato requerido: {}", nombreUsuarioGenerado);
                // Generar uno de emergencia con el formato correcto
                String prefijo = usuario.getRol().toUpperCase().contains("ADMIN") ? "ADM" : "EMP";
                String emergencyUsuario = prefijo + String.format("%05d", (int)(System.currentTimeMillis() % 100000));
                log.info("Generado nombre de usuario de emergencia con formato correcto: {}", emergencyUsuario);
                nombreUsuarioGenerado = emergencyUsuario;
            }
            
            // Establecer el nombre de usuario generado
            usuario.setUsuario(nombreUsuarioGenerado);
            log.info("Nombre de usuario establecido a: {}", nombreUsuarioGenerado);
            
            // SIEMPRE generamos una contraseña única basada en el nombre de usuario
            // Esto garantiza que nunca sea nula y que sea única para cada usuario
            String contraseñaGenerada = generarContraseñaAleatoria(nombreUsuarioGenerado);
            if (contraseñaGenerada == null || contraseñaGenerada.isEmpty()) {
                log.error("Error crítico: no se pudo generar una contraseña válida");
                throw new IllegalStateException("No se pudo generar una contraseña válida");
            }
            
            // Guardar la contraseña generada en el objeto usuario
            usuario.setPassword(contraseñaGenerada);
            log.info("Contraseña única generada para el usuario: {}", nombreUsuarioGenerado);
            
            // Guardar la contraseña en una variable temporal para devolverla al frontend
            final String passwordParaFrontend = contraseñaGenerada;
            
            // Establecer fecha de creación si no existe
            if (usuario.getFechaCreacion() == null) {
                usuario.setFechaCreacion(LocalDateTime.now());
            }
            
            // Activar usuario por defecto
            usuario.setActivo(true);
            
            // Verificar una última vez que el usuario tenga el formato correcto antes de guardar
            if (!usuario.getUsuario().matches("^(ADM|EMP)\\d{5}$")) {
                log.warn("Formato incorrecto detectado antes de guardar: {}, corrigiendo...", usuario.getUsuario());
                String prefijo = usuario.getRol().toUpperCase().contains("ADMIN") ? "ADM" : "EMP";
                usuario.setUsuario(prefijo + String.format("%05d", (int)(System.currentTimeMillis() % 100000)));
                log.info("Usuario corregido a: {}", usuario.getUsuario());
            }
            
            // Guardar el usuario en la base de datos
            try {
                log.info("Guardando usuario con nombre de usuario: {}", usuario.getUsuario());
                Usuario usuarioGuardado = usuarioRepository.save(usuario);
                log.info("Usuario guardado exitosamente con ID: {}, nombre de usuario: {}", 
                        usuarioGuardado.getId(), usuarioGuardado.getUsuario());
                
                // Importante: Asegurar que la contraseña se devuelve al frontend
                // pero solo en esta respuesta inicial, para que pueda ser mostrada al usuario
                usuarioGuardado.setPassword(passwordParaFrontend);
                
                log.info("Usuario creado exitosamente: ID={}, Usuario={}, Contraseña generada correctamente", 
                        usuarioGuardado.getId(), usuarioGuardado.getUsuario());
                
                return usuarioGuardado;
            } catch (DataIntegrityViolationException e) {
                log.error("Error de integridad al guardar usuario: {}", e.getMessage());
                throw new IllegalArgumentException("Ya existe un usuario con ese nombre de usuario o email");
            } catch (DataAccessException e) {
                log.error("Error de acceso a datos al guardar usuario: {}", e.getMessage(), e);
                throw new RuntimeException("Error al guardar el usuario en la base de datos");
            }
        } catch (IllegalArgumentException e) {
            log.error("Error de validación al crear usuario: {}", e.getMessage());
            throw e;
        } catch (IllegalStateException e) {
            // Manejo específico para errores de estado
            log.error("Error de estado al crear usuario: {}", e.getMessage(), e);
            throw new RuntimeException("Error en el estado de la aplicación: " + e.getMessage(), e);
        } catch (DataAccessException e) {
            // Manejo para errores de acceso a datos que no se capturaron en el bloque try interno
            log.error("Error de acceso a datos al crear usuario: {}", e.getMessage(), e);
            throw new RuntimeException("Error al acceder a la base de datos: " + e.getMessage(), e);
        } catch (RuntimeException e) {
            // Capturar otras excepciones de runtime
            log.error("Error de runtime al crear usuario: {}", e.getMessage(), e);
            throw new RuntimeException("Error de runtime al crear el usuario: " + e.getMessage(), e);
        } catch (Exception e) {
            // Capturar cualquier otra excepción no anticipada
            log.error("Error inesperado al crear usuario: {}", e.getMessage(), e);
            throw new RuntimeException("Error inesperado al crear el usuario", e);
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
            // Determinar prefijo según rol - siempre exactamente 3 caracteres
            String prefijo;
            if (rol != null && rol.toUpperCase().contains("ADMIN")) {
                prefijo = "ADM";
                log.info("Generando usuario con prefijo ADM para rol: {}", rol);
            } else {
                prefijo = "EMP";
                log.info("Generando usuario con prefijo EMP para rol: {}", rol);
            }
            
            // Generar exactamente 5 dígitos (entre 10000 y 99999)
            Random random = new Random();
            int numeroAleatorio = 10000 + random.nextInt(90000); // Número entre 10000 y 99999
            
            // Asegurar que el número tenga exactamente 5 dígitos
            String numeroFormateado = String.format("%05d", numeroAleatorio % 100000);
            
            // Construir nombre de usuario con prefijo + 5 dígitos exactos
            String nombreUsuarioBase = prefijo + numeroFormateado;
            log.info("Nombre de usuario base generado: {}", nombreUsuarioBase);
            
            // Verificar si ya existe y generar alternativas si es necesario
            String nombreUsuario = nombreUsuarioBase;
            int contador = 1;
            
            while (usuarioRepository.existsByUsuario(nombreUsuario) && contador < 100) {
                log.info("El usuario {} ya existe, generando alternativa", nombreUsuario);
                // Si ya existe, intentamos con otro número
                numeroAleatorio = 10000 + random.nextInt(90000);
                numeroFormateado = String.format("%05d", numeroAleatorio % 100000);
                nombreUsuario = prefijo + numeroFormateado;
                contador++;
            }
            
            if (contador >= 100) {
                log.error("No se pudo generar un nombre de usuario único después de múltiples intentos");
                // En lugar de lanzar excepción, generamos un nombre con timestamp que será único
                // Pero manteniendo el formato ADM/EMP + 5 dígitos exactamente
                long timestamp = System.currentTimeMillis() % 100000;
                String fallbackUsuario = prefijo + String.format("%05d", timestamp);
                log.info("Generado nombre de usuario alternativo con timestamp: {}", fallbackUsuario);
                return fallbackUsuario;
            }
            
            // Verificación final para asegurar que el formato sea correcto: 3 letras + 5 dígitos
            if (nombreUsuario.length() != 8 || !nombreUsuario.substring(0, 3).matches("[A-Z]{3}") || 
                !nombreUsuario.substring(3).matches("\\d{5}")) {
                log.error("El nombre de usuario generado no cumple con el formato requerido: {}", nombreUsuario);
                // Generar uno de emergencia con el formato correcto
                String emergencyUsuario = prefijo + String.format("%05d", System.currentTimeMillis() % 100000);
                log.info("Generado nombre de usuario de formato correcto: {}", emergencyUsuario);
                return emergencyUsuario;
            }
            
            log.info("Usuario final generado: {}", nombreUsuario);
            return nombreUsuario;
        } catch (Exception e) {
            log.error("Error generando nombre de usuario: {}", e.getMessage(), e);
            // En cualquier caso de error, asegurar que devolvemos ADM/EMP + 5 dígitos
            String fallbackPrefijo = (rol != null && rol.toUpperCase().contains("ADMIN")) ? "ADM" : "EMP";
            long timestamp = System.currentTimeMillis() % 100000;
            String fallbackUsuario = fallbackPrefijo + String.format("%05d", timestamp);
            log.info("Generado nombre de usuario de emergencia: {}", fallbackUsuario);
            return fallbackUsuario;
        }
    }
    
    /**
     * Genera una contraseña aleatoria segura y única para el usuario
     * @param usuario El nombre de usuario para el que se genera la contraseña
     * @return Una contraseña única para el usuario
     */
    private String generarContraseñaAleatoria(String usuario) {
        // Caracteres para la contraseña
        final String mayusculas = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        final String minusculas = "abcdefghijklmnopqrstuvwxyz";
        final String numeros = "0123456789";
        final String especiales = "!@#$%^&*()_-+=<>?/[]{}|";
        final int longitud = 12; // Longitud de la contraseña
        
        StringBuilder contraseña = new StringBuilder();
        Random random = new Random();
        
        // PARTE 1: Componente único basado en el usuario
        if (usuario != null && !usuario.isEmpty()) {
            // Extraer prefijo (ADM/EMP) y convertirlo en caracteres especiales
            String prefijo = usuario.substring(0, 3);
            contraseña.append(mayusculas.charAt(prefijo.charAt(0) % mayusculas.length()));
            contraseña.append(especiales.charAt(prefijo.charAt(1) % especiales.length()));
            
            // Extraer dígitos del usuario y usarlos para seleccionar caracteres
            String digitos = usuario.substring(3);
            for (int i = 0; i < Math.min(3, digitos.length()); i++) {
                int valor = Character.getNumericValue(digitos.charAt(i));
                contraseña.append(minusculas.charAt((valor * 2) % minusculas.length()));
            }
        }
        
        // PARTE 2: Componente único basado en tiempo
        // Usar milisegundos actuales para garantizar unicidad incluso para usuarios creados al mismo tiempo
        long timestamp = System.currentTimeMillis();
        String timeComponent = String.format("%04d", timestamp % 10000);
        contraseña.append(timeComponent);
        
        // PARTE 3: Garantizar requisitos de seguridad (al menos un caracter de cada tipo)
        contraseña.append(mayusculas.charAt(random.nextInt(mayusculas.length())));
        contraseña.append(minusculas.charAt(random.nextInt(minusculas.length())));
        contraseña.append(numeros.charAt(random.nextInt(numeros.length())));
        contraseña.append(especiales.charAt(random.nextInt(especiales.length())));
        
        // PARTE 4: Completar hasta la longitud deseada con caracteres aleatorios
        String todosCaracteres = mayusculas + minusculas + numeros + especiales;
        while (contraseña.length() < longitud) {
            contraseña.append(todosCaracteres.charAt(random.nextInt(todosCaracteres.length())));
        }
        
        // PARTE 5: Mezclar todos los caracteres para mayor seguridad
        char[] caracteres = contraseña.toString().toCharArray();
        for (int i = 0; i < caracteres.length; i++) {
            int j = random.nextInt(caracteres.length);
            char temp = caracteres[i];
            caracteres[i] = caracteres[j];
            caracteres[j] = temp;
        }
        
        // Generar la contraseña final
        String passwordFinal = new String(caracteres);
        if (passwordFinal.length() > longitud) {
            passwordFinal = passwordFinal.substring(0, longitud);
        }
        
        log.info("Contraseña única generada para usuario {}", usuario);
        return passwordFinal;
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
     * Convierte un Usuario en un DTO para la UI
     */
    private UsuarioDTO convertirADTO(Usuario usuario) {
        UsuarioDTO dto = new UsuarioDTO();
        dto.setId(usuario.getId());
        dto.setNombre(usuario.getNombre());
        dto.setEmail(usuario.getEmail());
        dto.setTelefono(usuario.getTelefono());
        dto.setUsuario(usuario.getUsuario());
        dto.setRol(usuario.getRol());
        dto.setActivo(usuario.isActivo());
        dto.setFechaCreacion(usuario.getFechaCreacion());
        dto.setUltimoAcceso(usuario.getUltimoAcceso());
        dto.setNotas(usuario.getNotas());
        return dto;
    }
    
    /**
     * Busca un usuario por su nombre de usuario
     * @param username El nombre de usuario a buscar
     * @return Optional conteniendo el usuario si existe
     */
    public Optional<Usuario> findByUsuario(String username) {
        return usuarioRepository.findByUsuario(username);
    }
} 