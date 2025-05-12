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
        if (usuario.getId() != null) {
            return ResponseEntity.badRequest().build();
        }
        
        // Verificamos si ya existe un usuario con el mismo nombre de usuario
        if (usuarioRepository.existsByUsuario(usuario.getUsuario())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
        
        Usuario nuevoUsuario = usuarioRepository.save(usuario);
        nuevoUsuario.setPassword(null); // No devolvemos la contraseña
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevoUsuario);
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