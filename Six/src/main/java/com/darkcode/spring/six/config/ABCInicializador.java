package com.darkcode.spring.six.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import com.darkcode.spring.six.services.ClasificacionABCService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Inicializador para precalcular la clasificación ABC al iniciar la aplicación
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
public class ABCInicializador {
    
    private final ClasificacionABCService clasificacionABCService;
    
    @Bean
    @Order(1) // Alta prioridad
    public CommandLineRunner inicializarClasificacionABC() {
        return args -> {
            log.info("Inicializando el cálculo de clasificación ABC en segundo plano...");
            
            // Iniciar el cálculo en un hilo separado para no bloquear el arranque de la aplicación
            Thread calculoABC = new Thread(() -> {
                try {
                    // Esperar un momento para que la aplicación se inicie completamente
                    Thread.sleep(2000); // Reducido de 5000ms a 2000ms
                    
                    // Llamar al servicio para precalcular la clasificación ABC
                    log.info("Comenzando el precálculo de clasificación ABC...");
                    clasificacionABCService.precalcularClasificacionABC();
                    
                    log.info("Precálculo de clasificación ABC completado con éxito");
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    log.error("Proceso de precálculo interrumpido: {}", e.getMessage());
                } catch (Exception e) {
                    log.error("Error al precalcular la clasificación ABC: {}", e.getMessage());
                    
                    // Intentar nuevamente después de un breve retraso
                    try {
                        log.info("Reintentando precálculo después de un error...");
                        Thread.sleep(3000);
                        clasificacionABCService.precalcularClasificacionABC();
                        log.info("Reintento de precálculo completado");
                    } catch (Exception ex) {
                        log.error("Error en el reintento de precálculo: {}", ex.getMessage());
                    }
                }
            });
            
            calculoABC.setDaemon(true);
            calculoABC.setPriority(Thread.MAX_PRIORITY);
            calculoABC.setName("ClasificacionABC-Initializer");
            calculoABC.start();
            
            log.info("Inicialización de clasificación ABC programada en segundo plano");
        };
    }
} 