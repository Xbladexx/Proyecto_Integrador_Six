package com.darkcode.spring.six.models.entities;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "clientes")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Cliente {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String nombre;
    
    @Column
    private String dni;
    
    @Column
    private String email;
    
    @Column
    private String telefono;
    
    @Column
    private String direccion;
    
    /**
     * Fecha y hora de registro del cliente en zona horaria de Perú (America/Lima) sin milisegundos
     */
    @Column(name = "fecha_registro")
    private LocalDateTime fechaRegistro = LocalDateTime.now();
    
    @OneToMany(mappedBy = "cliente")
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Set<Venta> ventas = new HashSet<>();
} 