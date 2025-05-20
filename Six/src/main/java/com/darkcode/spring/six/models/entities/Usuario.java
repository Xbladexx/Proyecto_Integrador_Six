package com.darkcode.spring.six.models.entities;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "usuarios")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Usuario {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String nombre;
    
    @Column(unique = true, nullable = false, length = 10)
    private String usuario;
    
    @Column(nullable = false)
    private String password = "password123"; // Valor por defecto para evitar nulos
    
    @Column(nullable = false)
    private String rol = "EMPLEADO"; // Valor por defecto para evitar nulos
    
    @Column
    private String email;
    
    @Column
    private String telefono;
    
    @Column(nullable = false)
    private boolean activo = true;
    
    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;
    
    @Column(name = "ultimo_acceso")
    private LocalDateTime ultimoAcceso;
    
    @Column
    private String notas;
    
    /**
     * Método que se ejecuta antes de persistir la entidad
     * para asegurarse de que los campos obligatorios tengan valores
     */
    @PrePersist
    public void prePersist() {
        if (fechaCreacion == null) {
            fechaCreacion = LocalDateTime.now();
        }
        
        if (password == null || password.isEmpty()) {
            // Contraseña por defecto simple
            password = "Temporal" + System.currentTimeMillis() % 10000;
        }
        
        if (rol == null || rol.isEmpty()) {
            rol = "EMPLEADO";
        }
        
        // Última línea de defensa para evitar nulos en el campo usuario
        if (usuario == null || usuario.isEmpty()) {
            // Generamos un valor temporal único que será reemplazado por el servicio
            // Asegurando que nunca exceda 10 caracteres
            String valorTemporal = "TEMP" + (System.currentTimeMillis() % 9999);
            if (valorTemporal.length() > 10) {
                valorTemporal = valorTemporal.substring(0, 10);
            }
            usuario = valorTemporal;
            // Este valor es solo una protección contra nulos, no sustituye la lógica del servicio
        } else if (usuario.length() > 10) {
            // Si ya tiene un valor pero es demasiado largo, lo truncamos
            usuario = usuario.substring(0, 10);
        }
        
        // Ya no generamos usuario aquí para evitar conflictos con el servicio
        // El servicio UsuarioService se encargará de generar el usuario según el formato requerido
    }
    
    /**
     * Método que se ejecuta antes de actualizar la entidad
     * para asegurarse de que los campos obligatorios no se vuelvan nulos
     */
    @PreUpdate
    public void preUpdate() {
        if (password == null || password.isEmpty()) {
            // No permitimos que la contraseña se vuelva nula en actualizaciones
            // En este caso, mantenemos la contraseña anterior (esto se maneja en el servicio)
            throw new IllegalArgumentException("La contraseña no puede ser nula o vacía");
        }
        
        if (rol == null || rol.isEmpty()) {
            rol = "EMPLEADO";
        }
    }
} 