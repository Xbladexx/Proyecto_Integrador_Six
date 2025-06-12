// Función para mostrar información de depuración
function mostrarInfoDepuracion() {
    console.log('=== Información de depuración de reportes.js ===');
    console.log('Chart.js cargado:', typeof Chart !== 'undefined');
    console.log('jQuery cargado:', typeof jQuery !== 'undefined');
    
    // Verificar elementos clave
    const elementosClaves = [
        'salesChart', 'categorySalesChart', 'abcDistributionChart', 'abcValueChart',
        'abcProductsTable', 'abcCountA', 'abcValueA', 'abcTotalProducts'
    ];
    
    elementosClaves.forEach(id => {
        const elemento = document.getElementById(id);
        console.log(`Elemento ${id} encontrado:`, !!elemento);
    });
    
    // Verificar pestañas
    const tabButtons = document.querySelectorAll('.tab-button');
    console.log('Botones de pestañas:', tabButtons.length);
    tabButtons.forEach(btn => {
        console.log(`- Pestaña: ${btn.textContent.trim()}, data-tab: ${btn.dataset.tab}, activa: ${btn.classList.contains('active')}`);
    });
    
    // Verificar contenidos de pestañas
    const tabContents = document.querySelectorAll('.tab-content');
    console.log('Contenidos de pestañas:', tabContents.length);
    tabContents.forEach(content => {
        console.log(`- Contenido: ${content.id}, activo: ${content.classList.contains('active')}`);
    });
    
    // Verificar funciones globales
    console.log('Función updateCharts disponible:', typeof window.updateCharts === 'function');
    console.log('Función switchTab disponible:', typeof window.switchTab === 'function');
    
    console.log('======================================');
}

