import { cerrarModal, setupFiltroTabla } from './ui.js';

export async function reporteUsuarios() {
    try {
        cerrarModal();
        await new Promise(res => setTimeout(res, 80));

        // ✅ CORREGIDO
        const usuarios = await api.get(API_ENDPOINTS.usuarios.listar);

        const html = `
        <div class="modal">
            <div class="modal-content">
                <span class="close" onclick="cerrarModal()">&times;</span>
                <h2>👥 Gestor de Usuarios</h2>

                <input type="text" id="filtroUsuarios" placeholder="Filtrar por nombre, correo o rol..." class="input-filtro">

                <div class="tabla-container">
                    <table id="tablaUsuarios">
                        <thead>
                            <tr>
                                <th>Nombre</th>
                                <th>Correo</th>
                                <th>Rol</th>
                                <th>Programa</th>
                                <th>Documento</th>
                                <th>Estado</th>
                                <th>Conexión</th>
                                <th>Acciones</th>
                            </tr>
                        </thead>
                        <tbody>
                            ${usuarios.map(u => `
                                <tr id="usuario-${u.id}">
                                    <td>${u.nombre} ${u.apellido}</td>
                                    <td>${u.correo}</td>
                                    <td>${u.rol}</td>
                                    <td>${u.programa || '-'}</td>
                                    <td>${u.documento || '-'}</td>
                                    <td>${u.activo ? '🟢 Activo' : '🔴 Inactivo'}</td>
                                    <td>${u.online ? '🟢 Online' : '⚫ Offline'}</td>
                                    <td>
                                        <button onclick="editarUsuario(${u.id})">Editar</button>
                                        <button onclick="cambiarEstadoUsuario(${u.id})">
                                            ${u.activo ? 'Desactivar' : 'Activar'}
                                        </button>
                                    </td>
                                </tr>
                            `).join('')}
                        </tbody>
                    </table>
                </div>
            </div>
        </div>`;

        document.body.insertAdjacentHTML('beforeend', html);
        setupFiltroTabla('filtroUsuarios', 'tablaUsuarios');

    } catch (error) {
        console.error(error);
        alert('Error cargando usuarios');
    }
}

// ✅ CORREGIDO
window.cambiarEstadoUsuario = async function (id) {
    try {
        const usuario = await api.get(API_ENDPOINTS.usuarios.obtener(id));
        const nuevoEstado = !usuario.activo;

        const actualizado = await api.put(
            API_ENDPOINTS.usuarios.cambiarEstado(id),
            { activo: nuevoEstado }
        );

        const fila = document.querySelector(`#usuario-${id}`);
        fila.children[5].textContent = actualizado.activo ? '🟢 Activo' : '🔴 Inactivo';
        fila.children[7].children[1].textContent = actualizado.activo ? 'Desactivar' : 'Activar';

    } catch (e) {
        console.error(e);
        alert('Error cambiando estado del usuario');
    }
};

// ✅ CORREGIDO
window.editarUsuario = async function (id) {
    try {
        const usuario = await api.get(API_ENDPOINTS.usuarios.obtener(id));

        const nuevoNombre = prompt("Nuevo nombre:", usuario.nombre) ?? usuario.nombre;
        const nuevoApellido = prompt("Nuevo apellido:", usuario.apellido) ?? usuario.apellido;
        const nuevoCorreo = prompt("Nuevo correo:", usuario.correo) ?? usuario.correo;
        const nuevoPrograma = prompt("Programa:", usuario.programa || "") ?? usuario.programa;
        const nuevoDocumento = prompt("Documento:", usuario.documento || "") ?? usuario.documento;

        if (!nuevoNombre || !nuevoApellido || !nuevoCorreo) {
            return alert("Nombre, apellido y correo son obligatorios");
        }

        const payload = {
            ...usuario,
            nombre: nuevoNombre,
            apellido: nuevoApellido,
            correo: nuevoCorreo,
            programa: nuevoPrograma,
            documento: nuevoDocumento
        };

        const actualizado = await api.put(API_ENDPOINTS.usuarios.actualizar(id), payload);

        const fila = document.querySelector(`#usuario-${id}`);
        fila.children[0].textContent = `${actualizado.nombre} ${actualizado.apellido}`;
        fila.children[1].textContent = actualizado.correo;
        fila.children[3].textContent = actualizado.programa || '-';
        fila.children[4].textContent = actualizado.documento || '-';

    } catch (e) {
        console.error(e);
        alert("Error al editar usuario");
    }
};

// ✅ CORREGIDO
window.agregarUsuario = async function () {
    const nombre = document.getElementById('usuarioNombre').value.trim();
    const apellido = document.getElementById('usuarioApellido').value.trim();
    const correo = document.getElementById('usuarioCorreo').value.trim();
    const password = document.getElementById('usuarioPassword').value;
    const rol = document.getElementById('usuarioRol').value;
    const programa = document.getElementById('usuarioPrograma').value.trim();
    const documento = document.getElementById('usuarioDocumento').value.trim();

    if (!nombre || !apellido || !correo || !password || !rol) {
        return alert("Por favor complete los campos obligatorios (nombre, apellido, correo, contraseña y rol)");
    }

    if (!correo.includes('@')) {
        return alert("Por favor ingrese un correo válido");
    }

    try {
        await api.post(API_ENDPOINTS.usuarios.base, {
            nombre,
            apellido,
            correo,
            password,
            rol,
            programa: programa || null,
            documento: documento || null,
            activo: true,
            online: false
        });

        cerrarModal();
        reporteUsuarios();

    } catch (e) {
        console.error(e);
        alert("Error agregando usuario: " + (e.message || "Error desconocido"));
    }
};
