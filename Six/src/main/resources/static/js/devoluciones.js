/**
 * devoluciones.js - Manejo de devoluciones de ventas
 * Proyecto SIX Inventario
 */

// Función para mostrar el indicador de carga
function mostrarCargando(tbody, colSpan) {
    tbody.innerHTML = `
        <tr>
            <td colspan="${colSpan}" class="text-center">
                <div class="loading-state">
                    <div class="spinner-border text-primary mb-3" role="status">
                        <span class="visually-hidden">Cargando...</span>
                    </div>
                    <p class="text-muted">Cargando datos...</p>
                </div>
            </td>
        </tr>
    `;
}

// Función para mostrar mensaje cuando no hay datos
function mostrarNoData(tbody, colSpan, mensaje = "No hay datos disponibles") {
    tbody.innerHTML = `
        <tr>
            <td colspan="${colSpan}" class="text-center">
                <div class="empty-state">
                    <i class="fas fa-info-circle fs-2 text-muted mb-3"></i>
                    <p class="text-muted">${mensaje}</p>
                </div>
            </td>
        </tr>
    `;
}

// Función para cargar las devoluciones de ventas
function cargarDevolucionesVentas() {
    const tbody = document.getElementById('tbody-devoluciones-ventas');
    if (!tbody) {
        console.error('No se encontró el elemento tbody-devoluciones-ventas');
        return;
    }
    
    // Mostrar indicador de carga
    mostrarCargando(tbody, 8); 
    
    console.log('Cargando devoluciones de ventas desde /api/ventas/devoluciones');
    
    // Obtener devoluciones desde el servidor
    fetch('/api/ventas/devoluciones')
        .then(response => {
            console.log('Respuesta recibida con status:', response.status);
            if (!response.ok) {
                throw new Error('Error al obtener devoluciones: ' + response.statusText);
            }
            return response.json();
        })
        .then(data => {
            console.log('Datos recibidos de devoluciones:', data);
            if (data && data.length > 0) {
                renderizarTablaDevolucionesVentas(data);
            } else {
                console.log('No se encontraron devoluciones');
                mostrarNoData(tbody, 8, "No hay devoluciones de ventas registradas");
            }
        })
        .catch(error => {
            console.error('Error al cargar devoluciones:', error);
            mostrarNoData(tbody, 8, "Error al cargar devoluciones: " + error.message);
        });
}