document.addEventListener('DOMContentLoaded', function() {
    console.log('DOM cargado - Iniciando reportes.js');
    
    // Mostrar información de depuración
    mostrarInfoDepuracion();
    
    // Referencias a elementos del DOM
    const sidebar = document.querySelector('.sidebar');
    const mobileMenuButton = document.querySelector('.mobile-menu-button');
    const userInitial = document.getElementById('userInitial');

    // Elementos de la interfaz de reportes
    const tabButtons = document.querySelectorAll('.tab-button');
    const tabContents = document.querySelectorAll('.tab-content');
    const startDate = document.getElementById('startDate');
    const endDate = document.getElementById('endDate');
    const applyDateRange = document.getElementById('applyDateRange');
    const exportPdfButton = document.getElementById('exportPdfButton');
    const exportExcelButton = document.getElementById('exportExcelButton');
    const printReportButton = document.getElementById('printReportButton');

    // Gráficos
    let salesChart = null;
    let categorySalesChart = null;
    let inventoryValueChart = null;
    let inventoryTurnoverChart = null;
    let categoryPerformanceChart = null;
    let productMarginChart = null;
    let abcDistributionChart = null;
    let abcValueChart = null;

    // Variables para el caché de datos ABC
    let abcDataCache = null;
    let abcDataCacheTimestamp = null;
    let abcDataCacheExpiry = 5 * 60 * 1000; // Reducido a 5 minutos
    let abcDataLoadingAttempts = 0;
    const MAX_LOADING_ATTEMPTS = 5;
    let abcDataLoadingInProgress = false;
    const RETRY_DELAY = 1000; // 1 segundo entre reintentos

    // Inicializar fechas
    initializeDates();
    
    // Mostrar inicial del usuario
    if (userInitial) {
        const usuario = userInitial.getAttribute('data-usuario') || 'A';
        userInitial.textContent = usuario.charAt(0).toUpperCase();
    }

    // Función para mostrar toast
    function showToast(title, message, type = 'info') {
        console.log(`Toast: ${title} - ${message} (${type})`);
        
        // Verificar si existe el contenedor de toast, si no, crearlo
        let toastContainer = document.querySelector('.toast-container');
        if (!toastContainer) {
            toastContainer = document.createElement('div');
            toastContainer.className = 'toast-container';
            document.body.appendChild(toastContainer);
        }
        
        // Crear el toast
        const toast = document.createElement('div');
        toast.className = `toast toast-${type} show`;
        toast.innerHTML = `
            <div class="toast-header">
                <strong>${title}</strong>
                <button type="button" class="close-toast" onclick="this.parentElement.parentElement.remove()">
                    <i class="fas fa-times"></i>
                </button>
            </div>
            <div class="toast-body">
                ${message}
            </div>
        `;

        // Añadir el toast al contenedor
        toastContainer.appendChild(toast);

        // Eliminar el toast después de un tiempo
        setTimeout(() => {
            toast.classList.remove('show');
            setTimeout(() => {
                toast.remove();
            }, 300);
        }, type === 'error' ? 8000 : 5000);
    }

    // Cerrar toast al hacer clic en el botón de cerrar
    const toastCloseButton = document.querySelector('.toast-close');
    if (toastCloseButton) {
        toastCloseButton.addEventListener('click', function() {
            document.getElementById('toast').classList.add('hidden');
        });
    }

    // Manejar clic en el botón de menú móvil
    if (mobileMenuButton) {
        mobileMenuButton.addEventListener('click', function() {
            sidebar.classList.toggle('open');
        });
    }

    // Cerrar el sidebar al hacer clic fuera de él en dispositivos móviles
    document.addEventListener('click', function(event) {
        const isMobile = window.innerWidth <= 768;
        const isClickInsideSidebar = sidebar.contains(event.target);
        const isClickOnMenuButton = mobileMenuButton.contains(event.target);

        if (isMobile && sidebar.classList.contains('open') && !isClickInsideSidebar && !isClickOnMenuButton) {
            sidebar.classList.remove('open');
        }
    });

    // Inicializar fechas
    function initializeDates() {
        console.log('Inicializando fechas');
        if (!startDate || !endDate) {
            console.error('Elementos de fecha no encontrados');
            return;
        }
        
        const today = new Date();
        const firstDayOfMonth = new Date(today.getFullYear(), today.getMonth(), 1);

        // Formatear fechas para input date (YYYY-MM-DD)
        const formatDate = (date) => {
            const year = date.getFullYear();
            const month = (date.getMonth() + 1).toString().padStart(2, '0');
            const day = date.getDate().toString().padStart(2, '0');
            return `${year}-${month}-${day}`;
        };

        startDate.value = formatDate(firstDayOfMonth);
        endDate.value = formatDate(today);
        
        console.log(`Fechas inicializadas: desde ${startDate.value} hasta ${endDate.value}`);
    }

    // Aplicar rango de fechas
    if (applyDateRange) {
        applyDateRange.addEventListener('click', function() {
            if (!startDate.value || !endDate.value) {
                showToast('Error', 'Por favor seleccione un rango de fechas válido', 'error');
                return;
            }

            const start = new Date(startDate.value);
            const end = new Date(endDate.value);

            if (start > end) {
                showToast('Error', 'La fecha de inicio debe ser anterior a la fecha de fin', 'error');
                return;
            }

            // Actualizar todos los gráficos con el nuevo rango de fechas
            updateAllCharts();

            showToast('Rango aplicado', 'Los reportes han sido actualizados con el nuevo rango de fechas');
        });
    }

    // Exportar a PDF
    exportPdfButton.addEventListener('click', function() {
        showToast('Exportando', 'Generando archivo PDF...');
        
        // Obtener la pestaña activa
        const activeTab = document.querySelector('.tab-button.active').dataset.tab;
        
        // Convertir el nombre de la pestaña al valor correcto para el backend
        let seccion;
        switch(activeTab) {
            case 'sales': seccion = 'ventas'; break;
            case 'inventory': seccion = 'inventario'; break;
            case 'products': seccion = 'productos'; break;
            default: seccion = 'ventas';
        }
        
        // Construir parámetros de fecha
        let params = '';
        if (startDate.value && endDate.value) {
            const startISO = new Date(startDate.value + 'T00:00:00').toISOString();
            const endISO = new Date(endDate.value + 'T23:59:59').toISOString();
            params = `?desde=${encodeURIComponent(startISO)}&hasta=${encodeURIComponent(endISO)}&seccion=${seccion}`;
        } else {
            params = `?seccion=${seccion}`;
        }
        
        // Redirigir a la URL de exportación para descargar el PDF
        window.location.href = `/api/reportes/exportar-pdf${params}`;
    });

    // Exportar a Excel
    exportExcelButton.addEventListener('click', function() {
        showToast('Exportando', 'Generando archivo Excel...');
        
        // Obtener la pestaña activa
        const activeTab = document.querySelector('.tab-button.active').dataset.tab;
        
        // Convertir el nombre de la pestaña al valor correcto para el backend
        let seccion;
        switch(activeTab) {
            case 'sales': seccion = 'ventas'; break;
            case 'inventory': seccion = 'inventario'; break;
            case 'products': seccion = 'productos'; break;
            default: seccion = 'ventas';
        }
        
        // Construir parámetros de fecha
        let params = '';
        if (startDate.value && endDate.value) {
            const startISO = new Date(startDate.value + 'T00:00:00').toISOString();
            const endISO = new Date(endDate.value + 'T23:59:59').toISOString();
            params = `?desde=${encodeURIComponent(startISO)}&hasta=${encodeURIComponent(endISO)}&seccion=${seccion}`;
        } else {
            params = `?seccion=${seccion}`;
        }
        
        // Redirigir a la URL de exportación para descargar el Excel
        window.location.href = `/api/reportes/exportar-excel${params}`;
    });

    // Imprimir reporte
    printReportButton.addEventListener('click', function() {
        showToast('Imprimiendo', 'Preparando documento para imprimir...');
        
        // Preparar el contenido para impresión
        const activeTab = document.querySelector('.tab-content.active');
        const tabName = document.querySelector('.tab-button.active').textContent.trim();
        
        // Crear un elemento para el título de la impresión
        const printTitle = document.createElement('div');
        printTitle.className = 'print-title';
        printTitle.innerHTML = `
            <h1>SIX - Reporte de ${tabName}</h1>
            <p class="print-date">Generado: ${new Date().toLocaleString()}</p>
            <p class="print-period">Período: ${startDate.value ? new Date(startDate.value).toLocaleDateString() : 'No especificado'} - 
                                  ${endDate.value ? new Date(endDate.value).toLocaleDateString() : 'Actual'}</p>
        `;
        
        // Guardar el contenido original para restaurarlo después
        const originalContent = activeTab.innerHTML;
        
        // Insertar el título al principio del contenido
        activeTab.prepend(printTitle);
        
        // Esperar a que se apliquen los estilos y luego imprimir
        setTimeout(() => {
            window.print();
            
            // Restaurar después de imprimir
            setTimeout(() => {
                printTitle.remove();
            }, 500);
        }, 500);
    });

    // Función para verificar si Chart.js está cargado
    function isChartJsLoaded() {
        const loaded = typeof Chart !== 'undefined';
        console.log(`Chart.js cargado: ${loaded}`);
        return loaded;
    }

    // Función para verificar si un canvas existe
    function canvasExists(canvasId) {
        const canvas = document.getElementById(canvasId);
        if (!canvas) {
            console.warn(`Canvas con ID '${canvasId}' no encontrado`);
            return false;
        }
        return true;
    }

    // Función para inicializar un gráfico de manera segura
    function initializeChartSafely(canvasId, config) {
        try {
            if (!canvasExists(canvasId)) {
            return null;
        }

            // Destruir el gráfico si ya existe
            if (window.chartInstances && window.chartInstances[canvasId]) {
                window.chartInstances[canvasId].destroy();
        }

            // Inicializar el gráfico
            const ctx = document.getElementById(canvasId).getContext('2d');
            const chart = new Chart(ctx, config);
            
            // Almacenar la instancia del gráfico para referencia futura
            if (!window.chartInstances) {
                window.chartInstances = {};
            }
            window.chartInstances[canvasId] = chart;
            
            return chart;
        } catch (error) {
            console.error(`Error al inicializar el gráfico ${canvasId}:`, error);
            return null;
        }
    }

    // Función para cargar datos de ventas por período
    function cargarVentasPorPeriodo() {
        console.log('Cargando datos de ventas por período');
        // Construir parámetros de fecha si están disponibles
        let params = '';
        if (startDate && startDate.value && endDate && endDate.value) {
            const startISO = new Date(startDate.value + 'T00:00:00').toISOString();
            const endISO = new Date(endDate.value + 'T23:59:59').toISOString();
            params = `?desde=${encodeURIComponent(startISO)}&hasta=${encodeURIComponent(endISO)}`;
        }

        fetch(`/api/reportes/ventas-por-periodo${params}`)
            .then(response => {
                if (!response.ok) {
                    throw new Error(`Error al cargar los datos de ventas por período: ${response.status}`);
                }
                return response.json();
            })
            .then(data => {
                console.log('Datos de ventas por período recibidos:', data);
                if (salesChart) {
                    // Actualizar datos del gráfico
                    salesChart.data.labels = data.labels;
                    salesChart.data.datasets[0].data = data.values;
                    salesChart.update();
                    console.log('Gráfico de ventas por período actualizado');
                } else {
                    console.error('El gráfico salesChart no está inicializado');
                }
            })
            .catch(error => {
                console.error('Error al cargar los datos de ventas por período:', error);
                showToast('Error', 'No se pudieron cargar los datos de ventas por período', 'error');
            });
    }

    // Función para cargar datos de ventas por categoría
    function cargarVentasPorCategoria() {
        console.log('Cargando datos de ventas por categoría');
        // Construir parámetros de fecha si están disponibles
        let params = '';
        if (startDate && startDate.value && endDate && endDate.value) {
            const startISO = new Date(startDate.value + 'T00:00:00').toISOString();
            const endISO = new Date(endDate.value + 'T23:59:59').toISOString();
            params = `?desde=${encodeURIComponent(startISO)}&hasta=${encodeURIComponent(endISO)}`;
        }

        fetch(`/api/reportes/ventas-por-categoria${params}`)
            .then(response => {
                if (!response.ok) {
                    throw new Error(`Error al cargar los datos de ventas por categoría: ${response.status}`);
                }
                return response.json();
            })
            .then(data => {
                console.log('Datos de ventas por categoría recibidos:', data);
                if (categorySalesChart) {
                    // Actualizar datos del gráfico
                    categorySalesChart.data.labels = data.labels;
                    categorySalesChart.data.datasets[0].data = data.values;
                    categorySalesChart.update();
                    console.log('Gráfico de ventas por categoría actualizado');
                } else {
                    console.error('El gráfico categorySalesChart no está inicializado');
                }
            })
            .catch(error => {
                console.error('Error al cargar los datos de ventas por categoría:', error);
                showToast('Error', 'No se pudieron cargar los datos de ventas por categoría', 'error');
            });
    }

    // Función para cargar datos de productos más vendidos
    function cargarProductosMasVendidos() {
        // Construir parámetros de fecha si están disponibles
        let params = '';
        if (startDate.value && endDate.value) {
            const startISO = new Date(startDate.value + 'T00:00:00').toISOString();
            const endISO = new Date(endDate.value + 'T23:59:59').toISOString();
            params = `?desde=${encodeURIComponent(startISO)}&hasta=${encodeURIComponent(endISO)}`;
        }

        fetch(`/api/reportes/productos-mas-vendidos${params}`)
            .then(response => {
                if (!response.ok) {
                    throw new Error('Error al cargar los datos de productos más vendidos');
                }
                return response.json();
            })
            .then(data => {
                // Actualizar la tabla de productos más vendidos
                const tabla = document.getElementById('topProductsTable').querySelector('tbody');
                
                // Limpiar la tabla
                tabla.innerHTML = '';
                
                // Llenar con los nuevos datos
                data.forEach(producto => {
                    const fila = document.createElement('tr');
                    
                    // Formatear el precio para mostrar en la tabla
                    const ingresos = typeof producto.ingresos === 'number' 
                        ? 'S/. ' + producto.ingresos.toFixed(2)
                        : 'S/. ' + parseFloat(producto.ingresos).toFixed(2);
                    
                    fila.innerHTML = `
                        <td>${producto.producto}</td>
                        <td>${producto.categoria}</td>
                        <td>${producto.unidades}</td>
                        <td>${ingresos}</td>
                    `;
                    
                    tabla.appendChild(fila);
                });
            })
            .catch(error => {
                console.error('Error al cargar los datos de productos más vendidos:', error);
                showToast('Error', 'No se pudieron cargar los datos de productos más vendidos', 'error');
            });
    }

    // Función para cargar datos del resumen de ventas
    function cargarResumenVentas() {
        // Construir parámetros de fecha si están disponibles
        let params = '';
        if (startDate.value && endDate.value) {
            const startISO = new Date(startDate.value + 'T00:00:00').toISOString();
            const endISO = new Date(endDate.value + 'T23:59:59').toISOString();
            params = `?desde=${encodeURIComponent(startISO)}&hasta=${encodeURIComponent(endISO)}`;
        }

        fetch(`/api/reportes/resumen-ventas${params}`)
            .then(response => {
                if (!response.ok) {
                    throw new Error('Error al cargar los datos de resumen de ventas');
                }
                return response.json();
            })
            .then(data => {
                // Actualizar el resumen de ventas
                const statCards = document.querySelectorAll('.stat-card');
                
                // Total de ventas
                updateStatCard(statCards[0], 'S/. ' + data.totalVentas.toFixed(2), data.varTotalVentas);
                
                // Transacciones
                updateStatCard(statCards[1], data.transacciones, data.varTransacciones);
                
                // Ticket promedio
                updateStatCard(statCards[2], 'S/. ' + data.ticketPromedio.toFixed(2), data.varTicketPromedio);
                
                // Margen bruto
                updateStatCard(statCards[3], (data.margenBruto * 100).toFixed(1) + '%', data.varMargenBruto);
            })
            .catch(error => {
                console.error('Error al cargar los datos de resumen de ventas:', error);
                showToast('Error', 'No se pudieron cargar los datos de resumen de ventas', 'error');
            });
    }

    // Función para actualizar una tarjeta de estadísticas
    function updateStatCard(card, value, variation, invertir = false) {
        if (!card) return;
        
        const valueElement = card.querySelector('.stat-value');
        const changeElement = card.querySelector('.stat-change');
        
        if (valueElement) {
            valueElement.textContent = value;
        }
        
        if (changeElement) {
            // Para alertas, un valor negativo es positivo (menos alertas es mejor)
            let esPositivo = invertir ? variation < 0 : variation > 0;
            
            // Formatear la variación con signo y decimales
            let textoVariacion = `${esPositivo ? '+' : ''}${variation.toFixed(1)}% vs mes anterior`;
            changeElement.textContent = textoVariacion;
            
            // Actualizar la clase para el estilo
            changeElement.classList.remove('positive', 'negative');
            changeElement.classList.add(esPositivo ? 'positive' : 'negative');
        }
    }

    // Función para cargar datos de valor de inventario por categoría
    function cargarValorInventarioPorCategoria() {
        fetch('/api/reportes/valor-inventario-por-categoria')
            .then(response => {
                if (!response.ok) {
                    throw new Error('Error al cargar los datos de valor de inventario por categoría');
                }
                return response.json();
            })
            .then(data => {
                if (inventoryValueChart) {
                    // Actualizar datos del gráfico
                    inventoryValueChart.data.labels = data.labels;
                    inventoryValueChart.data.datasets[0].data = data.values;
                    inventoryValueChart.update();
                }
            })
            .catch(error => {
                console.error('Error al cargar los datos de valor de inventario por categoría:', error);
                showToast('Error', 'No se pudieron cargar los datos de valor de inventario por categoría', 'error');
            });
    }

    // Función para cargar datos de rotación de inventario
    function cargarRotacionInventario() {
        fetch('/api/reportes/rotacion-inventario')
            .then(response => {
                if (!response.ok) {
                    throw new Error('Error al cargar los datos de rotación de inventario');
                }
                return response.json();
            })
            .then(data => {
                if (inventoryTurnoverChart) {
                    // Actualizar datos del gráfico
                    inventoryTurnoverChart.data.labels = data.labels;
                    inventoryTurnoverChart.data.datasets[0].data = data.values;
                    inventoryTurnoverChart.update();
                }
            })
            .catch(error => {
                console.error('Error al cargar los datos de rotación de inventario:', error);
                showToast('Error', 'No se pudieron cargar los datos de rotación de inventario', 'error');
            });
    }

    // Función para cargar datos de productos con bajo stock
    function cargarProductosBajoStock() {
        fetch('/api/reportes/productos-bajo-stock')
            .then(response => {
                if (!response.ok) {
                    throw new Error('Error al cargar los datos de productos con bajo stock');
                }
                return response.json();
            })
            .then(data => {
                // Actualizar la tabla de productos con bajo stock
                const tabla = document.getElementById('lowStockTable').querySelector('tbody');
                
                // Limpiar la tabla
                tabla.innerHTML = '';
                
                // Llenar con los nuevos datos
                data.forEach(producto => {
                    const fila = document.createElement('tr');
                    
                    // Determinar la clase para el estado
                    let estadoClase = '';
                    if (producto.estado === 'Crítico') {
                        estadoClase = 'critico';
                    } else if (producto.estado === 'Bajo') {
                        estadoClase = 'bajo';
                    }
                    
                    fila.innerHTML = `
                        <td>${producto.producto}</td>
                        <td>${producto.variante}</td>
                        <td>${producto.stockActual}</td>
                        <td>${producto.stockMinimo}</td>
                        <td><span class="estado-stock ${estadoClase}">${producto.estado}</span></td>
                    `;
                    
                    tabla.appendChild(fila);
                });
            })
            .catch(error => {
                console.error('Error al cargar los datos de productos con bajo stock:', error);
                showToast('Error', 'No se pudieron cargar los datos de productos con bajo stock', 'error');
            });
    }

    // Función para cargar datos del resumen de inventario
    function cargarResumenInventario() {
        fetch('/api/reportes/resumen-inventario')
            .then(response => {
                if (!response.ok) {
                    throw new Error('Error al cargar los datos de resumen de inventario');
                }
                return response.json();
            })
            .then(data => {
                // Actualizar el resumen de inventario
                const inventoryTab = document.getElementById('inventory-tab');
                if (!inventoryTab) return;
                
                // Seleccionar las tarjetas de estadísticas dentro de la pestaña de inventario
                const statCards = inventoryTab.querySelectorAll('.stat-card');
                if (statCards.length < 4) return;
                
                // Actualizar cada tarjeta con los datos recibidos
                // Valor total
                updateStatCard(statCards[0], 'S/. ' + data.valorTotal.toFixed(2), data.varValorTotal);
                
                // Unidades
                updateStatCard(statCards[1], data.unidades, data.varUnidades);
                
                // Rotación
                updateStatCard(statCards[2], data.rotacion.toFixed(1), data.varRotacion);
                
                // Alertas
                updateStatCard(statCards[3], data.alertas, data.varAlertas, true);
            })
            .catch(error => {
                console.error('Error al cargar los datos de resumen de inventario:', error);
                showToast('Error', 'No se pudieron cargar los datos de resumen de inventario', 'error');
            });
    }

    // Función para cargar datos de rendimiento de productos
    function cargarRendimientoProductos() {
        // Construir parámetros de fecha si están disponibles
        let params = '';
        if (startDate.value && endDate.value) {
            const startISO = new Date(startDate.value + 'T00:00:00').toISOString();
            const endISO = new Date(endDate.value + 'T23:59:59').toISOString();
            params = `?desde=${encodeURIComponent(startISO)}&hasta=${encodeURIComponent(endISO)}`;
        }

        fetch(`/api/reportes/rendimiento-productos${params}`)
            .then(response => {
                if (!response.ok) {
                    throw new Error('Error al cargar los datos de rendimiento de productos');
                }
                return response.json();
            })
            .then(data => {
                // Actualizar la tabla de productos con mayor margen
                const tabla = document.getElementById('topMarginTable').querySelector('tbody');
                
                // Limpiar la tabla
                tabla.innerHTML = '';
                
                // Tomar los 5 productos con mayor margen
                const topProductos = data.slice(0, 5);
                
                // Llenar con los nuevos datos
                topProductos.forEach(producto => {
                    const fila = document.createElement('tr');
                    
                    // Preparar los datos para la fila
                    const precio = producto.ingresos.toFixed(2);
                    const costo = producto.costoTotal.toFixed(2);
                    const margenPorcentaje = producto.margenPorcentaje.toFixed(1);
                    
                    fila.innerHTML = `
                        <td>${producto.nombre}</td>
                        <td>${producto.categoria}</td>
                        <td>S/. ${precio}</td>
                        <td>S/. ${costo}</td>
                        <td>${margenPorcentaje}%</td>
                    `;
                    
                    tabla.appendChild(fila);
                });
                
                // Actualizar gráfico de margen por producto si existe
                if (productMarginChart) {
                    // Obtener los nombres de productos y márgenes para el gráfico
                    const labels = topProductos.map(p => p.nombre);
                    const values = topProductos.map(p => p.margenPorcentaje);
                    
                    productMarginChart.data.labels = labels;
                    productMarginChart.data.datasets[0].data = values;
                    productMarginChart.update();
                }
                
                // Actualizar gráfico de rendimiento por categoría si existe
                if (categoryPerformanceChart) {
                    // Agrupar por categoría
                    const categorias = {};
                    data.forEach(producto => {
                        if (!categorias[producto.categoria]) {
                            categorias[producto.categoria] = {
                                ingresos: 0,
                                unidades: 0
                            };
                        }
                        
                        categorias[producto.categoria].ingresos += parseFloat(producto.ingresos);
                        categorias[producto.categoria].unidades += producto.unidadesVendidas;
                    });
                    
                    // Convertir a arrays para el gráfico
                    const categoriasLabels = Object.keys(categorias);
                    const categoriasIngresos = categoriasLabels.map(cat => categorias[cat].ingresos);
                    const categoriasUnidades = categoriasLabels.map(cat => categorias[cat].unidades);
                    
                    categoryPerformanceChart.data.labels = categoriasLabels;
                    categoryPerformanceChart.data.datasets[0].data = categoriasIngresos;
                    categoryPerformanceChart.data.datasets[1].data = categoriasUnidades;
                    categoryPerformanceChart.update();
                }
            })
            .catch(error => {
                console.error('Error al cargar los datos de rendimiento de productos:', error);
                showToast('Error', 'No se pudieron cargar los datos de rendimiento de productos', 'error');
            });
    }
    
    // Función para cargar datos de tendencias de productos
    function cargarTendenciasProductos() {
        // Construir parámetros de fecha si están disponibles
        let params = '';
        if (startDate.value && endDate.value) {
            const startISO = new Date(startDate.value + 'T00:00:00').toISOString();
            const endISO = new Date(endDate.value + 'T23:59:59').toISOString();
            params = `?desde=${encodeURIComponent(startISO)}&hasta=${encodeURIComponent(endISO)}&limite=5`;
        }

        fetch(`/api/reportes/tendencias-productos${params}`)
            .then(response => {
                if (!response.ok) {
                    throw new Error('Error al cargar los datos de tendencias de productos');
                }
                return response.json();
            })
            .then(data => {
                // Actualizar los datos de tendencias
                console.log('Tendencias cargadas:', data);
                // Aquí podrías actualizar un gráfico o tabla adicional si quisieras mostrar tendencias
            })
            .catch(error => {
                console.error('Error al cargar los datos de tendencias de productos:', error);
                showToast('Error', 'No se pudieron cargar los datos de tendencias de productos', 'error');
            });
    }
    
    // Función para cargar datos de distribución de productos por categoría
    function cargarDistribucionProductosCategoria() {
        fetch('/api/reportes/distribucion-productos-categoria')
            .then(response => {
                if (!response.ok) {
                    throw new Error('Error al cargar los datos de distribución de productos');
                }
                return response.json();
            })
            .then(data => {
                // Actualizar el resumen de productos con los datos
                const totalProductosElement = document.querySelector('.stat-card:nth-child(1) .stat-value');
                if (totalProductosElement) {
                    totalProductosElement.textContent = data.totalProductos;
                }
                
                const categoriasElement = document.querySelector('.stat-card:nth-child(2) .stat-value');
                if (categoriasElement) {
                    categoriasElement.textContent = data.distribucion.length;
                }
                
                // También podrías actualizar otros elementos si tienes más datos
                console.log('Distribución cargada:', data);
            })
            .catch(error => {
                console.error('Error al cargar los datos de distribución de productos:', error);
                showToast('Error', 'No se pudieron cargar los datos de distribución de productos', 'error');
            });
    }

    /**
     * Carga y procesa los datos de clasificación ABC
     * @param {boolean} forzarRecarga - Indica si se debe forzar la recarga de datos
     * @returns {Promise} - Promesa con los datos de clasificación ABC
     */
    async function cargarClasificacionABC(forzarRecarga = false) {
        const ahora = new Date().getTime();
        
        // Verificar si podemos usar el caché
        if (!forzarRecarga && abcDataCache && abcDataCacheTimestamp && 
            (ahora - abcDataCacheTimestamp) < abcDataCacheExpiry) {
            console.log('Usando datos ABC en caché');
            return abcDataCache;
        }

        // Evitar múltiples cargas simultáneas
        if (abcDataLoadingInProgress) {
            console.log('Ya hay una carga de datos ABC en progreso');
            showToast('Información', 'Cargando datos de clasificación ABC, por favor espere...', 'info');
            return;
        }

        abcDataLoadingInProgress = true;
        abcDataLoadingAttempts = 0;
        
        // Mostrar indicador de carga
        const tablaABC = document.querySelector('#abcProductsTable tbody');
        if (tablaABC) {
            tablaABC.innerHTML = '<tr><td colspan="6" class="loading-message"><i class="fas fa-spinner fa-spin"></i> Cargando datos de clasificación ABC...</td></tr>';
        }
        
        // Mostrar indicadores de carga en los resúmenes
        const elementos = ['abcCountA', 'abcCountB', 'abcCountC', 'abcTotalProducts', 'abcValueA', 'abcValueB', 'abcValueC', 'abcTotalValue'];
        elementos.forEach(id => {
            const elemento = document.getElementById(id);
            if (elemento) elemento.textContent = 'Cargando...';
        });

        // Mostrar indicadores de carga en los gráficos
        if (abcDistributionChart) {
            abcDistributionChart.data.datasets[0].data = [33, 33, 34]; // Valores temporales para mostrar algo
            abcDistributionChart.update();
        }
        
        if (abcValueChart) {
            abcValueChart.data.datasets[0].data = [80, 15, 5]; // Valores temporales para mostrar algo
            abcValueChart.update();
        }

        // Primero intentar precalcular la clasificación ABC para asegurar que existan datos
        try {
            console.log('Intentando precalcular clasificación ABC...');
            await fetch('/api/reportes/precalcular-abc?forzar=' + (forzarRecarga ? 'true' : 'false'), {
                method: 'GET',
                headers: { 'Cache-Control': 'no-cache' }
            });
            console.log('Precálculo de clasificación ABC completado');
        } catch (error) {
            console.warn('No se pudo precalcular la clasificación ABC:', error);
            // Continuamos de todos modos, ya que podría haber datos existentes
        }

        async function intentarCarga() {
            try {
                // Usar un parámetro de tiempo para evitar caché del navegador
                const timestamp = new Date().getTime();
                console.log('Intentando cargar datos ABC, intento:', abcDataLoadingAttempts + 1);
                
                // Mostrar feedback visual
                if (abcDataLoadingAttempts > 0) {
                    const tablaABC = document.querySelector('#abcProductsTable tbody');
                    if (tablaABC) {
                        tablaABC.innerHTML = `<tr><td colspan="6" class="loading-message"><i class="fas fa-spinner fa-spin"></i> Cargando datos de clasificación ABC... (Intento ${abcDataLoadingAttempts + 1})</td></tr>`;
                    }
                }
                
                const response = await fetch(`/api/reportes/clasificacion-abc?_=${timestamp}`, {
                    method: 'GET',
                    headers: { 'Cache-Control': 'no-cache' }
                });
                
                if (!response.ok) {
                    throw new Error(`HTTP error! status: ${response.status}`);
                }
                
                const data = await response.json();
                console.log('Datos ABC recibidos:', data);
                
                // Verificar si hay datos válidos
                if (!data || !data.productos || data.productos.length === 0) {
                    console.warn('No hay productos en los datos ABC recibidos');
                    throw new Error('No hay productos en los datos ABC');
                }
                
                abcDataCache = data;
                abcDataCacheTimestamp = new Date().getTime();
                abcDataLoadingInProgress = false;
                abcDataLoadingAttempts = 0;
                
                console.log('Datos ABC cargados exitosamente');
                
                // Procesar y mostrar datos inmediatamente
                await procesarYMostrarDatosABC(data);
                
                return data;
            } catch (error) {
                console.error('Error cargando datos ABC:', error);
                abcDataLoadingAttempts++;
                
                if (abcDataLoadingAttempts < MAX_LOADING_ATTEMPTS) {
                    console.log(`Reintentando carga (intento ${abcDataLoadingAttempts})...`);
                    showToast('Información', `Reintentando cargar datos ABC (intento ${abcDataLoadingAttempts})...`, 'info');
                    await new Promise(resolve => setTimeout(resolve, RETRY_DELAY * abcDataLoadingAttempts));
                    return intentarCarga();
                } else {
                    abcDataLoadingInProgress = false;
                    mostrarEstadoSinDatos();
                    
                    // Agregar botón de reintento
                    const tablaABC = document.querySelector('#abcProductsTable tbody');
                    if (tablaABC) {
                        tablaABC.innerHTML = `
                            <tr>
                                <td colspan="6" class="error-message">
                                    No se pudieron cargar los datos de clasificación ABC.
                                    <button id="retryABCButton" class="button button-primary" onclick="cargarClasificacionABC(true)">
                                        <i class="fas fa-sync-alt"></i> Reintentar
                                    </button>
                                </td>
                            </tr>
                        `;
                    }
                    
                    showToast('Error', 'No se pudieron cargar los datos de clasificación ABC después de varios intentos', 'error');
                    throw new Error('Máximo número de intentos alcanzado');
                }
            }
        }

        return intentarCarga();
    }

    // Inicializar gráficos
    function initializeCharts() {
        console.log('Inicializando gráficos');
        
        // Gráfico de ventas por período
        if (canvasExists('salesChart')) {
            console.log('Inicializando gráfico de ventas por período');
            salesChart = initializeChartSafely('salesChart', {
                type: 'line',
                data: {
                    labels: ['Cargando datos...'],
                    datasets: [{
                        label: 'Ventas (S/.)',
                        data: [0],
                        backgroundColor: 'rgba(59, 130, 246, 0.2)',
                        borderColor: 'rgba(59, 130, 246, 1)',
                        borderWidth: 2,
                        tension: 0.3,
                        pointBackgroundColor: 'rgba(59, 130, 246, 1)'
                    }]
                },
                options: {
                    responsive: true,
                    maintainAspectRatio: false,
                    scales: {
                        y: {
                            beginAtZero: true,
                            ticks: {
                                callback: function(value) {
                                    return 'S/. ' + value.toLocaleString();
                                }
                            }
                        }
                    },
                    plugins: {
                        tooltip: {
                            callbacks: {
                                label: function(context) {
                                    return 'S/. ' + context.raw.toLocaleString();
                                }
                            }
                        }
                    }
                }
            });
        }

        // Gráfico de ventas por categoría
        if (canvasExists('categorySalesChart')) {
            console.log('Inicializando gráfico de ventas por categoría');
            categorySalesChart = initializeChartSafely('categorySalesChart', {
                type: 'doughnut',
                data: {
                    labels: ['Cargando datos...'],
                    datasets: [{
                        data: [100],
                        backgroundColor: [
                            'rgba(59, 130, 246, 0.7)',
                            'rgba(16, 185, 129, 0.7)',
                            'rgba(245, 158, 11, 0.7)',
                            'rgba(236, 72, 153, 0.7)',
                            'rgba(139, 92, 246, 0.7)'
                        ],
                        borderColor: [
                            'rgba(59, 130, 246, 1)',
                            'rgba(16, 185, 129, 1)',
                            'rgba(245, 158, 11, 1)',
                            'rgba(236, 72, 153, 1)',
                            'rgba(139, 92, 246, 1)'
                        ],
                        borderWidth: 1
                    }]
                },
                options: {
                    responsive: true,
                    maintainAspectRatio: false,
                    plugins: {
                        tooltip: {
                            callbacks: {
                                label: function(context) {
                                    const label = context.label || '';
                                    const value = context.raw || 0;
                                    const total = context.dataset.data.reduce((acc, data) => acc + data, 0);
                                    const percentage = Math.round((value / total) * 100);
                                    return `${label}: S/. ${value.toLocaleString()} (${percentage}%)`;
                                }
                            }
                        }
                    }
                }
            });
        }

        // Inicializar los gráficos restantes
        initializeRemainingCharts();
    }
    
    // Inicializar los gráficos restantes
    function initializeRemainingCharts() {
        console.log('Inicializando gráficos restantes');
        
        // Gráfico de valor de inventario por categoría
        if (canvasExists('inventoryValueChart')) {
            console.log('Inicializando gráfico de valor de inventario');
            inventoryValueChart = initializeChartSafely('inventoryValueChart', {
                type: 'bar',
                data: {
                    labels: ['Camisetas', 'Pantalones', 'Vestidos', 'Blusas', 'Chaquetas'],
                    datasets: [{
                        label: 'Valor (S/.)',
                        data: [8500, 12000, 7200, 6350, 8800],
                        backgroundColor: 'rgba(16, 185, 129, 0.7)',
                        borderColor: 'rgba(16, 185, 129, 1)',
                        borderWidth: 1
                    }]
                },
                options: {
                    responsive: true,
                    maintainAspectRatio: false,
                    scales: {
                        y: {
                            beginAtZero: true,
                            ticks: {
                                callback: function(value) {
                                    return 'S/. ' + value.toLocaleString();
                                }
                            }
                        }
                    },
                    plugins: {
                        tooltip: {
                            callbacks: {
                                label: function(context) {
                                    return 'S/. ' + context.raw.toLocaleString();
                                }
                            }
                        }
                    }
                }
            });
        }

        // Gráfico de rotación de inventario
        if (canvasExists('inventoryTurnoverChart')) {
            console.log('Inicializando gráfico de rotación de inventario');
            inventoryTurnoverChart = initializeChartSafely('inventoryTurnoverChart', {
                type: 'line',
                data: {
                    labels: ['Ene', 'Feb', 'Mar', 'Abr', 'May', 'Jun'],
                    datasets: [{
                        label: 'Rotación',
                        data: [3.8, 3.9, 4.0, 4.1, 3.9, 4.2],
                        backgroundColor: 'rgba(245, 158, 11, 0.2)',
                        borderColor: 'rgba(245, 158, 11, 1)',
                        borderWidth: 2,
                        tension: 0.3,
                        pointBackgroundColor: 'rgba(245, 158, 11, 1)'
                    }]
                },
                options: {
                    responsive: true,
                    maintainAspectRatio: false,
                    scales: {
                        y: {
                            beginAtZero: false
                        }
                    }
                }
            });
        }

        // Gráfico de rendimiento por categoría
        if (canvasExists('categoryPerformanceChart')) {
            console.log('Inicializando gráfico de rendimiento por categoría');
            categoryPerformanceChart = initializeChartSafely('categoryPerformanceChart', {
                type: 'bar',
                data: {
                    labels: ['Camisetas', 'Pantalones', 'Vestidos', 'Blusas', 'Chaquetas'],
                    datasets: [
                        {
                            label: 'Ingresos (S/.)',
                            data: [3500, 4200, 3800, 2900, 3100],
                            backgroundColor: 'rgba(66, 135, 245, 0.7)',
                            borderColor: 'rgba(66, 135, 245, 1)',
                            borderWidth: 1,
                            yAxisID: 'y'
                        },
                        {
                            label: 'Unidades Vendidas',
                            data: [45, 32, 28, 25, 18],
                            backgroundColor: 'rgba(153, 102, 255, 0.7)',
                            borderColor: 'rgba(153, 102, 255, 1)',
                            borderWidth: 1,
                            yAxisID: 'y1'
                        }
                    ]
                },
                options: {
                    responsive: true,
                    maintainAspectRatio: false,
                    scales: {
                        y: {
                            beginAtZero: true,
                            type: 'linear',
                            position: 'left',
                            title: {
                                display: true,
                                text: 'Ingresos (S/.)'
                            }
                        },
                        y1: {
                            beginAtZero: true,
                            type: 'linear',
                            position: 'right',
                            grid: {
                                drawOnChartArea: false
                            },
                            title: {
                                display: true,
                                text: 'Unidades'
                            }
                        }
                    }
                }
            });
        }

        // Gráfico de margen por producto
        if (canvasExists('productMarginChart')) {
            console.log('Inicializando gráfico de margen por producto');
            productMarginChart = initializeChartSafely('productMarginChart', {
                type: 'bar',
                data: {
                    labels: ['Vestido Casual', 'Chaqueta Denim', 'Pantalón Chino', 'Camiseta Slim Fit', 'Blusa Estampada'],
                    datasets: [{
                        label: 'Margen (%)',
                        data: [50.0, 50.0, 50.0, 50.0, 50.0],
                        backgroundColor: 'rgba(255, 159, 64, 0.7)',
                        borderColor: 'rgba(255, 159, 64, 1)',
                        borderWidth: 1
                    }]
                },
                options: {
                    indexAxis: 'y',
                    responsive: true,
                    maintainAspectRatio: false,
                    scales: {
                        x: {
                            beginAtZero: true,
                            max: 100
                        }
                    }
                }
            });
        }
        
        // Gráfico de distribución ABC
        if (canvasExists('abcDistributionChart')) {
            console.log('Inicializando gráfico de distribución ABC');
            abcDistributionChart = initializeChartSafely('abcDistributionChart', {
                type: 'doughnut',
                data: {
                    labels: ['Categoría A', 'Categoría B', 'Categoría C'],
                    datasets: [{
                        data: [20, 30, 50], // Valores predeterminados según la teoría ABC
                        backgroundColor: [
                            'rgba(59, 130, 246, 0.7)', // Azul para A
                            'rgba(16, 185, 129, 0.7)', // Verde para B
                            'rgba(245, 158, 11, 0.7)'  // Naranja para C
                        ],
                        borderColor: [
                            'rgba(59, 130, 246, 1)',
                            'rgba(16, 185, 129, 1)',
                            'rgba(245, 158, 11, 1)'
                        ],
                        borderWidth: 1
                    }]
                },
                options: {
                    responsive: true,
                    maintainAspectRatio: false,
                    cutout: '60%',
                    plugins: {
                        legend: {
                            position: 'bottom',
                            labels: {
                                padding: 20,
                                boxWidth: 12,
                                font: {
                                    size: 12
                                }
                            }
                        },
                        tooltip: {
                            callbacks: {
                                label: function(context) {
                                    const label = context.label || '';
                                    const value = context.raw || 0;
                                    const total = context.dataset.data.reduce((acc, data) => acc + data, 0);
                                    const percentage = Math.round((value / total) * 100);
                                    return `${label}: ${value} productos (${percentage}%)`;
                                }
                            }
                        },
                        title: {
                            display: true,
                            text: 'Distribución de Productos por Categoría ABC',
                            font: {
                                size: 16
                            },
                            padding: {
                                top: 10,
                                bottom: 20
                            }
                        }
                    }
                }
            });
        }
        
        // Gráfico de valor ABC
        if (canvasExists('abcValueChart')) {
            console.log('Inicializando gráfico de valor ABC');
            abcValueChart = initializeChartSafely('abcValueChart', {
                type: 'bar',
                data: {
                    labels: ['Categoría A', 'Categoría B', 'Categoría C'],
                    datasets: [{
                        label: 'Valor (S/.)',
                        data: [80, 15, 5], // Valores predeterminados según la teoría ABC
                        backgroundColor: [
                            'rgba(59, 130, 246, 0.7)', // Azul para A
                            'rgba(16, 185, 129, 0.7)', // Verde para B
                            'rgba(245, 158, 11, 0.7)'  // Naranja para C
                        ],
                        borderColor: [
                            'rgba(59, 130, 246, 1)',
                            'rgba(16, 185, 129, 1)',
                            'rgba(245, 158, 11, 1)'
                        ],
                        borderWidth: 1
                    }]
                },
                options: {
                    responsive: true,
                    maintainAspectRatio: false,
                    indexAxis: 'y',
                    scales: {
                        x: {
                            beginAtZero: true,
                            ticks: {
                                callback: function(value) {
                                    return 'S/. ' + value.toLocaleString();
                                }
                            },
                            grid: {
                                display: true,
                                drawBorder: true,
                                drawOnChartArea: true,
                                drawTicks: true,
                            }
                        },
                        y: {
                            grid: {
                                display: false
                            }
                        }
                    },
                    plugins: {
                        legend: {
                            display: false
                        },
                        tooltip: {
                            callbacks: {
                                label: function(context) {
                                    const label = context.dataset.label || '';
                                    const value = context.raw || 0;
                                    const total = context.dataset.data.reduce((acc, data) => acc + data, 0);
                                    const percentage = Math.round((value / total) * 100);
                                    return `${label}: S/. ${value.toLocaleString()} (${percentage}%)`;
                                }
                            }
                        },
                        title: {
                            display: true,
                            text: 'Valor de Inventario por Categoría ABC',
                            font: {
                                size: 16
                            },
                            padding: {
                                top: 10,
                                bottom: 20
                            }
                        }
                    }
                }
            });
        }
    }

    // Cambiar entre pestañas - CORREGIDO
    tabButtons.forEach(button => {
        button.addEventListener('click', function(event) {
            // Prevenir comportamiento por defecto
            event.preventDefault();
            
            const tabId = this.dataset.tab;
            console.log(`Cambiando a la pestaña: ${tabId}`);

            // Desactivar todas las pestañas
            tabButtons.forEach(btn => btn.classList.remove('active'));
            tabContents.forEach(content => content.classList.remove('active'));

            // Activar la pestaña seleccionada
            this.classList.add('active');
            const tabContent = document.getElementById(`${tabId}-tab`);
            if (tabContent) {
                tabContent.classList.add('active');
                console.log(`Pestaña ${tabId} activada`);
            } else {
                console.error(`No se encontró el contenido para la pestaña ${tabId}`);
            }

            // Actualizar gráficos
            updateCharts(tabId);
        });
    });

    /**
     * Actualiza los gráficos según la pestaña activa
     * @param {string} tabId - ID de la pestaña activa
     */
    function updateCharts(tabId) {
        console.log('Actualizando gráficos para la pestaña:', tabId);
        
        try {
            switch (tabId) {
                case 'sales':
            cargarVentasPorPeriodo();
            cargarVentasPorCategoria();
            cargarProductosMasVendidos();
            cargarResumenVentas();
                    break;
                case 'inventory':
            cargarValorInventarioPorCategoria();
            cargarRotacionInventario();
            cargarProductosBajoStock();
            cargarResumenInventario();
                    break;
                case 'products':
            cargarRendimientoProductos();
            cargarTendenciasProductos();
            cargarDistribucionProductosCategoria();
                    break;
                case 'abc':
                    console.log('Cargando datos de clasificación ABC');
                    cargarClasificacionABC();
                    break;
                default:
                    console.warn('Pestaña desconocida:', tabId);
                    break;
            }
        } catch (error) {
            console.error('Error al actualizar gráficos:', error);
        }
    }
    
    /**
     * Actualiza todos los gráficos en todas las pestañas
     */
    function updateAllCharts() {
        console.log('Actualizando todos los gráficos');
        
        // Obtener la pestaña activa
        const activeTab = document.querySelector('.tab-button.active');
        if (activeTab) {
            // Actualizar solo los gráficos de la pestaña activa
            updateCharts(activeTab.dataset.tab);
        } else {
            // Si no hay pestaña activa, actualizar todos los gráficos
            updateCharts('sales');
            updateCharts('inventory');
            updateCharts('products');
            updateCharts('abc');
        }
    }

    // Inicializar la aplicación
    function initialize() {
        console.log('Inicializando aplicación de reportes');
        
        // Verificar si Chart.js está cargado
        if (!isChartJsLoaded()) {
            console.error('Chart.js no está disponible. Los gráficos no se pueden mostrar.');
            showToast('Error', 'No se pudieron cargar los gráficos. Por favor, recargue la página.', 'error');
            return;
        }
        
        // Inicializar gráficos
        initializeCharts();
        
        // Precalcular datos ABC en segundo plano
        setTimeout(() => {
            try {
                console.log('Precalculando datos ABC en segundo plano...');
                fetch('/api/reportes/precalcular-abc', {
                    method: 'GET',
                    headers: { 'Cache-Control': 'no-cache' }
                }).then(response => {
                    if (response.ok) {
                        console.log('Precálculo de datos ABC completado');
                    } else {
                        console.warn('El precálculo de datos ABC no fue exitoso');
                    }
                }).catch(error => {
                    console.error('Error en precálculo de datos ABC:', error);
                });
            } catch (error) {
                console.error('Error al iniciar precálculo de datos ABC:', error);
            }
        }, 1000);
        
        // Cargar datos iniciales
        const activeTab = document.querySelector('.tab-button.active').dataset.tab;
        updateCharts(activeTab);
        
        // Mostrar mensaje de inicialización completada
        console.log('Inicialización de reportes completada');
    }

    // Iniciar la aplicación
    initialize();

    // Función para procesar y mostrar datos ABC
    async function procesarYMostrarDatosABC(data) {
        console.log('Procesando datos ABC:', data);
        
        if (!data) {
            console.error('No hay datos ABC para procesar');
            mostrarEstadoSinDatos();
            return;
        }

        try {
            // Verificar si hay productos en los datos
            if (!data.productos || data.productos.length === 0) {
                console.warn('No hay productos en los datos ABC');
                
                // Intentar precalcular nuevamente si no hay productos
                try {
                    console.log('Intentando forzar precálculo de clasificación ABC...');
                    await fetch('/api/reportes/precalcular-abc?forzar=true', {
                        method: 'GET',
                        headers: { 'Cache-Control': 'no-cache' }
                    });
                    
                    // Esperar un momento y volver a cargar los datos
                    await new Promise(resolve => setTimeout(resolve, 2000));
                    
                    // Intentar cargar los datos nuevamente
                    const response = await fetch('/api/reportes/clasificacion-abc', {
                        method: 'GET',
                        headers: { 'Cache-Control': 'no-cache' }
                    });
                    
                    if (response.ok) {
                        const nuevosDatos = await response.json();
                        if (nuevosDatos && nuevosDatos.productos && nuevosDatos.productos.length > 0) {
                            console.log('Se obtuvieron nuevos datos después del precálculo');
                            data = nuevosDatos;
                        } else {
                            mostrarEstadoSinDatos();
                            return;
                        }
                    } else {
                        mostrarEstadoSinDatos();
                        return;
                    }
                } catch (error) {
                    console.error('Error al intentar forzar el precálculo:', error);
                    mostrarEstadoSinDatos();
                    return;
                }
            }

            // Actualizar visualizaciones
            actualizarGraficosABC(data);
            actualizarTablaABC(data);
            actualizarResumenABC(data);
            
            showToast('Éxito', 'Datos ABC actualizados correctamente', 'success');
        } catch (error) {
            console.error('Error procesando datos ABC:', error);
            showToast('Error', 'Error al procesar los datos ABC: ' + error.message, 'error');
            mostrarEstadoSinDatos();
        }
    }

    function mostrarEstadoSinDatos() {
        console.log('Mostrando estado sin datos ABC');
        
        const elementos = {
            'abcCountA': '0',
            'abcCountB': '0',
            'abcCountC': '0',
            'abcTotalProducts': '0',
            'abcValueA': 'S/. 0.00',
            'abcValueB': 'S/. 0.00',
            'abcValueC': 'S/. 0.00',
            'abcTotalValue': 'S/. 0.00'
        };

        Object.entries(elementos).forEach(([id, valor]) => {
            const elemento = document.getElementById(id);
            if (elemento) elemento.textContent = valor;
        });

        const tablaTbody = document.querySelector('#abcProductsTable tbody');
        if (tablaTbody) {
            tablaTbody.innerHTML = `
                <tr>
                    <td colspan="6" class="empty-message">
                        No hay datos disponibles para el análisis ABC.
                        <p>Asegúrate de tener productos y ventas registradas.</p>
                        <button onclick="cargarClasificacionABC(true)" class="button button-primary">
                            <i class="fas fa-sync-alt"></i> Intentar nuevamente
                        </button>
                    </td>
                </tr>
            `;
        }

        // Actualizar los gráficos con datos vacíos
        if (abcDistributionChart) {
            abcDistributionChart.data.datasets[0].data = [0, 0, 0];
            abcDistributionChart.update();
        }
        
        if (abcValueChart) {
            abcValueChart.data.datasets[0].data = [0, 0, 0];
            abcValueChart.update();
        }

        showToast('Información', 'No hay datos disponibles para el análisis ABC', 'info');
    }

    /**
     * Actualiza los gráficos de clasificación ABC
     * @param {Object} data - Datos de clasificación ABC
     */
    function actualizarGraficosABC(data) {
        console.log('Actualizando gráficos ABC con datos');
        
        if (!data) {
            console.error('No hay datos para actualizar los gráficos ABC');
            return;
        }
        
        const { conteoCategoria = {}, valorCategoria = {} } = data;
        
        // Preparar datos para los gráficos
        const labels = ['Categoría A', 'Categoría B', 'Categoría C'];
        
        // Datos para el gráfico de distribución de productos
        const datosConteo = [
            conteoCategoria.A || 0,
            conteoCategoria.B || 0,
            conteoCategoria.C || 0
        ];
        
        // Datos para el gráfico de valor por categoría
        const datosValor = [
            valorCategoria.A || 0,
            valorCategoria.B || 0,
            valorCategoria.C || 0
        ];
        
        // Colores para las categorías
        const backgroundColors = [
            'rgba(59, 130, 246, 0.7)',   // Azul para A
            'rgba(16, 185, 129, 0.7)',    // Verde para B
            'rgba(245, 158, 11, 0.7)'     // Naranja para C
        ];
        
        const borderColors = [
            'rgba(59, 130, 246, 1)',
            'rgba(16, 185, 129, 1)',
            'rgba(245, 158, 11, 1)'
        ];

        // Calcular porcentajes para las etiquetas
        const totalProductos = datosConteo.reduce((a, b) => a + b, 0);
        const totalValor = datosValor.reduce((a, b) => a + b, 0);
        
        const labelsConPorcentaje = labels.map((label, index) => {
            const porcentajeProductos = totalProductos > 0 ? 
                ((datosConteo[index] / totalProductos) * 100).toFixed(1) : 0;
            return `${label} (${porcentajeProductos}%)`;
        });
        
        // Actualizar gráfico de distribución de productos
        if (abcDistributionChart) {
            try {
                abcDistributionChart.data.labels = labelsConPorcentaje;
                abcDistributionChart.data.datasets[0].data = datosConteo;
                abcDistributionChart.data.datasets[0].backgroundColor = backgroundColors;
                abcDistributionChart.data.datasets[0].borderColor = borderColors;
                abcDistributionChart.options.plugins.tooltip = {
                    callbacks: {
                        label: function(context) {
                            const label = context.label || '';
                            const value = context.raw || 0;
                            const percentage = totalProductos > 0 ? 
                                ((value / totalProductos) * 100).toFixed(1) : 0;
                            return `${label.split(' (')[0]}: ${value} productos (${percentage}%)`;
                        }
                    }
                };
                abcDistributionChart.update();
                console.log('Gráfico de distribución ABC actualizado');
            } catch (error) {
                console.error('Error al actualizar gráfico de distribución ABC:', error);
            }
        } else {
            console.warn('El gráfico abcDistributionChart no está inicializado');
        }

        // Actualizar gráfico de valor por categoría
        if (abcValueChart) {
            try {
                // Crear etiquetas con porcentaje de valor
                const labelsConPorcentajeValor = labels.map((label, index) => {
                    const porcentajeValor = totalValor > 0 ? 
                        ((datosValor[index] / totalValor) * 100).toFixed(1) : 0;
                    return `${label} (${porcentajeValor}%)`;
                });
                
                abcValueChart.data.labels = labelsConPorcentajeValor;
                abcValueChart.data.datasets[0].data = datosValor;
                abcValueChart.data.datasets[0].backgroundColor = backgroundColors;
                abcValueChart.data.datasets[0].borderColor = borderColors;
                abcValueChart.options.plugins.tooltip = {
                    callbacks: {
                        label: function(context) {
                            const label = context.label || '';
                            const value = context.raw || 0;
                            const percentage = totalValor > 0 ? 
                                ((value / totalValor) * 100).toFixed(1) : 0;
                            return `${label.split(' (')[0]}: ${formatearMoneda(value)} (${percentage}%)`;
                        }
                    }
                };
                abcValueChart.update();
                console.log('Gráfico de valor ABC actualizado');
            } catch (error) {
                console.error('Error al actualizar gráfico de valor ABC:', error);
            }
        } else {
            console.warn('El gráfico abcValueChart no está inicializado');
        }
    }

    /**
     * Actualiza la tabla de productos clasificados ABC
     * @param {Object} data - Datos de clasificación ABC
     */
    function actualizarTablaABC(data) {
        console.log('Actualizando tabla ABC con datos');
        
        const tablaTbody = document.querySelector('#abcProductsTable tbody');
        if (!tablaTbody) {
            console.error('No se encontró la tabla ABC');
            return;
        }

        // Limpiar la tabla
        tablaTbody.innerHTML = '';
        
        // Verificar si hay productos
        const productos = data.productos || [];
        if (productos.length === 0) {
            tablaTbody.innerHTML = `
                <tr>
                    <td colspan="6" class="empty-message">
                        No hay productos clasificados en el análisis ABC.
                        <button onclick="cargarClasificacionABC(true)" class="button button-primary">
                            <i class="fas fa-sync-alt"></i> Actualizar datos
                        </button>
                    </td>
                </tr>
            `;
            return;
        }
        
        // Ordenar productos: primero por categoría (A, B, C) y luego por valor anual (descendente)
        const productosOrdenados = [...productos].sort((a, b) => {
            const catOrder = { 'A': 0, 'B': 1, 'C': 2 };
            const catDiff = catOrder[a.categoria] - catOrder[b.categoria];
            return catDiff !== 0 ? catDiff : b.valorAnual - a.valorAnual;
        });

        // Crear filas para cada producto
        productosOrdenados.forEach(producto => {
            if (!producto) return;

            // Formatear valores
            const valorAnual = formatearMoneda(producto.valorAnual);
            const porcentajeValor = formatearPorcentaje(producto.porcentajeValor);
            const porcentajeAcumulado = formatearPorcentaje(producto.porcentajeAcumulado);
            
            // Determinar clase CSS para la categoría
            const categoriaClase = `categoria-${producto.categoria.toLowerCase()}`;
            
            // Crear fila
            const row = document.createElement('tr');
            row.innerHTML = `
                <td>${producto.codigo || 'N/A'}</td>
                <td>${producto.nombre || 'Sin nombre'}</td>
                <td><span class="badge ${categoriaClase}">${producto.categoria}</span></td>
                <td class="text-right">${valorAnual}</td>
                <td class="text-right">${porcentajeValor}</td>
                <td class="text-right">${porcentajeAcumulado}</td>
            `;
            
            tablaTbody.appendChild(row);
        });
        
        // Si hay muchos productos, agregar un mensaje informativo
        if (productosOrdenados.length > 50) {
            const infoRow = document.createElement('tr');
            infoRow.innerHTML = `
                <td colspan="6" class="info-message">
                    <i class="fas fa-info-circle"></i> Mostrando ${productosOrdenados.length} productos clasificados.
                </td>
            `;
            tablaTbody.appendChild(infoRow);
    }
    }
    
    /**
     * Formatea un valor monetario
     * @param {number} valor - Valor a formatear
     * @returns {string} - Valor formateado como moneda
     */
    function formatearMoneda(valor) {
        if (valor === undefined || valor === null) return 'S/. 0.00';
        return 'S/. ' + parseFloat(valor).toFixed(2).replace(/\d(?=(\d{3})+\.)/g, '$&,');
    }
    
    /**
     * Formatea un porcentaje
     * @param {number} valor - Valor a formatear
     * @returns {string} - Valor formateado como porcentaje
     */
    function formatearPorcentaje(valor) {
        if (valor === undefined || valor === null) return '0.00%';
        return parseFloat(valor).toFixed(2) + '%';
    }

    /**
     * Actualiza el resumen de clasificación ABC
     * @param {Object} data - Datos de clasificación ABC
     */
    function actualizarResumenABC(data) {
        console.log('Actualizando resumen ABC con datos');
        
        if (!data) {
            console.error('No hay datos para actualizar el resumen ABC');
            return;
        }
        
        try {
            const { conteoCategoria = {}, valorCategoria = {}, productos = [] } = data;
        
            // Obtener conteos por categoría
            const countA = conteoCategoria.A || 0;
            const countB = conteoCategoria.B || 0;
            const countC = conteoCategoria.C || 0;
            const totalProductos = countA + countB + countC;
            
            // Obtener valores por categoría
            const valorA = valorCategoria.A || 0;
            const valorB = valorCategoria.B || 0;
            const valorC = valorCategoria.C || 0;
            const valorTotal = valorA + valorB + valorC;
            
            // Actualizar elementos del DOM con los valores
            document.getElementById('abcCountA').textContent = countA;
            document.getElementById('abcCountB').textContent = countB;
            document.getElementById('abcCountC').textContent = countC;
            document.getElementById('abcTotalProducts').textContent = totalProductos;
            
            document.getElementById('abcValueA').textContent = formatearMoneda(valorA);
            document.getElementById('abcValueB').textContent = formatearMoneda(valorB);
            document.getElementById('abcValueC').textContent = formatearMoneda(valorC);
            document.getElementById('abcTotalValue').textContent = formatearMoneda(valorTotal);

            // Agregar porcentajes si hay productos
            if (totalProductos > 0) {
                const porcentajeA = (countA / totalProductos * 100).toFixed(1);
                const porcentajeB = (countB / totalProductos * 100).toFixed(1);
                const porcentajeC = (countC / totalProductos * 100).toFixed(1);
                
                // Crear elementos para mostrar porcentajes
                const countAElement = document.getElementById('abcCountA');
                const countBElement = document.getElementById('abcCountB');
                const countCElement = document.getElementById('abcCountC');
                
                if (countAElement) countAElement.innerHTML = `${countA} <small>(${porcentajeA}%)</small>`;
                if (countBElement) countBElement.innerHTML = `${countB} <small>(${porcentajeB}%)</small>`;
                if (countCElement) countCElement.innerHTML = `${countC} <small>(${porcentajeC}%)</small>`;
        }

            // Agregar porcentajes de valor si hay valor total
            if (valorTotal > 0) {
                const porcentajeValorA = (valorA / valorTotal * 100).toFixed(1);
                const porcentajeValorB = (valorB / valorTotal * 100).toFixed(1);
                const porcentajeValorC = (valorC / valorTotal * 100).toFixed(1);
                
                // Crear elementos para mostrar porcentajes de valor
                const valueAElement = document.getElementById('abcValueA');
                const valueBElement = document.getElementById('abcValueB');
                const valueCElement = document.getElementById('abcValueC');
                
                if (valueAElement) valueAElement.innerHTML = `${formatearMoneda(valorA)} <small>(${porcentajeValorA}%)</small>`;
                if (valueBElement) valueBElement.innerHTML = `${formatearMoneda(valorB)} <small>(${porcentajeValorB}%)</small>`;
                if (valueCElement) valueCElement.innerHTML = `${formatearMoneda(valorC)} <small>(${porcentajeValorC}%)</small>`;
            }
            
            console.log('Resumen ABC actualizado correctamente');
        } catch (error) {
            console.error('Error al actualizar resumen ABC:', error);
        }
    }

    // Exponer funciones para uso global
    window.updateCharts = updateCharts;
    window.cargarClasificacionABC = cargarClasificacionABC;
    window.exportarABC = exportarABC;
    window.switchTab = switchTab;
    window.showToast = showToast;
});

