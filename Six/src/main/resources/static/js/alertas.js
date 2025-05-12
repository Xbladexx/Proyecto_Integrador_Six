document.addEventListener('DOMContentLoaded', function() {
    // Referencias a elementos del DOM
    const sidebar = document.querySelector('.sidebar');
    const mobileMenuButton = document.querySelector('.mobile-menu-button');
    const userInitial = document.getElementById('userInitial');

    // Elementos de la interfaz de alertas
    const tabButtons = document.querySelectorAll('.tab-button');
    const tabContents = document.querySelectorAll('.tab-content');
    const settingsTabButtons = document.querySelectorAll('.settings-tab-button');
    const settingsTabContents = document.querySelectorAll('.settings-tab-content');
    const alertsSearch = document.getElementById('alertsSearch');
    const statusFilter = document.getElementById('statusFilter');
    const alertsTableBody = document.getElementById('alertsTableBody');

    // Elementos de configuración
    const enableLowStock = document.getElementById('enableLowStock');
    const lowStockThreshold = document.getElementById('lowStockThreshold');
    const criticalStockThreshold = document.getElementById('criticalStockThreshold');
    const enableAutoOrder = document.getElementById('enableAutoOrder');
    const autoOrderThreshold = document.getElementById('autoOrderThreshold');
    const defaultOrderQuantity = document.getElementById('defaultOrderQuantity');
    const saveStockSettings = document.getElementById('saveStockSettings');

    const emailNotifications = document.getElementById('emailNotifications');
    const emailRecipients = document.getElementById('emailRecipients');
    const dashboardNotifications = document.getElementById('dashboardNotifications');
    const dailyDigest = document.getElementById('dailyDigest');
    const criticalOnly = document.getElementById('criticalOnly');
    const saveNotificationSettings = document.getElementById('saveNotificationSettings');

    const saveCategorySettings = document.getElementById('saveCategorySettings');

    // Datos de ejemplo de alertas
    const alertsData = [{
            id: 1,
            productCode: "CAM-001-M-NEG",
            productName: "Camiseta Slim Fit",
            variant: "Negro, M",
            currentStock: 3,
            minStock: 5,
            status: "critical", // critical, low, resolved
            category: "Camisetas",
            lastUpdated: new Date(2023, 5, 15)
        },
        {
            id: 2,
            productCode: "VES-001-S-AZU",
            productName: "Vestido Casual",
            variant: "Azul, S",
            currentStock: 2,
            minStock: 5,
            status: "critical",
            category: "Vestidos",
            lastUpdated: new Date(2023, 5, 16)
        },
        {
            id: 3,
            productCode: "PAN-001-32-BEI",
            productName: "Pantalón Chino",
            variant: "Beige, 32",
            currentStock: 5,
            minStock: 8,
            status: "low",
            category: "Pantalones",
            lastUpdated: new Date(2023, 5, 17)
        },
        {
            id: 4,
            productCode: "CHA-001-XL-AZU",
            productName: "Chaqueta Denim",
            variant: "Azul, XL",
            currentStock: 4,
            minStock: 6,
            status: "low",
            category: "Chaquetas",
            lastUpdated: new Date(2023, 5, 18)
        },
        {
            id: 5,
            productCode: "CAM-002-S-ROJ",
            productName: "Camiseta Estampada",
            variant: "Rojo, S",
            currentStock: 7,
            minStock: 5,
            status: "resolved",
            category: "Camisetas",
            lastUpdated: new Date(2023, 5, 19)
        },
        {
            id: 6,
            productCode: "BLU-001-L-MUL",
            productName: "Blusa Estampada",
            variant: "Multicolor, L",
            currentStock: 6,
            minStock: 5,
            status: "resolved",
            category: "Blusas",
            lastUpdated: new Date(2023, 5, 20)
        }
    ];

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

    // Funciones para la interfaz de alertas

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
        });
    });

    // Cambiar entre pestañas de configuración
    settingsTabButtons.forEach(button => {
        button.addEventListener('click', function() {
            const tabId = this.dataset.settingsTab;

            // Desactivar todas las pestañas
            settingsTabButtons.forEach(btn => btn.classList.remove('active'));
            settingsTabContents.forEach(content => content.classList.remove('active'));

            // Activar la pestaña seleccionada
            this.classList.add('active');
            document.getElementById(`${tabId}-settings-tab`).classList.add('active');
        });
    });

    // Cargar datos de alertas
    function loadAlertsData(data) {
        alertsTableBody.innerHTML = '';

        if (data.length === 0) {
            const emptyRow = document.createElement('tr');
            emptyRow.innerHTML = `
                <td colspan="7" class="text-center">No se encontraron alertas.</td>
            `;
            alertsTableBody.appendChild(emptyRow);
            return;
        }

        data.forEach(alert => {
            const row = document.createElement('tr');

            // Determinar la clase y el icono según el estado
            let statusClass = '';
            let statusIcon = '';
            let statusText = '';

            if (alert.status === 'critical') {
                statusClass = 'status-critical';
                statusIcon = '<i class="fas fa-exclamation-triangle"></i>';
                statusText = 'Crítico';
            } else if (alert.status === 'low') {
                statusClass = 'status-low';
                statusIcon = '<i class="fas fa-arrow-down"></i>';
                statusText = 'Bajo';
            } else {
                statusClass = 'status-resolved';
                statusIcon = '<i class="fas fa-check-circle"></i>';
                statusText = 'Resuelto';
            }

            // Crear el contenido de la fila
            row.innerHTML = `
                <td><span class="status-badge ${statusClass}">${statusIcon} ${statusText}</span></td>
                <td>${alert.productCode}</td>
                <td>${alert.productName}</td>
                <td>${alert.variant}</td>
                <td class="${alert.status === 'critical' ? 'text-red-500 font-medium' : alert.status === 'low' ? 'text-amber-500 font-medium' : 'text-green-500 font-medium'}">${alert.currentStock}</td>
                <td>${alert.minStock}</td>
                <td>${alert.category}</td>
            `;

            alertsTableBody.appendChild(row);
        });
    }

    // Buscar alertas
    alertsSearch.addEventListener('input', function() {
        filterAlerts();
    });

    // Filtrar por estado
    statusFilter.addEventListener('change', function() {
        filterAlerts();
    });

    // Función para filtrar alertas
    function filterAlerts() {
        const searchTerm = alertsSearch.value.trim().toLowerCase();
        const statusValue = statusFilter.value;

        const filteredData = alertsData.filter(alert => {
            // Filtro por búsqueda
            const matchesSearch =
                alert.productCode.toLowerCase().includes(searchTerm) ||
                alert.productName.toLowerCase().includes(searchTerm) ||
                alert.variant.toLowerCase().includes(searchTerm) ||
                alert.category.toLowerCase().includes(searchTerm);

            // Filtro por estado
            const matchesStatus = statusValue === 'all' || alert.status === statusValue;

            return matchesSearch && matchesStatus;
        });

        loadAlertsData(filteredData);
    }

    // Habilitar/deshabilitar campos de pedido automático
    enableAutoOrder.addEventListener('change', function() {
        autoOrderThreshold.disabled = !this.checked;
        defaultOrderQuantity.disabled = !this.checked;
    });

    // Guardar configuración de stock
    saveStockSettings.addEventListener('click', function() {
        // Validar campos
        if (parseInt(lowStockThreshold.value) <= parseInt(criticalStockThreshold.value)) {
            showToast('Error', 'El umbral de stock bajo debe ser mayor que el umbral de stock crítico', 'error');
            return;
        }

        // Aquí iría la lógica para guardar la configuración
        showToast('Configuración guardada', 'La configuración de alertas ha sido actualizada correctamente');
    });

    // Guardar configuración de notificaciones
    saveNotificationSettings.addEventListener('click', function() {
        // Validar campos
        if (emailNotifications.checked && !emailRecipients.value) {
            showToast('Error', 'Debe ingresar al menos un destinatario de email', 'error');
            return;
        }

        // Aquí iría la lógica para guardar la configuración
        showToast('Configuración guardada', 'La configuración de notificaciones ha sido actualizada correctamente');
    });

    // Guardar configuración por categoría
    saveCategorySettings.addEventListener('click', function() {
        // Aquí iría la lógica para guardar la configuración
        showToast('Configuración guardada', 'La configuración por categoría ha sido actualizada correctamente');
    });

    // Inicializar la interfaz
    loadAlertsData(alertsData);
});