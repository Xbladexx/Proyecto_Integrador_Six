document.addEventListener('DOMContentLoaded', function() {
    // Referencias a elementos del DOM
    const sidebar = document.querySelector('.sidebar');
    const mobileMenuButton = document.querySelector('.mobile-menu-button');
    const inventorySearch = document.getElementById('inventorySearch');
    const inventoryTableBody = document.getElementById('inventoryTableBody');
    const toastCloseButton = document.querySelector('.toast-close');
    
    // Elementos para el modal de stock (solo en vista admin)
    const stockModal = document.getElementById('stockModal');
    const stockModalTitle = stockModal ? document.getElementById('stockModalTitle') : null;
    const stockProductInfo = stockModal ? document.getElementById('stockProductInfo') : null;
    const stockProductId = stockModal ? document.getElementById('stockProductId') : null;
    const stockAction = stockModal ? document.getElementById('stockAction') : null;
    const stockQuantity = stockModal ? document.getElementById('stockQuantity') : null;
    const stockReason = stockModal ? document.getElementById('stockReason') : null;
    const otherReasonGroup = stockModal ? document.getElementById('otherReasonGroup') : null;
    const otherReason = stockModal ? document.getElementById('otherReason') : null;
    const btnSaveStock = stockModal ? document.getElementById('btnSaveStock') : null;
    const modalCloseButtons = stockModal ? document.querySelectorAll('.modal-close, .modal-close-btn') : null;
    
    // Elementos para acciones de la tabla (solo en vista admin)
    const inventoryTable = document.getElementById('inventoryTable');
    const addStockButtons = document.querySelectorAll('.add-stock-action');
    const reduceStockButtons = document.querySelectorAll('.reduce-stock-action');

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

    // Función para mostrar toast (común para ambas vistas)
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

    // Cerrar toast al hacer clic en el botón de cerrar (común para ambas vistas)
    if (toastCloseButton) {
        toastCloseButton.addEventListener('click', function() {
            const toast = document.getElementById('toast');
            if (toast) toast.classList.add('hidden');
        });
    }

    // Manejar clic en el botón de menú móvil (común para ambas vistas)
    if (mobileMenuButton) {
        mobileMenuButton.addEventListener('click', function() {
            sidebar.classList.toggle('open');
        });
    }

    // FUNCIONALIDAD ESPECÍFICA PARA LA VISTA DE ADMINISTRADOR
    if (esAdmin && stockModal) {
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

        // Configurar los botones de toggle para los menús de acciones
        document.querySelectorAll('.actions-toggle').forEach(button => {
            button.addEventListener('click', function(e) {
                e.stopPropagation(); // Evitar que el clic se propague
                closeAllActionMenus();
                const menu = this.nextElementSibling;
                menu.classList.toggle('show');
            });
        });

        // Configurar los botones de añadir stock
        addStockButtons.forEach(button => {
            button.addEventListener('click', function(e) {
                e.preventDefault();
                const id = this.getAttribute('data-id');
                const nombre = this.getAttribute('data-nombre');
                const color = this.getAttribute('data-color');
                const talla = this.getAttribute('data-talla');
                openStockModal({
                    id: id,
                    nombreProducto: nombre,
                    color: color,
                    talla: talla
                }, 'add');
            });
        });

        // Configurar los botones de reducir stock
        reduceStockButtons.forEach(button => {
            button.addEventListener('click', function(e) {
                e.preventDefault();
                const id = this.getAttribute('data-id');
                const nombre = this.getAttribute('data-nombre');
                const color = this.getAttribute('data-color');
                const talla = this.getAttribute('data-talla');
                openStockModal({
                    id: id,
                    nombreProducto: nombre,
                    color: color,
                    talla: talla
                }, 'reduce');
            });
        });

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

                // Enviar solicitud al servidor para actualizar el stock
                const motivoParam = reason === 'other' ? reasonDetail : reason;
                // Obtener el ID de usuario de la sesión
                let usuarioId = 1; // Valor por defecto
                try {
                    const userElement = document.getElementById('userInitial');
                    if (userElement && userElement.getAttribute('data-usuario-id')) {
                        usuarioId = userElement.getAttribute('data-usuario-id');
                    }
                } catch (e) {
                    console.error('Error al obtener ID de usuario:', e);
                }
                
                const url = action === 'add' ? 
                    `/api/inventario/${productId}/aumentar?cantidad=${quantity}&motivo=${encodeURIComponent(motivoParam)}&usuarioId=${usuarioId}` : 
                    `/api/inventario/${productId}/disminuir?cantidad=${quantity}&motivo=${encodeURIComponent(motivoParam)}&usuarioId=${usuarioId}`;
                
                fetch(url, {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json'
                    }
                })
                .then(response => {
                    if (!response.ok) {
                        return response.text().then(text => {
                            throw new Error(text || 'Error al actualizar el stock');
                        });
                    }
                    // Primero intentamos obtener el texto de la respuesta
                    return response.text();
                })
                .then(text => {
                    // Si la respuesta está vacía, simplemente continuamos
                    if (!text) return {};
                    
                    // Si hay contenido, intentamos parsearlo como JSON
                    try {
                        return JSON.parse(text);
                    } catch (e) {
                        console.warn('Respuesta no es JSON válido pero continuamos:', text);
                        // Si no es JSON válido, seguimos sin error
                        return {};
                    }
                })
                .then(data => {
                    // Usar el mensaje de la respuesta si está disponible
                    let mensaje;
                    if (data && typeof data === 'object') {
                        // Si tenemos una respuesta en formato RespuestaDTO
                        if (data.hasOwnProperty('exito') && data.hasOwnProperty('mensaje')) {
                            // Si no fue exitoso, lanzamos un error para ir al catch
                            if (data.exito === false) {
                                throw new Error(data.mensaje);
                            }
                            mensaje = data.mensaje;
                        } else {
                            // Formato antiguo o formato desconocido
                            mensaje = data.mensaje || `Stock ${action === 'add' ? 'aumentado' : 'reducido'} correctamente`;
                        }
                    } else {
                        // Sin datos o datos no válidos
                        mensaje = `Stock ${action === 'add' ? 'aumentado' : 'reducido'} correctamente`;
                    }
                    
                    showToast(
                        'Éxito',
                        mensaje,
                        'success'
                    );
                    closeStockModal();
                    
                    // Recargar la página para mostrar los cambios
                    // Dar un pequeño retraso para que el usuario vea el mensaje de éxito
                    setTimeout(() => {
                        window.location.reload();
                    }, 1000);
                })
                .catch(error => {
                    console.error('Error:', error);
                    const errorMessage = error.message || 'No se pudo actualizar el stock';
                    showToast('Error', errorMessage, 'error');
                });
            });
        }
    }

    // FUNCIONALIDAD COMÚN PARA AMBAS VISTAS (ADMIN Y EMPLEADO)
    
    // Manejar la búsqueda en la tabla de inventario
    if (inventorySearch) {
        inventorySearch.addEventListener('input', function() {
            const searchTerm = this.value.toLowerCase();
            const rows = inventoryTableBody.querySelectorAll('tr');
            
            rows.forEach(row => {
                // Ignorar la fila de "No hay productos"
                if (row.cells.length === 1 && row.cells[0].classList.contains('empty-data')) {
                    return;
                }
                
                const text = row.textContent.toLowerCase();
                if (text.includes(searchTerm)) {
                    row.style.display = '';
                } else {
                    row.style.display = 'none';
                }
            });
        });
    }
});