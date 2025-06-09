document.addEventListener('DOMContentLoaded', function() {
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

    // Mostrar inicial del usuario
    if (userInitial) {
        const usuario = userInitial.getAttribute('data-usuario') || 'A';
        userInitial.textContent = usuario.charAt(0).toUpperCase();
    }

    // Función para mostrar toast
    function showToast(title, message, type = 'success') {
        const toast = document.getElementById('toast');
        const toastTitle = toast.querySelector('.toast-title');
        const toastMessage = toast.querySelector('.toast-message');

        toastTitle.textContent = title;
        toastMessage.textContent = message;

        toast.classList.remove('hidden', 'success', 'error');
        toast.classList.add(type);

        // Ocultar el toast después de 5 segundos
        setTimeout(() => {
            toast.classList.add('hidden');
        }, 5000);
    }

    // Cerrar toast al hacer clic en el botón de cerrar
    document.querySelector('.toast-close').addEventListener('click', function() {
        document.getElementById('toast').classList.add('hidden');
    });

    // Manejar clic en el botón de menú móvil
    mobileMenuButton.addEventListener('click', function() {
        sidebar.classList.toggle('open');
    });

    // Cerrar el sidebar al hacer clic fuera de él en dispositivos móviles
    document.addEventListener('click', function(event) {
        const isMobile = window.innerWidth <= 768;
        const isClickInsideSidebar = sidebar.contains(event.target);
        const isClickOnMenuButton = mobileMenuButton.contains(event.target);

        if (isMobile && sidebar.classList.contains('open') && !isClickInsideSidebar && !isClickOnMenuButton) {
            sidebar.classList.remove('open');
        }
    });

    // Funciones para la interfaz de reportes

    // Cambiar entre pestañas
    tabButtons.forEach(button => {
        button.addEventListener('click', function() {
            const tabId = this.dataset.tab;

            // Desactivar todas las pestañas
            tabButtons.forEach(btn => btn.classList.remove('active'));
            tabContents.forEach(content => content.classList.remove('active'));

            // Activar la pestaña seleccionada
            this.classList.add('active');
            document.getElementById(`${tabId}-tab`).classList.add('active');

            // Actualizar gráficos
            updateCharts(tabId);
        });
    });

    // Inicializar fechas
    function initializeDates() {
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
    }

    // Aplicar rango de fechas
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

    // Función para cargar datos de ventas por período
    function cargarVentasPorPeriodo() {
        // Construir parámetros de fecha si están disponibles
        let params = '';
        if (startDate.value && endDate.value) {
            const startISO = new Date(startDate.value + 'T00:00:00').toISOString();
            const endISO = new Date(endDate.value + 'T23:59:59').toISOString();
            params = `?desde=${encodeURIComponent(startISO)}&hasta=${encodeURIComponent(endISO)}`;
        }

        fetch(`/api/reportes/ventas-por-periodo${params}`)
            .then(response => {
                if (!response.ok) {
                    throw new Error('Error al cargar los datos de ventas por período');
                }
                return response.json();
            })
            .then(data => {
                if (salesChart) {
                    // Actualizar datos del gráfico
                    salesChart.data.labels = data.labels;
                    salesChart.data.datasets[0].data = data.values;
                    salesChart.update();
                }
            })
            .catch(error => {
                console.error('Error al cargar los datos de ventas por período:', error);
                showToast('Error', 'No se pudieron cargar los datos de ventas por período', 'error');
            });
    }

    // Función para cargar datos de ventas por categoría
    function cargarVentasPorCategoria() {
        // Construir parámetros de fecha si están disponibles
        let params = '';
        if (startDate.value && endDate.value) {
            const startISO = new Date(startDate.value + 'T00:00:00').toISOString();
            const endISO = new Date(endDate.value + 'T23:59:59').toISOString();
            params = `?desde=${encodeURIComponent(startISO)}&hasta=${encodeURIComponent(endISO)}`;
        }

        fetch(`/api/reportes/ventas-por-categoria${params}`)
            .then(response => {
                if (!response.ok) {
                    throw new Error('Error al cargar los datos de ventas por categoría');
                }
                return response.json();
            })
            .then(data => {
                if (categorySalesChart) {
                    // Actualizar datos del gráfico
                    categorySalesChart.data.labels = data.labels;
                    categorySalesChart.data.datasets[0].data = data.values;
                    categorySalesChart.update();
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

    // Inicializar gráficos
    function initializeCharts() {
        // Gráfico de ventas por período
        const salesChartCtx = document.getElementById('salesChart').getContext('2d');
        salesChart = new Chart(salesChartCtx, {
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

        // Gráfico de ventas por categoría
        const categorySalesChartCtx = document.getElementById('categorySalesChart').getContext('2d');
        categorySalesChart = new Chart(categorySalesChartCtx, {
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

        // Inicializar los gráficos de productos
        initializeRemainingCharts();
        
        // Cargar datos reales
        cargarVentasPorPeriodo();
        cargarVentasPorCategoria();
        cargarProductosMasVendidos();
        cargarResumenVentas();
        
        // Cargar datos de inventario
        cargarValorInventarioPorCategoria();
        cargarRotacionInventario();
        cargarProductosBajoStock();
        cargarResumenInventario();
    }
    
    // Inicializar los gráficos restantes
    function initializeRemainingCharts() {
        // Estos gráficos no se conectarán a la API en esta primera implementación
        // pero se mantienen para no romper la funcionalidad existente
        
        // Gráfico de valor de inventario por categoría
        const inventoryValueChartCtx = document.getElementById('inventoryValueChart');
        if (inventoryValueChartCtx) {
            inventoryValueChart = new Chart(inventoryValueChartCtx.getContext('2d'), {
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
        const inventoryTurnoverChartCtx = document.getElementById('inventoryTurnoverChart');
        if (inventoryTurnoverChartCtx) {
            inventoryTurnoverChart = new Chart(inventoryTurnoverChartCtx.getContext('2d'), {
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
        const categoryPerformanceCtx = document.getElementById('categoryPerformanceChart');
        categoryPerformanceChart = new Chart(categoryPerformanceCtx, {
            type: 'bar',
            data: {
                labels: [],
                datasets: [
                    {
                        label: 'Ingresos (S/.)',
                        data: [],
                        backgroundColor: 'rgba(66, 135, 245, 0.7)',
                        borderColor: 'rgba(66, 135, 245, 1)',
                        borderWidth: 1,
                        yAxisID: 'y'
                    },
                    {
                        label: 'Unidades Vendidas',
                        data: [],
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
                },
                plugins: {
                    title: {
                        display: true,
                        text: 'Rendimiento por Categoría'
                    },
                    legend: {
                        position: 'bottom'
                    }
                }
            }
        });

        // Gráfico de margen por producto
        const productMarginCtx = document.getElementById('productMarginChart');
        productMarginChart = new Chart(productMarginCtx, {
            type: 'bar',
            data: {
                labels: [],
                datasets: [{
                    label: 'Margen (%)',
                    data: [],
                    backgroundColor: 'rgba(255, 159, 64, 0.7)',
                    borderColor: 'rgba(255, 159, 64, 1)',
                    borderWidth: 1
                }]
            },
            options: {
                indexAxis: 'y', // Esto hace que el gráfico sea horizontal
                responsive: true,
                maintainAspectRatio: false,
                scales: {
                    x: {
                        beginAtZero: true,
                        max: 100
                    }
                },
                plugins: {
                    title: {
                        display: true,
                        text: 'Margen por Producto (%)'
                    },
                    legend: {
                        display: false
                    }
                }
            }
        });
    }

    // Actualizar gráficos según la pestaña seleccionada
    function updateCharts(tabId) {
        if (tabId === 'sales') {
            cargarVentasPorPeriodo();
            cargarVentasPorCategoria();
            cargarProductosMasVendidos();
            cargarResumenVentas();
        } else if (tabId === 'inventory') {
            cargarValorInventarioPorCategoria();
            cargarRotacionInventario();
            cargarProductosBajoStock();
            cargarResumenInventario();
        } else if (tabId === 'products') {
            cargarRendimientoProductos();
            cargarTendenciasProductos();
            cargarDistribucionProductosCategoria();
        }
    }

    // Actualizar todos los gráficos con el nuevo rango de fechas
    function updateAllCharts() {
        // Cargar datos para todos los gráficos
        cargarVentasPorPeriodo();
        cargarVentasPorCategoria();
        cargarProductosMasVendidos();
        cargarResumenVentas();
        
        cargarValorInventarioPorCategoria();
        cargarRotacionInventario();
        cargarProductosBajoStock();
        cargarResumenInventario();
        
        cargarRendimientoProductos();
        cargarTendenciasProductos();
        cargarDistribucionProductosCategoria();
    }

    // Inicializar la aplicación
    function initialize() {
        // Establecer las fechas predeterminadas
        initializeDates();
        
        // Inicializar los gráficos
        initializeCharts();
        
        // Cargar los datos iniciales
        updateAllCharts();
        
        // Inicializar los elementos interactivos
        // Ya están inicializados en el código anterior (eventos, etc.)
    }
    
    // Iniciar la aplicación
    initialize();
});