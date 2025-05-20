document.addEventListener('DOMContentLoaded', function() {
    // Referencias a elementos del DOM
    const forgotPasswordForm = document.getElementById('forgotPasswordForm');
    const emailInput = document.getElementById('email');
    const recoverButton = document.getElementById('recoverButton');
    const btnText = recoverButton.querySelector('.btn-text');
    const btnLoader = recoverButton.querySelector('.btn-loader');

    // Manejar envío del formulario
    forgotPasswordForm.addEventListener('submit', function(e) {
        e.preventDefault();

        const email = emailInput.value.trim();

        // Validación básica
        if (!email) {
            showToast('Error', 'Por favor, ingresa tu correo electrónico', 'error');
            return;
        }

        // Deshabilitar el botón y mostrar loader
        recoverButton.disabled = true;
        btnText.classList.add('hidden');
        btnLoader.classList.remove('hidden');

        // Simulación de envío (en producción, esto sería una llamada a la API)
        setTimeout(() => {
            // Mostrar mensaje de éxito
            showToast('Solicitud enviada', 'Se han enviado instrucciones a tu correo electrónico', 'success');

            // Redirigir después de un tiempo
            setTimeout(() => {
                window.location.href = '/login?mensaje=Se han enviado instrucciones a tu correo electrónico';
            }, 3000);
        }, 1500);
    });

    // Función para mostrar toast (si no está definida en scripts.js)
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
}); 