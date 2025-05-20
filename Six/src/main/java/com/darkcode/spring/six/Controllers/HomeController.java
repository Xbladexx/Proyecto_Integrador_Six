package com.darkcode.spring.six.Controllers;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.lang.NonNull;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.darkcode.spring.six.dtos.DashboardEmpleadoDTO;
import com.darkcode.spring.six.dtos.InventarioDTO;
import com.darkcode.spring.six.dtos.VentaRecienteDTO;
import com.darkcode.spring.six.models.entities.Categoria;
import com.darkcode.spring.six.models.entities.DetalleVenta;
import com.darkcode.spring.six.models.entities.Producto;
import com.darkcode.spring.six.models.entities.Usuario;
import com.darkcode.spring.six.models.entities.VarianteProducto;
import com.darkcode.spring.six.models.entities.Venta;
import com.darkcode.spring.six.models.repositories.CategoriaRepository;
import com.darkcode.spring.six.models.repositories.DetalleVentaRepository;
import com.darkcode.spring.six.models.repositories.ProductoRepository;
import com.darkcode.spring.six.models.repositories.VentaRepository;
import com.darkcode.spring.six.services.AuthService;
import com.darkcode.spring.six.services.InventarioService;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller
@RequiredArgsConstructor
@Slf4j
public class HomeController {

    private final AuthService authService;
    private final InventarioService inventarioService;
    private final CategoriaRepository categoriaRepository;
    private final ProductoRepository productoRepository;
    private final VentaRepository ventaRepository;
    private final DetalleVentaRepository detalleVentaRepository;

    @GetMapping("/")
    public String index() {
        return "Login/index";
    }
    
    @PostMapping("/procesar")
    public String procesarFormulario(
            @RequestParam String usuario, 
            @RequestParam String password, 
            @NonNull Model model,
            HttpSession session) {
        
        log.info("Intento de inicio de sesión para el usuario: {}", usuario);
        
        // Validación de credenciales usando el servicio de autenticación y la base de datos
        Optional<Usuario> usuarioOpt = authService.autenticar(usuario, password);
        
        if (usuarioOpt.isPresent()) {
            Usuario usuarioEntity = usuarioOpt.get();
            
            // Verificar si el usuario está activo
            if (!usuarioEntity.isActivo()) {
                model.addAttribute("mensaje", "Su cuenta está desactivada. Contacte al administrador.");
                return "Login/index";
            }
            
            // Guardar datos en la sesión
            session.setAttribute("usuario", usuarioEntity.getUsuario());
            session.setAttribute("nombreUsuario", usuarioEntity.getNombre());
            session.setAttribute("rol", usuarioEntity.getRol());
            session.setAttribute("usuarioId", usuarioEntity.getId());
            
            // Actualizar la fecha de último acceso
            usuarioEntity.setUltimoAcceso(LocalDateTime.now());
            authService.actualizarUsuario(usuarioEntity);
            
            log.info("Usuario {} autenticado con éxito. Rol: {}", usuarioEntity.getUsuario(), usuarioEntity.getRol());
            
            // Redireccionar según el rol
            if (AuthService.ROL_ADMIN.equals(usuarioEntity.getRol())) {
                return "redirect:/dashboard-admin";
            } else {
                return "redirect:/dashboard-empleado";
            }
        } else {
            // Autenticación fallida
            log.warn("Intento de inicio de sesión fallido para el usuario: {}", usuario);
            model.addAttribute("mensaje", "Credenciales incorrectas. Por favor, inténtalo de nuevo.");
            return "Login/index";
        }
    }
    
    // Endpoint para el dashboard de administrador
    @GetMapping("/dashboard-admin")
    public String dashboardAdmin(@NonNull Model model, HttpSession session) {
        // Verificar si el usuario tiene el rol de admin
        if (!authService.esAdmin(session)) {
            return "redirect:/";
        }
        
        try {
            // Obtener el total de productos registrados
            long totalProductos = productoRepository.count();
            model.addAttribute("totalProductos", totalProductos);
            
            // Obtener el total de productos activos
            long productosActivos = productoRepository.findByEstado(Producto.Estado.ACTIVO).size();
            model.addAttribute("productosActivos", productosActivos);
            
            // Obtener productos con bajo stock
            // Para simplificar, consideramos que un producto tiene bajo stock si tiene menos de 10 unidades
            List<InventarioDTO> inventario = inventarioService.obtenerDetallesInventario();
            long productosBajoStock = inventario.stream()
                .filter(item -> item.getStock() != null && item.getStock() < 10)
                .count();
            model.addAttribute("productosBajoStock", productosBajoStock);
            
            // Calcular la tasa de rotación (esto es sólo un ejemplo, necesitaría datos reales de ventas)
            // En una aplicación real, se calcularía usando datos de ventas y stock promedio
            // rotación = ventas / stock promedio
            double tasaRotacion = 4.2; // Valor de ejemplo
            model.addAttribute("tasaRotacion", tasaRotacion);
            
            log.info("Dashboard Admin cargado con {} productos totales, {} productos activos y {} con bajo stock", 
                    totalProductos, productosActivos, productosBajoStock);
        } catch (Exception e) {
            log.error("Error al cargar datos para el dashboard de administrador", e);
            model.addAttribute("error", "Ocurrió un error al cargar los datos del dashboard");
        }
        
        return "Administrador/dashboard-admin";
    }
    
