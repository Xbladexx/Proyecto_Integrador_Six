/**
 * devoluciones-lotes.js - Manejo de devoluciones de lotes a proveedor
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

// Función para cargar las devoluciones de lotes
function cargarDevolucionesLotes() {
    const tbody = document.getElementById('tbody-devoluciones-lotes');
    if (!tbody) {
        console.error('No se encontró el elemento tbody-devoluciones-lotes');
        return;
    }
    
    // Mostrar indicador de carga
    mostrarCargando(tbody, 10);
    
    console.log('Cargando devoluciones de lotes desde /api/lotes/devoluciones');
    
    // Obtener devoluciones desde el servidor
    fetch('/api/lotes/devoluciones')
        .then(response => {
            console.log('Respuesta recibida con status:', response.status);
            if (!response.ok) {
                throw new Error('Error al obtener devoluciones de lotes: ' + response.statusText);
            }
            return response.json();
        })
        .then(data => {
            console.log('Datos recibidos de devoluciones de lotes:', data);
            if (data && data.length > 0) {
                renderizarTablaDevolucionesLotes(data);
            } else {
                console.log('No se encontraron devoluciones de lotes');
                mostrarNoData(tbody, 10, "No hay devoluciones de lotes registradas");
            }
        })
        .catch(error => {
            console.error('Error al cargar devoluciones de lotes:', error);
            mostrarNoData(tbody, 10, "Error al cargar devoluciones: " + error.message);
        });
}

// Función para renderizar la tabla de devoluciones de lotes
function renderizarTablaDevolucionesLotes(devoluciones) {
    const tbody = document.getElementById('tbody-devoluciones-lotes');
    if (!tbody) return;
    
    tbody.innerHTML = '';
    
    console.log(`Renderizando ${devoluciones.length} devoluciones de lotes`);
    
    devoluciones.forEach(dev => {
        console.log('Procesando devolución de lote:', dev);
        const tr = document.createElement('tr');
        
        // Formatear fecha
        let fecha = 'N/A';
        try {
            if (dev.fechaDevolucion) {
                fecha = new Date(dev.fechaDevolucion).toLocaleDateString('es-ES');
            } else if (dev.fecha) {
                fecha = new Date(dev.fecha).toLocaleDateString('es-ES');
            }
        } catch (e) {
            console.error('Error al formatear fecha:', e);
        }
        
        // Obtener información del proveedor
        const proveedorNombre = dev.proveedor ? dev.proveedor.nombre : 'N/A';
        
        // Obtener información del lote
        let loteInfo = 'N/A';
        if (dev.lote && dev.lote.codigo) {
            loteInfo = dev.lote.codigo;
        } else if (dev.codigo) {
            loteInfo = dev.codigo;
        } else if (dev.id) {
            loteInfo = 'LOT-' + dev.id;
        }
        
        // Obtener productos del lote
        let productosTexto = 'N/A';
        let cantidad = dev.cantidad || 0;
        
        if (dev.productos && dev.productos.length > 0) {
            const productos = dev.productos;
            
            // Extraer información del primer producto
            const primerProducto = productos[0];
            
            // Intentar obtener el nombre del producto
            if (primerProducto.nombre) {
                productosTexto = primerProducto.nombre;
            } else if (primerProducto.producto && primerProducto.producto.nombre) {
                productosTexto = primerProducto.producto.nombre;
            }
            
            // Si hay más de un producto, indicarlo
            if (productos.length > 1) {
                productosTexto += ` y ${productos.length - 1} más`;
            }
            
            // Actualizar cantidad si no está definida a nivel de la devolución
            if (!dev.cantidad) {
                cantidad = productos.reduce((sum, p) => {
                    return sum + (p.cantidad || 0);
                }, 0);
            }
        } else if (dev.variante && dev.variante.producto) {
            // Si no hay array de productos pero tenemos información de variante/producto
            productosTexto = dev.variante.producto.nombre || 'Producto';
            cantidad = dev.cantidadInicial || 1;
        }
        
        // Formatear monto
        let valorFormateado = 'S/ 0.00';
        try {
            const valor = dev.valor || dev.total || 0;
            const formatter = new Intl.NumberFormat('es-PE', {
                style: 'currency',
                currency: 'PEN',
                minimumFractionDigits: 2
            });
            valorFormateado = formatter.format(valor).replace('PEN', 'S/');
        } catch (e) {
            console.error('Error al formatear valor:', e);
        }
        
        // Obtener responsable
        let responsable = 'N/A';
        if (dev.responsable) {
            responsable = dev.responsable;
        } else if (dev.usuario && dev.usuario.nombre) {
            responsable = dev.usuario.nombre;
        } else if (dev.usuarioNombre) {
            responsable = dev.usuarioNombre;
        }
        
        tr.innerHTML = `
            <td>${dev.id || 'N/A'}</td>
            <td>${fecha}</td>
            <td>${proveedorNombre}</td>
            <td>${loteInfo}</td>
            <td>${productosTexto}</td>
            <td class="text-center">${cantidad}</td>
            <td class="text-center">
                <span class="estado-badge estado-devuelto">DEVUELTO</span>
            </td>
            <td class="text-right">${valorFormateado}</td>
            <td>${responsable}</td>
            <td class="text-center">
                <button class="btn btn-sm btn-info ver-detalle-lote" data-id="${dev.id}">
                    <i class="fas fa-eye"></i> Ver
                </button>
            </td>
        `;
        
        tbody.appendChild(tr);
    });
    
    // Configurar eventos para botones de ver detalle
    document.querySelectorAll('.ver-detalle-lote').forEach(btn => {
        btn.addEventListener('click', (e) => {
            e.preventDefault();
            const id = btn.getAttribute('data-id');
            mostrarDetalleLoteDevolucion(id);
        });
    });
}

// Función para mostrar detalles de una devolución de lote
function mostrarDetalleLoteDevolucion(id) {
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
    fetch(`/api/lotes/devolucion/${id}/detalles`)
        .then(response => {
            if (!response.ok) {
                throw new Error('Error al obtener detalles de la devolución: ' + response.statusText);
            }
            return response.json();
        })
        .then(devolucion => {
            console.log('Detalle de devolución recibido:', devolucion);
            
            // Formato de moneda
            const formatter = new Intl.NumberFormat('es-PE', {
                style: 'currency',
                currency: 'PEN',
                minimumFractionDigits: 2
            });
            
            // Información básica
            const fechaDevolucion = devolucion.fechaDevolucion || devolucion.fecha;
            const fecha = fechaDevolucion ? new Date(fechaDevolucion).toLocaleDateString('es-ES') : 'N/A';
            
            // Obtener información del proveedor
            const proveedor = devolucion.proveedor ? devolucion.proveedor.nombre : 'No especificado';
            
            // Obtener información del lote
            let loteInfo = 'N/A';
            if (devolucion.lote && devolucion.lote.codigo) {
                loteInfo = devolucion.lote.codigo;
            } else if (devolucion.codigo) {
                loteInfo = devolucion.codigo;
            } else if (devolucion.numeroLote) {
                loteInfo = devolucion.numeroLote;
            } else if (devolucion.id) {
                loteInfo = 'LOT-' + devolucion.id;
            }
            
            // Obtener información del motivo y responsable
            const motivo = devolucion.motivoDevolucion || devolucion.motivo || 'No especificado';
            let responsable = 'N/A';
            if (devolucion.responsable) {
                responsable = devolucion.responsable;
            } else if (devolucion.usuario && devolucion.usuario.nombre) {
                responsable = devolucion.usuario.nombre;
            } else if (devolucion.usuarioNombre) {
                responsable = devolucion.usuarioNombre;
            }
            
            // Calcular valor total
            let valorTotal = 0;
            if (devolucion.valor) {
                valorTotal = devolucion.valor;
            } else if (devolucion.total) {
                valorTotal = devolucion.total;
            } else if (devolucion.costoUnitario && devolucion.cantidadInicial) {
                valorTotal = devolucion.costoUnitario * devolucion.cantidadInicial;
            }
            
            // HTML para la información básica
            let html = `
                <div class="row g-3">
                    <div class="col-md-6">
                        <p><strong>ID:</strong> ${devolucion.id}</p>
                        <p><strong>Proveedor:</strong> ${proveedor}</p>
                        <p><strong>Lote:</strong> ${loteInfo}</p>
                        <p><strong>Fecha:</strong> ${fecha}</p>
                    </div>
                    <div class="col-md-6">
                        <p><strong>Estado:</strong> <span class="estado-badge estado-devuelto">DEVUELTO</span></p>
                        <p><strong>Motivo:</strong> ${motivo}</p>
                        <p><strong>Valor:</strong> ${formatter.format(valorTotal).replace('PEN', 'S/')}</p>
                        <p><strong>Responsable:</strong> ${responsable}</p>
                    </div>
                </div>
            `;
            
            // Información de productos
            let productos = [];
            
            // Intentar obtener productos de diferentes fuentes posibles
            if (devolucion.productos && devolucion.productos.length > 0) {
                productos = devolucion.productos;
            } else if (devolucion.variante && devolucion.variante.producto) {
                // Si no hay array de productos pero hay información de variante/producto
                productos = [{
                    nombre: devolucion.variante.producto.nombre,
                    cantidad: devolucion.cantidadInicial || 1,
                    precioUnitario: devolucion.costoUnitario || 0
                }];
            }
            
            // Tabla de productos
            if (productos.length > 0) {
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
                
                productos.forEach(producto => {
                    // Obtener nombre del producto
                    let nombre = 'Producto sin nombre';
                    if (producto.nombre) {
                        nombre = producto.nombre;
                    } else if (producto.producto && producto.producto.nombre) {
                        nombre = producto.producto.nombre;
                    }
                    
                    // Calcular valores
                    const precio = producto.precioUnitario || 0;
                    const cantidad = producto.cantidad || 0;
                    const subtotal = precio * cantidad;
                    
                    html += `
                        <tr>
                            <td>${nombre}</td>
                            <td class="text-center">${cantidad}</td>
                            <td class="text-right">${formatter.format(precio).replace('PEN', 'S/')}</td>
                            <td class="text-right">${formatter.format(subtotal).replace('PEN', 'S/')}</td>
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
            
            // Comentarios adicionales
            if (devolucion.comentarios || devolucion.comentariosDevolucion) {
                const comentarios = devolucion.comentariosDevolucion || devolucion.comentarios || '';
                html += `
                    <div class="mt-3">
                        <h6>Comentarios adicionales</h6>
                        <div class="p-3 bg-light rounded">
                            ${comentarios}
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
    console.log('DOM cargado en devoluciones-lotes.js');
    
    // Inicializar carga de devoluciones de lotes si estamos en la pestaña correspondiente
    const contentLotes = document.getElementById('content-lotes');
    if (contentLotes && contentLotes.classList.contains('active')) {
        cargarDevolucionesLotes();
    }
    
    // Configurar evento de búsqueda
    const searchLotes = document.getElementById('search-lotes');
    if (searchLotes) {
        searchLotes.addEventListener('input', () => {
            console.log('Buscando en lotes:', searchLotes.value);
            cargarDevolucionesLotes(); // Simplificamos por ahora, solo recarga todos los datos
        });
    }
});
 