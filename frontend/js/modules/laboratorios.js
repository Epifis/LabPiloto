import { cerrarModal, setupFiltroTabla } from './ui.js';

export async function reporteLaboratorio() {
    try {
        cerrarModal();
        await new Promise(res => setTimeout(res, 80));

        const labs = await api.get(API_ENDPOINTS.laboratorios);

        const html = `
        <div class="modal">
            <div class="modal-content">
                <span class="close" onclick="cerrarModal()">&times;</span>
                <h2>🏫 Inventario de Laboratorios</h2>

                <input type="text" id="filtroLab" placeholder="Filtrar por nombre o ubicación..." class="input-filtro">

                <div class="tabla-container">
                    <table id="tablaLaboratorios">
                        <thead>
                            <tr>
                                <th>Nombre</th>
                                <th>Ubicación</th>
                                <th>Capacidad</th>
                                <th>Disponible</th>
                                <th>Estado</th>
                                <th>Descripción</th>
                                <th>Acciones</th>
                            </tr>
                        </thead>
                        <tbody>
                            ${labs.map(l => `
                                <tr id="lab-${l.id}">
                                    <td>${l.nombre}</td>
                                    <td>${l.ubicacion}</td>
                                    <td>${l.capacidad}</td>
                                    <td>${l.capacidadDisponible}</td>
                                    <td>${l.estado}</td>
                                    <td>${l.descripcion || ''}</td>
                                    <td>
                                        <button onclick="editarLaboratorio(${l.id})">Editar</button>
                                        <button onclick="cambiarEstado(${l.id})">
                                            ${l.estado === 'Activo' ? 'Inactivar' : 'Activar'}
                                        </button>
                                    </td>
                                </tr>
                            `).join('')}
                        </tbody>
                    </table>
                </div>

                <h3>➕ Agregar laboratorio</h3>
                <input type="text" id="labNombre" placeholder="Nombre">
                <input type="text" id="labUbicacion" placeholder="Ubicación">
                <input type="number" id="labCapacidad" placeholder="Capacidad" min="1">
                <input type="text" id="labDescripcion" placeholder="Descripción">
                <button onclick="agregarLaboratorio()">Agregar</button>
            </div>
        </div>`;

        document.body.insertAdjacentHTML('beforeend', html);

        setupFiltroTabla('filtroLab', 'tablaLaboratorios');

    } catch (error) {
        console.error(error);
        alert('Error cargando laboratorios');
    }
}

/* =======================================
   CAMBIAR ESTADO ACTIVO / INACTIVO
======================================= */
window.cambiarEstado = async function (id) {
    try {
        const lab = await api.get(`${API_ENDPOINTS.laboratorios}/${id}`);
        const nuevoEstado = lab.estado === 'Activo' ? 'Inactivo' : 'Activo';

        const payload = { ...lab, estado: nuevoEstado };

        const actualizado = await api.put(`${API_ENDPOINTS.laboratorios}/${id}`, payload);

        document.querySelector(`#lab-${id}`).children[4].textContent = actualizado.estado;
        document.querySelector(`#lab-${id}`).children[6].children[1].textContent =
            actualizado.estado === 'Activo' ? 'Inactivar' : 'Activar';

    } catch (e) {
        console.error(e);
        alert('Error cambiando estado');
    }
};

/* =======================================
   EDITAR LABORATORIO
======================================= */
window.editarLaboratorio = async function (id) {
    try {
        const lab = await api.get(`${API_ENDPOINTS.laboratorios}/${id}`);

        const nuevoNombre = prompt("Nuevo nombre:", lab.nombre) ?? lab.nombre;
        const nuevaUbicacion = prompt("Nueva ubicación:", lab.ubicacion) ?? lab.ubicacion;
        const nuevaCapacidad = parseInt(prompt("Nueva capacidad:", lab.capacidad)) || lab.capacidad;
        const nuevaDisp = parseInt(prompt("Capacidad disponible:", lab.capacidadDisponible)) || lab.capacidadDisponible;
        const nuevaDesc = prompt("Descripción:", lab.descripcion) ?? lab.descripcion;

        if (nuevaCapacidad < 0 || nuevaDisp < 0) return alert("Valores no pueden ser negativos");

        const payload = {
            ...lab,
            nombre: nuevoNombre,
            ubicacion: nuevaUbicacion,
            capacidad: nuevaCapacidad,
            capacidadDisponible: nuevaDisp,
            descripcion: nuevaDesc
        };

        const actualizado = await api.put(`${API_ENDPOINTS.laboratorios}/${id}`, payload);

        const fila = document.querySelector(`#lab-${id}`);
        fila.children[0].textContent = actualizado.nombre;
        fila.children[1].textContent = actualizado.ubicacion;
        fila.children[2].textContent = actualizado.capacidad;
        fila.children[3].textContent = actualizado.capacidadDisponible;
        fila.children[5].textContent = actualizado.descripcion;

    } catch (e) {
        console.error(e);
        alert("Error al editar laboratorio");
    }
};

/* =======================================
   AGREGAR LABORATORIO NUEVO
======================================= */
window.agregarLaboratorio = async function () {
    const nombre = document.getElementById('labNombre').value;
    const ubicacion = document.getElementById('labUbicacion').value;
    const capacidad = parseInt(document.getElementById('labCapacidad').value) || 0;
    const descripcion = document.getElementById('labDescripcion').value;

    if (!nombre || !capacidad) return alert("Datos insuficientes");

    try {
        await api.post(API_ENDPOINTS.laboratorios, {
            nombre,
            ubicacion,
            capacidad,
            capacidadDisponible: capacidad,
            estado: 'Activo',
            descripcion
        });

        cerrarModal();
        reporteLaboratorios();

    } catch (e) {
        console.error(e);
        alert("Error agregando laboratorio");
    }
};
