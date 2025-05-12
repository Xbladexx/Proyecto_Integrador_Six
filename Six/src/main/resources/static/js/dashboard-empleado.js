document.addEventListener('DOMContentLoaded', function() {
    // Referencias a elementos del DOM
    const sidebar = document.querySelector('.sidebar');
    const mobileMenuButton = document.querySelector('.mobile-menu-button');
    const toastCloseButton = document.querySelector('.toast-close');

    // Función para mostrar toast
    window.showToast = function(title, message, type = 'success') {
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
    };

    // Cerrar toast al hacer clic en el botón de cerrar
    if (toastCloseButton) {
        toastCloseButton.addEventListener('click', function() {
            document.getElementById('toast').classList.add('hidden');
        });
    }

    // Manejar clic en el botón de menú móvil
    if (mobileMenuButton) {
        mobileMenuButton.addEventListener('click', function() {
            sidebar.classList.toggle('open');
        });
    }

    // Cerrar el sidebar al hacer clic fuera de él en dispositivos móviles
    document.addEventListener('click', function(event) {
        const isMobile = window.innerWidth <= 768;
        const isClickInsideSidebar = sidebar.contains(event.target);
        const isClickOnMenuButton = mobileMenuButton && mobileMenuButton.contains(event.target);

        if (isMobile && sidebar.classList.contains('open') && !isClickInsideSidebar && !isClickOnMenuButton) {
            sidebar.classList.remove('open');
        }
    });
});