package com.darkcode.spring.six.Controllers;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.darkcode.spring.six.dtos.UsuarioDTO;
import com.darkcode.spring.six.models.entities.Usuario;
import com.darkcode.spring.six.services.AuthService;
import com.darkcode.spring.six.services.UsuarioService;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminViewController {

    private static final Logger log = LoggerFactory.getLogger(AdminViewController.class);
    private final UsuarioService usuarioService;
    private final AuthService authService;
    
    /**
     * Vista mejorada de usuarios que recibe los datos directamente del modelo
     */
    @GetMapping("/usuarios")
    public String vistaUsuarios(@NonNull Model model, HttpSession session) {
        log.info("Cargando vista mejorada de usuarios");
        
        try {
            // Obtener los usuarios desde el servicio
            List<UsuarioDTO> usuarios = usuarioService.obtenerTodosUsuariosDTO();
            
            // Añadir usuarios al modelo para que estén disponibles en la vista
            model.addAttribute("usuarios", usuarios);
            model.addAttribute("totalUsuarios", usuarios.size());
            
            // Contar usuarios por rol
            long countAdmin = usuarios.stream()
                    .filter(u -> u.getRol() != null && u.getRol().toUpperCase().contains("ADMIN"))
                    .count();
            
            long countEmpleado = usuarios.stream()
                    .filter(u -> u.getRol() != null && u.getRol().toUpperCase().contains("EMPLEADO"))
                    .count();
            
            model.addAttribute("countAdmin", countAdmin);
            model.addAttribute("countEmpleado", countEmpleado);
            
            log.info("Vista de usuarios cargada con {} usuarios", usuarios.size());
        } catch (Exception e) {
            log.error("Error al cargar datos de usuarios para la vista", e);
            model.addAttribute("error", "Ocurrió un error al cargar los usuarios");
        }
        
        return "Administrador/usuarios-con-datos";
    }
    
    /**
     * Obtener datos de un usuario para edición (AJAX)
     */
    @GetMapping("/usuarios/obtener/{id}")
    @ResponseBody
    public ResponseEntity<?> obtenerUsuario(@PathVariable Long id) {
        log.info("Obteniendo datos de usuario con ID: {}", id);
        
        try {
            Optional<Usuario> usuarioOpt = usuarioService.obtenerPorId(id);
            
            if (usuarioOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            Usuario usuario = usuarioOpt.get();
            usuario.setPassword(null); // Por seguridad no enviamos la contraseña
            
            return ResponseEntity.ok(usuario);
        } catch (Exception e) {
            log.error("Error al obtener usuario", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al obtener el usuario: " + e.getMessage());
        }
    }
    
    /**
     * Procesar la edición de un usuario
     */
    @PostMapping("/usuarios/guardar")
    public String guardarUsuario(Usuario usuario, @RequestParam(required = false) String notas, @NonNull RedirectAttributes redirectAttributes) {
        log.info("Procesando guardado de usuario: {}", usuario.getNombre());
        
        try {
            // Verificar si es una actualización o creación
            boolean esNuevo = usuario.getId() == null;
            
            // Si es una actualización, obtener el usuario existente
            if (!esNuevo) {
                Optional<Usuario> usuarioExistente = usuarioService.obtenerPorId(usuario.getId());
                
                if (usuarioExistente.isEmpty()) {
                    redirectAttributes.addFlashAttribute("error", "Usuario no encontrado");
                    return "redirect:/admin/usuarios";
                }
                
                // Si no se proporciona contraseña, mantener la existente
                if (usuario.getPassword() == null || usuario.getPassword().isEmpty()) {
                    usuario.setPassword(usuarioExistente.get().getPassword());
                }
                
                // Mantener otros campos que no se actualizan en el formulario
                usuario.setFechaCreacion(usuarioExistente.get().getFechaCreacion());
                usuario.setUltimoAcceso(usuarioExistente.get().getUltimoAcceso());
            }
            
            // Establecer notas si se proporcionan
            if (notas != null && !notas.isEmpty()) {
                usuario.setNotas(notas);
            }
            
            // Guardar el usuario
            usuarioService.guardarUsuario(usuario);
            
            redirectAttributes.addFlashAttribute("mensaje", 
                    esNuevo ? "Usuario creado correctamente" : "Usuario actualizado correctamente");
            
            return "redirect:/admin/usuarios";
        } catch (Exception e) {
            log.error("Error al guardar usuario", e);
            redirectAttributes.addFlashAttribute("error", "Error al guardar el usuario: " + e.getMessage());
            return "redirect:/admin/usuarios";
        }
    }
    
    /**
     * Activar un usuario
     */
    @GetMapping("/usuarios/activar/{id}")
    public String activarUsuario(@PathVariable Long id, @NonNull RedirectAttributes redirectAttributes) {
        log.info("Activando usuario con ID: {}", id);
        
        try {
            usuarioService.activarUsuario(id);
            redirectAttributes.addFlashAttribute("mensaje", "Usuario activado correctamente");
        } catch (Exception e) {
            log.error("Error al activar usuario", e);
            redirectAttributes.addFlashAttribute("error", "Error al activar el usuario: " + e.getMessage());
        }
        
        return "redirect:/admin/usuarios";
    }
    
    /**
     * Desactivar un usuario
     */
    @GetMapping("/usuarios/desactivar/{id}")
    public String desactivarUsuario(@PathVariable Long id, @NonNull RedirectAttributes redirectAttributes) {
        log.info("Desactivando usuario con ID: {}", id);
        
        try {
            usuarioService.desactivarUsuario(id);
            redirectAttributes.addFlashAttribute("mensaje", "Usuario desactivado correctamente");
        } catch (Exception e) {
            log.error("Error al desactivar usuario", e);
            redirectAttributes.addFlashAttribute("error", "Error al desactivar el usuario: " + e.getMessage());
        }
        
        return "redirect:/admin/usuarios";
    }
    
    /**
     * Eliminar un usuario
     */
    @GetMapping("/usuarios/eliminar/{id}")
    public String eliminarUsuario(@PathVariable Long id, @NonNull RedirectAttributes redirectAttributes) {
        log.info("Eliminando usuario con ID: {}", id);
        
        try {
            usuarioService.eliminarUsuario(id);
            redirectAttributes.addFlashAttribute("mensaje", "Usuario eliminado correctamente");
        } catch (Exception e) {
            log.error("Error al eliminar usuario", e);
            redirectAttributes.addFlashAttribute("error", "Error al eliminar el usuario: " + e.getMessage());
        }
        
        return "redirect:/admin/usuarios";
    }
    
    /**
     * Resetear contraseña de usuario
     */
    @PostMapping("/usuarios/resetear-password")
    public String resetearPassword(
            @RequestParam Long id,
            @RequestParam String password,
            @NonNull RedirectAttributes redirectAttributes) {
        
        log.info("Procesando reseteo de contraseña para usuario con ID: {}", id);
        
        try {
            usuarioService.resetearPassword(id, password);
            redirectAttributes.addFlashAttribute("mensaje", "Contraseña reseteada correctamente");
        } catch (Exception e) {
            log.error("Error al resetear contraseña", e);
            redirectAttributes.addFlashAttribute("error", "Error al resetear la contraseña: " + e.getMessage());
        }
        
        return "redirect:/admin/usuarios";
    }

    @GetMapping("/ventas-registradas")
    public String ventasRegistradas(Model model, HttpSession session) {
        log.info("Cargando vista de ventas registradas");
        return "Administrador/ventas-registradas";
    }
    
    /**
     * Muestra la página de devoluciones para el administrador
     * @param session Sesión del usuario
     * @param model Modelo para la vista
     * @return Vista de devoluciones
     */
    @GetMapping("/devoluciones")
    public String showDevolucionesPage(HttpSession session, Model model) {
        // Verificar autenticación usando AuthService
        if (!authService.estaAutenticado(session)) {
            log.warn("Intento de acceso sin autenticación a la página de devoluciones");
            return "redirect:/";
        }
        
        // Verificar si es administrador usando AuthService
        if (!authService.esAdmin(session)) {
            log.warn("Usuario sin privilegios de administrador intentó acceder a devoluciones");
            if (authService.esEmpleado(session)) {
                return "redirect:/dashboard-empleado";
            } else {
                return "redirect:/";
            }
        }
        
        // Obtener información del usuario de la sesión
        String username = (String) session.getAttribute("usuario");
        model.addAttribute("usuario", username);
        
        return "Administrador/devoluciones";
    }
} 