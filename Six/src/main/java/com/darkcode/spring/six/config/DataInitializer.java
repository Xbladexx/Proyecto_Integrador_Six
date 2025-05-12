package com.darkcode.spring.six.config;

import java.math.BigDecimal;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.darkcode.spring.six.models.entities.Categoria;
import com.darkcode.spring.six.models.entities.Inventario;
import com.darkcode.spring.six.models.entities.Producto;
import com.darkcode.spring.six.models.entities.VarianteProducto;
import com.darkcode.spring.six.models.repositories.CategoriaRepository;
import com.darkcode.spring.six.models.repositories.InventarioRepository;
import com.darkcode.spring.six.models.repositories.ProductoRepository;
import com.darkcode.spring.six.models.repositories.UsuarioRepository;
import com.darkcode.spring.six.models.repositories.VarianteProductoRepository;

import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
public class DataInitializer {

    @Bean
    public CommandLineRunner initData(
            UsuarioRepository usuarioRepository,
            CategoriaRepository categoriaRepository,
            ProductoRepository productoRepository,
            VarianteProductoRepository varianteRepository,
            InventarioRepository inventarioRepository) {
        
        return args -> {
            log.info("Iniciando carga de datos iniciales");
            
            // No se crean usuarios por defecto - Los usuarios deben ser creados manualmente en la BD
            
            // Crear categorías por defecto si no existen
            if (categoriaRepository.count() == 0) {
                log.info("Creando categorías por defecto");
                
                String[] categorias = {"Camisetas", "Pantalones", "Vestidos", "Chaquetas", "Blusas", "Accesorios"};
                
                for (String nombre : categorias) {
                    Categoria categoria = new Categoria();
                    categoria.setNombre(nombre);
                    categoria.setDescripcion("Categoría de " + nombre.toLowerCase());
                    categoriaRepository.save(categoria);
                }
                
                log.info("Categorías creadas: {}", categoriaRepository.count());
            }
            
            // Crear productos y variantes de ejemplo si no existen
            if (productoRepository.count() == 0) {
                log.info("Creando productos y variantes de ejemplo");
                
                // Obtener categorías
                Categoria camisetas = categoriaRepository.findByNombre("Camisetas").orElseThrow();
                Categoria pantalones = categoriaRepository.findByNombre("Pantalones").orElseThrow();
                Categoria vestidos = categoriaRepository.findByNombre("Vestidos").orElseThrow();
                Categoria chaquetas = categoriaRepository.findByNombre("Chaquetas").orElseThrow();
                Categoria blusas = categoriaRepository.findByNombre("Blusas").orElseThrow();
                
                // Crear productos
                Producto camisetaSlim = new Producto();
                camisetaSlim.setCodigo("CAM-001");
                camisetaSlim.setNombre("Camiseta Slim Fit");
                camisetaSlim.setCategoria(camisetas);
                camisetaSlim.setDescripcion("Camiseta de algodón con corte slim fit");
                camisetaSlim.setPrecio(new BigDecimal("79.90"));
                camisetaSlim.setCosto(new BigDecimal("40.00"));
                productoRepository.save(camisetaSlim);
                
                Producto vestidoCasual = new Producto();
                vestidoCasual.setCodigo("VES-001");
                vestidoCasual.setNombre("Vestido Casual");
                vestidoCasual.setCategoria(vestidos);
                vestidoCasual.setDescripcion("Vestido casual para el día a día");
                vestidoCasual.setPrecio(new BigDecimal("129.90"));
                vestidoCasual.setCosto(new BigDecimal("65.00"));
                productoRepository.save(vestidoCasual);
                
                Producto pantalonChino = new Producto();
                pantalonChino.setCodigo("PAN-001");
                pantalonChino.setNombre("Pantalón Chino");
                pantalonChino.setCategoria(pantalones);
                pantalonChino.setDescripcion("Pantalón chino de algodón");
                pantalonChino.setPrecio(new BigDecimal("99.90"));
                pantalonChino.setCosto(new BigDecimal("50.00"));
                productoRepository.save(pantalonChino);
                
                Producto blusaEstampada = new Producto();
                blusaEstampada.setCodigo("BLU-001");
                blusaEstampada.setNombre("Blusa Estampada");
                blusaEstampada.setCategoria(blusas);
                blusaEstampada.setDescripcion("Blusa con estampado multicolor");
                blusaEstampada.setPrecio(new BigDecimal("89.90"));
                blusaEstampada.setCosto(new BigDecimal("45.00"));
                productoRepository.save(blusaEstampada);
                
                Producto chaquetaDenim = new Producto();
                chaquetaDenim.setCodigo("CHA-001");
                chaquetaDenim.setNombre("Chaqueta Denim");
                chaquetaDenim.setCategoria(chaquetas);
                chaquetaDenim.setDescripcion("Chaqueta de jean clásica");
                chaquetaDenim.setPrecio(new BigDecimal("159.90"));
                chaquetaDenim.setCosto(new BigDecimal("80.00"));
                productoRepository.save(chaquetaDenim);
                
                Producto camisetaEstampada = new Producto();
                camisetaEstampada.setCodigo("CAM-002");
                camisetaEstampada.setNombre("Camiseta Estampada");
                camisetaEstampada.setCategoria(camisetas);
                camisetaEstampada.setDescripcion("Camiseta con estampado gráfico");
                camisetaEstampada.setPrecio(new BigDecimal("69.90"));
                camisetaEstampada.setCosto(new BigDecimal("35.00"));
                productoRepository.save(camisetaEstampada);
                
                Producto pantalonSkinny = new Producto();
                pantalonSkinny.setCodigo("PAN-002");
                pantalonSkinny.setNombre("Pantalón Skinny");
                pantalonSkinny.setCategoria(pantalones);
                pantalonSkinny.setDescripcion("Pantalón skinny de algodón elástico");
                pantalonSkinny.setPrecio(new BigDecimal("109.90"));
                pantalonSkinny.setCosto(new BigDecimal("55.00"));
                productoRepository.save(pantalonSkinny);
                
                log.info("Productos creados: {}", productoRepository.count());
                
                // Crear variantes para los productos
                crearVariante(camisetaSlim, "Negro", "M", "CAM-001-M-NEG", 15, varianteRepository, inventarioRepository);
                crearVariante(camisetaSlim, "Blanco", "L", "CAM-001-L-BLA", 8, varianteRepository, inventarioRepository);
                crearVariante(vestidoCasual, "Azul", "S", "VES-001-S-AZU", 2, varianteRepository, inventarioRepository);
                crearVariante(pantalonChino, "Beige", "32", "PAN-001-32-BEI", 5, varianteRepository, inventarioRepository);
                crearVariante(blusaEstampada, "Multicolor", "L", "BLU-001-L-MUL", 12, varianteRepository, inventarioRepository);
                crearVariante(chaquetaDenim, "Azul", "XL", "CHA-001-XL-AZU", 4, varianteRepository, inventarioRepository);
                crearVariante(camisetaEstampada, "Rojo", "S", "CAM-002-S-ROJ", 20, varianteRepository, inventarioRepository);
                crearVariante(pantalonSkinny, "Negro", "30", "PAN-002-30-NEG", 10, varianteRepository, inventarioRepository);
                
                log.info("Variantes creadas: {}", varianteRepository.count());
                log.info("Registros de inventario creados: {}", inventarioRepository.count());
            }
            
            log.info("Carga de datos iniciales completada");
        };
    }
    
    private void crearVariante(Producto producto, String color, String talla, String sku, int stock,
                               VarianteProductoRepository varianteRepository,
                               InventarioRepository inventarioRepository) {
        VarianteProducto variante = new VarianteProducto();
        variante.setProducto(producto);
        variante.setColor(color);
        variante.setTalla(talla);
        variante.setSku(sku);
        varianteRepository.save(variante);
        
        Inventario inventario = new Inventario();
        inventario.setVariante(variante);
        inventario.setStock(stock);
        inventarioRepository.save(inventario);
    }
} 