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
            
            // Preparar el campo usuario para que el servicio lo genere correctamente
            // En lugar de anularlo, generamos un valor temporal con el formato correcto
            // que el servicio podrá reemplazar si es necesario
            String prefijo = usuario.getRol().toUpperCase().contains("ADMIN") ? "ADM" : "EMP";
            String usuarioTemporal = prefijo + String.format("%05d", (int)(System.currentTimeMillis() % 100000));
            log.info("Generando nombre de usuario temporal con formato correcto: {}", usuarioTemporal);
            usuario.setUsuario(usuarioTemporal);
            
            // Asegurarse de que no se proporcione una contraseña (el servicio la generará)
            if (usuario.getPassword() != null && !usuario.getPassword().isEmpty()) {
                log.warn("Se proporcionó una contraseña, pero será ignorada y generada por el sistema");
                usuario.setPassword(null); // La anulamos para que el servicio la genere
            }
            
            // Establecer como activo por defecto
            usuario.setActivo(true);
            
            // Llamar al servicio para crear el usuario con contraseña autogenerada
            Usuario nuevoUsuario = usuarioService.crearUsuarioConContraseña(usuario);
            
            // IMPORTANTE: Devolver el usuario completo con la contraseña generada
            // Esta es la única vez que se devuelve la contraseña al frontend
            log.info("Usuario creado exitosamente: id={}, usuario={}, contraseña generada", 
                    nuevoUsuario.getId(), nuevoUsuario.getUsuario());
            
            return ResponseEntity.status(HttpStatus.CREATED).body(nuevoUsuario);
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
    
    @GetMapping("/verificar/{usuario}")
    public ResponseEntity<Void> verificarUsuarioExiste(@PathVariable String usuario) {
        log.info("Verificando si existe el usuario: {}", usuario);
        boolean existe = usuarioRepository.existsByUsuario(usuario);
        
        if (existe) {
            log.info("El usuario {} ya existe", usuario);
            return ResponseEntity.status(HttpStatus.CONFLICT).build(); // 409 Conflict
        } else {
            log.info("El usuario {} no existe, está disponible", usuario);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build(); // 404 Not Found
        }
    }
} 