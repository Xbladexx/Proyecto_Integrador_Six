document.addEventListener('DOMContentLoaded', function() {
    // Limpiar cualquier sesión existente al cargar la página
    localStorage.removeItem('userRole');
    localStorage.removeItem('userName');

    // Referencias a elementos del DOM
    const loginForm = document.getElementById('loginForm');
    const usernameInput = document.getElementById('username');
    const passwordInput = document.getElementById('password');
    const loginButton = document.getElementById('loginButton');
    const togglePasswordButton = document.querySelector('.toggle-password');
    const btnText = document.querySelector('.btn-text');
    const btnLoader = document.querySelector('.btn-loader');

    // Función para mostrar/ocultar contraseña
    togglePasswordButton.addEventListener('click', function() {
        const type = passwordInput.getAttribute('type') === 'password' ? 'text' : 'password';
        passwordInput.setAttribute('type', type);

        // Cambiar el icono
        const icon = this.querySelector('i');
        if (type === 'password') {
            icon.classList.remove('fa-eye-slash');
            icon.classList.add('fa-eye');
        } else {
            icon.classList.remove('fa-eye');
            icon.classList.add('fa-eye-slash');
        }
    });

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
    document.querySelector('.toast-close').addEventListener('click', function() {
        document.getElementById('toast').classList.add('hidden');
    });

    // Manejar envío del formulario
    loginForm.addEventListener('submit', function(e) {
        e.preventDefault();

        const username = usernameInput.value.trim();
        const password = passwordInput.value.trim();

        // Deshabilitar el botón y mostrar loader
        loginButton.disabled = true;
        btnText.classList.add('hidden');
        btnLoader.classList.remove('hidden');

        // Simular autenticación (en un sistema real, esto sería una llamada a la API)
        setTimeout(() => {
            if (username === 'admin' && password === 'admin') {
                // Guardar el rol en localStorage para mantener la sesión
                localStorage.setItem('userRole', 'admin');
                localStorage.setItem('userName', 'Admin');

                showToast('Inicio de sesión exitoso', 'Bienvenido al sistema de gestión de inventario');

                // Redirigir al dashboard de admin
                setTimeout(() => {
                    window.location.href = 'dashboard-admin.html';
                }, 1000);
            } else if (username === 'empleado' && password === 'empleado') {
                // Guardar el rol en localStorage para mantener la sesión
                localStorage.setItem('userRole', 'empleado');
                localStorage.setItem('userName', 'Empleado');

                showToast('Inicio de sesión exitoso', 'Bienvenido al sistema de gestión de inventario');

                // Redirigir al dashboard de empleado
                setTimeout(() => {
                    window.location.href = 'dashboard-empleado.html';
                }, 1000);
            } else {
                showToast('Error de autenticación', 'Usuario o contraseña incorrectos', 'error');

                // Restaurar el botón
                loginButton.disabled = false;
                btnText.classList.remove('hidden');
                btnLoader.classList.add('hidden');
            }
        }, 1500);
    });
});