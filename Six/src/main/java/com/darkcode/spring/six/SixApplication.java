package com.darkcode.spring.six;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.JdbcTemplate;

@SpringBootApplication
public class SixApplication {
    
    private static final Logger log = LoggerFactory.getLogger(SixApplication.class);
    
    @Autowired
    private JdbcTemplate jdbcTemplate;

	public static void main(String[] args) {
		SpringApplication.run(SixApplication.class, args);
	}

    @Bean
    public CommandLineRunner verificarConexion(JdbcTemplate jdbcTemplate) {
        return args -> {
            try {
                log.info("Verificando conexión a la base de datos...");
                String version = jdbcTemplate.queryForObject("SELECT VERSION()", String.class);
                log.info("Conexión exitosa. Versión de la base de datos: {}", version);
            } catch (Exception e) {
                log.error("Error al verificar la conexión a la base de datos", e);
            }
        };
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        try {
            log.info("Verificando base de datos al iniciar la aplicación...");
            
            // Verificar la estructura de la tabla movimientos_stock
            log.info("Verificando estructura de la tabla movimientos_stock...");
            
            jdbcTemplate.query(
                "SHOW COLUMNS FROM movimientos_stock",
                (rs, rowNum) -> rs.getString("Field")
            );
            
            log.info("Base de datos verificada con éxito.");
        } catch (Exception e) {
            log.error("Error al verificar la base de datos al iniciar: {}", e.getMessage(), e);
        }
    }
}
