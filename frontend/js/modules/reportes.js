import { cerrarModal, formatFecha, setupFiltroTabla } from './ui.js';

// ========== NUEVO PANEL DE REPORTES CON BOTONES ==========
export async function mostrarReportes() {
    let html = `
        <div class="modal">
            <div class="modal-content">
                <span class="close" onclick="cerrarModal()">&times;</span>
                <h2>📊 Selecciona un Reporte</h2>

                <div class="reportes-grid">
                    <button class="reporte-card" onclick="reporteUsuarios()">
                        <h3>👤 Usuarios</h3>
                    </button>
                    <button class="reporte-card" onclick="reporteLaboratorios()">
                        <h3>🧪 Laboratorios</h3>
                    </button>
                    <button class="reporte-card" onclick="reporteElementos()">
                        <h3>📦 Elementos</h3>
                    </button>
                    <button class="reporte-card" onclick="reporteReservas()">
                        <h3>📅 Reservas</h3>
                    </button>
                    <button class="reporte-card" onclick="reportePrestamos()">
                        <h3>📘 Préstamos</h3>
                    </button>
                </div>
            </div>
        </div>
    `;

    document.body.insertAdjacentHTML('beforeend', html);
}

export async function reporteUsuarios() {
    try {
        // Cerrar modal anterior
        cerrarModal();
        await new Promise(res => setTimeout(res, 80));

        const usuarios = await api.get(API_ENDPOINTS.usuarios);

        let html = `
            <div class="modal">
                <div class="modal-content">
                    <button class="btn-volver" onclick="volverAReportes()">⬅ Volver</button>
                    <span class="close" onclick="cerrarModal()">&times;</span>
                    <h2>👤 Reporte de Usuarios</h2>

                    <input type="text" id="filtroUsuario" placeholder="Filtrar por nombre..." class="input-filtro">

                    <div class="tabla-container">
                        <table id="tablaUsuarios">
                            <thead>
                                <tr>
                                    <th>ID</th>
                                    <th>Nombre</th>
                                    <th>Correo</th>
                                    <th>Estado</th>
                                </tr>
                            </thead>
                            <tbody> 
                                ${usuarios.map(u => `
                                    <tr>
                                        <td>${u.id}</td>
                                        <td>${u.nombre} ${u.apellido}</td>
                                        <td>${u.correo}</td>
                                        <td>${u.activo ? "Activo" : "Inactivo"}</td>
                                    </tr>
                                `).join('')}
                            </tbody>
                        </table>
                    </div>
                </div>
            </div>
        `;

        document.body.insertAdjacentHTML('beforeend', html);
        setupFiltroTabla("filtroUsuario", "tablaUsuarios");

    } catch (e) {
        alert("Error al cargar usuarios");
    }
}

export async function reporteLaboratorios() {
    try {
        // Cerrar modal anterior
        cerrarModal();
        await new Promise(res => setTimeout(res, 80));
        const labs = await api.get(API_ENDPOINTS.laboratorios);

        let html = `
            <div class="modal">
                <div class="modal-content">
                    <button class="btn-volver" onclick="volverAReportes()">⬅ Volver</button>
                    <span class="close" onclick="cerrarModal()">&times;</span>
                    <h2>🧪 Reporte de Laboratorios</h2>

                    <input type="text" id="filtroLab" placeholder="Filtrar por nombre..." class="input-filtro">

                    <div class="tabla-container">
                        <table id="tablaLabs">
                            <thead>
                                <tr>
                                    <th>ID</th>
                                    <th>Nombre</th>
                                    <th>Capacidad</th>
                                    <th>Estado</th>
                                </tr>
                            </thead>
                            <tbody>
                                ${labs.map(l => `
                                    <tr>
                                        <td>${l.id}</td>
                                        <td>${l.nombre}</td>
                                        <td>${l.capacidad}</td>
                                        <td>${l.estado}</td>
                                    </tr>
                                `).join('')}
                            </tbody>
                        </table>
                    </div>
                </div>
            </div>
        `;

        document.body.insertAdjacentHTML('beforeend', html);
        setupFiltroTabla("filtroLab", "tablaLabs");

    } catch (e) {
        alert("Error al cargar laboratorios");
    }
}

