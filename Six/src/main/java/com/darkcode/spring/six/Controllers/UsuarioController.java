package com.darkcode.spring.six.Controllers;

import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.darkcode.spring.six.dtos.UsuarioDTO;
import com.darkcode.spring.six.models.entities.Usuario;
import com.darkcode.spring.six.models.repositories.UsuarioRepository;
import com.darkcode.spring.six.services.UsuarioService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
@Slf4j
public class UsuarioController {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioService usuarioService;
    
    @GetMapping
    public ResponseEntity<List<Usuario>> obtenerTodos() {
        log.info("Obteniendo lista de usuarios");
        List<Usuario> usuarios = usuarioRepository.findAll();
        // Por seguridad, no devolvemos las contraseñas
        usuarios.forEach(u -> u.setPassword(null));
        return ResponseEntity.ok(usuarios);
    }
    
    @GetMapping("/detalles")
    public ResponseEntity<List<UsuarioDTO>> obtenerDetallesUsuarios() {
        log.info("Obteniendo detalles completos de usuarios");
        List<UsuarioDTO> usuarios = usuarioService.obtenerTodosUsuariosDTO();
        return ResponseEntity.ok(usuarios);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Usuario> obtenerPorId(@PathVariable Long id) {
        log.info("Obteniendo usuario con ID: {}", id);
        Optional<Usuario> usuario = usuarioRepository.findById(id);
        return usuario.map(u -> {
            u.setPassword(null); // No devolvemos la contraseña
            return ResponseEntity.ok(u);
        }).orElseGet(() -> ResponseEntity.notFound().build());
    }
    
    @GetMapping("/buscar")
    public ResponseEntity<Usuario> buscarPorUsuario(@RequestParam String usuario) {
        log.info("Buscando usuario por código de usuario: {}", usuario);
        Optional<Usuario> usuarioOpt = usuarioRepository.findByUsuario(usuario);
        return usuarioOpt.map(u -> {
            u.setPassword(null); // No devolvemos la contraseña
            return ResponseEntity.ok(u);
        }).orElseGet(() -> ResponseEntity.notFound().build());
    }
    
    @PostMapping
    public ResponseEntity<Usuario> crear(@RequestBody Usuario usuario) {
        log.info("Creando nuevo usuario: {}", usuario.getNombre());
        
        try {
            // Verificar campos obligatorios
            if (usuario.getNombre() == null || usuario.getNombre().isEmpty()) {
                log.error("Intento de crear usuario sin nombre");
                return ResponseEntity.badRequest().build();
            }
            
            // Verificar rol (el servicio asignará EMPLEADO si es nulo)
            if (usuario.getRol() == null || usuario.getRol().isEmpty()) {
                log.warn("Rol no especificado, se asignará EMPLEADO por defecto");
                usuario.setRol("EMPLEADO");
            } else {
                // Normalizar el rol para que coincida con el formato esperado
                if (usuario.getRol().toUpperCase().contains("ADMIN")) {
                    usuario.setRol("ADMIN");
                } else {
                    usuario.setRol("EMPLEADO");
                }
            }
            
            // Asegurarse de que no se proporcione un nombre de usuario (el servicio lo generará)
            if (usuario.getUsuario() != null && !usuario.getUsuario().isEmpty()) {
                log.warn("Se proporcionó un nombre de usuario, pero será generado por el sistema");
                // No lo establecemos a null, permitiendo que la entidad mantenga un valor temporal
                // El servicio se encargará de reemplazarlo con el valor correcto
            }
            
            // Si el ID no es nulo, rechazamos la solicitud
        if (usuario.getId() != null) {
                log.error("Intento de crear usuario con ID predefinido: {}", usuario.getId());
            return ResponseEntity.badRequest().build();
        }
        
            log.info("Datos del usuario a crear: nombre={}, email={}, rol={}", 
                    usuario.getNombre(), usuario.getEmail(), usuario.getRol());
            
            // Usamos el método del servicio para crear usuarios con comprobación adicional
            Usuario nuevoUsuario = null;
            try {
                // El servicio generará automáticamente: usuario, teléfono y contraseña
                nuevoUsuario = usuarioService.crearUsuarioConContraseña(usuario);
                log.info("Usuario creado en el servicio: id={}, usuario={}", 
                        nuevoUsuario.getId(), nuevoUsuario.getUsuario());
            } catch (Exception e) {
                log.error("Error al crear usuario en el servicio: {}", e.getMessage(), e);
                throw e;
            }
            
            // Verificar que el usuario se haya generado correctamente
            if (nuevoUsuario.getUsuario() == null || nuevoUsuario.getUsuario().isEmpty()) {
                log.error("Error: el servicio no generó un nombre de usuario válido");
                throw new IllegalStateException("No se pudo generar un nombre de usuario válido");
            }
            
            // Por seguridad, no devolvemos la contraseña en la respuesta
            Usuario respuesta = new Usuario();
            respuesta.setId(nuevoUsuario.getId());
            respuesta.setNombre(nuevoUsuario.getNombre());
            
            // Asegurarnos de que el usuario nunca sea nulo en la respuesta
            String nombreUsuario = nuevoUsuario.getUsuario();
            if (nombreUsuario == null || nombreUsuario.isEmpty()) {
                // Esto no debería ocurrir con las validaciones implementadas
                // Pero como última línea de defensa, generamos un valor
                nombreUsuario = (nuevoUsuario.getRol() != null && nuevoUsuario.getRol().toUpperCase().contains("ADMIN") 
                    ? "ADM" : "EMP") + (System.currentTimeMillis() % 9999);
                // Asegurar que no exceda 10 caracteres
                if (nombreUsuario.length() > 10) {
                    nombreUsuario = nombreUsuario.substring(0, 10);
                }
                log.warn("Se generó un usuario de emergencia en el controlador: {}", nombreUsuario);
            } else if (nombreUsuario.length() > 10) {
                // Si ya tiene un valor pero es demasiado largo, lo truncamos
                nombreUsuario = nombreUsuario.substring(0, 10);
                log.warn("Se truncó el usuario a 10 caracteres: {}", nombreUsuario);
            }
            respuesta.setUsuario(nombreUsuario);
            
            respuesta.setEmail(nuevoUsuario.getEmail());
            respuesta.setTelefono(nuevoUsuario.getTelefono());
            respuesta.setRol(nuevoUsuario.getRol());
            respuesta.setActivo(nuevoUsuario.isActivo());
            respuesta.setFechaCreacion(nuevoUsuario.getFechaCreacion());
            respuesta.setNotas(nuevoUsuario.getNotas());
            
            log.info("Usuario creado exitosamente: id={}, usuario={}, telefono={}", 
                    respuesta.getId(), respuesta.getUsuario(), respuesta.getTelefono());
            return ResponseEntity.status(HttpStatus.CREATED).body(respuesta);
        } catch (IllegalArgumentException e) {
            log.error("Error al crear usuario: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        } catch (Exception e) {
            log.error("Error interno al crear usuario: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<Usuario> actualizar(@PathVariable Long id, @RequestBody Usuario usuario) {
        log.info("Actualizando usuario con ID: {}", id);
        Optional<Usuario> usuarioExistente = usuarioRepository.findById(id);
        
        if (usuarioExistente.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        // Si no se proporciona contraseña, mantenemos la existente
        if (usuario.getPassword() == null || usuario.getPassword().isEmpty()) {
            usuario.setPassword(usuarioExistente.get().getPassword());
        }
        
        usuario.setId(id);
        Usuario usuarioActualizado = usuarioRepository.save(usuario);
        usuarioActualizado.setPassword(null); // No devolvemos la contraseña
        return ResponseEntity.ok(usuarioActualizado);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        log.info("Eliminando usuario con ID: {}", id);
        if (!usuarioRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        usuarioRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
    
    @PutMapping("/{id}/desactivar")
    public ResponseEntity<Usuario> desactivar(@PathVariable Long id) {
        log.info("Desactivando usuario con ID: {}", id);
        Optional<Usuario> usuarioOpt = usuarioRepository.findById(id);
        
        if (usuarioOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        Usuario usuario = usuarioOpt.get();
        usuario.setActivo(false);
        Usuario usuarioActualizado = usuarioRepository.save(usuario);
        usuarioActualizado.setPassword(null); // No devolvemos la contraseña
        return ResponseEntity.ok(usuarioActualizado);
    }
    
    @PutMapping("/{id}/activar")
    public ResponseEntity<Usuario> activar(@PathVariable Long id) {
        log.info("Activando usuario con ID: {}", id);
        Optional<Usuario> usuarioOpt = usuarioRepository.findById(id);
        
        if (usuarioOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        Usuario usuario = usuarioOpt.get();
        usuario.setActivo(true);
        Usuario usuarioActualizado = usuarioRepository.save(usuario);
        usuarioActualizado.setPassword(null); // No devolvemos la contraseña
        return ResponseEntity.ok(usuarioActualizado);
    }
} 