/**
 * Exporta los datos de análisis ABC a Excel o PDF
 * @param {string} formato - Formato de exportación ('excel' o 'pdf')
 */
function exportarABC(formato) {
    console.log(`Exportando datos ABC a ${formato}`);
    
    // Verificar si hay datos para exportar
    if (!window.abcDataCache || !window.abcDataCache.productos || window.abcDataCache.productos.length === 0) {
        showToast('Error', 'No hay datos disponibles para exportar', 'error');
        return;
    }
    
    try {
        // Construir la URL de exportación
        const fechaActual = new Date().toISOString().split('T')[0];
        const url = `/api/reportes/exportar-${formato === 'excel' ? 'excel' : 'pdf'}?seccion=abc`;
        
        // Mostrar mensaje de progreso
        showToast('Exportando', `Generando archivo ${formato.toUpperCase()}...`, 'info');
        
        // Crear un enlace temporal para la descarga
        const link = document.createElement('a');
        link.href = url;
        link.target = '_blank';
        link.download = `analisis-abc-${fechaActual}.${formato === 'excel' ? 'xlsx' : 'pdf'}`;
        
        // Simular clic para iniciar la descarga
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
        
        // Mostrar mensaje de éxito después de un breve retraso
        setTimeout(() => {
            showToast('Éxito', `Datos exportados a ${formato.toUpperCase()} correctamente`, 'success');
        }, 2000);
    } catch (error) {
        console.error(`Error al exportar datos a ${formato}:`, error);
        showToast('Error', `No se pudo exportar a ${formato.toUpperCase()}: ${error.message}`, 'error');
    }
}

/**
 * Cambia entre las pestañas de reportes
 * @param {string} tabId - ID de la pestaña a mostrar
 */
function switchTab(tabId) {
    console.log(`Cambiando a pestaña: ${tabId}`);
    
    // Actualizar botones de pestañas
    document.querySelectorAll('.tab-button').forEach(button => {
        button.classList.remove('active');
    });
    document.querySelector(`.tab-button[data-tab="${tabId}"]`).classList.add('active');
    
    // Actualizar contenido de pestaña
    document.querySelectorAll('.tab-content').forEach(content => {
        content.classList.remove('active');
    });
    document.getElementById(`${tabId}-tab`).classList.add('active');
    
    // Actualizar gráficos para la pestaña seleccionada
    updateCharts(tabId);
}