export async function reporteElementos() {
    try {
        cerrarModal();
        await new Promise(res => setTimeout(res, 80));

        const elementos = await api.get(API_ENDPOINTS.elementos);

        let html = `
            <div class="modal">
                <div class="modal-content">
                    <button class="btn-volver" onclick="volverAReportes()">⬅ Volver</button>
                    <span class="close" onclick="cerrarModal()">&times;</span>
                    <h2>📦 Reporte de Elementos</h2>

                    <input type="text" id="filtroElem" placeholder="Filtrar por nombre..." class="input-filtro">

                    <div class="tabla-container">
                        <table id="tablaElementos">
                            <thead>
                                <tr>
                                    <th>ID</th>
                                    <th>Nombre</th>
                                    <th>Descripción</th>
                                    <th>Total</th>
                                    <th>Disponible</th>
                                    <th>Estado</th>
                                </tr>
                            </thead>
                            <tbody>
                                ${elementos.map(e => `
                                    <tr>
                                        <td>${e.id}</td>
                                        <td>${e.nombre}</td>
                                        <td>${e.descripcion}</td>
                                        <td>${e.cantidadTotal}</td>
                                        <td>${e.cantidadDisponible}</td>
                                        <td>${e.estado}</td>
                                    </tr>
                                `).join('')}
                            </tbody>
                        </table>
                    </div>
                </div>
            </div>
        `;

        document.body.insertAdjacentHTML('beforeend', html);
        setupFiltroTabla("filtroElem", "tablaElementos");

    } catch (e) {
        alert("Error al cargar elementos");
    }
}

export async function reporteReservas() {
    try {
        cerrarModal();
        await new Promise(res => setTimeout(res, 80));

        const reservas = await api.get(API_ENDPOINTS.reservas);

        let html = `
            <div class="modal">
                <div class="modal-content">
                    <button class="btn-volver" onclick="volverAReportes()">⬅ Volver</button>
                    <span class="close" onclick="cerrarModal()">&times;</span>
                    <h2>📅 Reporte de Reservas</h2>

                    <input type="text" id="filtroReserva" placeholder="Filtrar por usuario o laboratorio..." class="input-filtro">

                    <div class="tabla-container">
                        <table id="tablaReservas">
                            <thead>
                                <tr>
                                    <th>ID</th>
                                    <th>Usuario</th>
                                    <th>Laboratorio</th>
                                    <th>Fecha Inicio</th>
                                    <th>Fecha Fin</th>
                                    <th>Estado</th>
                                </tr>
                            </thead>
                            <tbody>
                                ${reservas.map(r => `
                                    <tr>
                                        <td>${r.id}</td>
                                        <td>${r.usuario?.nombre ?? "Sin usuario"}</td>
                                        <td>${r.laboratorio?.nombre ?? "Sin laboratorio"}</td>
                                        <td>${formatFecha(r.fechaInicio)}</td>
                                        <td>${formatFecha(r.fechaFin)}</td>
                                        <td>${r.estado}</td>
                                    </tr>
                                `).join('')}
                            </tbody>
                        </table>
                    </div>
                </div>
            </div>
        `;

        document.body.insertAdjacentHTML('beforeend', html);
        setupFiltroTabla("filtroReserva", "tablaReservas");

    } catch (e) {
        alert("Error al cargar reservas");
    }
}

export async function reportePrestamos() {
    try {
        cerrarModal();
        await new Promise(res => setTimeout(res, 80));

        const prestamos = await api.get(API_ENDPOINTS.prestamos);

        let html = `
            <div class="modal">
                <div class="modal-content">
                    <button class="btn-volver" onclick="volverAReportes()">⬅ Volver</button>
                    <span class="close" onclick="cerrarModal()">&times;</span>
                    <h2>📘 Reporte de Préstamos</h2>

                    <input type="text" id="filtroPrestamo" placeholder="Filtrar por usuario o elemento..." class="input-filtro">

                    <div class="tabla-container">
                        <table id="tablaPrestamos">
                            <thead>
                                <tr>
                                    <th>ID</th>
                                    <th>Usuario</th>
                                    <th>Elemento</th>
                                    <th>Fecha Préstamo</th>
                                    <th>Fecha Devolución</th>
                                    <th>Estado</th>
                                </tr>
                            </thead>
                            <tbody>
                                ${prestamos.map(p => `
                                    <tr>
                                        <td>${p.id}</td>
                                        <td>${p.usuario?.nombre ?? "Desconocido"}</td>
                                        <td>${p.elemento?.nombre ?? "Desconocido"}</td>
                                        <td>${formatFecha(p.fechaPrestamo)}</td>
                                        <td>${p.fechaDevolucion ? formatFecha(p.fechaDevolucion) : "—"}</td>
                                        <td>${p.estado}</td>
                                    </tr>
                                `).join('')}
                            </tbody>
                        </table>
                    </div>
                </div>
            </div>
        `;

        document.body.insertAdjacentHTML('beforeend', html);
        setupFiltroTabla("filtroPrestamo", "tablaPrestamos");

    } catch (e) {
        alert("Error al cargar préstamos");
    }
}

export function volverAReportes() {
    cerrarModal();
    setTimeout(() => mostrarReportes(), 150);
}
