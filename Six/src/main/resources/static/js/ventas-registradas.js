/*
 * Ventas Registradas - JavaScript
 */

document.addEventListener('DOMContentLoaded', function() {
    console.log('DOM cargado - inicializando funcionalidad de ventas registradas');
    
    // Referencias a elementos del DOM
    const ventasTable = document.getElementById('ventasTable');
    const detallesTable = document.getElementById('detallesTable');
    const detallesVentaModal = document.getElementById('detallesVentaModal');
    const fechaDesde = document.getElementById('fechaDesde');
    const fechaHasta = document.getElementById('fechaHasta');
    const aplicarFiltro = document.getElementById('aplicarFiltro');
    const ventasSearch = document.getElementById('ventasSearch');
    
    // Almacenar todas las ventas para la búsqueda
    let todasLasVentas = [];
    
    // Configuración WebSocket
    let socket = null;
    let isConnected = false;
    
    // Datos de ejemplo para mostrar cuando falla la conexión
    const ventasEjemplo = [
        {
            id: 1,
            codigo: "VENTA-001",
            fecha: new Date().toISOString(),
            cliente: { 
                id: 1,
                nombre: "Juan",
                apellido: "Pérez",
                dni: "12345678"
            },
            subtotal: 100.00,
            igv: 18.00,
            total: 118.00,
            estado: "COMPLETADA",
            detalles: [
                { 
                    id: 1, 
                    cantidad: 2,
                    precioUnitario: 50.00,
                    subtotal: 100.00,
                    variante: { 
                        id: 1,
                        nombre: "Variante estándar",
                        color: "Negro",
                        talla: "M",
                        producto: { 
                            id: 1,
                            nombre: "Camiseta",
                            codigo: "CAM-001",
                            imagen: "/img/producto-default.png"
                        } 
                    }
                }
            ]
        },
        {
            id: 2,
            codigo: "VENTA-002",
            fecha: new Date().toISOString(),
            cliente: { 
                id: 2,
                nombre: "María",
                apellido: "González",
                dni: "87654321"
            },
            subtotal: 200.00,
            igv: 36.00,
            total: 236.00,
            estado: "COMPLETADA",
            detalles: [
                { 
                    id: 2,
                    cantidad: 1,
                    precioUnitario: 150.00,
                    subtotal: 150.00,
                    variante: { 
                        id: 2,
                        nombre: "Variante premium",
                        color: "Azul",
                        talla: "L",
                        producto: { 
                            id: 2,
                            nombre: "Pantalón",
                            codigo: "PAN-001",
                            imagen: "/img/producto-default.png"
                        } 
                    }
                },
                { 
                    id: 3,
                    cantidad: 1,
                    precioUnitario: 50.00,
                    subtotal: 50.00,
                    variante: { 
                        id: 3,
                        nombre: "Variante estándar",
                        color: "Blanco",
                        talla: "S",
                        producto: { 
                            id: 3,
                            nombre: "Calcetines",
                            codigo: "CAL-001",
                            imagen: "/img/producto-default.png"
                        } 
                    }
                }
            ]
        }
    ];
    
    // Inicializar modal
    const detallesModal = document.getElementById('detallesVentaModal');
    detallesModal.addEventListener('shown.bs.modal', function() {
        console.log('Modal mostrado correctamente');
    });
    
    detallesModal.addEventListener('hidden.bs.modal', function() {
        console.log('Modal ocultado correctamente');
    });
    
    // Almacenar la venta actualmente mostrada en el modal
    let ventaActual = null;

    // Delegación de eventos para el botón de devolución
    document.addEventListener('click', function(e) {
        if (e.target && e.target.id === 'btnDevolverVenta' || 
            (e.target.parentElement && e.target.parentElement.id === 'btnDevolverVenta')) {
            
            if (ventaActual && ventaActual.id) {
                confirmarDevolucionVenta(ventaActual.id);
            } else {
                mostrarToast('Error', 'No se pudo identificar la venta a devolver', 'error');
            }
        }
    });
    
    // Delegación de eventos para los botones de acciones
    document.addEventListener('click', function(e) {
        // Verificar si el clic fue en un botón de acciones o en su icono
        if (e.target && (e.target.classList.contains('btn-acciones') || 
            (e.target.parentElement && e.target.parentElement.classList.contains('btn-acciones')))) {
            
            // Obtener el botón (puede ser el target o su padre)
            const boton = e.target.classList.contains('btn-acciones') ? e.target : e.target.parentElement;
            const ventaId = boton.getAttribute('data-venta-id');
            
            if (ventaId) {
                console.log('Abriendo modal para venta ID:', ventaId);
                mostrarDetallesVenta(parseInt(ventaId));
            }
        }
    });
    
    // Cargar ventas al iniciar
    cargarVentas();
    
    // Iniciar conexión WebSocket
    conectarWebSocket();
    
    // Event listeners
    // Filtrar ventas por fecha
    aplicarFiltro.addEventListener('click', () => {
        console.log('Aplicando filtro de fechas');
        console.log('Fecha desde:', fechaDesde.value);
        console.log('Fecha hasta:', fechaHasta.value);
        cargarVentas(fechaDesde.value, fechaHasta.value);
    });
    
    // Búsqueda en tiempo real
    ventasSearch.addEventListener('input', () => {
        const searchTerm = ventasSearch.value.toLowerCase().trim();
        
        if (!searchTerm) {
            // Si el campo de búsqueda está vacío, mostrar todas las ventas
            mostrarVentas(todasLasVentas);
            return;
        }
        
        // Filtrar ventas por término de búsqueda
            const ventasFiltradas = todasLasVentas.filter(venta => {
                // Buscar en código de venta
                if (venta.codigo && venta.codigo.toLowerCase().includes(searchTerm)) {
                    return true;
                }
                
                // Buscar en nombre de cliente
                if (venta.cliente && venta.cliente.nombre && 
                    venta.cliente.nombre.toLowerCase().includes(searchTerm)) {
                    return true;
                }
                
            // Buscar en apellido de cliente
            if (venta.cliente && venta.cliente.apellido && 
                venta.cliente.apellido.toLowerCase().includes(searchTerm)) {
                return true;
            }
            
            // Buscar en DNI de cliente
            if (venta.cliente && venta.cliente.dni && 
                venta.cliente.dni.toLowerCase().includes(searchTerm)) {
                return true;
            }
            
            // Buscar en detalles de venta (productos)
            if (venta.detalles && venta.detalles.length > 0) {
                return venta.detalles.some(detalle => {
                    // Buscar en nombre de producto
                    if (detalle.variante && detalle.variante.producto && 
                        detalle.variante.producto.nombre && 
                        detalle.variante.producto.nombre.toLowerCase().includes(searchTerm)) {
                        return true;
                    }
                    
                    // Buscar en SKU
                    if (detalle.variante && detalle.variante.sku && 
                        detalle.variante.sku.toLowerCase().includes(searchTerm)) {
                        return true;
                    }
                    
                    return false;
                });
            }
            
                return false;
        });
        
        // Mostrar resultados filtrados
        mostrarVentas(ventasFiltradas);
    });
    
    // Función para conectar WebSocket
    function conectarWebSocket() {
        // Determinar el protocolo (ws o wss) basado en el protocolo HTTP actual
        const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
        const host = window.location.host;
        const wsUrl = `${protocol}//${host}/ws/ventas`;
        
        try {
        socket = new WebSocket(wsUrl);
        
        socket.onopen = function() {
            console.log('Conexión WebSocket establecida');
            isConnected = true;
            console.log('Conexión en tiempo real establecida');
        };
        
        socket.onmessage = function(event) {
            console.log('Mensaje recibido del servidor:', event.data);
            try {
                const data = JSON.parse(event.data);
                
                    if (data.tipo === 'NUEVA_VENTA') {
                        // Recargar ventas cuando se registra una nueva
                        cargarVentas(fechaDesde.value, fechaHasta.value);
                }
            } catch (error) {
                console.error('Error al procesar mensaje WebSocket:', error);
            }
        };
        
        socket.onclose = function() {
            console.log('Conexión WebSocket cerrada');
            isConnected = false;
            
            // Intentar reconectar después de 5 segundos
            setTimeout(conectarWebSocket, 5000);
        };
        
        socket.onerror = function(error) {
            console.error('Error en la conexión WebSocket:', error);
            isConnected = false;
        };
        } catch (error) {
            console.error('Error al conectar WebSocket:', error);
            isConnected = false;
        }
    }
    
    // Función para cargar ventas
    function cargarVentas(desde = null, hasta = null) {
        // Mostrar mensaje de carga
        ventasTable.querySelector('tbody').innerHTML = `
            <tr class="loading-row">
                <td colspan="9" class="text-center">
                    <i class="fas fa-spinner fa-spin"></i> Cargando ventas...
                </td>
            </tr>
        `;
        
        // URL para la petición
        let url = '/api/ventas';
        let params = [];
        
        if (desde) {
            params.push(`desde=${desde}`);
        }
        
        if (hasta) {
            params.push(`hasta=${hasta}`);
        }
        
        if (params.length > 0) {
            url += '?' + params.join('&');
        }
        
        // Obtener datos con un timeout para evitar bloqueos de UI
        setTimeout(() => {
        fetch(url, {
            method: 'GET',
            headers: {
                'Accept': 'application/json',
                'Content-Type': 'application/json'
            },
            credentials: 'same-origin'
        })
        .then(response => {
            if (!response.ok) {
                throw new Error(`Error al cargar ventas: ${response.status} ${response.statusText}`);
            }
            return response.json();
        })
        .then(ventas => {
            // Si ventas es un objeto con una propiedad específica que contiene el array
            if (ventas && typeof ventas === 'object' && !Array.isArray(ventas)) {
                // Verificar si hay alguna propiedad que pueda contener el array de ventas
                const posiblesPropiedades = ['ventas', 'data', 'content', 'results', 'items'];
                for (const prop of posiblesPropiedades) {
                    if (ventas[prop] && Array.isArray(ventas[prop])) {
                        ventas = ventas[prop];
                        break;
                    }
                }
            }
            
            // Verificar si la respuesta es un array vacío o null
            if (!ventas || (Array.isArray(ventas) && ventas.length === 0)) {
                ventasTable.querySelector('tbody').innerHTML = `
                    <tr>
                        <td colspan="9" class="text-center">No hay ventas registradas en el período seleccionado</td>
                    </tr>
                `;
                return;
            }
            
            // Asegurarse de que ventas sea un array
            if (!Array.isArray(ventas)) {
                try {
                    // Intentar convertir a array si es posible
                    ventas = [ventas];
                } catch (e) {
                    ventasTable.querySelector('tbody').innerHTML = `
                        <tr>
                            <td colspan="9" class="text-center">Error al procesar los datos de ventas</td>
                        </tr>
                    `;
                    return;
                }
            }
            
                // Pre-procesar los datos de ventas para mejorar rendimiento
                ventas = ventas.map(venta => {
                    // Asegurarse de que detalles sea un array
                    if (!venta.detalles) {
                        venta.detalles = [];
                    }
                    
                    // Pre-procesar detalles para tener información de productos lista
                    venta.detalles = venta.detalles.map(detalle => {
                        // Asegurarse de que variante exista
                        if (!detalle.variante) {
                            detalle.variante = {};
                        }
                        
                        // Asegurarse de que producto exista
                        if (!detalle.variante.producto) {
                            detalle.variante.producto = {};
                        }
                        
                        // Preparar información del producto para mostrar
                        let sku = 'SKU no disponible';
                        
                        // Intentar obtener el SKU de diferentes maneras
                        if (detalle.variante && detalle.variante.sku) {
                            sku = detalle.variante.sku;
                        } else if (detalle.variante && detalle.variante.id) {
                            // Si no hay SKU pero hay ID de variante, generar uno basado en la info disponible
                            const productoInfo = detalle.variante.producto || {};
                            const codigo = productoInfo.codigo || '';
                            const color = detalle.variante.color || '';
                            const talla = detalle.variante.talla || '';
                            
                            if (codigo) {
                                if (color && talla) {
                                    sku = `${codigo}-${talla}-${color}`;
                                } else if (color) {
                                    sku = `${codigo}-${color}`;
                                } else if (talla) {
                                    sku = `${codigo}-${talla}`;
                                } else {
                                    sku = `${codigo}-VAR${detalle.variante.id}`;
                                }
                            } else {
                                sku = `VAR-${detalle.variante.id}`;
                            }
                        }
                        
                        // Obtener información del producto
                        let nombreProducto = 'Producto sin nombre';
                        let color = 'N/A';
                        let talla = 'N/A';
                        
                        if (detalle.variante.producto && detalle.variante.producto.nombre) {
                            nombreProducto = detalle.variante.producto.nombre;
                            color = detalle.variante.color || 'N/A';
                            talla = detalle.variante.talla || 'N/A';
                        } else if (sku !== 'SKU no disponible') {
                            const infoSKU = obtenerInfoDesdeSkU(sku);
                            nombreProducto = infoSKU.nombreProducto || 'Producto sin nombre';
                            color = detalle.variante.color || infoSKU.color || 'N/A';
                            talla = detalle.variante.talla || infoSKU.talla || 'N/A';
                        }
                        
                        // Añadir información procesada al detalle
                        detalle._procesado = {
                            nombreProducto,
                            color,
                            talla,
                            sku,
                            mostrarComoTexto: nombreProducto !== 'Producto sin nombre' && (color === 'N/A' || talla === 'N/A')
                        };
                        
                        return detalle;
                    });
                    
                    return venta;
                });
                
                // Guardar todas las ventas para búsqueda
            todasLasVentas = ventas;
            
                // Mostrar las ventas
            mostrarVentas(ventas);
        })
        .catch(error => {
                // Mostrar mensaje de error
            ventasTable.querySelector('tbody').innerHTML = `
                <tr>
                        <td colspan="9" class="text-center">
                            Error al cargar ventas: ${error.message}
                        </td>
                </tr>
            `;
                
                // Si no se pudieron cargar datos reales, mostrar datos de ejemplo
                if (confirm("No se pudieron cargar los datos reales. ¿Desea ver datos de ejemplo?")) {
                    todasLasVentas = ventasEjemplo;
                    mostrarVentas(ventasEjemplo);
                }
            });
        }, 10); // Pequeño retraso para no bloquear la UI
    }
    
    // Función para mostrar ventas en la tabla
    function mostrarVentas(ventas) {
        const tbody = ventasTable.querySelector('tbody');
        
        // Si no hay ventas, mostrar mensaje
        if (!ventas || ventas.length === 0) {
            tbody.innerHTML = `
                <tr>
                    <td colspan="9" class="text-center">No hay ventas registradas</td>
                </tr>
            `;
            return;
        }
        
        // Generar filas de la tabla
        let html = '';
        
        ventas.forEach(venta => {
            // Preparar datos para mostrar
            const fecha = new Date(venta.fecha).toLocaleDateString();
            
            // Preparar nombre del cliente
            let nombreCliente = 'Cliente General';
            if (venta.cliente) {
                if (venta.cliente.nombre && venta.cliente.apellido) {
                    nombreCliente = `${venta.cliente.nombre} ${venta.cliente.apellido}`;
                } else if (venta.cliente.nombre) {
                    nombreCliente = venta.cliente.nombre;
                }
            }
            
            // Preparar estado
            const estadoInfo = actualizarEstadoVisual(venta);
            
            // Contar productos
            const detalles = venta.detalles || [];
            let totalCantidadProductos = 0;
            
            detalles.forEach(detalle => {
                    if (detalle.cantidad) {
                        totalCantidadProductos += detalle.cantidad;
                    }
                });
            
            // Texto para mostrar cantidad de productos
            const textoProductos = totalCantidadProductos === 1 ? 
                `${totalCantidadProductos} producto` : 
                `${totalCantidadProductos} productos`;
                
            html += `
                <tr data-venta-id="${venta.id}">
                    <td>${venta.codigo}</td>
                    <td>${nombreCliente}</td>
                    <td>${textoProductos}</td>
                    <td>S/. ${parseFloat(venta.subtotal).toFixed(2)}</td>
                    <td>S/. ${parseFloat(venta.igv).toFixed(2)}</td>
                    <td>S/. ${parseFloat(venta.total).toFixed(2)}</td>
                    <td>${fecha}</td>
                    <td><span class="estado-venta ${estadoInfo.clase}">${estadoInfo.texto}</span></td>
                    <td>
                        <div class="acciones">
                            <button class="btn-acciones" data-venta-id="${venta.id}">
                                <i class="fas fa-ellipsis-v"></i>
                            </button>
                        </div>
                    </td>
                </tr>
            `;
        });
        
        tbody.innerHTML = html;
    }
    
    // Función para mostrar detalles de venta en el modal
    function mostrarDetallesVenta(id) {
        // Mostrar indicador de carga en el modal
        const tbodyDetalles = detallesTable.querySelector('tbody');
        tbodyDetalles.innerHTML = `
            <tr>
                <td colspan="3" class="text-center">
                    <i class="fas fa-spinner fa-spin"></i> Cargando detalles...
                </td>
            </tr>
        `;
        
        // Mostrar el modal inmediatamente para mejorar la percepción de velocidad
        document.getElementById('detallesVentaModal').style.display = 'block';
        
        // Buscar la venta en nuestros datos locales
        const venta = todasLasVentas.find(v => v.id === parseInt(id));
            
            if (!venta) {
            tbodyDetalles.innerHTML = `
                <tr>
                    <td colspan="3" class="text-center">Error: No se encontró la venta</td>
                </tr>
            `;
                return;
            }
        
        // Almacenar la venta actual para la devolución
        ventaActual = venta;
        // Exponer la venta actual globalmente para que otros scripts puedan acceder
        window.ventaActual = venta;
        
        // Actualizar el estado del botón de devolución según el estado de la venta
        const btnDevolver = document.getElementById('btnDevolverVenta');
        if (venta.estado === 'DEVUELTA') {
            btnDevolver.disabled = true;
            btnDevolver.style.opacity = '0.5';
            btnDevolver.title = 'Esta venta ya ha sido devuelta';
        } else {
            btnDevolver.disabled = false;
            btnDevolver.style.opacity = '1';
            btnDevolver.title = 'Devolver toda la venta';
        }
        
        // Actualizar información general de la venta
        document.getElementById('ventaCodigo').textContent = venta.codigo || 'N/A';
        document.getElementById('ventaFecha').textContent = new Date(venta.fecha).toLocaleString();
        
        // Preparar cliente
        let clienteNombre = 'Cliente General';
        let clienteDNI = 'Sin DNI';
        
        if (venta.cliente) {
            clienteNombre = venta.cliente.nombre || 'Sin nombre';
            clienteDNI = venta.cliente.dni || 'Sin DNI';
        }
        
        document.getElementById('ventaCliente').textContent = clienteNombre;
        document.getElementById('ventaDNI').textContent = clienteDNI;
        
        // Actualizar totales
        document.getElementById('ventaSubtotal').textContent = `S/. ${parseFloat(venta.subtotal).toFixed(2)}`;
        document.getElementById('ventaIGV').textContent = `S/. ${parseFloat(venta.igv).toFixed(2)}`;
        document.getElementById('ventaTotal').textContent = `S/. ${parseFloat(venta.total).toFixed(2)}`;
        
        // Mostrar estado de la venta
        const estadoVenta = document.getElementById('ventaEstado');
        const estadoInfo = actualizarEstadoVisual(venta);
        
        estadoVenta.textContent = estadoInfo.texto;
        estadoVenta.className = 'estado-venta ' + estadoInfo.clase;
        
        // Verificar si hay detalles
        const detalles = venta.detalles || [];
        
        if (!detalles.length) {
            tbodyDetalles.innerHTML = `
                <tr>
                    <td colspan="3" class="text-center">No hay productos en esta venta</td>
                </tr>
            `;
            return;
        }
        
        // Generar filas para los detalles inmediatamente (ya están pre-procesados)
            let html = '';
            
            detalles.forEach(detalle => {
                // Solo mostrar detalles con cantidad > 0
                if (detalle.cantidad <= 0) {
                    return;
                }
                
                // Usar la información pre-procesada
                const info = detalle._procesado || {
                    nombreProducto: 'Producto sin nombre',
                    color: 'N/A',
                    talla: 'N/A',
                    sku: 'SKU no disponible',
                    mostrarComoTexto: false
                };
                
                // Preparar la clase para el SKU según si está disponible o fue generado
                const skuClass = info.sku.startsWith('VAR-') || info.sku === 'SKU no disponible' 
                    ? 'text-secondary' 
                    : 'text-primary';
                
                if (info.mostrarComoTexto) {
                    html += `
                        <tr>
                            <td>
                                <strong>${info.nombreProducto}</strong><br>
                                <small class="${skuClass}" style="font-size: 85%;">${info.sku}</small>
                            </td>
                            <td class="text-center">${detalle.cantidad}</td>
                            <td class="text-right">S/. ${parseFloat(detalle.subtotal).toFixed(2)}</td>
                        </tr>
                    `;
                } else {
                html += `
                    <tr>
                            <td>
                                <strong>${info.nombreProducto}</strong> 
                                <span class="text-muted">(${info.talla}, ${info.color})</span><br>
                                <small class="${skuClass}" style="font-size: 85%;">${info.sku}</small>
                        </td>
                            <td class="text-center">${detalle.cantidad}</td>
                            <td class="text-right">S/. ${parseFloat(detalle.subtotal).toFixed(2)}</td>
                    </tr>
                `;
                }
            });
            
            tbodyDetalles.innerHTML = html;
        }
        
    // Función mejorada para extraer información del producto desde el SKU
    function obtenerInfoDesdeSkU(sku) {
        if (!sku || typeof sku !== 'string') {
            return {
                tipo: 'N/A',
                codigo: 'N/A',
                talla: 'N/A',
                color: 'N/A',
                nombreProducto: 'Producto desconocido'
            };
        }
        
        // Limpiar el SKU de posibles espacios o caracteres extraños
        sku = sku.trim();
        
        // Verificar si es un SKU generado para variantes sin SKU original
        if (sku.startsWith('VAR-')) {
            return {
                tipo: 'VAR',
                codigo: sku.substring(4),
                talla: 'N/A',
                color: 'N/A',
                nombreProducto: 'Variante de Producto'
            };
        }
        
        // Separar el SKU en sus partes
        const partes = sku.split('-');
        
        // Estructura básica del objeto de resultado
        const resultado = {
            tipo: partes[0] || 'N/A',
            codigo: partes[1] || 'N/A',
            talla: 'N/A',
            color: 'N/A',
            nombreProducto: 'Producto desconocido'
        };
        
        // Intentar extraer información adicional
        if (partes.length >= 3) {
            // Si hay al menos 3 partes, la tercera puede ser talla o color
            // Intentamos determinar cuál es basándonos en patrones comunes
            const posibleTalla = partes[2];
            
            // Verificamos si la tercera parte parece una talla
            // Las tallas suelen ser: S, M, L, XL, XXL, números como 32, 34, etc.
            if (posibleTalla.match(/^(XS|S|M|L|XL|XXL|XXXL|\d+)$/i)) {
                resultado.talla = posibleTalla;
                // Si hay una cuarta parte, probablemente sea el color
                if (partes.length >= 4) {
                    resultado.color = partes[3];
                }
        } else {
                // Si no parece una talla estándar, asumimos que es un color
                resultado.color = posibleTalla;
                // Si hay una cuarta parte, podría ser la talla en un orden diferente
                if (partes.length >= 4) {
                    resultado.talla = partes[3];
                }
            }
        }
        
        // Intentar determinar el nombre del producto basado en el tipo
        switch (resultado.tipo.toUpperCase()) {
            case 'CAM':
                resultado.nombreProducto = 'Camiseta';
                break;
            case 'PAN':
                resultado.nombreProducto = 'Pantalón';
                break;
            case 'ZAP':
                resultado.nombreProducto = 'Zapatos';
                break;
            case 'VES':
                resultado.nombreProducto = 'Vestido';
                break;
            case 'CHA':
                resultado.nombreProducto = 'Chaqueta';
                break;
            case 'ACC':
                resultado.nombreProducto = 'Accesorio';
                break;
            case 'POL':
                resultado.nombreProducto = 'Polo';
                break;
            case 'BLU':
                resultado.nombreProducto = 'Blusa';
                break;
            case 'FAL':
                resultado.nombreProducto = 'Falda';
                break;
            case 'SUD':
                resultado.nombreProducto = 'Sudadera';
                break;
            case 'ABR':
                resultado.nombreProducto = 'Abrigo';
                break;
            case 'CAL':
                resultado.nombreProducto = 'Calcetines';
                break;
            case 'GOR':
                resultado.nombreProducto = 'Gorra';
                break;
            case 'BUF':
                resultado.nombreProducto = 'Bufanda';
                break;
            case 'BOL':
                resultado.nombreProducto = 'Bolso';
                break;
            case 'CIN':
                resultado.nombreProducto = 'Cinturón';
                break;
            case 'JEA':
                resultado.nombreProducto = 'Jeans';
                break;
            default:
                // Si no se encuentra un tipo conocido, usar el tipo como nombre genérico
                resultado.nombreProducto = resultado.tipo + ' ' + resultado.codigo;
        }
        
        return resultado;
    }
    
    // Función para mostrar notificaciones toast
    function mostrarToast(titulo, mensaje, tipo = 'info') {
        // Crear elemento toast si no existe
        let toastContainer = document.getElementById('toastContainer');
        if (!toastContainer) {
            toastContainer = document.createElement('div');
            toastContainer.id = 'toastContainer';
            toastContainer.className = 'toast-container';
            document.body.appendChild(toastContainer);
        }
        
        // Crear toast
        const toastElement = document.createElement('div');
        toastElement.className = `toast toast-${tipo}`;
        toastElement.innerHTML = `
            <div class="toast-header">
                <strong>${titulo}</strong>
                <button type="button" class="close-toast">&times;</button>
            </div>
            <div class="toast-body">
                ${mensaje}
            </div>
        `;
        
        toastContainer.appendChild(toastElement);
        
        // Mostrar toast
        setTimeout(() => {
            toastElement.classList.add('show');
        }, 100);
        
        // Cerrar toast al hacer clic en el botón de cerrar
        const closeButton = toastElement.querySelector('.close-toast');
        if (closeButton) {
            closeButton.addEventListener('click', () => {
                toastElement.classList.remove('show');
                setTimeout(() => {
                    toastElement.remove();
                }, 300);
            });
        }
        
        // Auto-cerrar después de 5 segundos
        setTimeout(() => {
            toastElement.classList.remove('show');
            setTimeout(() => {
                toastElement.remove();
            }, 300);
        }, 5000);
    }
    
    // Exponer funciones necesarias globalmente
    window.mostrarDetallesVenta = mostrarDetallesVenta;
    
    function actualizarEstadoVisual(venta) {
        let estadoTexto = '';
        let estadoClase = '';
        
        // Determinar clase CSS según el estado
        if (venta.estado === 'COMPLETADA') {
            estadoTexto = 'Completada';
            estadoClase = 'completada';
        } else if (venta.estado === 'PENDIENTE') {
            estadoTexto = 'Pendiente';
            estadoClase = 'pendiente';
        } else if (venta.estado === 'ANULADA') {
            estadoTexto = 'Anulada';
            estadoClase = 'anulada';
        } else {
            estadoTexto = venta.estado || 'Desconocido';
            estadoClase = 'desconocido';
        }
        
        return { texto: estadoTexto, clase: estadoClase };
    }
    
    // Función para confirmar la devolución de una venta
    function confirmarDevolucionVenta(ventaId) {
        if (!confirm('¿Está seguro que desea realizar la devolución completa de esta venta? Esta acción no se puede deshacer.')) {
            return;
        }
        
        // Mostrar indicador de carga
        const btnDevolver = document.getElementById('btnDevolverVenta');
        const textoOriginal = btnDevolver.innerHTML;
        btnDevolver.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Procesando...';
        btnDevolver.disabled = true;
        
        // Realizar la petición al servidor
        fetch(`/api/ventas/devolucion/${ventaId}`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            credentials: 'same-origin' // Esto asegura que la cookie de sesión se envíe
        })
        .then(response => {
            if (!response.ok) {
                return response.text().then(text => {
                    throw new Error(text || 'Error al procesar la devolución');
                });
            }
            return response.json();
        })
        .then(data => {
            // Cerrar modal
            document.getElementById('detallesVentaModal').style.display = 'none';
            
            // Mostrar mensaje de éxito
            mostrarToast('Devolución completada', 'La venta ha sido devuelta correctamente', 'success');
            
            // Recargar datos de ventas
            cargarVentas(fechaDesde.value, fechaHasta.value);
        })
        .catch(error => {
            console.error('Error al procesar devolución:', error);
            mostrarToast('Error', 'No se pudo procesar la devolución: ' + error.message, 'error');
        })
        .finally(() => {
            // Restaurar botón
            btnDevolver.innerHTML = textoOriginal;
            btnDevolver.disabled = false;
        });
    }
}); 