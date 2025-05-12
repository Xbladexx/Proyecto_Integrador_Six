package com.darkcode.spring.six.Controllers;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.darkcode.spring.six.models.entities.Usuario;
import com.darkcode.spring.six.services.AuthService;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller
@RequiredArgsConstructor
@Slf4j
public class HomeController {

    private final AuthService authService;

    @GetMapping("/")
    public String index() {
        return "Login/index";
    }
    
    @PostMapping("/procesar")
    public String procesarFormulario(
            @RequestParam String usuario, 
            @RequestParam String password, 
            Model model,
            HttpSession session) {
        
        log.info("Intento de inicio de sesión para el usuario: {}", usuario);
        
        // Validación de credenciales usando el servicio de autenticación y la base de datos
        Optional<Usuario> usuarioOpt = authService.autenticar(usuario, password);
        
        if (usuarioOpt.isPresent()) {
            Usuario usuarioEntity = usuarioOpt.get();
            
            // Verificar si el usuario está activo
            if (!usuarioEntity.isActivo()) {
                model.addAttribute("mensaje", "Su cuenta está desactivada. Contacte al administrador.");
                return "Login/index";
            }
            
            // Guardar datos en la sesión
            session.setAttribute("usuario", usuarioEntity.getUsuario());
            session.setAttribute("nombreUsuario", usuarioEntity.getNombre());
            session.setAttribute("rol", usuarioEntity.getRol());
            session.setAttribute("usuarioId", usuarioEntity.getId());
            
            // Actualizar la fecha de último acceso
            usuarioEntity.setUltimoAcceso(LocalDateTime.now());
            authService.actualizarUsuario(usuarioEntity);
            
            log.info("Usuario {} autenticado con éxito. Rol: {}", usuarioEntity.getUsuario(), usuarioEntity.getRol());
            
            // Redireccionar según el rol
            if (AuthService.ROL_ADMIN.equals(usuarioEntity.getRol())) {
                return "redirect:/dashboard-admin";
            } else {
                return "redirect:/dashboard-empleado";
            }
        } else {
            // Autenticación fallida
            log.warn("Intento de inicio de sesión fallido para el usuario: {}", usuario);
            model.addAttribute("mensaje", "Credenciales incorrectas. Por favor, inténtalo de nuevo.");
            return "Login/index";
        }
    }
    
    // Endpoint para el dashboard de administrador
    @GetMapping("/dashboard-admin")
    public String dashboardAdmin(Model model, HttpSession session) {
        // Verificar si el usuario tiene el rol de admin
        if (!authService.esAdmin(session)) {
            return "redirect:/";
        }
        
        return "Administrador/dashboard-admin";
    }
    
    // Endpoint para el dashboard de empleado
    @GetMapping("/dashboard-empleado")
    public String dashboardEmpleado(Model model, HttpSession session) {
        // Verificar si el usuario tiene el rol de empleado
        if (!authService.esEmpleado(session)) {
            return "redirect:/";
        }
        
        // Obtener nombre de usuario de la sesión
        String usuario = (String) session.getAttribute("usuario");
        model.addAttribute("mensaje", "Bienvenido " + usuario + ", has iniciado sesión como Empleado");
        return "Empleado/dashboard-empleado";
    }
    
    // Endpoints para páginas accesibles tanto por admin como por empleado
    @GetMapping("/inventario")
    public String inventario(Model model, HttpSession session) {
        // Verificar si el usuario está autenticado
        if (!authService.estaAutenticado(session)) {
            return "redirect:/";
        }
        
        // Añadir el rol al modelo para que la vista pueda ajustarse según el rol
        model.addAttribute("esAdmin", authService.esAdmin(session));
        
        if (authService.esAdmin(session)) {
            return "Administrador/inventario";
        } else {
            return "Empleado/inventario";
        }
    }
    
    @GetMapping("/ventas")
    public String ventas(Model model, HttpSession session) {
        // Solo empleados tienen acceso a ventas
        if (!authService.esEmpleado(session)) {
            return "redirect:/";
        }
        return "Empleado/ventas";
    }
    
    // Endpoints para páginas accesibles solo por administrador
    @GetMapping("/productos")
    public String productos(Model model, HttpSession session) {
        if (!authService.esAdmin(session)) {
            return "redirect:/";
        }
        return "Administrador/productos";
    }
    
    @GetMapping("/reportes")
    public String reportes(Model model, HttpSession session) {
        if (!authService.esAdmin(session)) {
            return "redirect:/";
        }
        return "Administrador/reportes";
    }
    
    @GetMapping("/alertas")
    public String alertas(Model model, HttpSession session) {
        if (!authService.esAdmin(session)) {
            return "redirect:/";
        }
        return "Administrador/alertas";
    }
    
    @GetMapping("/usuarios")
    public String usuarios(Model model, HttpSession session) {
        if (!authService.esAdmin(session)) {
            return "redirect:/";
        }
        return "redirect:/admin/usuarios";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }
    
    @PostMapping("/logout")
    public String logoutPost(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }
} 