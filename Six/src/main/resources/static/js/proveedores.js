document.addEventListener('DOMContentLoaded', function() {
    // Referencias a elementos del DOM
    const sidebar = document.querySelector('.sidebar');
    const mobileMenuButton = document.querySelector('.mobile-menu-button');
    const searchProveedor = document.getElementById('searchProveedor');
    const proveedoresTableBody = document.getElementById('proveedoresTableBody');
    const btnNuevoProveedor = document.getElementById('btnNuevoProveedor');
    const proveedorModal = document.getElementById('proveedorModal');
    const proveedorModalTitle = document.getElementById('proveedorModalTitle');
    const proveedorForm = document.getElementById('proveedorForm');
    const proveedorId = document.getElementById('proveedorId');
    const nombre = document.getElementById('nombre');
    const ruc = document.getElementById('ruc');
    const telefono = document.getElementById('telefono');
    const email = document.getElementById('email');
    const direccion = document.getElementById('direccion');
    const contacto = document.getElementById('contacto');
    const telefonoContacto = document.getElementById('telefonoContacto');
    const observaciones = document.getElementById('observaciones');
    const modalCloseButtons = document.querySelectorAll('.modal-close, .modal-close-btn');
    const toast = document.getElementById('toast');
    const toastTitle = document.querySelector('.toast-title');
    const toastMessage = document.querySelector('.toast-message');
    const toastCloseButton = document.querySelector('.toast-close');
    const btnEmptyStateNuevoProveedor = document.getElementById('btnEmptyStateNuevoProveedor');
    const filterEstado = document.getElementById('filterEstado');
    const btnListView = document.getElementById('btnListView');
    const btnGridView = document.getElementById('btnGridView');
    const tableView = document.getElementById('tableView');
    const gridView = document.getElementById('gridView');

    let proveedores = []; // Almacena todos los proveedores
    let proveedoresFiltrados = []; // Almacena los proveedores filtrados

    // Manejar clic en el botón de menú móvil
    if (mobileMenuButton) {
        mobileMenuButton.addEventListener('click', function() {
            sidebar.classList.toggle('open');
        });
    }

    // Función para mostrar toast
    function showToast(title, message, type = 'success') {
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
    if (toastCloseButton) {
        toastCloseButton.addEventListener('click', function() {
            toast.classList.add('hidden');
        });
    }

    // Manejar clic en el botón de nuevo proveedor
    if (btnNuevoProveedor) {
        btnNuevoProveedor.addEventListener('click', function() {
            abrirModalNuevoProveedor();
        });
    }

    // Manejar clic en el botón de nuevo proveedor desde el estado vacío
    if (btnEmptyStateNuevoProveedor) {
        btnEmptyStateNuevoProveedor.addEventListener('click', function() {
            abrirModalNuevoProveedor();
        });
    }

    // Manejar clic en el botón de cerrar modal
    modalCloseButtons.forEach(button => {
        button.addEventListener('click', function() {
            cerrarModal();
        });
    });

    // Cerrar modal al hacer clic fuera de él
    window.addEventListener('click', function(event) {
        if (event.target === proveedorModal) {
            cerrarModal();
        }
    });

    // Manejar búsqueda de proveedores
    if (searchProveedor) {
        searchProveedor.addEventListener('input', function() {
            filtrarProveedores();
        });
    }

    // Manejar cambio en el filtro de estado
    if (filterEstado) {
        filterEstado.addEventListener('change', function() {
            filtrarProveedores();
        });
    }

    // Manejar envío del formulario
    if (proveedorForm) {
        proveedorForm.addEventListener('submit', function(e) {
            e.preventDefault();
            guardarProveedor();
        });
    }

    // Manejar cambio de vista (lista o tarjetas)
    if (btnListView && btnGridView) {
        btnListView.addEventListener('click', function() {
            cambiarVista('lista');
        });

        btnGridView.addEventListener('click', function() {
            cambiarVista('tarjetas');
        });
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

    // Función para abrir el modal para un nuevo proveedor
    function abrirModalNuevoProveedor() {
        proveedorModalTitle.textContent = 'Nuevo Proveedor';
        proveedorForm.reset();
        proveedorId.value = '';
        proveedorModal.style.display = 'block';
        
        // Centrar el modal después de abrirlo
        setTimeout(() => centerModal(proveedorModal), 10);
    }

    // Función para abrir el modal para editar un proveedor
    function abrirModalEditarProveedor(id) {
        proveedorModalTitle.textContent = 'Editar Proveedor';
        
        // Buscar el proveedor por ID
        const proveedor = proveedores.find(p => p.id === id);
        if (!proveedor) return;
        
        // Llenar el formulario con los datos del proveedor
        proveedorId.value = proveedor.id;
        nombre.value = proveedor.nombre || '';
        ruc.value = proveedor.ruc || '';
        telefono.value = proveedor.telefono || '';
        email.value = proveedor.email || '';
        direccion.value = proveedor.direccion || '';
        contacto.value = proveedor.contacto || '';
        telefonoContacto.value = proveedor.telefonoContacto || '';
        observaciones.value = proveedor.observaciones || '';
        
        // Mostrar el modal
        proveedorModal.style.display = 'block';
        
        // Centrar el modal después de abrirlo
        setTimeout(() => centerModal(proveedorModal), 10);
    }

    // Función para cerrar el modal
    function cerrarModal() {
        proveedorModal.style.display = 'none';
        proveedorForm.reset();
    }

    // Función para guardar un proveedor (crear o actualizar)
    function guardarProveedor() {
        const data = {
            nombre: nombre.value,
            ruc: ruc.value,
            telefono: telefono.value,
            email: email.value,
            direccion: direccion.value,
            contacto: contacto.value,
            telefonoContacto: telefonoContacto.value,
            observaciones: observaciones.value,
            activo: true
        };
        
        const id = proveedorId.value;
        const method = id ? 'PUT' : 'POST';
        const url = id ? `/api/proveedores/${id}` : '/api/proveedores';
        
        fetch(url, {
            method: method,
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(data)
        })
        .then(response => {
            if (!response.ok) {
                return response.json().then(data => {
                    throw new Error(data.error || 'Error al guardar el proveedor');
                });
            }
            return response.json();
        })
        .then(proveedor => {
            cerrarModal();
            cargarProveedores();
            showToast('Éxito', id ? 'Proveedor actualizado correctamente' : 'Proveedor creado correctamente', 'success');
        })
        .catch(error => {
            console.error('Error:', error);
            showToast('Error', error.message || 'No se pudo guardar el proveedor', 'error');
        });
    }

    // Función para cambiar el estado de un proveedor
    function cambiarEstadoProveedor(id, activo) {
        fetch(`/api/proveedores/${id}/estado`, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ activo: activo })
        })
        .then(response => {
            if (!response.ok) {
                return response.json().then(data => {
                    throw new Error(data.error || 'Error al cambiar el estado del proveedor');
                });
            }
            return response.json();
        })
        .then(proveedor => {
            cargarProveedores();
            showToast('Éxito', activo ? 'Proveedor activado correctamente' : 'Proveedor desactivado correctamente', 'success');
        })
        .catch(error => {
            console.error('Error:', error);
            showToast('Error', error.message || 'No se pudo cambiar el estado del proveedor', 'error');
        });
    }

    // Función para cargar los proveedores
    function cargarProveedores() {
        fetch('/api/proveedores')
            .then(response => {
                if (!response.ok) {
                    throw new Error('Error al cargar proveedores');
                }
                return response.json();
            })
            .then(data => {
                proveedores = data;
                filtrarProveedores();
            })
            .catch(error => {
                console.error('Error:', error);
                showToast('Error', 'No se pudieron cargar los proveedores', 'error');
                mostrarEstadoVacio('Error al cargar los proveedores');
            });
    }

    // Función para filtrar proveedores según búsqueda y estado
    function filtrarProveedores() {
        const busqueda = searchProveedor.value.toLowerCase();
        const estado = filterEstado.value;
        
        proveedoresFiltrados = proveedores.filter(proveedor => {
            // Filtrar por estado
            if (estado === 'activos' && !proveedor.activo) return false;
            if (estado === 'inactivos' && proveedor.activo) return false;
            
            // Filtrar por búsqueda
            const nombreMatch = proveedor.nombre && proveedor.nombre.toLowerCase().includes(busqueda);
            const rucMatch = proveedor.ruc && proveedor.ruc.toLowerCase().includes(busqueda);
            const emailMatch = proveedor.email && proveedor.email.toLowerCase().includes(busqueda);
            
            return nombreMatch || rucMatch || emailMatch;
        });
        
        renderizarProveedores();
    }

    // Función para renderizar los proveedores en la vista actual
    function renderizarProveedores() {
        const vistaActual = btnListView.classList.contains('active') ? 'lista' : 'tarjetas';
        
        if (vistaActual === 'lista') {
            renderizarTablaProveedores();
        } else {
            renderizarTarjetasProveedores();
        }
    }

    // Función para renderizar la tabla de proveedores
    function renderizarTablaProveedores() {
        if (proveedoresFiltrados.length === 0) {
            proveedoresTableBody.innerHTML = `
                <tr>
                    <td colspan="7" class="empty-data">No se encontraron proveedores</td>
                </tr>
            `;
            return;
        }
        
        let html = '';
        
        proveedoresFiltrados.forEach(proveedor => {
            html += `
                <tr>
                    <td>${proveedor.id}</td>
                    <td>${proveedor.nombre || ''}</td>
                    <td>${proveedor.ruc || ''}</td>
                    <td>${proveedor.telefono || ''}</td>
                    <td>${proveedor.email || ''}</td>
                    <td>
                        <span class="badge ${proveedor.activo ? 'badge-success' : 'badge-danger'}">
                            ${proveedor.activo ? 'Activo' : 'Inactivo'}
                        </span>
                    </td>
                    <td class="actions-cell">
                        <button class="btn btn-primary btn-sm" onclick="editarProveedor(${proveedor.id})" style="padding: 0.2rem 0.4rem; font-size: 0.8rem;">
                            <i class="fas fa-edit"></i>
                        </button>
                        ${proveedor.activo ? 
                            `<button class="btn btn-secondary btn-sm" onclick="desactivarProveedor(${proveedor.id})" style="padding: 0.2rem 0.4rem; font-size: 0.8rem;">
                                <i class="fas fa-ban"></i>
                            </button>` : 
                            `<button class="btn btn-primary btn-sm" onclick="activarProveedor(${proveedor.id})" style="padding: 0.2rem 0.4rem; font-size: 0.8rem;">
                                <i class="fas fa-check"></i>
                            </button>`
                        }
                    </td>
                </tr>
            `;
        });
        
        proveedoresTableBody.innerHTML = html;
    }

    // Función para renderizar las tarjetas de proveedores
    function renderizarTarjetasProveedores() {
        if (proveedoresFiltrados.length === 0) {
            gridView.innerHTML = `
                <div class="empty-state">
                    <div class="empty-state-icon">
                        <i class="fas fa-truck"></i>
                    </div>
                    <div class="empty-state-text">
                        No se encontraron proveedores
                    </div>
                    <button id="btnEmptyStateNuevoProveedor" class="btn btn-primary" onclick="abrirModalNuevoProveedor()">
                        <i class="fas fa-plus"></i> Registrar Proveedor
                    </button>
                </div>
            `;
            return;
        }
        
        let html = '';
        
        proveedoresFiltrados.forEach(proveedor => {
            html += `
                <div class="provider-card">
                    <div class="provider-header">
                        <h3 class="provider-name">${proveedor.nombre || ''}</h3>
                        <span class="badge ${proveedor.activo ? 'badge-success' : 'badge-danger'}">
                            ${proveedor.activo ? 'Activo' : 'Inactivo'}
                        </span>
                    </div>
                    ${proveedor.ruc ? `<div class="provider-ruc">RUC: ${proveedor.ruc}</div>` : ''}
                    
                    <div class="card-separator"></div>
                    
                    <div style="display: grid; grid-template-columns: 1fr; gap: 10px;">
                        ${proveedor.telefono ? 
                            `<div class="provider-detail">
                                <i class="fas fa-phone"></i> ${proveedor.telefono}
                            </div>` : ''
                        }
                        
                        ${proveedor.email ? 
                            `<div class="provider-detail">
                                <i class="fas fa-envelope"></i> ${proveedor.email}
                            </div>` : ''
                        }
                        
                        ${proveedor.direccion ? 
                            `<div class="provider-detail" style="margin-top: 8px;">
                                <i class="fas fa-map-marker-alt"></i> ${proveedor.direccion}
                            </div>` : ''
                        }
                        
                        ${proveedor.contacto ? 
                            `<div class="provider-detail" style="margin-top: 8px;">
                                <i class="fas fa-user"></i> ${proveedor.contacto}
                            </div>` : ''
                        }
                    </div>
                    
                    <div class="card-separator"></div>
                    
                    <div class="provider-actions">
                        <button class="btn btn-primary btn-sm" onclick="editarProveedor(${proveedor.id})" style="padding: 0.2rem 0.5rem; font-size: 0.8rem;">
                            <i class="fas fa-edit"></i> Editar
                        </button>
                        ${proveedor.activo ? 
                            `<button class="btn btn-secondary btn-sm" onclick="desactivarProveedor(${proveedor.id})" style="padding: 0.2rem 0.5rem; font-size: 0.8rem;">
                                <i class="fas fa-ban"></i> Desactivar
                            </button>` : 
                            `<button class="btn btn-primary btn-sm" onclick="activarProveedor(${proveedor.id})" style="padding: 0.2rem 0.5rem; font-size: 0.8rem;">
                                <i class="fas fa-check"></i> Activar
                            </button>`
                        }
                    </div>
                </div>
            `;
        });
        
        gridView.innerHTML = html;
    }

    // Función para mostrar un estado vacío con mensaje personalizado
    function mostrarEstadoVacio(mensaje) {
        proveedoresTableBody.innerHTML = `
            <tr>
                <td colspan="7" class="empty-data">${mensaje}</td>
            </tr>
        `;
        
        gridView.innerHTML = `
            <div class="empty-state">
                <div class="empty-state-icon">
                    <i class="fas fa-exclamation-triangle"></i>
                </div>
                <div class="empty-state-text">
                    ${mensaje}
                </div>
            </div>
        `;
    }

    // Función para cambiar entre vista de lista y tarjetas
    function cambiarVista(vista) {
        if (vista === 'lista') {
            tableView.style.display = 'block';
            gridView.style.display = 'none';
            btnListView.classList.add('active');
            btnGridView.classList.remove('active');
        } else {
            tableView.style.display = 'none';
            gridView.style.display = 'block';
            btnListView.classList.remove('active');
            btnGridView.classList.add('active');
        }
        
        renderizarProveedores();
    }

    // Funciones globales para manejar acciones en los botones de la tabla/tarjetas
    window.editarProveedor = function(id) {
        abrirModalEditarProveedor(id);
    };
    
    window.activarProveedor = function(id) {
        cambiarEstadoProveedor(id, true);
    };
    
    window.desactivarProveedor = function(id) {
        cambiarEstadoProveedor(id, false);
    };
    
    window.abrirModalNuevoProveedor = function() {
        abrirModalNuevoProveedor();
    };

    // Cargar los proveedores al iniciar
    cargarProveedores();
}); 