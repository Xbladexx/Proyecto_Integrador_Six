// Dashboard para empleados - SIX Inventario

document.addEventListener('DOMContentLoaded', function() {
    // Inicializar la actualización periódica de datos
    iniciarActualizacionPeriodica();

    // Manejar eventos de la UI
    initializeUI();
});

/**
 * Inicializa la interfaz de usuario y sus eventos
 */
function initializeUI() {
    // Manejar el botón del menú móvil
    const menuButton = document.querySelector('.mobile-menu-button');
    const sidebar = document.querySelector('.sidebar');

    if (menuButton) {
        menuButton.addEventListener('click', function() {
            sidebar.classList.toggle('show');
        });
    }

    // Cerrar toast si existe
    const toastCloseButton = document.querySelector('.toast-close');
    if (toastCloseButton) {
        toastCloseButton.addEventListener('click', function() {
            document.getElementById('toast').classList.add('hidden');
        });
    }
}

/**
 * Configura la actualización periódica de los datos del dashboard
 */
function iniciarActualizacionPeriodica() {
    // Actualizar datos inmediatamente al cargar
    actualizarDatosDashboard();

    // Actualizar cada 1 minuto (60000ms)
    setInterval(actualizarDatosDashboard, 60000);
}

/**
 * Realiza una petición al servidor para obtener los datos actualizados del dashboard
 */
function actualizarDatosDashboard() {
    fetch('/api/ventas/dashboard-empleado', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            credentials: 'same-origin'
        })
        .then(response => {
            if (!response.ok) {
                throw new Error('Error al obtener datos del dashboard');
            }
            return response.json();
        })
        .then(data => {
            // Actualizar la UI con los nuevos datos
            actualizarUI(data);
        })
        .catch(error => {
            console.error('Error al actualizar dashboard:', error);
        });
}

/**
 * Actualiza los elementos de la UI con los datos recibidos
 */
function actualizarUI(data) {
    // Actualizar ventas diarias
    const ventasDiariasElement = document.querySelector('.card-grid .card:nth-child(1) .card-value');
    if (ventasDiariasElement) {
        ventasDiariasElement.textContent = 'S/. ' + formatearNumero(data.ventasDiarias);
    }

    // Actualizar descripción de ventas
    const descripcionVentasElement = document.querySelector('.card-grid .card:nth-child(1) .card-description');
    if (descripcionVentasElement) {
        descripcionVentasElement.textContent = data.cantidadVentas + ' ventas realizadas hoy';
    }

    // Actualizar productos vendidos
    const productosVendidosElement = document.querySelector('.card-grid .card:nth-child(2) .card-value');
    if (productosVendidosElement) {
        productosVendidosElement.textContent = data.productosVendidos;
    }

    // Actualizar clientes atendidos
    const clientesAtendidosElement = document.querySelector('.card-grid .card:nth-child(3) .card-value');
    if (clientesAtendidosElement) {
        clientesAtendidosElement.textContent = data.clientesAtendidos;
    }

    // Actualizar ventas recientes si están incluidas en la respuesta
    if (data.ventasRecientes && data.ventasRecientes.length > 0) {
        const ventasRecientesContainer = document.querySelector('.recent-sales');
        if (ventasRecientesContainer) {
            // No actualizamos dinámicamente la lista de ventas recientes por ahora
            // ya que la estructura HTML es compleja. Para ver las ventas actualizadas,
            // el usuario debe recargar la página manualmente.

            // Esto podría implementarse en el futuro si es necesario
        }
    }
}

/**
 * Formatea un número para mostrar con dos decimales
 */
function formatearNumero(numero) {
    return numero.toFixed(2);
}

/**
 * Muestra una notificación toast
 */
function mostrarToast(titulo, mensaje, tipo = 'info') {
    const toast = document.getElementById('toast');
    const toastTitle = document.querySelector('.toast-title');
    const toastMessage = document.querySelector('.toast-message');

    // Establecer contenido
    toastTitle.textContent = titulo;
    toastMessage.textContent = mensaje;

    // Establecer clase según el tipo sin eliminar otras clases de estilo
    toast.classList.remove('info', 'success', 'error', 'warning', 'hidden');
    toast.classList.add(tipo);

    // Mostrar el toast
    toast.classList.remove('hidden');

    // Ocultar automáticamente después de 3 segundos
    setTimeout(function() {
        toast.classList.add('hidden');
    }, 3000); // Reducido a 3 segundos
}