// Función para renderizar la tabla de devoluciones de ventas de manera más simple
function renderizarTablaDevolucionesVentas(devoluciones) {
    const tbody = document.getElementById('tbody-devoluciones-ventas');
    if (!tbody) return;
    
    tbody.innerHTML = '';
    
    console.log(`Renderizando ${devoluciones.length} devoluciones`);
    
    devoluciones.forEach(dev => {
        console.log('Procesando devolución:', dev);
        const tr = document.createElement('tr');
        
        // Formatear fecha
        let fecha = 'N/A';
        try {
            if (dev.fechaDevolucion) {
                fecha = new Date(dev.fechaDevolucion).toLocaleDateString('es-CO');
            } else if (dev.fechaVenta) {
                fecha = new Date(dev.fechaVenta).toLocaleDateString('es-CO');
            }
        } catch (e) {
            console.error('Error al formatear fecha:', e);
        }
        
        // Obtener cliente
        const cliente = dev.cliente ? (dev.cliente.nombre || 'N/A') : 'N/A';
        
        // Obtener producto
        let productoTexto = 'N/A';
        let cantidad = 0;
        
        if (dev.detalles && dev.detalles.length > 0) {
            // Extraer información del primer producto
            const primerDetalle = dev.detalles[0];
            
            // Intentar obtener el nombre del producto desde diferentes estructuras posibles
            if (primerDetalle.variante && primerDetalle.variante.producto) {
                productoTexto = primerDetalle.variante.producto.nombre || 'Producto sin nombre';
            } else if (primerDetalle.producto) {
                productoTexto = primerDetalle.producto.nombre || 'Producto sin nombre';
            }
            
            // Si hay más de un producto, indicarlo
            if (dev.detalles.length > 1) {
                productoTexto += ` y ${dev.detalles.length - 1} más`;
            }
            
            // Calcular cantidad total
            cantidad = dev.detalles.reduce((sum, det) => {
                return sum + (det.cantidad || 0);
            }, 0);
        }
        
        // Formatear monto
        let valorFormateado = 'S/. 0.00';
        try {
            const formatter = new Intl.NumberFormat('es-PE', {
                style: 'currency',
                currency: 'PEN',
                minimumFractionDigits: 2
            });
            valorFormateado = formatter.format(dev.total || 0);
        } catch (e) {
            console.error('Error al formatear valor:', e);
        }
        
        // Obtener vendedor
        const vendedor = dev.usuarioNombre || (dev.usuario ? dev.usuario.nombre : 'N/A');
        
        tr.innerHTML = `
            <td>${dev.codigo || dev.id || 'N/A'}</td>
            <td>${fecha}</td>
            <td>${cliente}</td>
            <td>${productoTexto}</td>
            <td class="text-center">${cantidad}</td>
            <td class="text-center">
                <span class="estado-badge estado-devuelta">DEVUELTA</span>
            </td>
            <td class="text-right">${valorFormateado}</td>
            <td>${vendedor}</td>
            <td class="text-center">
                <button class="btn btn-sm btn-info ver-detalle" data-id="${dev.id}">
                    <i class="fas fa-eye"></i> Ver
                </button>
            </td>
        `;
        
        tbody.appendChild(tr);
    });
    
    // Configurar eventos para botones de ver detalle
    document.querySelectorAll('.ver-detalle').forEach(btn => {
        btn.addEventListener('click', (e) => {
            e.preventDefault();
            const id = btn.getAttribute('data-id');
            mostrarDetalleDevolucion(id);
        });
    });
}

// Función auxiliar para capitalizar texto
function capitalizarPalabra(texto) {
    if (!texto) return '';
    return texto.charAt(0).toUpperCase() + texto.slice(1).toLowerCase();
}

