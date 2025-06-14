package com.darkcode.spring.six.models.entities;

import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "variantes_producto")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VarianteProducto {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "producto_id", nullable = false)
    @JsonBackReference
    private Producto producto;
    
    @Column(nullable = false)
    private String color;
    
    @Column(nullable = false)
    private String talla;
    
    @Column(unique = true)
    private String sku;
    
    @Column(name = "imagen_url")
    private String imagenUrl;
    
    @OneToMany(mappedBy = "variante")
    @JsonIgnore
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Set<MovimientoStock> movimientos = new HashSet<>();
    
    @OneToMany(mappedBy = "variante")
    @JsonIgnore
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Set<LoteProducto> lotes = new HashSet<>();
} 