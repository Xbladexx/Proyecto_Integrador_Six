package com.darkcode.spring.six.Controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping("/proveedores")
@RequiredArgsConstructor
@Slf4j
public class ProveedorViewController {

    /**
     * Vista principal de proveedores (solo accesible para administradores)
     */
    @GetMapping
    public String vistaProveedores() {
        log.info("Accediendo a la vista de proveedores");
        return "Administrador/proveedores";
    }
} 