// Script simplificado para cargar usuarios
document.addEventListener('DOMContentLoaded', function() {
    console.log('Inicializando script de usuarios simplificado');

    // Elemento de la tabla
    const usersTableBody = document.getElementById('usersTableBody');

    if (!usersTableBody) {
        console.error('ERROR: No se encontró el elemento usersTableBody');
        return;
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
            usersTableBody.innerHTML = '<tr><td colspan="7" class="text-center">No hay usuarios para mostrar</td></tr>';
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

    // Cargar usuarios al iniciar
    cargarUsuarios();
});