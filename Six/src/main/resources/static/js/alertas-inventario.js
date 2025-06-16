document.addEventListener('DOMContentLoaded', function() {
    const alertsTableBody = document.getElementById('alertsTableBody');
    const statusFilter = document.getElementById('statusFilter');
    const alertsSearch = document.getElementById('alertsSearch');
    
    // Función para cargar alertas de stock
    function cargarAlertasInventario() {
        if (!alertsTableBody) return;
        
        alertsTableBody.innerHTML = `
            <tr>
                <td colspan="8" class="text-center">
                    <i class="fas fa-spinner fa-spin"></i> Cargando alertas...
                </td>
            </tr>
        `;
        
        // Obtener valores de filtro
        const filtroEstado = statusFilter ? statusFilter.value : 'all';
        const terminoBusqueda = alertsSearch ? alertsSearch.value.toLowerCase() : '';
        
        fetch('/api/alertas-inventario')
            .then(response => response.json())
            .then(data => {
                // Filtrar alertas según el estado seleccionado
                let alertas = data;
                
                if (filtroEstado !== 'all') {
                    alertas = alertas.filter(alerta => {
                        if (filtroEstado === 'critical' && alerta.estado === 'Crítico') return true;
                        if (filtroEstado === 'low' && alerta.estado === 'Bajo') return true;
                        if (filtroEstado === 'resolved' && alerta.estado === 'Resuelto') return true;
                        return false;
                    });
                }
                
                // Filtrar por término de búsqueda si existe
                if (terminoBusqueda) {
                    alertas = alertas.filter(alerta => 
                        (alerta.producto && alerta.producto.toLowerCase().includes(terminoBusqueda)) ||
                        (alerta.codigo && alerta.codigo.toLowerCase().includes(terminoBusqueda)) ||
                        (alerta.color && alerta.color.toLowerCase().includes(terminoBusqueda)) ||
                        (alerta.talla && alerta.talla.toLowerCase().includes(terminoBusqueda))
                    );
                }
                
                mostrarAlertasInventario(alertas);
            })
            .catch(error => {
                console.error('Error al cargar alertas:', error);
                alertsTableBody.innerHTML = `
                    <tr>
                        <td colspan="8" class="text-center">
                            Error al cargar las alertas. Por favor, intente nuevamente.
                        </td>
                    </tr>
                `;
            });
    }
    
    // Función para mostrar alertas de inventario en la tabla
    function mostrarAlertasInventario(alertas) {
        if (!alertsTableBody) return;
        
        if (alertas.length === 0) {
            alertsTableBody.innerHTML = `
                <tr>
                    <td colspan="8" class="text-center">No se encontraron alertas que coincidan con los criterios de búsqueda.</td>
                </tr>
            `;
            return;
        }
        
        let html = '';
        
        alertas.forEach(alerta => {
            let estadoClase = '';
            let estadoIcono = '';
            let estadoTexto = '';
            
            // Determinar estado y estilo según el estado de la alerta
            switch (alerta.estado) {
                case 'Bajo':
                    estadoClase = 'status-low';
                    estadoIcono = '<i class="fas fa-arrow-down"></i>';
                    estadoTexto = 'Stock Bajo';
                    break;
                case 'Crítico':
                    estadoClase = 'status-critical';
                    estadoIcono = '<i class="fas fa-exclamation-triangle"></i>';
                    estadoTexto = 'STOCK CRÍTICO';
                    break;
                case 'Resuelto':
                    estadoClase = 'status-resolved';
                    estadoIcono = '<i class="fas fa-check-circle"></i>';
                    estadoTexto = 'Resuelto';
                    break;
                default:
                    estadoClase = 'status-info';
                    estadoIcono = '<i class="fas fa-info-circle"></i>';
                    estadoTexto = alerta.estado || 'Información';
            }
            
            // Crear mensaje para el producto
            let mensajeProducto;
            if (alerta.estado === 'Crítico') {
                mensajeProducto = `El stock del producto ${alerta.producto} ha alcanzado un nivel crítico. Stock actual: ${alerta.stockActual} unidades (umbral crítico: 3).`;
            } else if (alerta.estado === 'Bajo') {
                mensajeProducto = `El producto ${alerta.producto} tiene un stock bajo de ${alerta.stockActual} unidades (umbral: ${alerta.stockMinimo}).`;
            } else {
                mensajeProducto = `El producto ${alerta.producto} tiene stock adecuado: ${alerta.stockActual} unidades.`;
            }
            
            // Obtener la fecha actual para alertas que no tienen fecha específica
            const ahora = new Date();
            const fechaFormateada = `${ahora.getDate().toString().padStart(2, '0')}/${(ahora.getMonth() + 1).toString().padStart(2, '0')}/${ahora.getFullYear()} ${ahora.getHours().toString().padStart(2, '0')}:${ahora.getMinutes().toString().padStart(2, '0')}`;
            
            html += `
                <tr>
                    <td><span class="status-badge ${estadoClase}">${estadoIcono} ${estadoTexto}</span></td>
                    <td>${alerta.codigo || 'N/A'}</td>
                    <td>${mensajeProducto}</td>
                    <td>${alerta.talla || 'N/A'}</td>
                    <td>${alerta.color || 'N/A'}</td>
                    <td>${alerta.stockActual || 0}</td>
                    <td>${alerta.stockMinimo || 0}</td>
                    <td>${fechaFormateada}</td>
                </tr>
            `;
        });
        
        alertsTableBody.innerHTML = html;
    }
    
    // Inicializar carga de alertas
    cargarAlertasInventario();
    
    // Refrescar alertas periódicamente (cada 30 segundos)
    setInterval(cargarAlertasInventario, 30000);
    
    // Escuchar eventos del filtro de estado
    if (statusFilter) {
        statusFilter.addEventListener('change', cargarAlertasInventario);
    }
    
    // Escuchar eventos de búsqueda
    if (alertsSearch) {
        alertsSearch.addEventListener('input', function() {
            // Usar debounce para evitar múltiples llamadas durante la escritura rápida
            clearTimeout(this.searchTimeout);
            this.searchTimeout = setTimeout(cargarAlertasInventario, 300);
        });
    }
});
