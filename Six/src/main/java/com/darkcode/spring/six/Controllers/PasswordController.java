package com.darkcode.spring.six.Controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import lombok.extern.slf4j.Slf4j;

@Controller
@Slf4j
public class PasswordController {

    /**
     * Muestra la página de recuperación de contraseña
     */
    @GetMapping("/forgot-password")
    public String showForgotPasswordForm() {
        return "Login/forgot-password";
    }

    /**
     * Procesa la solicitud de recuperación de contraseña
     */
    @PostMapping("/recuperar-password")
    public String processForgotPassword(@RequestParam String email, RedirectAttributes redirectAttributes) {
        log.info("Solicitud de recuperación de contraseña para: {}", email);
        
        // En un entorno real, aquí se enviaría un correo con un token de recuperación
        // Por ahora, solo simulamos que se envió el correo
        
        redirectAttributes.addFlashAttribute("mensaje", "Se han enviado instrucciones a tu correo electrónico");
        return "redirect:/login";
    }
} 