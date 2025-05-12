package com.darkcode.spring.six.Controllers;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.darkcode.spring.six.dtos.UsuarioDTO;
import com.darkcode.spring.six.services.UsuarioService;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
@Slf4j
public class AdminViewController {

    private final UsuarioService usuarioService;
    
    /**
     * Vista mejorada de usuarios que recibe los datos directamente del modelo
     */
    @GetMapping("/usuarios")
    public String vistaUsuarios(Model model, HttpSession session) {
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
} 