// Mostrar detalles de una devolución
function mostrarDetalleDevolucion(id) {
    const modalEl = document.getElementById('modal-detalle-devolucion');
    if (!modalEl) {
        console.error('No se encontró el modal de detalle');
        return;
    }
    
    const modalBody = document.getElementById('detalle-devolucion-content');
    if (!modalBody) {
        console.error('No se encontró el contenido del modal');
        return;
    }
    
    // Mostrar loader en el modal
    modalBody.innerHTML = `
        <div class="text-center p-4">
            <div class="spinner-border text-primary mb-3" role="status">
                <span class="visually-hidden">Cargando...</span>
            </div>
            <p class="text-muted">Cargando detalles...</p>
        </div>
    `;
    
    // Mostrar modal mientras cargamos los datos
    const modalInstance = new bootstrap.Modal(modalEl);
    modalInstance.show();
    
    // Cargar los detalles desde el servidor
    fetch(`/api/ventas/devolucion/${id}/detalles`)
        .then(response => {
            if (!response.ok) {
                throw new Error('Error al obtener detalles de la devolución: ' + response.statusText);
            }
            return response.json();
        })
        .then(devolucion => {
            // Formato de moneda
            const formatter = new Intl.NumberFormat('es-PE', {
                style: 'currency',
                currency: 'PEN',
                minimumFractionDigits: 2
            });
            
            // Formato de fecha
            const fecha = new Date(devolucion.fechaVenta).toLocaleDateString('es-CO');
            const fechaDevolucion = devolucion.fechaDevolucion ? new Date(devolucion.fechaDevolucion).toLocaleDateString('es-CO') : 'N/A';
            
            // Cliente
            const clienteNombre = devolucion.cliente ? devolucion.cliente.nombre : 'N/A';
            
            // Vendedor
            const vendedor = devolucion.usuario ? devolucion.usuario.nombre : 'N/A';
            
            // HTML para la información básica
            let html = `
                <div class="row g-3">
                    <div class="col-md-6">
                        <p><strong>Código:</strong> ${devolucion.codigo || devolucion.id}</p>
                        <p><strong>Cliente:</strong> ${clienteNombre}</p>
                        <p><strong>Fecha de venta:</strong> ${fecha}</p>
                        <p><strong>Fecha de devolución:</strong> ${fechaDevolucion}</p>
                    </div>
                    <div class="col-md-6">
                        <p><strong>Estado:</strong> <span class="estado-badge estado-devuelta">DEVUELTA</span></p>
                        <p><strong>Subtotal:</strong> ${formatter.format(devolucion.subtotal || 0)}</p>
                        <p><strong>IGV:</strong> ${formatter.format(devolucion.igv || 0)}</p>
                        <p><strong>Total:</strong> ${formatter.format(devolucion.total || 0)}</p>
                        <p><strong>Vendedor:</strong> ${vendedor}</p>
                    </div>
                </div>
            `;
            
            // Tabla de productos
            if (devolucion.detalles && devolucion.detalles.length > 0) {
                html += `
                    <div class="mt-4">
                        <h6>Productos devueltos</h6>
                        <div class="table-responsive">
                            <table class="table table-sm">
                                <thead>
                                    <tr>
                                        <th>Producto</th>
                                        <th class="text-center">Cantidad</th>
                                        <th class="text-right">Precio Unit.</th>
                                        <th class="text-right">Subtotal</th>
                                    </tr>
                                </thead>
                                <tbody>
                `;
                
                devolucion.detalles.forEach(detalle => {
                    // Intentar obtener el nombre del producto desde diferentes estructuras
                    let productoNombre = 'Producto sin nombre';
                    
                    if (detalle.variante && detalle.variante.producto) {
                        productoNombre = detalle.variante.producto.nombre || 'Producto sin nombre';
                    } else if (detalle.producto) {
                        productoNombre = detalle.producto.nombre || 'Producto sin nombre';
                    }
                    
                    const precio = detalle.precioUnitario || 0;
                    const cantidad = detalle.cantidad || 0;
                    const subtotal = precio * cantidad;
                    
                    html += `
                        <tr>
                            <td>${productoNombre}</td>
                            <td class="text-center">${cantidad}</td>
                            <td class="text-right">${formatter.format(precio)}</td>
                            <td class="text-right">${formatter.format(subtotal)}</td>
                        </tr>
                    `;
                });
                
                html += `
                                </tbody>
                            </table>
                        </div>
                    </div>
                `;
            }
            
            modalBody.innerHTML = html;
        })
        .catch(error => {
            console.error('Error:', error);
            modalBody.innerHTML = `
                <div class="alert alert-danger">
                    <i class="fas fa-exclamation-triangle me-2"></i>
                    Error al cargar los detalles: ${error.message}
                </div>
            `;
        });
}

// Event listeners - Asegurar que se ejecuten solo cuando el DOM está listo
document.addEventListener('DOMContentLoaded', function() {
    console.log('DOM cargado en devoluciones.js');
    
    // Inicializar carga de devoluciones si estamos en la pestaña correspondiente
    const contentVentas = document.getElementById('content-ventas');
    if (contentVentas && contentVentas.classList.contains('active')) {
        cargarDevolucionesVentas();
    }
    
    // Configurar evento de búsqueda
    const searchVentas = document.getElementById('search-ventas');
    if (searchVentas) {
        searchVentas.addEventListener('input', () => {
            console.log('Buscando:', searchVentas.value);
            cargarDevolucionesVentas(); // Simplificamos por ahora, solo recarga todos los datos
        });
    }
}); 