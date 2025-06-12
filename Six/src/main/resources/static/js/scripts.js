// Funciones principales de JavaScript

// Función que se ejecuta cuando el DOM está completamente cargado
document.addEventListener('DOMContentLoaded', function() {
    console.log('La página se ha cargado completamente');

    // Inicialización de elementos UI
    setupPasswordToggle();
    setupLoginForm();

    // Animaciones para los elementos del menú lateral
    const navLinks = document.querySelectorAll('.sidebar-nav .nav-link');
    
    navLinks.forEach(link => {
        link.addEventListener('click', function(e) {
            // Si no es el enlace activo, aplicar animación de clic
            if (!this.classList.contains('active')) {
                // Crear efecto de onda
                const ripple = document.createElement('span');
                ripple.classList.add('nav-ripple-effect');
                
                // Estilo para el efecto de onda
                ripple.style.position = 'absolute';
                ripple.style.borderRadius = '50%';
                ripple.style.backgroundColor = 'rgba(255, 255, 255, 0.7)';
                ripple.style.pointerEvents = 'none';
                ripple.style.width = '100px';
                ripple.style.height = '100px';
                ripple.style.transform = 'translate(-50%, -50%) scale(0)';
                ripple.style.animation = 'rippleEffect 0.6s linear';
                
                // Posicionar el efecto donde se hizo clic
                const rect = this.getBoundingClientRect();
                const x = e.clientX - rect.left;
                const y = e.clientY - rect.top;
                ripple.style.left = x + 'px';
                ripple.style.top = y + 'px';
                
                // Añadir el efecto al elemento
                this.appendChild(ripple);
                
                // Escalar el icono brevemente
                const icon = this.querySelector('i');
                if (icon) {
                    icon.style.transform = 'scale(1.5)';
                    setTimeout(() => {
                        icon.style.transform = '';
                    }, 300);
                }
                
                // Eliminar el efecto después de la animación
                setTimeout(() => {
                    ripple.remove();
                }, 600);
            }
        });
    });
});

// Añadir keyframes para la animación de onda
const style = document.createElement('style');
style.textContent = `
    @keyframes rippleEffect {
        to {
            transform: translate(-50%, -50%) scale(4);
            opacity: 0;
        }
    }
`;
document.head.appendChild(style);

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