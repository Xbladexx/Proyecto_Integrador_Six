package com.darkcode.spring.six.Controllers;

import java.util.Optional;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.darkcode.spring.six.models.entities.Inventario;
import com.darkcode.spring.six.models.entities.VarianteProducto;
import com.darkcode.spring.six.services.AuthService;
import com.darkcode.spring.six.services.InventarioService;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller
@RequiredArgsConstructor
@Slf4j
public class InventarioViewController {
    
    private final InventarioService inventarioService;
    private final AuthService authService;
    
    @GetMapping("/inventario/detalles")
    public String mostrarDetallesInventario(@RequestParam("id") Long inventarioId, Model model, HttpSession session) {
        // Verificar si el usuario está autenticado
        if (!authService.estaAutenticado(session)) {
            return "redirect:/login";
        }
        
        // Obtener el registro de inventario por ID
        Optional<Inventario> inventarioOpt = inventarioService.obtenerPorId(inventarioId);
        
        if (inventarioOpt.isPresent()) {
            Inventario inventario = inventarioOpt.get();
            VarianteProducto variante = inventario.getVariante();
            
            model.addAttribute("inventario", inventario);
            model.addAttribute("variante", variante);
            model.addAttribute("producto", variante.getProducto());
            model.addAttribute("esAdmin", authService.esAdmin(session));
            
            log.info("Mostrando detalles del inventario ID: {}, Producto: {}, Variante: {}", 
                    inventarioId, variante.getProducto().getNombre(), variante.getSku());
            
            // Determinar qué vista mostrar según el rol del usuario
            if (authService.esAdmin(session)) {
                return "Administrador/inventario-detalles";
            } else {
                return "Empleado/inventario-detalles";
            }
        } else {
            // Si no se encuentra el registro, mostrar un mensaje de error
            model.addAttribute("error", "No se encontró el registro de inventario solicitado");
            model.addAttribute("esAdmin", authService.esAdmin(session));
            
            log.warn("Intento de acceder a inventario no existente con ID: {}", inventarioId);
            
            // Redirigir a la lista de inventario
            return "redirect:/inventario";
        }
    }
}
