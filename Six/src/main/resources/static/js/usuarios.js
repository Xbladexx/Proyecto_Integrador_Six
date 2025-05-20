// Script simplificado para cargar usuarios
document.addEventListener('DOMContentLoaded', function() {
    console.log('Inicializando script de usuarios simplificado');

    // Elemento de la tabla
    const usersTableBody = document.getElementById('usersTableBody');

    // Elementos del modal
    const userModal = document.getElementById('userModal');
    const modalClose = userModal.querySelector('.modal-close');
    const newUserButton = document.getElementById('newUserButton');
    const saveUserButton = document.getElementById('saveUserButton');
    const cancelUserButton = document.getElementById('cancelUserButton');

    // Campos del formulario
    const userName = document.getElementById('userName');
    const userEmail = document.getElementById('userEmail');
    const userRole = document.getElementById('userRole');
    const userStatus = document.getElementById('userStatus');
    const userNotes = document.getElementById('userNotes');

    // Toast para notificaciones
    const toast = document.getElementById('toast');
    const toastTitle = toast.querySelector('.toast-title');
    const toastMessage = toast.querySelector('.toast-message');
    const toastClose = toast.querySelector('.toast-close');

    // Variables para el mensaje de error en el formulario
    const errorAlert = document.getElementById('error-alert');
    const errorMessage = document.getElementById('error-message');

    if (!usersTableBody) {
        console.error('ERROR: No se encontró el elemento usersTableBody');
        return;
    }

    // Función para mostrar error en el formulario
    function mostrarErrorEnFormulario(mensaje) {
        errorMessage.textContent = mensaje;
        errorAlert.classList.remove('hidden');
    }

    // Función para ocultar error en el formulario
    function ocultarErrorEnFormulario() {
        errorAlert.classList.add('hidden');
    }

    // Mostrar mensaje de carga
    usersTableBody.innerHTML = '<tr><td colspan="7" class="text-center">Cargando usuarios...</td></tr>';

    // Función para cargar los datos
    function cargarUsuarios() {
        console.log('Intentando cargar usuarios desde API...');

        fetch('/api/usuarios/detalles')
            .then(response => {
                console.log('Respuesta del servidor:', response.status);
                if (!response.ok) {
                    throw new Error('Error al obtener usuarios: ' + response.status);
                }
                return response.json();
            })
            .then(usuarios => {
                console.log('Usuarios recibidos:', usuarios);
                mostrarUsuarios(usuarios);
            })
            .catch(error => {
                console.error('Error al cargar usuarios:', error);
                usersTableBody.innerHTML = `
                    <tr>
                        <td colspan="7" class="text-center">
                            Error al cargar usuarios: ${error.message}
                        </td>
                    </tr>
                `;
            });
    }

    // Función para mostrar los usuarios en la tabla
    function mostrarUsuarios(usuarios) {
        // Limpiar tabla
        usersTableBody.innerHTML = '';

        if (!usuarios || usuarios.length === 0) {
            usersTableBody.innerHTML = '<tr><td colspan="9" class="text-center">No hay usuarios para mostrar</td></tr>';
            return;
        }

        // Crear filas para cada usuario
        usuarios.forEach(usuario => {
            // Formatear fechas
            let ultimoAcceso = 'Nunca';
            if (usuario.ultimoAcceso) {
                try {
                    ultimoAcceso = new Date(usuario.ultimoAcceso).toLocaleString('es');
                } catch (e) {
                    console.error('Error al formatear ultimoAcceso:', e);
                }
            }

            let fechaCreacion = 'Desconocida';
            if (usuario.fechaCreacion) {
                try {
                    fechaCreacion = new Date(usuario.fechaCreacion).toLocaleString('es');
                } catch (e) {
                    console.error('Error al formatear fechaCreacion:', e);
                }
            }

            // Crear fila
            const fila = document.createElement('tr');

            // Estado (activo/inactivo)
            const estadoClase = usuario.activo ? 'status-active' : 'status-inactive';
            const estadoTexto = usuario.activo ? 'Activo' : 'Inactivo';

            // Rol
            const rolClase = usuario.rol && usuario.rol.toLowerCase().includes('admin') ?
                'role-admin' : 'role-empleado';

            fila.innerHTML = `
                <td>${usuario.nombre || ''}</td>
                <td>${usuario.email || ''}</td>
                <td>${usuario.usuario || ''}</td>
                <td>${usuario.telefono || ''}</td>
                <td><span class="role-badge ${rolClase}">${usuario.rol || ''}</span></td>
                <td><span class="status-badge ${estadoClase}">${estadoTexto}</span></td>
                <td>${ultimoAcceso}</td>
                <td>${fechaCreacion}</td>
                <td class="text-right">
                    <button class="action-button">
                        <i class="fas fa-ellipsis-h"></i>
                    </button>
                </td>
            `;

            // Añadir fila a la tabla
            usersTableBody.appendChild(fila);
        });
    }

    // Función para generar una contraseña aleatoria
    function generarContraseña(longitud = 8) {
        const caracteres = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%';
        let contraseña = '';
        for (let i = 0; i < longitud; i++) {
            contraseña += caracteres.charAt(Math.floor(Math.random() * caracteres.length));
        }
        return contraseña;
    }

    // Función para mostrar el toast
    function mostrarToast(titulo, mensaje, tipo) {
        toastTitle.textContent = titulo;
        toastMessage.textContent = mensaje;

        // Asignar clase según tipo de mensaje
        toast.className = 'toast';
        if (tipo) {
            toast.classList.add(`toast-${tipo}`);
        }

        // Mostrar toast
        toast.classList.remove('hidden');

        // Ocultar automáticamente después de 5 segundos
        setTimeout(() => {
            toast.classList.add('hidden');
        }, 5000);
    }

    // Función para abrir el modal de nuevo usuario
    function abrirModalNuevoUsuario() {
        // Limpiar el formulario
        userName.value = '';
        userEmail.value = '';
        userRole.value = 'empleado';
        userStatus.value = 'active';
        userNotes.value = '';

        // Ocultar mensajes de error
        ocultarErrorEnFormulario();

        // Mostrar el modal
        userModal.classList.add('active');
    }

    // Función para cerrar el modal
    function cerrarModal() {
        userModal.classList.remove('active');
    }

    // Función para guardar un nuevo usuario
    function guardarNuevoUsuario() {
        // Ocultar mensajes de error previos
        ocultarErrorEnFormulario();

        // Validar formulario
        if (!userName.value.trim()) {
            mostrarErrorEnFormulario('El nombre del usuario es obligatorio');
            return;
        }

        if (!userEmail.value.trim() || !userEmail.value.includes('@')) {
            mostrarErrorEnFormulario('El email del usuario es inválido');
            return;
        }

        if (!userRole.value) {
            mostrarErrorEnFormulario('El rol del usuario es obligatorio');
            return;
        }

        // Crear objeto de usuario
        const nuevoUsuario = {
            nombre: userName.value.trim(),
            email: userEmail.value.trim(),
            rol: userRole.value === 'admin' ? 'ADMIN' : 'EMPLEADO',
            activo: userStatus.value === 'active',
            notas: userNotes.value.trim()
                // El backend generará automáticamente: usuario, teléfono y contraseña
        };

        console.log('Enviando datos para crear usuario:', nuevoUsuario);

        // Enviar a la API
        fetch('/api/usuarios', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(nuevoUsuario)
            })
            .then(response => {
                console.log('Respuesta del servidor:', response.status);

                // Si hay error, intentar obtener detalles del error
                if (!response.ok) {
                    if (response.status === 409) {
                        throw new Error('Ya existe un usuario con este nombre de usuario');
                    }
                    // Intentar obtener el mensaje de error del servidor
                    return response.text().then(text => {
                        console.error('Error del servidor:', text);
                        throw new Error('Error al crear el usuario: ' + response.status);
                    });
                }
                return response.json();
            })
            .then(usuario => {
                console.log('Usuario creado correctamente:', usuario);

                // Mostrar mensaje de éxito con la información generada automáticamente
                mostrarToast(
                    'Éxito',
                    `Usuario ${usuario.nombre} creado con éxito:\n` +
                    `- Usuario: ${usuario.usuario}\n` +
                    `- Teléfono: ${usuario.telefono}`,
                    'success'
                );

                // Cerrar modal
                cerrarModal();

                // Recargar usuarios
                cargarUsuarios();
            })
            .catch(error => {
                console.error('Error al crear usuario:', error);
                mostrarToast('Error', error.message, 'error');
                mostrarErrorEnFormulario(error.message);
            });
    }

    // Eventos
    if (newUserButton) {
        newUserButton.addEventListener('click', abrirModalNuevoUsuario);
    }

    if (modalClose) {
        modalClose.addEventListener('click', cerrarModal);
    }

    if (cancelUserButton) {
        cancelUserButton.addEventListener('click', cerrarModal);
    }

    if (saveUserButton) {
        saveUserButton.addEventListener('click', guardarNuevoUsuario);
    }

    if (toastClose) {
        toastClose.addEventListener('click', () => {
            toast.classList.add('hidden');
        });
    }

    // Cargar usuarios al iniciar
    cargarUsuarios();
});