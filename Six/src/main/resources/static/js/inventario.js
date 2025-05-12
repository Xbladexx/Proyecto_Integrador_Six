document.addEventListener('DOMContentLoaded', function() {
    // Referencias a elementos del DOM
    const sidebar = document.querySelector('.sidebar');
    const mobileMenuButton = document.querySelector('.mobile-menu-button');
    const inventorySearch = document.getElementById('inventorySearch');
    const inventoryTableBody = document.getElementById('inventoryTableBody');
    const toastCloseButton = document.querySelector('.toast-close');
    const stockModal = document.getElementById('stockModal');
    const stockModalTitle = document.getElementById('stockModalTitle');
    const stockProductInfo = document.getElementById('stockProductInfo');
    const stockProductId = document.getElementById('stockProductId');
    const stockAction = document.getElementById('stockAction');
    const stockQuantity = document.getElementById('stockQuantity');
    const stockReason = document.getElementById('stockReason');
    const otherReasonGroup = document.getElementById('otherReasonGroup');
    const otherReason = document.getElementById('otherReason');
    const btnSaveStock = document.getElementById('btnSaveStock');
    const modalCloseButtons = document.querySelectorAll('.modal-close, .modal-close-btn');
    const inventarioTable = document.getElementById('inventarioTable');
    const searchInput = document.getElementById('searchInventario');
    const stockFilter = document.getElementById('stockFilter');
    let inventarioData = [];

    // Verificar si estamos en la vista de administrador
    let esAdmin = false;
    const esAdminElement = document.getElementById('esAdminFlag');
    if (esAdminElement) {
        esAdmin = esAdminElement.getAttribute('data-es-admin') === 'true';
    } else {
        // Detectar por la estructura del menú como fallback
        esAdmin = document.querySelector('.sidebar-nav a[href*="dashboard-admin"]') !== null ||
            window.location.pathname.includes('/Administrador/') ||
            window.location.href.includes('/dashboard-admin') ||
            document.querySelector('.sidebar-nav a[href*="productos"]') !== null;
    }

    // Función para cargar los datos de inventario desde la API
    function cargarDatosInventario() {
        fetch('/api/inventario/detalles')
            .then(response => {
                if (!response.ok) {
                    throw new Error('Error al cargar los datos del inventario');
                }
                return response.json();
            })
            .then(data => {
                inventarioData = data; // Guardamos los datos para búsquedas y filtrado
                loadInventoryData(data);
            })
            .catch(error => {
                console.error('Error:', error);
                showToast('Error', 'No se pudieron cargar los datos del inventario', 'error');
                // Si hay un error, mostramos datos vacíos
                loadInventoryData([]);
            });
    }

    // Función para mostrar toast
    window.showToast = function(title, message, type = 'success') {
        const toast = document.getElementById('toast');
        if (!toast) return;

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
    };

    // Cerrar toast al hacer clic en el botón de cerrar
    if (toastCloseButton) {
        toastCloseButton.addEventListener('click', function() {
            const toast = document.getElementById('toast');
            if (toast) toast.classList.add('hidden');
        });
    }

    // Manejar clic en el botón de menú móvil
    if (mobileMenuButton) {
        mobileMenuButton.addEventListener('click', function() {
            sidebar.classList.toggle('open');
        });
    }

    // Función para cerrar todos los menús de acciones abiertos
    function closeAllActionMenus() {
        document.querySelectorAll('.actions-menu.show').forEach(menu => {
            menu.classList.remove('show');
        });
    }

    // Agregar evento para cerrar menús al hacer clic en cualquier parte
    document.addEventListener('click', function(event) {
        // Si el clic no fue en un botón de toggle o dentro de un menú, cerrar todos los menús
        if (!event.target.closest('.actions-toggle') && !event.target.closest('.actions-menu')) {
            closeAllActionMenus();
        }
    });

    // Función para cargar los datos del inventario
    function loadInventoryData(data) {
        // Limpiar el cuerpo de la tabla
        inventoryTableBody.innerHTML = '';

        // Si no hay datos, mostrar mensaje
        if (!data || data.length === 0) {
            const emptyRow = document.createElement('tr');
            const emptyCell = document.createElement('td');
            emptyCell.setAttribute('colspan', esAdmin ? '8' : '7');
            emptyCell.textContent = 'No hay productos para mostrar';
            emptyCell.className = 'empty-data';
            emptyRow.appendChild(emptyCell);
            inventoryTableBody.appendChild(emptyRow);
            return;
        }

        // Agregar filas de datos
        data.forEach(item => {
            const row = document.createElement('tr');

            // Agregar celdas con los datos del producto
            const codeCell = document.createElement('td');
            codeCell.textContent = item.sku || '';
            row.appendChild(codeCell);

            const nameCell = document.createElement('td');
            nameCell.textContent = item.nombreProducto || '';
            row.appendChild(nameCell);

            const categoryCell = document.createElement('td');
            categoryCell.textContent = item.nombreCategoria || '';
            row.appendChild(categoryCell);

            const colorCell = document.createElement('td');
            colorCell.textContent = item.color || '';
            row.appendChild(colorCell);

            const sizeCell = document.createElement('td');
            sizeCell.textContent = item.talla || '';
            row.appendChild(sizeCell);

            const stockCell = document.createElement('td');
            stockCell.classList.add('stock-column');
            // Crear un elemento span para el círculo del stock
            const stockSpan = document.createElement('span');
            stockSpan.textContent = item.stock || 0;
            stockSpan.classList.add('stock-badge');

            // Aplicar diferentes clases según el nivel de stock
            if (item.stock <= 3) {
                stockSpan.classList.add('stock-critical'); // Rojo
            } else if (item.stock <= 10) {
                stockSpan.classList.add('stock-warning'); // Amarillo
            } else {
                stockSpan.classList.add('stock-normal'); // Verde
            }

            stockCell.appendChild(stockSpan);
            row.appendChild(stockCell);

            const priceCell = document.createElement('td');
            priceCell.textContent = `S/. ${item.precio ? item.precio.toFixed(2) : '0.00'}`;
            row.appendChild(priceCell);

            // Solo añadir la columna de acciones si es administrador
            if (esAdmin) {
                const actionsCell = document.createElement('td');
                actionsCell.classList.add('actions-cell');

                // Crear el dropdown con los tres puntos (...)
                const actionsDropdown = document.createElement('div');
                actionsDropdown.classList.add('actions-dropdown');

                // Botón de tres puntos
                const toggleButton = document.createElement('button');
                toggleButton.classList.add('actions-toggle');
                toggleButton.innerHTML = '...';
                toggleButton.addEventListener('click', function(e) {
                    e.stopPropagation(); // Evitar que el clic se propague
                    closeAllActionMenus();
                    const menu = this.nextElementSibling;
                    menu.classList.toggle('show');
                });

                // Menú de acciones
                const actionsMenu = document.createElement('div');
                actionsMenu.classList.add('actions-menu');

                // Opción: Añadir stock
                const addStockLink = document.createElement('a');
                addStockLink.href = '#';
                addStockLink.innerHTML = '<i class="fas fa-plus-circle"></i> Añadir stock';
                addStockLink.addEventListener('click', function(e) {
                    e.preventDefault();
                    openStockModal(item, 'add');
                });

                // Opción: Reducir stock
                const reduceStockLink = document.createElement('a');
                reduceStockLink.href = '#';
                reduceStockLink.innerHTML = '<i class="fas fa-minus-circle"></i> Reducir stock';
                reduceStockLink.addEventListener('click', function(e) {
                    e.preventDefault();
                    openStockModal(item, 'reduce');
                });

                // Agregar opciones al menú
                actionsMenu.appendChild(addStockLink);
                actionsMenu.appendChild(reduceStockLink);

                // Añadir el botón y el menú al dropdown
                actionsDropdown.appendChild(toggleButton);
                actionsDropdown.appendChild(actionsMenu);

                // Añadir el dropdown a la celda de acciones
                actionsCell.appendChild(actionsDropdown);
                row.appendChild(actionsCell);
            }

            // Agregar la fila a la tabla
            inventoryTableBody.appendChild(row);
        });
    }

    // Función para abrir el modal de gestión de stock
    function openStockModal(product, action) {
        // Establecer el título según la acción
        stockModalTitle.textContent = action === 'add' ? 'Añadir Stock' : 'Reducir Stock';

        // Configurar la información del producto
        stockProductInfo.value = `${product.nombreProducto} - ${product.color} - ${product.talla}`;
        stockProductId.value = product.id;

        // Configurar la acción (añadir o reducir)
        stockAction.value = action;

        // Mostrar el modal
        stockModal.style.display = 'block';
    }

    // Función para cerrar el modal de stock
    function closeStockModal() {
        stockModal.style.display = 'none';
        // Resetear formulario
        document.getElementById('stockForm').reset();
    }

    // Manejar evento de cambio en el motivo del movimiento
    if (stockReason) {
        stockReason.addEventListener('change', function() {
            otherReasonGroup.style.display = this.value === 'other' ? 'block' : 'none';
        });
    }

    // Manejar botones para cerrar modales
    modalCloseButtons.forEach(button => {
        button.addEventListener('click', function() {
            closeStockModal();
        });
    });

    // Manejar el botón de guardar en el modal de stock
    if (btnSaveStock) {
        btnSaveStock.addEventListener('click', function() {
            const productId = stockProductId.value;
            const action = stockAction.value;
            const quantity = parseInt(stockQuantity.value, 10);
            const reason = stockReason.value;
            const reasonDetail = reason === 'other' ? otherReason.value : '';

            // Validar cantidad
            if (isNaN(quantity) || quantity <= 0) {
                showToast('Error', 'La cantidad debe ser un número mayor que cero', 'error');
                return;
            }

            // Validar motivo personalizado
            if (reason === 'other' && !reasonDetail.trim()) {
                showToast('Error', 'Debe especificar el motivo', 'error');
                return;
            }

            // Simular llamada a la API (en un sistema real, esto enviaría una solicitud al servidor)
            // Ejemplo: POST a /api/inventario/{productId}/stock con los datos necesarios

            // Aquí deberíamos tener el código para enviar una solicitud al servidor
            // Por ahora, simularemos una respuesta exitosa
            setTimeout(() => {
                showToast(
                    'Éxito',
                    `Stock ${action === 'add' ? 'aumentado' : 'reducido'} correctamente`,
                    'success'
                );
                closeStockModal();

                // Recargar datos del inventario para reflejar los cambios
                cargarDatosInventario();
            }, 500);
        });
    }

    // Manejar la búsqueda en la tabla de inventario
    if (inventorySearch) {
        inventorySearch.addEventListener('input', function() {
            const searchTerm = this.value.toLowerCase();
            const filteredData = inventarioData.filter(item =>
                (item.sku && item.sku.toLowerCase().includes(searchTerm)) ||
                (item.nombreProducto && item.nombreProducto.toLowerCase().includes(searchTerm)) ||
                (item.nombreCategoria && item.nombreCategoria.toLowerCase().includes(searchTerm)) ||
                (item.color && item.color.toLowerCase().includes(searchTerm)) ||
                (item.talla && item.talla.toLowerCase().includes(searchTerm))
            );
            loadInventoryData(filteredData);
        });
    }

    // Inicializar: cargar datos del inventario
    cargarDatosInventario();
});