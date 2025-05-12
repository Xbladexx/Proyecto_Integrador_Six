// Funciones principales de JavaScript

// Función que se ejecuta cuando el DOM está completamente cargado
document.addEventListener('DOMContentLoaded', function() {
    console.log('La página se ha cargado completamente');

    // Inicialización de elementos UI
    setupPasswordToggle();
    setupLoginForm();
});

// Configurar el botón para mostrar/ocultar contraseña
function setupPasswordToggle() {
    const toggleButtons = document.querySelectorAll('.toggle-password');

    toggleButtons.forEach(button => {
        button.addEventListener('click', function() {
            const input = this.parentElement.querySelector('input');
            const icon = this.querySelector('i');

            if (input.type === 'password') {
                input.type = 'text';
                icon.classList.remove('fa-eye');
                icon.classList.add('fa-eye-slash');
            } else {
                input.type = 'password';
                icon.classList.remove('fa-eye-slash');
                icon.classList.add('fa-eye');
            }
        });
    });
}

// Configurar el formulario de login
function setupLoginForm() {
    const loginForm = document.getElementById('loginForm');

    if (loginForm) {
        loginForm.addEventListener('submit', handleFormSubmit);
    }
}

// Manejar el envío del formulario
function handleFormSubmit(event) {
    // No prevenimos el envío por defecto ya que queremos que se procese en el servidor
    // event.preventDefault();

    const nombre = document.getElementById('nombre');
    const password = document.getElementById('password');
    const loginButton = document.getElementById('loginButton');

    // Validación básica del formulario
    if (!nombre.value.trim()) {
        showToast('Error', 'Por favor, ingresa tu nombre de usuario', 'error');
        event.preventDefault();
        return;
    }

    if (!password.value.trim()) {
        showToast('Error', 'Por favor, ingresa tu contraseña', 'error');
        event.preventDefault();
        return;
    }

    // Mostrar indicador de carga
    const btnText = loginButton.querySelector('.btn-text');
    const btnLoader = loginButton.querySelector('.btn-loader');

    if (btnText && btnLoader) {
        btnText.classList.add('hidden');
        btnLoader.classList.remove('hidden');
    }

    // El formulario se enviará al servidor
}

// Mostrar notificación toast
function showToast(title, message, type = '') {
    const toast = document.getElementById('toast');
    const toastTitle = toast.querySelector('.toast-title');
    const toastMessage = toast.querySelector('.toast-message');

    // Limpiar clases de tipo anteriores
    toast.classList.remove('error', 'success', 'warning', 'info');

    // Agregar clase de tipo si existe
    if (type) {
        toast.classList.add(type);
    }

    // Establecer contenido
    toastTitle.textContent = title;
    toastMessage.textContent = message;

    // Mostrar el toast
    toast.classList.remove('hidden');

    // Configurar cierre automático
    setTimeout(() => {
        toast.classList.add('hidden');
    }, 5000);

    // Configurar botón de cierre
    const closeButton = toast.querySelector('.toast-close');
    closeButton.addEventListener('click', () => {
        toast.classList.add('hidden');
    }, { once: true });
}