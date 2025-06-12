package com.darkcode.spring.six.Controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.darkcode.spring.six.services.ClasificacionABCService;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller
@RequiredArgsConstructor
@Slf4j
public class ViewController {

    private final ClasificacionABCService clasificacionABCService;

    @GetMapping("/reportes-abc")
    public String mostrarReportes(HttpSession session, Model model) {
        // Verificar si el usuario está autenticado
        if (session.getAttribute("usuario") == null) {
            return "redirect:/login";
        }
        
        // Iniciar precálculo de clasificación ABC en segundo plano
        Thread calculoABC = new Thread(() -> {
            try {
                log.info("Iniciando precálculo de clasificación ABC desde vista de reportes");
                clasificacionABCService.precalcularClasificacionABC();
            } catch (Exception e) {
                log.error("Error al iniciar precálculo de clasificación ABC: {}", e.getMessage(), e);
            }
        });
        calculoABC.setDaemon(true);
        calculoABC.start();
        
        return "Administrador/reportes";
    }
} 