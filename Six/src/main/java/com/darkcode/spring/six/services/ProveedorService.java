package com.darkcode.spring.six.services;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.darkcode.spring.six.models.entities.Proveedor;
import com.darkcode.spring.six.models.repositories.ProveedorRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProveedorService {
    
    @Autowired
    private ProveedorRepository proveedorRepository;
    
    /**
     * Obtiene todos los proveedores
     */
    public List<Proveedor> findAll() {
        return proveedorRepository.findAll();
    }
    
    /**
     * Busca un proveedor por su ID
     */
    public Proveedor findById(Long id) {
        return proveedorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Proveedor no encontrado con ID: " + id));
    }
    
    /**
     * Guarda un nuevo proveedor
     */
    @Transactional
    public Proveedor save(Proveedor proveedor) {
        // Validaciones
        if (proveedor.getNombre() == null || proveedor.getNombre().trim().isEmpty()) {
            throw new RuntimeException("El nombre del proveedor es obligatorio");
        }
        
        // Si es un nuevo proveedor, activarlo por defecto
        if (proveedor.getId() == null) {
            proveedor.setActivo(true);
        }
        
        return proveedorRepository.save(proveedor);
    }
    
    /**
     * Actualiza un proveedor existente
     */
    @Transactional
    public Proveedor update(Proveedor proveedor) {
        // Verificar que el proveedor existe
        if (proveedor.getId() == null) {
            throw new RuntimeException("No se puede actualizar un proveedor sin ID");
        }
        
        Proveedor proveedorExistente = findById(proveedor.getId());
        
        // Validaciones
        if (proveedor.getNombre() == null || proveedor.getNombre().trim().isEmpty()) {
            throw new RuntimeException("El nombre del proveedor es obligatorio");
        }
        
        return proveedorRepository.save(proveedor);
    }
    
    /**
     * Elimina un proveedor por su ID
     */
    @Transactional
    public void deleteById(Long id) {
        // Verificar que el proveedor existe
        Proveedor proveedor = findById(id);
        proveedorRepository.delete(proveedor);
    }
    
    /**
     * Busca proveedores cuyo nombre contiene el texto proporcionado
     */
    public List<Proveedor> findByNombreContaining(String nombre) {
        return proveedorRepository.findByNombreContainingIgnoreCase(nombre);
    }
    
    /**
     * Busca proveedores cuyo RUC contiene el texto proporcionado
     */
    public List<Proveedor> findByRucContaining(String ruc) {
        return proveedorRepository.findByRucContainingIgnoreCase(ruc);
    }
    
    /**
     * Obtiene todos los proveedores activos
     */
    public List<Proveedor> findAllActive() {
        return proveedorRepository.findByActivoTrue();
    }
    
    /**
     * Busca proveedores por nombre
     */
    public List<Proveedor> buscarPorNombre(String nombre) {
        return proveedorRepository.findByNombreContainingIgnoreCase(nombre);
    }
    
    /**
     * Busca un proveedor por RUC
     */
    public Optional<Proveedor> buscarPorRuc(String ruc) {
        return proveedorRepository.findByRuc(ruc);
    }
} 