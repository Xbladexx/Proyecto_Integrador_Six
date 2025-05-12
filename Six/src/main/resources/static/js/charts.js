document.addEventListener('DOMContentLoaded', function() {
    // Datos para los gráficos
    const salesData = {
        daily: {
            labels: ['Lun', 'Mar', 'Mié', 'Jue', 'Vie', 'Sáb', 'Dom'],
            values: [1200, 1500, 1800, 1200, 2200, 2500, 1900]
        },
        monthly: {
            labels: ['Ene', 'Feb', 'Mar', 'Abr', 'May', 'Jun', 'Jul', 'Ago', 'Sep', 'Oct', 'Nov', 'Dic'],
            values: [24000, 18000, 29800, 27908, 34800, 29800, 32300, 28400, 31200, 36500, 42000, 48000]
        },
        yearly: {
            labels: ['2020', '2021', '2022', '2023', '2024'],
            values: [240000, 320000, 380000, 450000, 520000]
        }
    };
    
    const inventoryData = {
        labels: ['Camisetas', 'Pantalones', 'Vestidos', 'Chaquetas', 'Accesorios'],
        values: [400, 300, 200, 150, 100]
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
    
    // Manejar cambio de tamaño de ventana
    window.addEventListener('resize', function() {
        if (salesChart) salesChart.resize();
        if (inventoryChart) inventoryChart.resize();
    });
});