    // Endpoint para el dashboard de empleado
    @GetMapping("/dashboard-empleado")
    public String dashboardEmpleado(@NonNull Model model, HttpSession session) {
        // Verificar si el usuario tiene el rol de empleado
        if (!authService.esEmpleado(session)) {
            return "redirect:/";
        }
        
        // Obtener id de usuario de la sesión
        Long usuarioId = (Long) session.getAttribute("usuarioId");
        String usuario = (String) session.getAttribute("usuario");
        
        try {
            // Crear instancia del DTO del dashboard
            DashboardEmpleadoDTO dashboardData = new DashboardEmpleadoDTO();
            
            // Obtener ventas diarias del empleado
            Double totalVentas = ventaRepository.getTotalVentasHoyByUsuario(usuarioId);
            dashboardData.setVentasDiarias(totalVentas != null ? totalVentas : 0.0);
            
            // Obtener número de ventas realizadas hoy
            List<Venta> ventasHoy = ventaRepository.findVentasHoyByUsuario(usuarioId);
            dashboardData.setCantidadVentas(ventasHoy.size());
            
            // Obtener productos vendidos hoy
            Long productosVendidos = detalleVentaRepository.countProductosVendidosHoyByUsuario(usuarioId);
            dashboardData.setProductosVendidos(productosVendidos != null ? productosVendidos : 0L);
            
            // Obtener clientes atendidos
            Long clientesAtendidos = ventaRepository.countClientesAtendidosByUsuario(usuarioId);
            dashboardData.setClientesAtendidos(clientesAtendidos != null ? clientesAtendidos : 0L);
            
            // Obtener ventas recientes
            List<DetalleVenta> detallesRecientes = detalleVentaRepository.findAllVentasByUsuario(usuarioId);
            Map<Long, VentaRecienteDTO> ventasPorId = new HashMap<>();
            
            // Procesar los detalles para mostrar en el dashboard
            for (DetalleVenta detalle : detallesRecientes) {
                Venta venta = detalle.getVenta();
                Long ventaId = venta.getId();
                
                // Si la venta ya existe en el mapa, sólo añadimos el producto
                if (ventasPorId.containsKey(ventaId)) {
                    VentaRecienteDTO ventaDTO = ventasPorId.get(ventaId);
                    
                    // Añadir la descripción del producto
                    VarianteProducto variante = detalle.getVariante();
                    String descripcion = variante.getProducto().getNombre() + " - Talla " + 
                                     variante.getTalla() + ", " + variante.getColor();
                    ventaDTO.getProductos().add(descripcion);
                } else {
                    // Si la venta no existe en el mapa, la creamos
                    VentaRecienteDTO ventaReciente = new VentaRecienteDTO();
                    ventaReciente.setClienteNombre(venta.getCliente().getNombre());
                    
                    // Obtener las iniciales del cliente
                    String[] nombrePartes = venta.getCliente().getNombre().split(" ");
                    String iniciales = "";
                    if (nombrePartes.length > 0) {
                        iniciales += nombrePartes[0].substring(0, 1);
                        if (nombrePartes.length > 1) {
                            iniciales += nombrePartes[nombrePartes.length - 1].substring(0, 1);
                        }
                    }
                    ventaReciente.setClienteInicial(iniciales.toUpperCase());
                    ventaReciente.setMonto(venta.getTotal());
                    ventaReciente.setFecha(venta.getFecha());
                    
                    // Añadir el primer producto
                    VarianteProducto variante = detalle.getVariante();
                    String descripcion = variante.getProducto().getNombre() + " - Talla " + 
                                     variante.getTalla() + ", " + variante.getColor();
                    ventaReciente.getProductos().add(descripcion);
                    
                    // Guardar la venta en el mapa
                    ventasPorId.put(ventaId, ventaReciente);
                }
            }
            
            // Convertir el mapa a una lista y ordenarla por fecha descendente
            List<VentaRecienteDTO> ventasRecientes = new ArrayList<>(ventasPorId.values());
            ventasRecientes.sort((v1, v2) -> v2.getFecha().compareTo(v1.getFecha()));
            
            dashboardData.setVentasRecientes(ventasRecientes);
            
            // Agregar datos al modelo
            model.addAttribute("dashboardData", dashboardData);
            log.info("Dashboard de empleado cargado para usuario: {}", usuario);
        } catch (Exception e) {
            log.error("Error al cargar datos para el dashboard de empleado", e);
            model.addAttribute("error", "Ocurrió un error al cargar los datos del dashboard");
        }
        
        return "Empleado/dashboard-empleado";
    }
    
