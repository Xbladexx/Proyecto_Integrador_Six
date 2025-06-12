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
    private String password; // La contraseña será generada por el servicio
    
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
        
        // IMPORTANTE: Verificamos si el campo usuario es nulo o vacío
        // Si es nulo, generamos un valor temporal que será reemplazado por el servicio
        if (usuario == null || usuario.isEmpty()) {
            // Generamos un valor temporal basado en el rol para evitar el error not-null
            // Este formato es compatible con la lógica del servicio y será reemplazado
            String prefijo = rol.toUpperCase().contains("ADMIN") ? "ADM" : "EMP";
            usuario = prefijo + String.format("%05d", (int)(System.currentTimeMillis() % 100000));
        } else if (usuario.length() > 10) {
            // Si ya tiene un valor pero es demasiado largo, lo truncamos
            usuario = usuario.substring(0, 10);
        }
        
        // El servicio UsuarioService se encargará de validar y posiblemente reemplazar
        // el nombre de usuario para garantizar unicidad
    }
    
    /**
     * Método que se ejecuta antes de actualizar la entidad
     * para asegurarse de que los campos obligatorios no se vuelvan nulos
     */
    @PreUpdate
    public void preUpdate() {
        if (fechaCreacion == null) {
            fechaCreacion = LocalDateTime.now();
        }
        
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