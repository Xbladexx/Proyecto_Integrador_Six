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

        // Simular tiempo de procesamiento
        setTimeout(() => {
            showToast('Exportación completada', 'El reporte ha sido exportado a PDF correctamente');
        }, 1500);
    });

    // Exportar a Excel
    exportExcelButton.addEventListener('click', function() {
        showToast('Exportando', 'Generando archivo Excel...');

        // Simular tiempo de procesamiento
        setTimeout(() => {
            showToast('Exportación completada', 'El reporte ha sido exportado a Excel correctamente');
        }, 1500);
    });

    // Imprimir reporte
    printReportButton.addEventListener('click', function() {
        showToast('Imprimiendo', 'Preparando documento para imprimir...');

        // Simular tiempo de procesamiento
        setTimeout(() => {
            window.print();
        }, 1000);
    });

    // Inicializar gráficos
    function initializeCharts() {
        // Gráfico de ventas por período
        const salesChartCtx = document.getElementById('salesChart').getContext('2d');
        const salesChart = new Chart(salesChartCtx, {
            type: 'line',
            data: {
                labels: ['Ene', 'Feb', 'Mar', 'Abr', 'May', 'Jun'],
                datasets: [{
                    label: 'Ventas (S/.)',
                    data: [12500, 13200, 14800, 13900, 15200, 15555],
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
        const categorySalesChart = new Chart(categorySalesChartCtx, {
            type: 'doughnut',
            data: {
                labels: ['Camisetas', 'Pantalones', 'Vestidos', 'Blusas', 'Chaquetas'],
                datasets: [{
                    data: [3595.5, 3196.8, 3637.2, 2247.5, 2878.2],
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

        // Gráfico de valor de inventario por categoría
        const inventoryValueChartCtx = document.getElementById('inventoryValueChart').getContext('2d');
        const inventoryValueChart = new Chart(inventoryValueChartCtx, {
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

        // Gráfico de rotación de inventario
        const inventoryTurnoverChartCtx = document.getElementById('inventoryTurnoverChart').getContext('2d');
        const inventoryTurnoverChart = new Chart(inventoryTurnoverChartCtx, {
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

        // Gráfico de rendimiento por categoría
        const categoryPerformanceChartCtx = document.getElementById('categoryPerformanceChart').getContext('2d');
        const categoryPerformanceChart = new Chart(categoryPerformanceChartCtx, {
            type: 'radar',
            data: {
                labels: ['Ventas', 'Margen', 'Rotación', 'Crecimiento', 'Stock'],
                datasets: [{
                        label: 'Camisetas',
                        data: [85, 70, 90, 75, 80],
                        backgroundColor: 'rgba(59, 130, 246, 0.2)',
                        borderColor: 'rgba(59, 130, 246, 1)',
                        borderWidth: 2,
                        pointBackgroundColor: 'rgba(59, 130, 246, 1)'
                    },
                    {
                        label: 'Pantalones',
                        data: [75, 80, 70, 85, 75],
                        backgroundColor: 'rgba(16, 185, 129, 0.2)',
                        borderColor: 'rgba(16, 185, 129, 1)',
                        borderWidth: 2,
                        pointBackgroundColor: 'rgba(16, 185, 129, 1)'
                    },
                    {
                        label: 'Vestidos',
                        data: [90, 85, 65, 80, 70],
                        backgroundColor: 'rgba(245, 158, 11, 0.2)',
                        borderColor: 'rgba(245, 158, 11, 1)',
                        borderWidth: 2,
                        pointBackgroundColor: 'rgba(245, 158, 11, 1)'
                    }
                ]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                scales: {
                    r: {
                        angleLines: {
                            display: true
                        },
                        suggestedMin: 50,
                        suggestedMax: 100
                    }
                }
            }
        });

        // Gráfico de margen por producto
        const productMarginChartCtx = document.getElementById('productMarginChart').getContext('2d');
        const productMarginChart = new Chart(productMarginChartCtx, {
            type: 'bar',
            data: {
                labels: ['Camiseta Slim Fit', 'Pantalón Chino', 'Vestido Casual', 'Blusa Estampada', 'Chaqueta Denim'],
                datasets: [{
                        label: 'Precio (S/.)',
                        data: [79.9, 99.9, 129.9, 89.9, 159.9],
                        backgroundColor: 'rgba(59, 130, 246, 0.7)',
                        borderColor: 'rgba(59, 130, 246, 1)',
                        borderWidth: 1
                    },
                    {
                        label: 'Costo (S/.)',
                        data: [39.95, 49.95, 64.95, 44.95, 79.95],
                        backgroundColor: 'rgba(239, 68, 68, 0.7)',
                        borderColor: 'rgba(239, 68, 68, 1)',
                        borderWidth: 1
                    }
                ]
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
                                return context.dataset.label + ': S/. ' + context.raw.toLocaleString();
                            }
                        }
                    }
                }
            }
        });
    }

    // Actualizar gráficos según la pestaña seleccionada
    function updateCharts(tabId) {
        // Aquí iría la lógica para actualizar los gráficos según la pestaña
        // Por ahora, no hacemos nada ya que los gráficos ya están inicializados
    }

    // Actualizar todos los gráficos
    function updateAllCharts() {
        // Aquí iría la lógica para actualizar todos los gráficos con el nuevo rango de fechas
        // Por ahora, no hacemos nada ya que es solo una simulación
    }

    // Inicializar la interfaz
    initializeDates();
    initializeCharts();
});