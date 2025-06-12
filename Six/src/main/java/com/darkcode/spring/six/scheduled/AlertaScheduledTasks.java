package com.darkcode.spring.six.scheduled;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.darkcode.spring.six.dtos.AlertaDTO;
import com.darkcode.spring.six.services.AlertaService;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class AlertaScheduledTasks {

    @Autowired
    private AlertaService alertaService;
    
    /**
     * Verifica el stock bajo cada 2 horas
     */
    @Scheduled(fixedRate = 7200000) // 2 horas = 7200000 ms
    public void verificarStockBajoRegularmente() {
        log.info("Ejecutando verificación programada de stock bajo");
        List<AlertaDTO> alertasGeneradas = alertaService.verificarStockBajo();
        log.info("Verificación programada completada. Se generaron {} alertas", alertasGeneradas.size());
    }
    
    /**
     * Verifica el stock bajo al inicio del sistema
     */
    @Scheduled(initialDelay = 60000, fixedDelay = Long.MAX_VALUE) // 60 segundos después del inicio
    public void verificarStockBajoInicial() {
        log.info("Ejecutando verificación inicial de stock bajo");
        List<AlertaDTO> alertasGeneradas = alertaService.verificarStockBajo();
        log.info("Verificación inicial completada. Se generaron {} alertas", alertasGeneradas.size());
    }
}
