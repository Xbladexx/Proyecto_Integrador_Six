document.addEventListener('DOMContentLoaded', function() {
    // Referencias a elementos del DOM
    const sidebar = document.querySelector('.sidebar');
    const mobileMenuButton = document.querySelector('.mobile-menu-button');
    const tabButtons = document.querySelectorAll('.tab-button');
    const tabContents = document.querySelectorAll('.tab-content');
    const userInitial = document.getElementById('userInitial');

    // Mostrar inicial del usuario si existe en la página
    if (userInitial) {
        // Obtener el nombre de usuario del modelo Thymeleaf (si está disponible)
        const usuario = userInitial.getAttribute('data-usuario') || 'A';
        userInitial.textContent = usuario.charAt(0).toUpperCase();
    }

    // Función para mostrar toast
    function showToast(title, message, type = 'success') {
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
    }

    // Cerrar toast al hacer clic en el botón de cerrar
    const toastCloseButton = document.querySelector('.toast-close');
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

    // Manejar cambio de tabs
    tabButtons.forEach(button => {
        button.addEventListener('click', function() {
            const tabName = this.dataset.tab;

            // Desactivar todos los botones y contenidos
            tabButtons.forEach(btn => btn.classList.remove('active'));
            tabContents.forEach(content => content.classList.remove('active'));

            // Activar el botón y contenido seleccionado
            this.classList.add('active');
            document.getElementById(tabName).classList.add('active');
        });
    });

    // Declarar la función updateSalesChart (simulando que viene de otro archivo)
    window.updateSalesChart = function(period) {
        console.log(`Actualizando gráfico de ventas para el período: ${period}`);
        // Aquí iría la lógica real para actualizar el gráfico
    };

    // Manejar cambio de período en gráficos
    const chartPeriodButtons = document.querySelectorAll('.chart-period-button');
    chartPeriodButtons.forEach(button => {
        button.addEventListener('click', function() {
            const period = this.dataset.period;

            // Desactivar todos los botones
            chartPeriodButtons.forEach(btn => btn.classList.remove('active'));

            // Activar el botón seleccionado
            this.classList.add('active');

            // Actualizar el gráfico
            if (typeof updateSalesChart === 'function') {
                updateSalesChart(period);
            }
        });
    });

    // Cerrar el sidebar al hacer clic fuera de él en dispositivos móviles
    document.addEventListener('click', function(event) {
        const isMobile = window.innerWidth <= 768;
        const isClickInsideSidebar = sidebar && sidebar.contains(event.target);
        const isClickOnMenuButton = mobileMenuButton && mobileMenuButton.contains(event.target);

        if (isMobile && sidebar && sidebar.classList.contains('open') && !isClickInsideSidebar && !isClickOnMenuButton) {
            sidebar.classList.remove('open');
        }
    });
});