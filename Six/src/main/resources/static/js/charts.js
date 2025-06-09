document.addEventListener('DOMContentLoaded', function() {
    // Datos para los gráficos (se actualizarán desde la API)
    const salesData = {
        daily: {
            labels: ['Lun', 'Mar', 'Mié', 'Jue', 'Vie', 'Sáb', 'Dom'],
            values: [0, 0, 0, 0, 0, 0, 0]
        },
        monthly: {
            labels: ['Ene', 'Feb', 'Mar', 'Abr', 'May', 'Jun', 'Jul', 'Ago', 'Sep', 'Oct', 'Nov', 'Dic'],
            values: [0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0]
        },
        yearly: {
            labels: ['2020', '2021', '2022', '2023', '2024'],
            values: [0, 0, 0, 0, 0]
        }
    };
    
    const inventoryData = {
        labels: ['Camisetas', 'Pantalones', 'Vestidos', 'Chaquetas', 'Accesorios'],
        values: [0, 0, 0, 0, 0]
    };
    
    // Colores para los gráficos
    const colors = {
        primary: '#071b7e',
        success: '#10b981',
        warning: '#f59e0b',
        danger: '#ef4444',
        info: '#6a28f8',
        secondary: '#000000'
    };
    
    // Configuración de los gráficos
    let salesChart = null;
    let inventoryChart = null;
    
    // Función para cargar los datos de ventas desde la API
    function cargarDatosVentas() {
        fetch('/api/ventas/ventas-por-periodo')
            .then(response => {
                if (!response.ok) {
                    throw new Error('Error al cargar los datos de ventas');
                }
                return response.json();
            })
            .then(data => {
                // Actualizar los datos de ventas con los obtenidos de la API
                if (data.daily) {
                    salesData.daily.labels = data.daily.labels;
                    salesData.daily.values = data.daily.values;
                }
                
                if (data.monthly) {
                    salesData.monthly.labels = data.monthly.labels;
                    salesData.monthly.values = data.monthly.values;
                }
                
                if (data.yearly) {
                    salesData.yearly.labels = data.yearly.labels;
                    salesData.yearly.values = data.yearly.values;
                }
                
                // Si el gráfico ya está inicializado, actualizarlo con los nuevos datos
                if (salesChart) {
                    // Determinar qué periodo está activo actualmente
                    const activePeriodButton = document.querySelector('.chart-period-button.active');
                    const activePeriod = activePeriodButton ? activePeriodButton.dataset.period : 'daily';
                    
                    // Actualizar el gráfico con los datos del periodo activo
                    salesChart.data.labels = salesData[activePeriod].labels;
                    salesChart.data.datasets[0].data = salesData[activePeriod].values;
                    salesChart.update();
                }
            })
            .catch(error => {
                console.error('Error al cargar los datos de ventas:', error);
                // Mostrar mensaje de error (opcional)
            });
    }
    
    // Función para cargar los datos de distribución de inventario desde la API
    function cargarDatosInventario() {
        fetch('/api/inventario/distribucion-por-categoria')
            .then(response => {
                if (!response.ok) {
                    throw new Error('Error al cargar los datos de inventario');
                }
                return response.json();
            })
            .then(data => {
                // Actualizar los datos de inventario con los obtenidos de la API
                if (data.labels && data.values) {
                    inventoryData.labels = data.labels;
                    inventoryData.values = data.values;
                }
                
                // Si el gráfico ya está inicializado, actualizarlo con los nuevos datos
                if (inventoryChart) {
                    inventoryChart.data.labels = inventoryData.labels;
                    inventoryChart.data.datasets[0].data = inventoryData.values;
                    inventoryChart.update();
                }
            })
            .catch(error => {
                console.error('Error al cargar los datos de inventario:', error);
                // Mostrar mensaje de error (opcional)
            });
    }
    
    // Inicializar gráfico de ventas
    function initSalesChart() {
        const ctx = document.getElementById('salesChart').getContext('2d');
        
        salesChart = new Chart(ctx, {
            type: 'bar',
            data: {
                labels: salesData.daily.labels,
                datasets: [{
                    label: 'Ventas',
                    data: salesData.daily.values,
                    backgroundColor: colors.primary,
                    borderColor: colors.primary,
                    borderWidth: 1,
                    borderRadius: 4
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
                                return 'S/ ' + value;
                            }
                        }
                    }
                },
                plugins: {
                    tooltip: {
                        callbacks: {
                            label: function(context) {
                                return 'S/ ' + context.raw;
                            }
                        }
                    }
                }
            }
        });
        
        // Cargar los datos reales después de inicializar el gráfico
        cargarDatosVentas();
    }
    
    // Actualizar gráfico de ventas según el período seleccionado
    window.updateSalesChart = function(period) {
        if (!salesChart) return;
        
        salesChart.data.labels = salesData[period].labels;
        salesChart.data.datasets[0].data = salesData[period].values;
        salesChart.update();
    };
    
    // Inicializar gráfico de inventario
    function initInventoryChart() {
        const ctx = document.getElementById('inventoryChart').getContext('2d');
        
        inventoryChart = new Chart(ctx, {
            type: 'pie',
            data: {
                labels: inventoryData.labels,
                datasets: [{
                    data: inventoryData.values,
                    backgroundColor: [
                        colors.primary,
                        colors.success,
                        colors.warning,
                        colors.danger,
                        colors.info
                    ],
                    borderWidth: 1
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: {
                        position: 'bottom'
                    },
                    tooltip: {
                        callbacks: {
                            label: function(context) {
                                const label = context.label || '';
                                const value = context.raw || 0;
                                const total = context.dataset.data.reduce((a, b) => a + b, 0);
                                const percentage = Math.round((value / total) * 100);
                                return `${label}: ${value} unidades (${percentage}%)`;
                            }
                        }
                    }
                }
            }
        });
        
        // Cargar los datos reales después de inicializar el gráfico
        cargarDatosInventario();
    }
    
    // Inicializar todos los gráficos
    function initCharts() {
        if (document.getElementById('salesChart')) {
            initSalesChart();
        }
        
        if (document.getElementById('inventoryChart')) {
            initInventoryChart();
        }
    }
    
    // Inicializar gráficos
    initCharts();
    
    // Configurar los botones de cambio de periodo
    document.querySelectorAll('.chart-period-button').forEach(button => {
        button.addEventListener('click', function() {
            // Remover clase active de todos los botones
            document.querySelectorAll('.chart-period-button').forEach(btn => {
                btn.classList.remove('active');
            });
            
            // Agregar clase active al botón clickeado
            this.classList.add('active');
            
            // Actualizar el gráfico con el periodo seleccionado
            const period = this.dataset.period;
            updateSalesChart(period);
        });
    });
    
    // Manejar cambio de tamaño de ventana
    window.addEventListener('resize', function() {
        if (salesChart) salesChart.resize();
        if (inventoryChart) inventoryChart.resize();
    });
});