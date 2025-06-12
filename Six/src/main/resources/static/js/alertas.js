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
    const notReadCount = document.getElementById('notReadCount');
    const markAllReadBtn = document.getElementById('markAllReadBtn');
    const refreshAlertsBtn = document.getElementById('refreshAlertsBtn');
    const notificationsContainer = document.getElementById('notificationsContainer');

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

    // Variables para almacenar datos
    let alertasActuales = [];
    let configuracionActual = null;

    // Inicializar WebSocket
    let socket = null;
    let isConnected = false;

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
    document.querySelector('.toast-close')?.addEventListener('click', function() {
        document.getElementById('toast').classList.add('hidden');
    });

    // Manejar clic en el botón de menú móvil
    mobileMenuButton?.addEventListener('click', function() {
        sidebar.classList.toggle('open');
    });

    // Cerrar el sidebar al hacer clic fuera de él en dispositivos móviles
    document.addEventListener('click', function(event) {
        const isMobile = window.innerWidth <= 768;
        const isClickInsideSidebar = sidebar?.contains(event.target);
        const isClickOnMenuButton = mobileMenuButton?.contains(event.target);

        if (isMobile && sidebar?.classList.contains('open') && !isClickInsideSidebar && !isClickOnMenuButton) {
            sidebar.classList.remove('open');
        }
    });

    // Inicializar WebSocket para notificaciones en tiempo real
    function inicializarWebSocket() {
        // Determinar el protocolo (ws o wss) basado en el protocolo HTTP actual
        const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
        const host = window.location.host;
        const wsUrl = `${protocol}//${host}/ws/alertas`;
        
        try {
            socket = new WebSocket(wsUrl);
            
            socket.onopen = function() {
                console.log('Conexión WebSocket de alertas establecida');
                isConnected = true;
            };
            
            socket.onmessage = function(event) {
                console.log('Mensaje de alerta recibido:', event.data);
                try {
                    const data = JSON.parse(event.data);
                    
                    if (data.tipo === 'NUEVA_ALERTA') {
                        // Mostrar notificación
                        mostrarNotificacion(data.alerta);
                        // Recargar datos
                        cargarAlertas();
                    }
                } catch (error) {
                    console.error('Error al procesar mensaje WebSocket:', error);
                }
            };
            
            socket.onclose = function() {
                console.log('Conexión WebSocket de alertas cerrada');
                isConnected = false;
                // Intentar reconectar después de 5 segundos
                setTimeout(inicializarWebSocket, 5000);
            };
            
            socket.onerror = function(error) {
                console.error('Error en WebSocket de alertas:', error);
                isConnected = false;
            };
        } catch (error) {
            console.error('Error al inicializar WebSocket de alertas:', error);
        }
    }

    // Funciones para la interfaz de alertas

    // Cambiar entre pestañas
    tabButtons?.forEach(button => {
        button.addEventListener('click', function() {
            const tabId = this.dataset.tab;

            // Desactivar todas las pestañas
            tabButtons.forEach(btn => btn.classList.remove('active'));
            tabContents.forEach(content => content.classList.remove('active'));

            // Activar la pestaña seleccionada
            this.classList.add('active');
            document.getElementById(`${tabId}-tab`)?.classList.add('active');
        });
    });

    // Cambiar entre pestañas de configuración
    settingsTabButtons?.forEach(button => {
        button.addEventListener('click', function() {
            const tabId = this.dataset.settingsTab;

            // Desactivar todas las pestañas
            settingsTabButtons.forEach(btn => btn.classList.remove('active'));
            settingsTabContents.forEach(content => content.classList.remove('active'));

            // Activar la pestaña seleccionada
            this.classList.add('active');
            document.getElementById(`${tabId}-settings-tab`)?.classList.add('active');
        });
    });

    // Cargar datos de alertas desde la API
    function cargarAlertas() {
        console.log('Cargando alertas...');
        
        // Mostrar indicador de carga
        if (alertsTableBody) {
            alertsTableBody.innerHTML = `
                <tr>
                    <td colspan="9" class="text-center">
                        <i class="fas fa-spinner fa-spin"></i> Cargando alertas...
                    </td>
                </tr>
            `;
        }

        fetch('/api/alertas')
            .then(response => response.json())
            .then(data => {
                // Almacenar alertas para búsqueda
                alertasActuales = data.content || [];

                if (alertsTableBody) {
                    mostrarAlertas(alertasActuales);
                    }

                // Actualizar contador de alertas no leídas
                actualizarContadorAlertas();
            })
            .catch(error => {
                console.error('Error al cargar alertas:', error);
                if (alertsTableBody) {
                alertsTableBody.innerHTML = `
                    <tr>
                        <td colspan="9" class="text-center text-red-500">
                            Error al cargar las alertas. Por favor, intente nuevamente.
                        </td>
                    </tr>
                `;
                }
            });
    }

    // Función para actualizar el contador de alertas no leídas
    function actualizarContadorAlertas() {
        fetch('/api/alertas/no-leidas')
            .then(response => response.json())
            .then(alertas => {
                const cantidad = alertas.length;
                
                if (notReadCount) {
                    notReadCount.textContent = cantidad > 0 ? cantidad : '';
                    notReadCount.style.display = cantidad > 0 ? 'block' : 'none';
                }

                // Actualizar icono en la barra de navegación si existe
                const navAlertCount = document.getElementById('navAlertCount');
                if (navAlertCount) {
                    navAlertCount.textContent = cantidad > 0 ? cantidad : '';
                    navAlertCount.style.display = cantidad > 0 ? 'block' : 'none';
                }
            })
            .catch(error => {
                console.error('Error al obtener alertas no leídas:', error);
            });
    }

    // Cargar configuración de alertas
    function cargarConfiguracion() {
        fetch('/api/alertas/configuracion')
            .then(response => response.json())
            .then(config => {
                configuracionActual = config;
                
                // Actualizar campos del formulario
                if (enableLowStock) {
                    enableLowStock.checked = config.alertasStockHabilitadas;
                }
                if (lowStockThreshold) {
                    lowStockThreshold.value = config.umbralStockBajo;
                }
                if (criticalStockThreshold) {
                    criticalStockThreshold.value = config.umbralStockCritico;
                }
                if (enableAutoOrder) {
                    enableAutoOrder.checked = config.pedidosAutomaticosHabilitados;
                }
                if (autoOrderThreshold) {
                    autoOrderThreshold.value = config.umbralPedidoAutomatico;
                    autoOrderThreshold.disabled = !config.pedidosAutomaticosHabilitados;
                }
                if (defaultOrderQuantity) {
                    defaultOrderQuantity.value = config.cantidadPedidoAutomatico;
                    defaultOrderQuantity.disabled = !config.pedidosAutomaticosHabilitados;
                }
                
                // Configuración de notificaciones
                if (emailNotifications) {
                    emailNotifications.checked = config.notificacionesEmailHabilitadas;
                }
                if (emailRecipients) {
                    emailRecipients.value = config.emailsNotificacion || '';
                    emailRecipients.disabled = !config.notificacionesEmailHabilitadas;
                }
                if (dashboardNotifications) {
                    dashboardNotifications.checked = config.notificacionesPushHabilitadas;
                }
                if (dailyDigest) {
                    dailyDigest.checked = config.enviarDigestoDiario;
                }
                if (criticalOnly) {
                    criticalOnly.checked = config.soloAlertasCriticas;
                }
            })
            .catch(error => {
                console.error('Error al cargar configuración de alertas:', error);
                showToast('Error', 'No se pudo cargar la configuración de alertas', 'error');
            });
    }

    // Buscar alertas
    alertsSearch?.addEventListener('input', function() {
        filterAlerts();
    });

    // Filtrar por estado
    statusFilter?.addEventListener('change', function() {
        filterAlerts();
    });

    // Función para filtrar alertas
    function filterAlerts() {
        if (!alertasActuales) return;
        
        let alertasFiltradas = [...alertasActuales];
        
        // Filtrar por término de búsqueda
        if (alertFilter && alertFilter.value.trim() !== '') {
            const searchTerm = alertFilter.value.toLowerCase().trim();
            alertasFiltradas = alertasFiltradas.filter(alerta => {
                const titulo = alerta.titulo ? alerta.titulo.toLowerCase() : '';
                const mensaje = alerta.mensaje ? alerta.mensaje.toLowerCase() : '';
                const producto = alerta.productoNombre ? alerta.productoNombre.toLowerCase() : '';
                const sku = alerta.productoSku ? alerta.productoSku.toLowerCase() : '';
                
                return titulo.includes(searchTerm) || 
                       mensaje.includes(searchTerm) || 
                       producto.includes(searchTerm) ||
                       sku.includes(searchTerm);
            });
        }
        
        // Filtrar por estado seleccionado
        if (alertStatusFilter && alertStatusFilter.value !== 'todos') {
            const statusValue = alertStatusFilter.value;
            if (statusValue === 'no-leidas') {
                alertasFiltradas = alertasFiltradas.filter(alerta => !alerta.leida);
            } else if (statusValue === 'leidas') {
                alertasFiltradas = alertasFiltradas.filter(alerta => alerta.leida);
            } else if (statusValue === 'alta-prioridad') {
                alertasFiltradas = alertasFiltradas.filter(alerta => alerta.prioridad === 'ALTA' || alerta.prioridad === 'CRITICA');
            } else if (statusValue === 'stockBajo') {
                alertasFiltradas = alertasFiltradas.filter(alerta => alerta.tipo === 'STOCK_BAJO');
            } else if (statusValue === 'stockCritico') {
                alertasFiltradas = alertasFiltradas.filter(alerta => alerta.tipo === 'STOCK_CRITICO');
            }
        }

        // Mostrar resultados
        mostrarAlertas(alertasFiltradas);
    }

    // Función para mostrar alertas en la tabla
    function mostrarAlertas(alertas) {
        if (!alertsTableBody) return;

                if (alertas.length === 0) {
            alertsTableBody.innerHTML = `
                <tr>
                        <td colspan="9" class="text-center">No se encontraron alertas que coincidan con los criterios de búsqueda.</td>
                </tr>
                    `;
                    return;
                }

        let html = '';
                alertas.forEach(alerta => {
            // Determinar la clase y el icono según el tipo y prioridad
                    let statusClass = '';
                    let statusIcon = '';
            let tipoCategoriaTexto = '';

            switch (alerta.tipo) {
                case 'STOCK_BAJO':
                    statusClass = 'status-low';
                    statusIcon = '<i class="fas fa-arrow-down"></i>';
                    tipoCategoriaTexto = 'Stock Bajo';
                    break;
                case 'STOCK_CRITICO':
                        statusClass = 'status-critical';
                        statusIcon = '<i class="fas fa-exclamation-triangle"></i>';
                    tipoCategoriaTexto = 'Stock Crítico';
                    break;
                case 'PEDIDO_AUTOMATICO':
                    statusClass = 'status-info';
                    statusIcon = '<i class="fas fa-shopping-cart"></i>';
                    tipoCategoriaTexto = 'Pedido Auto.';
                    break;
                case 'PRODUCTO_SIN_MOVIMIENTO':
                    statusClass = 'status-info';
                    statusIcon = '<i class="fas fa-pause-circle"></i>';
                    tipoCategoriaTexto = 'Sin Movimiento';
                    break;
                default:
                    statusClass = 'status-info';
                    statusIcon = '<i class="fas fa-info-circle"></i>';
                    tipoCategoriaTexto = 'Sistema';
            }

            // Añadir clase según si está leída o no
            const leidaClass = alerta.leida ? 'alert-read' : 'alert-unread';
            
            // Formatear fecha
            const fecha = new Date(alerta.fechaCreacion);
            const fechaFormateada = `${fecha.getDate().toString().padStart(2, '0')}/${(fecha.getMonth() + 1).toString().padStart(2, '0')}/${fecha.getFullYear()} ${fecha.getHours().toString().padStart(2, '0')}:${fecha.getMinutes().toString().padStart(2, '0')}`;

                    // Crear el contenido de la fila
            html += `
                <tr class="${leidaClass}" data-alerta-id="${alerta.id}">
                    <td><span class="status-badge ${statusClass}">${statusIcon} ${tipoCategoriaTexto}</span></td>
                    <td>${alerta.titulo || 'Sin título'}</td>
                    <td>${alerta.mensaje || 'Sin mensaje'}</td>
                    <td>${alerta.productoNombre || 'N/A'}</td>
                    <td>${alerta.productoSku || 'N/A'}</td>
                    <td>${alerta.varianteColor || 'N/A'} / ${alerta.varianteTalla || 'N/A'}</td>
                    <td class="${alerta.prioridad === 'ALTA' || alerta.prioridad === 'CRITICA' ? 'text-red-500 font-medium' : 
                            alerta.prioridad === 'MEDIA' ? 'text-amber-500 font-medium' : 
                            'text-green-500 font-medium'}">${alerta.stockActual || 'N/A'}</td>
                    <td>${fechaFormateada}</td>
                    <td>
                        <button class="btn-marcar-leida" data-alerta-id="${alerta.id}" ${alerta.leida ? 'disabled' : ''}>
                            ${alerta.leida ? '<i class="fas fa-check"></i>' : '<i class="fas fa-envelope-open"></i>'}
                        </button>
                    </td>
                </tr>
            `;
        });

        alertsTableBody.innerHTML = html;
        
        // Añadir event listeners a los botones de marcar como leída
        document.querySelectorAll('.btn-marcar-leida').forEach(btn => {
            if (!btn.disabled) {
                btn.addEventListener('click', function() {
                    const alertaId = this.getAttribute('data-alerta-id');
                    marcarComoLeida(alertaId);
                });
            }
        });
    }

    // Función para marcar una alerta como leída
    function marcarComoLeida(alertaId) {
        fetch(`/api/alertas/${alertaId}/marcar-leida`, {
            method: 'PUT'
        })
        .then(response => {
            if (response.ok) {
                // Actualizar UI
                const alertaRow = document.querySelector(`tr[data-alerta-id="${alertaId}"]`);
                if (alertaRow) {
                    alertaRow.classList.remove('alert-unread');
                    alertaRow.classList.add('alert-read');
                    
                    const btn = alertaRow.querySelector('.btn-marcar-leida');
                    if (btn) {
                        btn.disabled = true;
                        btn.innerHTML = '<i class="fas fa-check"></i>';
                    }
                }
                
                // Actualizar contador
                actualizarContadorAlertas();
                
                // Actualizar alertas actuales
                const alertaIndex = alertasActuales.findIndex(a => a.id === parseInt(alertaId));
                if (alertaIndex !== -1) {
                    alertasActuales[alertaIndex].leida = true;
                }
            }
        })
        .catch(error => {
            console.error('Error al marcar alerta como leída:', error);
            showToast('Error', 'No se pudo marcar la alerta como leída', 'error');
        });
    }

    // Función para marcar todas las alertas como leídas
    function marcarTodasComoLeidas() {
        fetch('/api/alertas/marcar-todas-leidas', {
            method: 'PUT'
        })
        .then(response => response.json())
        .then(data => {
            const cantidad = data.cantidadActualizada;
            
            // Actualizar UI
            document.querySelectorAll('tr.alert-unread').forEach(row => {
                row.classList.remove('alert-unread');
                row.classList.add('alert-read');
                
                const btn = row.querySelector('.btn-marcar-leida');
                if (btn) {
                    btn.disabled = true;
                    btn.innerHTML = '<i class="fas fa-check"></i>';
                }
            });
            
            // Actualizar contador
            actualizarContadorAlertas();
            
            // Actualizar alertas actuales
            alertasActuales.forEach(alerta => {
                alerta.leida = true;
            });
            
            showToast('Éxito', `${cantidad} alertas marcadas como leídas`);
            })
            .catch(error => {
            console.error('Error al marcar todas las alertas como leídas:', error);
            showToast('Error', 'No se pudieron marcar las alertas como leídas', 'error');
        });
    }

    // Función para mostrar notificación en tiempo real
    function mostrarNotificacion(alerta) {
        if (!notificationsContainer || !alerta) return;
        
        // Crear elemento de notificación
        const notificacion = document.createElement('div');
        notificacion.className = 'notification';
        
        // Determinar clase según prioridad
        let priorityClass = '';
        switch (alerta.prioridad) {
            case 'ALTA':
            case 'CRITICA':
                priorityClass = 'notification-critical';
                break;
            case 'MEDIA':
                priorityClass = 'notification-warning';
                break;
            default:
                priorityClass = 'notification-info';
        }
        
        notificacion.classList.add(priorityClass);
        
        // Crear contenido de notificación
        notificacion.innerHTML = `
            <div class="notification-header">
                <span class="notification-title">${alerta.titulo || 'Nueva Alerta'}</span>
                <button class="notification-close">&times;</button>
            </div>
            <div class="notification-body">
                <p>${alerta.mensaje || 'Se ha recibido una nueva alerta.'}</p>
            </div>
        `;
        
        // Añadir notificación al contenedor
        notificationsContainer.appendChild(notificacion);
        
        // Mostrar notificación
        setTimeout(() => {
            notificacion.classList.add('show');
        }, 100);
        
        // Cerrar notificación al hacer clic en el botón de cerrar
        const closeButton = notificacion.querySelector('.notification-close');
        if (closeButton) {
            closeButton.addEventListener('click', () => {
                notificacion.classList.remove('show');
                setTimeout(() => {
                    notificacion.remove();
                }, 300);
            });
        }
        
        // Auto-cerrar después de 7 segundos
        setTimeout(() => {
            if (notificacion.parentNode) {
                notificacion.classList.remove('show');
                setTimeout(() => {
                    if (notificacion.parentNode) {
                        notificacion.remove();
                    }
                }, 300);
            }
        }, 7000);
    }

    // Habilitar/deshabilitar campos de pedido automático
    enableAutoOrder?.addEventListener('change', function() {
        if (autoOrderThreshold) autoOrderThreshold.disabled = !this.checked;
        if (defaultOrderQuantity) defaultOrderQuantity.disabled = !this.checked;
    });

    // Habilitar/deshabilitar campos de notificaciones por email
    emailNotifications?.addEventListener('change', function() {
        if (emailRecipients) emailRecipients.disabled = !this.checked;
    });

    // Guardar configuración de stock
    saveStockSettings?.addEventListener('click', function() {
        // Validar campos
        if (parseInt(lowStockThreshold?.value) <= parseInt(criticalStockThreshold?.value)) {
            showToast('Error', 'El umbral de stock bajo debe ser mayor que el umbral de stock crítico', 'error');
            return;
        }

        // Crear objeto de configuración
        const config = {
            alertasStockHabilitadas: enableLowStock?.checked ?? true,
            umbralStockBajo: parseInt(lowStockThreshold?.value || 10),
            umbralStockCritico: parseInt(criticalStockThreshold?.value || 5),
            pedidosAutomaticosHabilitados: enableAutoOrder?.checked ?? false,
            umbralPedidoAutomatico: parseInt(autoOrderThreshold?.value || 3),
            cantidadPedidoAutomatico: parseInt(defaultOrderQuantity?.value || 10)
        };

        // Guardar configuración
        guardarConfiguracion(config);
    });

    // Guardar configuración de notificaciones
    saveNotificationSettings?.addEventListener('click', function() {
        // Validar campos
        if (emailNotifications?.checked && !emailRecipients?.value) {
            showToast('Error', 'Debe ingresar al menos un destinatario de email', 'error');
            return;
        }

        // Crear objeto de configuración
        const emails = emailRecipients?.value?.split(',').map(email => email.trim()).filter(email => email) || [];
        
        const config = {
            notificacionesEmailHabilitadas: emailNotifications?.checked ?? false,
            notificacionesPushHabilitadas: dashboardNotifications?.checked ?? true,
            emailsNotificacion: emails,
            enviarDigestoDiario: dailyDigest?.checked ?? false,
            soloAlertasCriticas: criticalOnly?.checked ?? false
        };

        // Guardar configuración
        guardarConfiguracion(config);
    });

    // Guardar configuración por categoría
    saveCategorySettings?.addEventListener('click', function() {
        // Aquí iría la lógica para guardar la configuración
        showToast('Configuración guardada', 'La configuración por categoría ha sido actualizada correctamente');
    });

    // Función para guardar configuración
    function guardarConfiguracion(configParcial) {
        // Obtener configuración actual y combinarla con la nueva
        const configuracionCompleta = { ...configuracionActual, ...configParcial };
        
        fetch('/api/alertas/configuracion', {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(configuracionCompleta)
        })
        .then(response => {
            if (response.ok) {
                return response.json();
            }
            throw new Error('Error al guardar configuración');
        })
        .then(data => {
            configuracionActual = data;
            showToast('Configuración guardada', 'La configuración de alertas ha sido actualizada correctamente');
        })
        .catch(error => {
            console.error('Error al guardar configuración:', error);
            showToast('Error', 'No se pudo guardar la configuración', 'error');
        });
    }

    // Verificar stock bajo manualmente
    refreshAlertsBtn?.addEventListener('click', function() {
        fetch('/api/alertas/verificar-stock', {
            method: 'POST'
        })
        .then(response => response.json())
        .then(data => {
            const cantidad = data.alertasGeneradas;
            
            showToast('Verificación completada', `Se generaron ${cantidad} nueva(s) alerta(s) de stock`);
            
            // Recargar alertas
            cargarAlertas();
        })
        .catch(error => {
            console.error('Error al verificar stock:', error);
            showToast('Error', 'No se pudo completar la verificación de stock', 'error');
        });
    });

    // Marcar todas las alertas como leídas
    markAllReadBtn?.addEventListener('click', function() {
        marcarTodasComoLeidas();
    });

    // Inicializar la página
    inicializarWebSocket();
    cargarAlertas();
    cargarConfiguracion();

    // Verificar nuevas alertas periódicamente (cada 5 minutos)
    setInterval(actualizarContadorAlertas, 300000);
});