    // Endpoints para páginas accesibles tanto por admin como por empleado
    @GetMapping("/inventario")
    public String inventario(@NonNull Model model, HttpSession session) {
        // Verificar si el usuario está autenticado
        if (!authService.estaAutenticado(session)) {
            return "redirect:/";
        }
        
        // Añadir el rol al modelo para que la vista pueda ajustarse según el rol
        model.addAttribute("esAdmin", authService.esAdmin(session));
        
        // Obtenemos la lista de inventario y la añadimos al modelo
        try {
            List<InventarioDTO> inventarioDetails = inventarioService.obtenerDetallesInventario();
            model.addAttribute("inventarioItems", inventarioDetails);
            model.addAttribute("totalItems", inventarioDetails.size());
            log.info("Cargando vista de inventario con {} items", inventarioDetails.size());
        } catch (Exception e) {
            log.error("Error al cargar datos de inventario para la vista", e);
            model.addAttribute("error", "Ocurrió un error al cargar los datos del inventario");
        }
        
        if (authService.esAdmin(session)) {
            return "Administrador/inventario";
        } else {
            return "Empleado/inventario";
        }
    }
    
    @GetMapping("/ventas")
    public String ventas(@NonNull Model model, HttpSession session) {
        // Solo empleados tienen acceso a ventas
        if (!authService.esEmpleado(session)) {
            return "redirect:/";
        }
        return "Empleado/ventas";
    }
    
    // Endpoints para páginas accesibles solo por administrador
    @GetMapping("/productos")
    public String productos(@NonNull Model model, HttpSession session) {
        if (!authService.esAdmin(session)) {
            log.warn("Intento de acceso no autorizado a la página de productos");
            return "redirect:/";
        }
        
        try {
            // Verificar si hay categorías en la base de datos
            List<Categoria> categorias = categoriaRepository.findAll();
            
            // Si no hay categorías, crear algunas por defecto
            if (categorias.isEmpty()) {
                log.info("No se encontraron categorías, creando categorías por defecto");
                
                String[] nombresCategorias = {"Camisetas", "Pantalones", "Vestidos", "Chaquetas", "Blusas", "Accesorios"};
                for (String nombre : nombresCategorias) {
                    Categoria categoria = new Categoria();
                    categoria.setNombre(nombre);
                    categoria.setActivo(true);
                    categoriaRepository.save(categoria);
                }
                
                log.info("Categorías por defecto creadas correctamente");
            } else {
                log.info("Se encontraron {} categorías existentes", categorias.size());
            }
            
            // Verificar si hay productos
            long countProductos = productoRepository.count();
            log.info("Encontrados {} productos en la base de datos", countProductos);
            
            // Pasar información útil al modelo
            model.addAttribute("countCategorias", categoriaRepository.count());
            model.addAttribute("countProductos", countProductos);
            
        } catch (Exception e) {
            log.error("Error al preparar datos para la vista de productos", e);
            model.addAttribute("error", "Ocurrió un error al cargar los datos de productos");
        }
        
        return "Administrador/productos";
    }
    
    @GetMapping("/reportes")
    public String reportes(@NonNull Model model, HttpSession session) {
        if (!authService.esAdmin(session)) {
            return "redirect:/";
        }
        return "Administrador/reportes";
    }
    
    @GetMapping("/alertas")
    public String alertas(@NonNull Model model, HttpSession session) {
        if (!authService.esAdmin(session)) {
            return "redirect:/";
        }
        return "Administrador/alertas";
    }
    
    @GetMapping("/usuarios")
    public String usuarios(HttpSession session) {
        if (!authService.esAdmin(session)) {
            return "redirect:/";
        }
        return "redirect:/admin/usuarios";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }
    
    @PostMapping("/logout")
    public String logoutPost(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }
} 