package com.darkcode.spring.six.dtos;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioDTO {
    private Long id;
    private String nombre;
    private String usuario;
    private String email;
    private String telefono;
    private String rol;
    private boolean activo;
    private LocalDateTime ultimoAcceso;
    private LocalDateTime fechaCreacion;
    private String notas;
} 