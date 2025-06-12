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
    
    // Nuevos elementos para el modal de lote
    const loteModal = document.getElementById('loteModal');
    const loteModalTitle = loteModal ? document.getElementById('loteModalTitle') : null;
    const loteForm = loteModal ? document.getElementById('loteForm') : null;
    const loteVarianteId = loteModal ? document.getElementById('loteVarianteId') : null;
    const loteProductInfo = loteModal ? document.getElementById('loteProductInfo') : null;
    const loteNumero = loteModal ? document.getElementById('loteNumero') : null;
    const loteCantidad = loteModal ? document.getElementById('loteCantidad') : null;
    const loteCosto = loteModal ? document.getElementById('loteCosto') : null;
    const loteFechaFabricacion = loteModal ? document.getElementById('loteFechaFabricacion') : null;
    const loteFechaVencimiento = loteModal ? document.getElementById('loteFechaVencimiento') : null;
    const loteProveedor = loteModal ? document.getElementById('loteProveedor') : null;
    const loteObservaciones = loteModal ? document.getElementById('loteObservaciones') : null;
    const btnSaveLote = loteModal ? document.getElementById('btnSaveLote') : null;
    const loteModalCloseButtons = loteModal ? document.querySelectorAll('.lote-modal-close, .lote-modal-close-btn') : null;
    
    // Elementos para acciones de la tabla (solo en vista admin)
    const inventoryTable = document.getElementById('inventoryTable');
    const addStockButtons = document.querySelectorAll('.add-stock-action');
    const reduceStockButtons = document.querySelectorAll('.reduce-stock-action');
    const addLoteButtons = document.querySelectorAll('.add-lote-action');

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

        toast.classList.remove('hidden', 'success', 'error', 'info');
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
        // Agregar evento para cerrar menús al hacer clic en cualquier parte
        document.addEventListener('click', function(event) {
            // Si el clic no fue en un botón de toggle o dentro de un menú, cerrar todos los menús
            if (!event.target.closest('.actions-toggle') && !event.target.closest('.actions-menu')) {
                document.querySelectorAll('.actions-menu.show').forEach(menu => {
                    menu.classList.remove('show');
                });
            }
        });

        // Configurar los botones de toggle para los menús de acciones
        document.querySelectorAll('.actions-toggle').forEach(button => {
            button.addEventListener('click', function(e) {
                e.stopPropagation(); // Evitar que el clic se propague
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

        // Configurar los botones de añadir lote
        if (addLoteButtons) {
            addLoteButtons.forEach(button => {
                button.addEventListener('click', function(e) {
                    e.preventDefault();
                    const id = this.getAttribute('data-id');
                    const nombre = this.getAttribute('data-nombre');
                    const color = this.getAttribute('data-color');
                    const talla = this.getAttribute('data-talla');
                    const productoId = this.getAttribute('data-producto-id');
                    
                    openLoteModal({
                        id: id,
                        nombreProducto: nombre,
                        color: color,
                        talla: talla,
                        productoId: productoId
                    });
                });
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

        // Función para abrir el modal de registro de lote
        function openLoteModal(product) {
            // Cargar los proveedores para el select
            cargarProveedores();
            
            // Configurar la información del producto
            loteProductInfo.value = `${product.nombreProducto} - ${product.color} - ${product.talla}`;
            loteVarianteId.value = product.id;
            
            // Asegurar que tengamos el ID del producto
            if (!product.productoId) {
                // Intentar obtener el ID del producto desde la API
                fetch(`/api/inventario/variante/${product.id}`)
                    .then(response => {
                        if (!response.ok) {
                            throw new Error('Error al obtener información de la variante');
                        }
                        return response.json();
                    })
                    .then(data => {
                        if (data.variante && data.variante.producto) {
                            product.productoId = data.variante.producto.id;
                            obtenerCostoUnitario(product);
                        } else {
                            // Si no podemos obtener el producto desde el inventario, intentar otra ruta
                            return fetch(`/api/lotes/variante/${product.id}`);
                        }
                    })
                    .then(response => {
                        if (!response || !response.ok) return null;
                        return response.json();
                    })
                    .then(lotes => {
                        if (lotes && lotes.length > 0) {
                            // Intentar obtener el ID del producto desde el primer lote
                            if (lotes[0].variante && lotes[0].variante.producto) {
                                product.productoId = lotes[0].variante.producto.id;
                                obtenerCostoUnitario(product);
                            } else {
                                buscarCostoEnLotes(product.id);
                            }
                        } else {
                            buscarCostoEnLotes(product.id);
                        }
                    })
                    .catch(error => {
                        console.warn('Error al obtener ID del producto:', error);
                        // Continuar con la obtención del costo sin el ID del producto
                        buscarCostoEnLotes(product.id);
                    });
            } else {
                obtenerCostoUnitario(product);
            }

            // Mostrar el modal
            loteModal.style.display = 'block';
        }
        
        // Función para obtener el costo unitario de un producto
        function obtenerCostoUnitario(product) {
            // Si tenemos el ID del producto, intentar obtener su costo
            if (product.productoId) {
                fetch(`/api/productos/${product.productoId}`)
                    .then(response => {
                        if (!response.ok) {
                            throw new Error('Error al obtener información del producto');
                        }
                        return response.json();
                    })
                    .then(data => {
                        console.log('Producto obtenido:', data);
                        // Establecer el costo unitario desde el producto
                        if (data && data.costo) {
                            console.log('Costo del producto encontrado:', data.costo);
                            loteCosto.value = data.costo;
                        } else {
                            console.warn('El producto no tiene costo definido, buscando en lotes anteriores');
                            buscarCostoEnLotes(product.id);
                        }
                    })
                    .catch(error => {
                        console.warn('Error al obtener costo unitario desde producto:', error);
                        buscarCostoEnLotes(product.id);
                    });
            } else {
                // Si no tenemos el ID del producto, buscar en lotes
                console.warn('No se tiene ID del producto, buscando costo en lotes anteriores');
                buscarCostoEnLotes(product.id);
            }
        }
        
        // Función para buscar el costo unitario en lotes anteriores
        function buscarCostoEnLotes(varianteId) {
            console.log('Buscando costo unitario en lotes para variante ID:', varianteId);
            fetch(`/api/lotes/variante/${varianteId}`)
                .then(response => {
                    if (!response.ok) {
                        throw new Error(`Error al obtener lotes para variante ${varianteId}`);
                    }
                    return response.json();
                })
                .then(lotes => {
                    console.log('Lotes encontrados:', lotes ? lotes.length : 0);
                    if (lotes && lotes.length > 0) {
                        // Ordenar lotes por fecha de entrada, descendente
                        lotes.sort((a, b) => new Date(b.fechaEntrada) - new Date(a.fechaEntrada));
                        // Tomar el costo del lote más reciente
                        const loteReciente = lotes[0];
                        console.log('Lote más reciente:', loteReciente);
                        
                        if (loteReciente.costoUnitario) {
                            console.log('Costo unitario encontrado en lote:', loteReciente.costoUnitario);
                            loteCosto.value = loteReciente.costoUnitario;
                        } else {
                            console.warn('El lote no tiene costo unitario definido');
                            // Si no hay costo en el lote, intentar obtenerlo desde el producto en el lote
                            if (loteReciente.variante && loteReciente.variante.producto && loteReciente.variante.producto.costo) {
                                console.log('Usando costo del producto desde el lote:', loteReciente.variante.producto.costo);
                                loteCosto.value = loteReciente.variante.producto.costo;
                            }
                        }
                    } else {
                        console.warn('No se encontraron lotes para esta variante');
                    }
                })
                .catch(error => {
                    console.warn('Error al obtener el historial de lotes:', error);
                });
        }

        // Función para cargar los proveedores en el select
        function cargarProveedores() {
            fetch('/api/proveedores')
                .then(response => {
                    if (!response.ok) {
                        throw new Error('Error al cargar proveedores');
                    }
                    return response.json();
                })
                .then(proveedores => {
                    // Limpiar el select
                    loteProveedor.innerHTML = '';
                    
                    // Agregar la opción por defecto
                    const defaultOption = document.createElement('option');
                    defaultOption.value = '';
                    
                    if (proveedores.length === 0) {
                        defaultOption.textContent = 'No hay proveedores - Cree uno nuevo';
                        loteProveedor.appendChild(defaultOption);
                        
                        // Mostrar un mensaje al usuario
                        showToast('Información', 'No hay proveedores registrados. Por favor, cree uno nuevo usando el botón +', 'info');
                    } else {
                        defaultOption.textContent = 'Seleccione un proveedor';
                        loteProveedor.appendChild(defaultOption);
                        
                        // Agregar las opciones de proveedores
                        proveedores.forEach(proveedor => {
                            const option = document.createElement('option');
                            option.value = proveedor.id;
                            option.textContent = proveedor.nombre;
                            if (proveedor.ruc) {
                                option.textContent += ` (${proveedor.ruc})`;
                            }
                            loteProveedor.appendChild(option);
                        });
                    }
                })
                .catch(error => {
                    console.error('Error:', error);
                    loteProveedor.innerHTML = '<option value="">Error al cargar proveedores</option>';
                    showToast('Error', 'No se pudieron cargar los proveedores', 'error');
                });
        }

        // Función para cerrar el modal de stock
        function closeStockModal() {
            stockModal.style.display = 'none';
            // Resetear formulario
            document.getElementById('stockForm').reset();
        }

        // Función para cerrar el modal de lote
        function closeLoteModal() {
            loteModal.style.display = 'none';
            // Resetear formulario
            loteForm.reset();
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

        if (loteModalCloseButtons) {
            loteModalCloseButtons.forEach(button => {
                button.addEventListener('click', function() {
                    closeLoteModal();
                });
            });
        }

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

        // Manejar el envío del formulario de lote
        if (loteForm) {
            loteForm.addEventListener('submit', function(e) {
                e.preventDefault();
                
                const varianteId = loteVarianteId.value;
                const cantidad = parseInt(loteCantidad.value, 10);
                const costoUnitario = parseFloat(loteCosto.value);
                const fechaFabricacion = loteFechaFabricacion.value;
                const fechaVencimiento = loteFechaVencimiento.value;
                const proveedorId = loteProveedor.value;
                const observaciones = loteObservaciones.value;
                
                // Validaciones
                if (isNaN(cantidad) || cantidad <= 0) {
                    showToast('Error', 'La cantidad debe ser un número mayor que cero', 'error');
                    return;
                }
                
                if (isNaN(costoUnitario) || costoUnitario <= 0) {
                    showToast('Error', 'El costo unitario debe ser un número mayor que cero', 'error');
                    return;
                }
                
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
                
                // Construir la URL con los parámetros
                let url = `/api/lotes?varianteId=${varianteId}&cantidad=${cantidad}&costoUnitario=${costoUnitario}&usuarioId=${usuarioId}`;
                
                if (fechaFabricacion) {
                    url += `&fechaFabricacion=${fechaFabricacion}`;
                }
                
                if (fechaVencimiento) {
                    url += `&fechaVencimiento=${fechaVencimiento}`;
                }
                
                if (proveedorId) {
                    url += `&proveedorId=${proveedorId}`;
                }
                
                if (observaciones) {
                    url += `&observaciones=${encodeURIComponent(observaciones)}`;
                }
                
                // Enviar la solicitud al servidor
                fetch(url, {
                    method: 'POST'
                })
                .then(response => {
                    if (!response.ok) {
                        return response.text().then(text => {
                            throw new Error(text || 'Error al registrar lote');
                        });
                    }
                    return response.json();
                })
                .then(data => {
                    showToast('Éxito', 'Lote registrado correctamente', 'success');
                    closeLoteModal();
                    
                    // Recargar la página para mostrar los cambios
                    setTimeout(() => {
                        window.location.reload();
                    }, 1000);
                })
                .catch(error => {
                    console.error('Error:', error);
                    const errorMessage = error.message || 'No se pudo registrar el lote';
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

    // Nuevos elementos para el modal de proveedor rápido
    const proveedorRapidoModal = document.getElementById('proveedorRapidoModal');
    const proveedorRapidoForm = document.getElementById('proveedorRapidoForm');
    const proveedorRapidoNombre = document.getElementById('proveedorRapidoNombre');
    const proveedorRapidoRuc = document.getElementById('proveedorRapidoRuc');
    const proveedorRapidoTelefono = document.getElementById('proveedorRapidoTelefono');
    const proveedorRapidoEmail = document.getElementById('proveedorRapidoEmail');
    const proveedorRapidoDireccion = document.getElementById('proveedorRapidoDireccion');
    const btnNuevoProveedorModal = document.getElementById('btnNuevoProveedorModal');
    const proveedorRapidoModalCloseButtons = document.querySelectorAll('.proveedor-rapido-modal-close, .proveedor-rapido-modal-close-btn');

    // Manejar clic en el botón de nuevo proveedor desde el modal de lote
    if (btnNuevoProveedorModal) {
        btnNuevoProveedorModal.addEventListener('click', function(e) {
            e.preventDefault();
            // Mostrar el modal de proveedor rápido
            proveedorRapidoModal.style.display = 'block';
            // Centrar el modal después de abrirlo
            setTimeout(() => centerModal(proveedorRapidoModal), 10);
        });
    }

    // Manejar botones para cerrar modal de proveedor rápido
    if (proveedorRapidoModalCloseButtons) {
        proveedorRapidoModalCloseButtons.forEach(button => {
            button.addEventListener('click', function() {
                closeProveedorRapidoModal();
            });
        });
    }

    // Función para cerrar el modal de proveedor rápido
    function closeProveedorRapidoModal() {
        proveedorRapidoModal.style.display = 'none';
        // Resetear formulario
        proveedorRapidoForm.reset();
    }

    // Manejar el envío del formulario de proveedor rápido
    if (proveedorRapidoForm) {
        proveedorRapidoForm.addEventListener('submit', function(e) {
            e.preventDefault();
            
            const data = {
                nombre: proveedorRapidoNombre.value,
                ruc: proveedorRapidoRuc.value,
                telefono: proveedorRapidoTelefono.value,
                email: proveedorRapidoEmail.value,
                direccion: proveedorRapidoDireccion.value,
                activo: true
            };
            
            // Enviar la solicitud al servidor
            fetch('/api/proveedores', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(data)
            })
            .then(response => {
                if (!response.ok) {
                    return response.json().then(data => {
                        throw new Error(data.error || 'Error al crear proveedor');
                    });
                }
                return response.json();
            })
            .then(proveedor => {
                showToast('Éxito', 'Proveedor creado correctamente', 'success');
                closeProveedorRapidoModal();
                
                // Añadir el nuevo proveedor al select y seleccionarlo
                const option = document.createElement('option');
                option.value = proveedor.id;
                option.textContent = proveedor.nombre;
                loteProveedor.appendChild(option);
                loteProveedor.value = proveedor.id;
            })
            .catch(error => {
                console.error('Error:', error);
                const errorMessage = error.message || 'No se pudo crear el proveedor';
                showToast('Error', errorMessage, 'error');
            });
        });
    }

    // Variables para el historial de lotes
    const lotesTable = document.getElementById('lotesTable');
    const lotesTableBody = document.getElementById('lotesTableBody');
    const lotesSearch = document.getElementById('lotesSearch');
    const loteDetalleModal = document.getElementById('loteDetalleModal');
    const loteDetalleModalCloseButtons = document.querySelectorAll('.lote-detalle-modal-close, .lote-detalle-modal-close-btn');
    const btnDevolverLoteDetalle = document.getElementById('btnDevolverLoteDetalle');
    
    // Variable para almacenar el lote actual que se está visualizando
    let loteActualDetalle = null;

    // Cargar el historial de lotes
    function cargarHistorialLotes() {
        fetch('/api/lotes/historial')
            .then(response => {
                if (!response.ok) {
                    throw new Error('Error al cargar historial de lotes');
                }
                return response.json();
            })
            .then(lotes => {
                renderizarTablaLotes(lotes);
            })
            .catch(error => {
                console.error('Error:', error);
                lotesTableBody.innerHTML = `<tr><td colspan="9" class="empty-data">Error al cargar historial: ${error.message}</td></tr>`;
                showToast('Error', 'No se pudo cargar el historial de lotes', 'error');
            });
    }

    // Renderizar la tabla de lotes
    function renderizarTablaLotes(lotes) {
        if (!lotesTableBody) {
            console.error('No se encontró el elemento para mostrar los lotes');
            return;
        }
        
        if (!lotes || lotes.length === 0) {
            lotesTableBody.innerHTML = '<tr><td colspan="10" class="text-center">No hay lotes registrados</td></tr>';
            return;
        }
        
        let html = '';
        
        lotes.forEach(lote => {
            const fechaEntrada = lote.fechaEntrada ? new Date(lote.fechaEntrada).toLocaleDateString() : '-';
            const fechaVencimiento = lote.fechaVencimiento ? new Date(lote.fechaVencimiento).toLocaleDateString() : '-';
            
            // Determinar el estado del lote
            let estadoLote = "DISPONIBLE";
            let estadoClase = "badge badge-success";
            
            if (lote.estado) {
                estadoLote = lote.estado;
                
                if (lote.estado === "DEVUELTO") {
                    estadoClase = "badge badge-danger";
                } else if (lote.estado === "PARCIAL") {
                    estadoClase = "badge badge-warning";
                }
            } else if (lote.cantidadActual <= 0) {
                estadoLote = "AGOTADO";
                estadoClase = "badge badge-secondary";
            } else if (lote.fechaVencimiento && new Date(lote.fechaVencimiento) < new Date()) {
                estadoLote = "VENCIDO";
                estadoClase = "badge badge-danger";
            }
            
            // Asegurar que tengamos el ID del producto disponible
            if (!lote.productoId && lote.variante && lote.variante.producto) {
                lote.productoId = lote.variante.producto.id;
            }
            
            html += `
                <tr>
                    <td>${lote.numeroLote || '-'}</td>
                    <td>${lote.nombreProducto || '-'}</td>
                    <td>${lote.talla || '-'} / ${lote.color || '-'}</td>
                    <td>${lote.cantidadInicial || 0}</td>
                    <td>S/ ${lote.costoUnitario ? lote.costoUnitario.toFixed(2) : '0.00'}</td>
                    <td>${lote.proveedorNombre || '-'}</td>
                    <td>${fechaEntrada}</td>
                    <td>${fechaVencimiento}</td>
                    <td class="text-center">
                        <span class="${estadoClase}">${estadoLote}</span>
                    </td>
                    <td class="actions-cell">
                        <div class="actions-dropdown">
                            <button class="actions-toggle">
                                <i class="fas fa-ellipsis-v"></i>
                        </button>
                            <div class="actions-menu">
                                <a href="#" class="view-lote-action" onclick="verDetalleLote(${JSON.stringify(lote).replace(/"/g, '&quot;')})">
                                    <i class="fas fa-eye"></i> Ver Detalle
                                </a>
                                ${estadoLote !== "DEVUELTO" ? `
                                <a href="#" class="devolver-lote-action" onclick="abrirDevolucionLote(${JSON.stringify(lote).replace(/"/g, '&quot;')})">
                                    <i class="fas fa-undo-alt"></i> Devolver Lote
                                </a>` : ''}
                            </div>
                        </div>
                    </td>
                </tr>
            `;
        });

        lotesTableBody.innerHTML = html;
        
        // Configurar nuevamente los botones de toggle para los menús de acciones
        configureActionButtons();
    }

    // Función para configurar los botones de acción
    function configureActionButtons() {
        document.querySelectorAll('.actions-toggle').forEach(button => {
            button.addEventListener('click', function(e) {
                e.stopPropagation(); // Evitar que el clic se propague
                
                // Cerrar todos los menús abiertos primero
                document.querySelectorAll('.actions-menu.show').forEach(menu => {
                    if (menu !== this.nextElementSibling) {
                        menu.classList.remove('show');
                    }
                });
                
                // Mostrar/ocultar el menú actual
                const menu = this.nextElementSibling;
                menu.classList.toggle('show');
            });
        });
    }

    // Ver detalle de lote
    window.verDetalleLote = function(lote) {
        loteActualDetalle = lote;
        
        // Actualizar información en el modal
        document.getElementById('loteDetalleNumero').textContent = lote.numeroLote || '-';
        document.getElementById('loteDetalleProducto').textContent = lote.nombreProducto || '-';
        document.getElementById('loteDetalleTallaColor').textContent = `${lote.talla || '-'} / ${lote.color || '-'}`;
        document.getElementById('loteDetalleSku').textContent = lote.sku || '-';
        
        // Actualizar cantidades
        document.getElementById('loteDetalleCantidadInicial').textContent = lote.cantidadInicial || '0';
        document.getElementById('loteDetalleCantidadActual').textContent = lote.cantidadActual || '0';
        
        // Actualizar costos
        document.getElementById('loteDetalleCostoUnitario').textContent = lote.costoUnitario ? `S/ ${parseFloat(lote.costoUnitario).toFixed(2)}` : 'S/ 0.00';
        
        const costoTotal = (lote.costoUnitario || 0) * (lote.cantidadInicial || 0);
        document.getElementById('loteDetalleCostoTotal').textContent = `S/ ${costoTotal.toFixed(2)}`;
        
        // Actualizar fechas
        document.getElementById('loteDetalleFechaEntrada').textContent = lote.fechaEntrada ? new Date(lote.fechaEntrada).toLocaleDateString() : '-';
        document.getElementById('loteDetalleFechaFabricacion').textContent = lote.fechaFabricacion ? new Date(lote.fechaFabricacion).toLocaleDateString() : '-';
        document.getElementById('loteDetalleFechaVencimiento').textContent = lote.fechaVencimiento ? new Date(lote.fechaVencimiento).toLocaleDateString() : '-';
        
        // Actualizar otros datos
        document.getElementById('loteDetalleProveedor').textContent = lote.proveedorNombre || '-';
        document.getElementById('loteDetalleObservaciones').textContent = lote.observaciones || 'Sin observaciones';
        
        // Actualizar estado del lote
        let estadoLote = "DISPONIBLE";
        if (lote.estado) {
            estadoLote = lote.estado;
        } else if (lote.cantidadActual <= 0) {
            estadoLote = "AGOTADO";
        } else if (lote.fechaVencimiento && new Date(lote.fechaVencimiento) < new Date()) {
            estadoLote = "VENCIDO";
        }
        
        document.getElementById('loteDetalleEstado').textContent = estadoLote;
        
        // Configurar botón de devolución según estado del lote
        const btnDevolverLote = document.getElementById('btnDevolverLoteDetalle');
        if (btnDevolverLote) {
            if (estadoLote !== "DEVUELTO") {
                btnDevolverLote.style.display = '';
            } else {
                btnDevolverLote.style.display = 'none';
            }
        }
        
        // Mostrar el modal
        const modal = document.getElementById('loteDetalleModal');
        if (modal) {
            modal.style.display = 'flex';
        }
    }

    // Manejar botones para cerrar modal de detalle de lote
    if (loteDetalleModalCloseButtons) {
        loteDetalleModalCloseButtons.forEach(button => {
            button.addEventListener('click', function() {
                loteDetalleModal.style.display = 'none';
            });
        });
    }
    
    // Manejar clic en el botón "Devolver Lote" desde el detalle
    if (btnDevolverLoteDetalle) {
        btnDevolverLoteDetalle.addEventListener('click', function() {
            // Cerrar el modal de detalle
            loteDetalleModal.style.display = 'none';
            
            // Abrir el modal de devolución con el lote actual
            if (loteActualDetalle) {
                abrirDevolucionLote(loteActualDetalle);
            } else {
                showToast('Error', 'No se encontró información del lote', 'error');
            }
        });
    }

    // Filtrar lotes al escribir en el campo de búsqueda
    if (lotesSearch) {
        lotesSearch.addEventListener('input', function() {
            const searchTerm = this.value.toLowerCase();
            const rows = lotesTableBody.querySelectorAll('tr');
            
            rows.forEach(row => {
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

    // Cargar historial de lotes al iniciar
    cargarHistorialLotes();

    // Configurar botones para cerrar el modal de devolución de lote
    const devolverLoteModalCloseButtons = document.querySelectorAll('.devolver-lote-modal-close, .devolver-lote-modal-close-btn');
    if (devolverLoteModalCloseButtons) {
        devolverLoteModalCloseButtons.forEach(button => {
            button.addEventListener('click', function() {
                const devolverLoteModal = document.getElementById('devolverLoteModal');
                if (devolverLoteModal) {
                    devolverLoteModal.style.display = 'none';
                }
            });
        });
    }

    // Función para abrir el modal de devolución de un lote específico
    window.abrirDevolucionLote = function(lote) {
        const devolverLoteModal = document.getElementById('devolverLoteModal');
        if (!devolverLoteModal) {
            console.error('Modal de devolución de lote no encontrado');
            return;
        }
        
        // Verificar si el lote está devuelto
        if (lote.estado === "DEVUELTO") {
            showToast('Información', 'Este lote ya ha sido devuelto', 'info');
            return;
        }
        
        console.log('Abriendo modal para devolver lote:', lote);
        
        // Mostrar el modal
        devolverLoteModal.style.display = 'block';
        
        // Mostrar información del lote directamente (sin necesidad de selección)
        document.getElementById('loteCodigo').textContent = lote.numeroLote || '-';
        document.getElementById('loteFecha').textContent = lote.fechaEntrada ? new Date(lote.fechaEntrada).toLocaleDateString() : '-';
        
        // Usar cantidadInicial para calcular el total
        const cantidad = lote.cantidadInicial || 0;
        const costoUnitario = lote.costoUnitario || 0;
        const total = cantidad * costoUnitario;
        document.getElementById('loteTotal').textContent = `S/ ${total.toFixed(2)}`;
        
        // Guardar la cantidad del lote para usarla en la devolución
        const cantidadInput = document.getElementById('cantidadLoteHidden') || document.createElement('input');
        cantidadInput.type = 'hidden';
        cantidadInput.id = 'cantidadLoteHidden';
        cantidadInput.value = cantidad;
        
        // Si no existe, agregarlo al formulario
        const form = document.getElementById('devolucionLoteForm');
        if (form && !document.getElementById('cantidadLoteHidden')) {
            form.appendChild(cantidadInput);
        }
        
        document.getElementById('proveedorNombre').textContent = lote.proveedorNombre || '-';
        
        // Mostrar detalles del lote
        document.getElementById('infoLote').style.display = 'block';
        
        // Ocultar completamente los contenedores de selección de proveedor y lote
        const proveedorContainer = document.querySelector('.form-group:has(#proveedorSelect)');
        const loteContainer = document.querySelector('.form-group:has(#loteSelect)');
        
        if (proveedorContainer) {
            proveedorContainer.style.display = 'none';
        }
        
        if (loteContainer) {
            loteContainer.style.display = 'none';
        }
        
        // Establecer el campo oculto con el ID del lote
        const loteIdInput = document.getElementById('loteIdHidden') || document.createElement('input');
        loteIdInput.type = 'hidden';
        loteIdInput.id = 'loteIdHidden';
        loteIdInput.value = lote.id;
        
        // Usar el mismo form que ya obtuvimos anteriormente
        if (form && !document.getElementById('loteIdHidden')) {
            form.appendChild(loteIdInput);
        }
        
        // Mostrar los productos del lote sin llamada API
        mostrarProductosDesdeDatosLote(lote);
    };
    
    // Función para mostrar productos directamente desde los datos del lote sin llamada API
    function mostrarProductosDesdeDatosLote(lote) {
        console.log('Mostrando productos desde datos del lote:', lote);
        const productosContainer = document.getElementById('productosLote');
        const productosTable = document.getElementById('tablaProductosLote')?.querySelector('tbody');
        
        if (!productosContainer || !productosTable) {
            console.error('No se encontraron los elementos para mostrar productos');
            return;
        }
        
        // Mostrar el contenedor de productos
        productosContainer.style.display = 'block';
        
        try {
            // Crear una fila para el producto del lote
            productosTable.innerHTML = '';
            
            // Obtener información del producto desde el lote
            const nombreProducto = lote.nombreProducto || 
                                (lote.variante && lote.variante.producto ? lote.variante.producto.nombre : 'Producto');
            
            const sku = lote.variante ? lote.variante.sku : (lote.sku || lote.numeroLote || '-');
            
            // Usar cantidadInicial para la devolución
            let cantidad = lote.cantidadInicial || 0;
            if (cantidad <= 0 && lote.cantidadActual && lote.cantidadActual > 0) {
                cantidad = lote.cantidadActual;
            }
            
            // Precio unitario y subtotal
            const precioUnitario = parseFloat(lote.costoUnitario || 0);
            const subtotal = precioUnitario * cantidad;
            
            const tr = document.createElement('tr');
            tr.innerHTML = `
                <td>
                    <div class="form-check">
                        <input class="form-check-input producto-check" type="checkbox" value="${lote.id || lote.varianteId || 0}" checked>
                    </div>
                </td>
                <td>${nombreProducto}</td>
                <td>${sku}</td>
                <td>${cantidad}</td>
                <td>S/ ${precioUnitario.toFixed(2)}</td>
                <td>S/ ${subtotal.toFixed(2)}</td>
            `;
            
            productosTable.appendChild(tr);
            
            // Configurar el checkbox "seleccionar todos"
            const selectAllCheckbox = document.getElementById('seleccionarTodos');
            if (selectAllCheckbox) {
                selectAllCheckbox.checked = true;
                
                selectAllCheckbox.addEventListener('change', function() {
                    document.querySelectorAll('.producto-check').forEach(checkbox => {
                        checkbox.checked = this.checked;
                    });
                });
            }
        } catch (error) {
            console.error('Error al procesar los productos del lote:', error);
            productosTable.innerHTML = `
                <tr>
                    <td colspan="6" class="text-center">
                        <div class="alert alert-warning">
                            <i class="fas fa-exclamation-triangle"></i> Error al procesar los productos del lote
                        </div>
                    </td>
                </tr>
            `;
        }
    }

    // Función para centrar el modal en la pantalla
    function centerModal(modalElement) {
        if (!modalElement) return;
        
        // Calcular la posición vertical óptima
        const windowHeight = window.innerHeight;
        const modalHeight = modalElement.querySelector('.modal-content').offsetHeight;
        
        // Si el modal es más pequeño que la ventana, centrarlo
        if (modalHeight < windowHeight * 0.8) {
            const topPosition = Math.max(10, (windowHeight - modalHeight) / 2);
            modalElement.querySelector('.modal-content').style.marginTop = topPosition + 'px';
            modalElement.querySelector('.modal-content').style.marginBottom = '20px';
        }
    }

    // Configurar los botones de acciones si ya existen en el DOM
    configureActionButtons();

    // Función para cargar proveedores para devolución de lotes
    function cargarProveedoresParaDevolucion(lotePreseleccionado = null) {
        console.log('Cargando proveedores para devolución de lote');
        const proveedorSelect = document.getElementById('proveedorSelect');
        
        if (!proveedorSelect) {
            console.error('No se encontró el elemento select de proveedores');
            return;
        }
        
        // Limpiar el select
        proveedorSelect.innerHTML = '<option value="" disabled selected>Cargando proveedores...</option>';
        
        // Cargar proveedores desde el API
        fetch('/api/proveedores')
            .then(response => {
                if (!response.ok) {
                    throw new Error('Error al cargar proveedores: ' + response.statusText);
                }
                return response.json();
            })
            .then(proveedores => {
                console.log('Proveedores cargados:', proveedores);
                
                // Limpiar el select
                proveedorSelect.innerHTML = '<option value="" disabled selected>Seleccione un proveedor</option>';
                
                if (!proveedores || proveedores.length === 0) {
                    proveedorSelect.innerHTML = '<option value="" disabled selected>No hay proveedores disponibles</option>';
                    console.log('No hay proveedores disponibles');
                    return;
                }
                
                // Agregar cada proveedor al select
                proveedores.forEach(proveedor => {
                    const option = document.createElement('option');
                    option.value = proveedor.id;
                    option.textContent = proveedor.nombre;
                    if (proveedor.ruc) {
                        option.textContent += ` (${proveedor.ruc})`;
                    }
                    proveedorSelect.appendChild(option);
                });
                
                // Si tenemos un lote preseleccionado con proveedorId, seleccionarlo
                if (lotePreseleccionado && lotePreseleccionado.proveedorId) {
                    proveedorSelect.value = lotePreseleccionado.proveedorId;
                    
                    // Disparar el evento change para cargar los lotes
                    const event = new Event('change');
                    proveedorSelect.dispatchEvent(event);
                }
            })
            .catch(error => {
                console.error('Error al cargar proveedores:', error);
                proveedorSelect.innerHTML = '<option value="" disabled selected>Error al cargar proveedores</option>';
                showToast('Error', 'No se pudieron cargar los proveedores: ' + error.message, 'error');
            });
    }

    // Configurar evento para cargar lotes cuando se selecciona un proveedor
    const proveedorSelect = document.getElementById('proveedorSelect');
    if (proveedorSelect) {
        proveedorSelect.addEventListener('change', function() {
            const proveedorId = this.value;
            const loteSelect = document.getElementById('loteSelect');
            
            if (!loteSelect) {
                console.error('No se encontró el elemento select de lotes');
                return;
            }
            
            // Habilitar o deshabilitar el select de lotes según si hay un proveedor seleccionado
            if (!proveedorId) {
                loteSelect.disabled = true;
                loteSelect.innerHTML = '<option value="" disabled selected>Seleccione primero un proveedor</option>';
                return;
            }
            
            // Habilitar el select y mostrar mensaje de carga
            loteSelect.disabled = false;
            loteSelect.innerHTML = '<option value="" disabled selected>Cargando lotes...</option>';
            
            // Cargar lotes del proveedor
            fetch(`/api/lotes/proveedor/${proveedorId}`)
                .then(response => {
                    if (!response.ok) {
                        throw new Error('Error al cargar lotes: ' + response.statusText);
                    }
                    return response.json();
                })
                .then(lotes => {
                    console.log('Lotes cargados:', lotes);
                    
                    // Limpiar el select
                    loteSelect.innerHTML = '<option value="" disabled selected>Seleccione un lote</option>';
                    
                    if (!lotes || lotes.length === 0) {
                        loteSelect.innerHTML = '<option value="" disabled selected>No hay lotes disponibles para este proveedor</option>';
                        console.log('No hay lotes disponibles para el proveedor seleccionado');
                        return;
                    }
                    
                    // Agregar cada lote al select
                    lotes.forEach(lote => {
                        const option = document.createElement('option');
                        option.value = lote.id;
                        option.textContent = `${lote.codigo} - ${lote.producto || 'Producto sin nombre'}`;
                        option.setAttribute('data-lote', JSON.stringify(lote));
                        loteSelect.appendChild(option);
                    });
                })
                .catch(error => {
                    console.error('Error al cargar lotes:', error);
                    loteSelect.innerHTML = '<option value="" disabled selected>Error al cargar lotes</option>';
                    showToast('Error', 'No se pudieron cargar los lotes: ' + error.message, 'error');
                });
        });
    }

    // Configurar evento para mostrar detalles del lote cuando se selecciona uno
    const loteSelect = document.getElementById('loteSelect');
    if (loteSelect) {
        loteSelect.addEventListener('change', function() {
            const selectedOption = this.options[this.selectedIndex];
            if (!selectedOption || !selectedOption.value) return;
            
            try {
                const loteData = JSON.parse(selectedOption.getAttribute('data-lote'));
                
                // Mostrar información del lote
                document.getElementById('loteCodigo').textContent = loteData.codigo || '-';
                document.getElementById('loteFecha').textContent = loteData.fechaRecepcion ? new Date(loteData.fechaRecepcion).toLocaleDateString() : '-';
                document.getElementById('loteTotal').textContent = loteData.total ? `S/ ${parseFloat(loteData.total).toFixed(2)}` : 'S/ 0.00';
                document.getElementById('proveedorNombre').textContent = document.getElementById('proveedorSelect').options[document.getElementById('proveedorSelect').selectedIndex].text;
                
                // Mostrar el panel de información
                document.getElementById('infoLote').style.display = 'block';
                
                // Cargar los productos del lote
                cargarProductosLote(loteData.id);
            } catch (error) {
                console.error('Error al procesar los datos del lote:', error);
                showToast('Error', 'Error al procesar los datos del lote', 'error');
            }
        });
    }

    // Configurar botón para procesar la devolución del lote
    const procesarDevolucionBtn = document.getElementById('procesarDevolucionLoteBtn');
    if (procesarDevolucionBtn) {
        procesarDevolucionBtn.addEventListener('click', function() {
            // Obtener el ID del lote (del campo oculto o del selector)
            let loteId;
            const loteIdHidden = document.getElementById('loteIdHidden');
            
            if (loteIdHidden && loteIdHidden.value) {
                // Si tenemos un ID oculto (lote preseleccionado), lo usamos
                loteId = loteIdHidden.value;
                console.log('Usando loteId del campo oculto:', loteId);
            } else {
                // Si no, verificamos el selector
                const loteSelect = document.getElementById('loteSelect');
                if (!loteSelect || !loteSelect.value) {
                    showToast('Error', 'Debe seleccionar un lote para devolver', 'error');
                    return;
                }
                loteId = loteSelect.value;
                console.log('Usando loteId del selector:', loteId);
            }
            
            // Validar que se haya seleccionado al menos un producto
            const productoCheckboxes = document.querySelectorAll('.producto-check:checked');
            if (productoCheckboxes.length === 0) {
                showToast('Error', 'Debe seleccionar al menos un producto para devolver', 'error');
                return;
            }
            
            // Obtener el motivo de devolución
            const motivoSelect = document.getElementById('motivoDevolucion');
            if (!motivoSelect || !motivoSelect.value) {
                showToast('Error', 'Debe seleccionar un motivo para la devolución', 'error');
                return;
            }
            
            // Preparar los datos para el backend
            let motivoDevolucion;
            if (motivoSelect.value === 'OTRO') {
                const otroMotivoInput = document.getElementById('otroMotivo');
                if (!otroMotivoInput || !otroMotivoInput.value.trim()) {
                    showToast('Error', 'Debe especificar el motivo de la devolución', 'error');
                    return;
                }
                motivoDevolucion = otroMotivoInput.value.trim();
            } else {
                motivoDevolucion = motivoSelect.options[motivoSelect.selectedIndex].text;
            }
            
            const comentarios = document.getElementById('comentarios')?.value || '';
            
            // Obtener los IDs de los productos seleccionados
            const productosIds = Array.from(productoCheckboxes).map(cb => parseInt(cb.value, 10));
            
            console.log('Productos seleccionados:', productosIds.length, productosIds);
            
            // Obtener la cantidad original del lote para la devolución
            let cantidadLote = 1; // Valor por defecto
            const cantidadLoteHidden = document.getElementById('cantidadLoteHidden');
            if (cantidadLoteHidden && cantidadLoteHidden.value) {
                cantidadLote = parseInt(cantidadLoteHidden.value, 10);
                if (isNaN(cantidadLote) || cantidadLote <= 0) {
                    cantidadLote = 1; // Si no es un número válido, usamos 1
                }
            }
            
            // Datos para enviar al servidor
            const data = {
                loteId: parseInt(loteId, 10), 
                detallesIds: productosIds, 
                motivo: motivoDevolucion, 
                comentarios: comentarios,
                // Usar la cantidad original del lote
                cantidad: cantidadLote
            };
            
            console.log('Datos de devolución a enviar:', data);
            
            // Mostrar indicador de carga
            procesarDevolucionBtn.disabled = true;
            procesarDevolucionBtn.innerHTML = '<span class="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span> Procesando...';
            
            // Enviar la solicitud al servidor
            fetch('/api/lotes/devolucion', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(data)
            })
            .then(response => {
                console.log('Respuesta del servidor:', response.status);
                
                // Intentar parsear la respuesta como JSON
                return response.json().catch(e => {
                    // Si no se puede parsear como JSON, crear un objeto de error
                    if (!response.ok) {
                        return { 
                            exito: false, 
                            mensaje: `Error ${response.status}: No se pudo procesar la solicitud` 
                        };
                    }
                    return {};
                }).then(data => {
                    // Si la respuesta no es OK, lanzar un error con el mensaje
                    if (!response.ok) {
                        const errorMsg = data && (data.mensaje || data.message) 
                            ? data.mensaje || data.message 
                            : `Error ${response.status}: ${response.statusText}`;
                        throw new Error(errorMsg);
                    }
                    return data;
                });
            })
            .then(result => {
                console.log('Devolución procesada exitosamente:', result);
                
                // Cerrar el modal
                document.getElementById('devolverLoteModal').style.display = 'none';
                
                // Mostrar mensaje de éxito
                showToast('Éxito', result.mensaje || 'El lote ha sido devuelto correctamente', 'success');
                
                console.log('Cantidad devuelta:', result.cantidadDevuelta);
                
                // Recargar la lista de lotes
                setTimeout(() => {
                    cargarHistorialLotes();
                    
                    // Mostrar mensaje adicional con detalles
                    showToast('Información', `Se ha devuelto un lote con ${result.cantidadDevuelta} unidades`, 'info');
                    
                    console.log('Actualizando inventario después de devolución de lote...');
                    
                    // Recargar la página después de un breve retraso para actualizar el inventario
                    setTimeout(() => {
                        console.log('Recargando página para reflejar los cambios en el inventario...');
                        // Usar reload(true) para forzar una recarga completa desde el servidor
                        window.location.reload(true);
                    }, 3000);
                }, 1500);
            })
            .catch(error => {
                console.error('Error al procesar la devolución:', error);
                
                // Mensaje detallado para el usuario
                let errorMsg = 'No se pudo procesar la devolución';
                
                // Intentar mostrar un mensaje más descriptivo según el tipo de error
                if (error.message) {
                    if (error.message.includes('variante de producto')) {
                        errorMsg = 'Error: El producto del lote no está correctamente configurado. Contacte al administrador.';
                    } else if (error.message.includes('cantidad')) {
                        errorMsg = 'Error: La cantidad del lote no es válida.';
                    } else if (error.message.includes('ya ha sido devuelto')) {
                        errorMsg = 'Este lote ya ha sido devuelto anteriormente.';
                    } else if (error.message.includes('Error 500')) {
                        errorMsg = 'Error del servidor. Por favor, contacte al administrador del sistema.';
                    } else {
                        errorMsg = error.message;
                    }
                }
                    
                showToast('Error', errorMsg, 'error');
            })
            .finally(() => {
                // Restaurar el botón
                procesarDevolucionBtn.disabled = false;
                procesarDevolucionBtn.innerHTML = '<i class="fas fa-undo-alt"></i> Procesar Devolución';
            });
        });
    }
    
    // Configuración del selector de motivos
    const motivoSelect = document.getElementById('motivoDevolucion');
    const otroMotivoContainer = document.getElementById('otroMotivoGroup');
    
    if (motivoSelect && otroMotivoContainer) {
        motivoSelect.addEventListener('change', function() {
            if (this.value === 'OTRO') {
                otroMotivoContainer.style.display = 'block';
            } else {
                otroMotivoContainer.style.display = 'none';
            }
        });
    }
});