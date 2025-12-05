import { cerrarModal, formatFecha, setupFiltroTabla } from './ui.js';

// ========== NUEVO PANEL DE REPORTES ==========
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

// --------------------------- USUARIOS ---------------------------
export async function reporteUsuarios() {
    try {
        cerrarModal();
        await new Promise(res => setTimeout(res, 80));

        const usuarios = await api.get(API_ENDPOINTS.usuarios.listar);

        let html = `
            <div class="modal">
                <div class="modal-content">
                    <button class="btn-volver" onclick="volverAReportes()">⬅ Volver</button>
                    <span class="close" onclick="cerrarModal()">&times;</span>
                    <h2>Reporte de Usuarios</h2>
                    <div class="acciones-reporte">
                        <button class="btn-descargar" onclick="descargarPDFUsuarios()">PDF</button>
                        <button class="btn-grafica" onclick="mostrarGraficaUsuarios()">Gráfica</button>
                    </div>

                    <canvas id="graficaUsuarios" style="margin-top:20px; display:none;"></canvas>

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

// --------------------------- LABORATORIOS ---------------------------
export async function reporteLaboratorios() {
    try {
        cerrarModal();
        await new Promise(res => setTimeout(res, 80));
        const labs = await api.get(API_ENDPOINTS.laboratorios.listar);

        let html = `
            <div class="modal">
                <div class="modal-content">
                    <button class="btn-volver" onclick="volverAReportes()">⬅ Volver</button>
                    <span class="close" onclick="cerrarModal()">&times;</span>
                    <h2>🧪 Reporte de Laboratorios</h2>
                    <div class="acciones-reporte">
                        <button class="btn-descargar" onclick="descargarPDFLaboratorios()">PDF</button>
                        <button class="btn-grafica" onclick="mostrarGraficaLaboratorios()">Gráfica</button>
                    </div>

                    <canvas id="graficaLaboratorios" style="margin-top:20px; display:none;"></canvas>

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

// --------------------------- ELEMENTOS ---------------------------
export async function reporteElementos() {
    try {
        cerrarModal();
        await new Promise(res => setTimeout(res, 80));

        const elementos = await api.get(API_ENDPOINTS.elementos.listar);

        let html = `
            <div class="modal">
                <div class="modal-content">
                    <button class="btn-volver" onclick="volverAReportes()">⬅ Volver</button>
                    <span class="close" onclick="cerrarModal()">&times;</span>
                    <h2>📦 Reporte de Elementos</h2>
                    <div class="acciones-reporte">
                        <button class="btn-descargar" onclick="descargarPDFElementos()">PDF</button>
                        <button class="btn-grafica" onclick="mostrarGraficaElementos()">Gráfica</button>
                    </div>

                    <canvas id="graficaElementos" style="margin-top:20px; display:none;"></canvas>

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

// --------------------------- RESERVAS ---------------------------
export async function reporteReservas() {
    try {
        cerrarModal();
        await new Promise(res => setTimeout(res, 80));

        const reservas = await api.get(API_ENDPOINTS.reservas.listar);

        let html = `
            <div class="modal">
                <div class="modal-content">
                    <button class="btn-volver" onclick="volverAReportes()">⬅ Volver</button>
                    <span class="close" onclick="cerrarModal()">&times;</span>
                    <h2>📅 Reporte de Reservas</h2>
                    <div class="acciones-reporte">
                        <button class="btn-descargar" onclick="descargarPDFReservas()">PDF</button>
                        <button class="btn-grafica" onclick="mostrarGraficaReservas()">Gráfica</button>
                    </div>

                    <canvas id="graficaReservas" style="margin-top:20px; display:none;"></canvas>

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

// --------------------------- PRÉSTAMOS ---------------------------
export async function reportePrestamos() {
    try {
        cerrarModal();
        await new Promise(res => setTimeout(res, 80));

        const prestamos = await api.get(API_ENDPOINTS.prestamos.listar);

        let html = `
            <div class="modal">
                <div class="modal-content">
                    <button class="btn-volver" onclick="volverAReportes()">⬅ Volver</button>
                    <span class="close" onclick="cerrarModal()">&times;</span>
                    <h2>📘 Reporte de Préstamos</h2>
                    <div class="acciones-reporte">
                        <button class="btn-descargar" onclick="descargarPDFPrestamos()">PDF</button>
                        <button class="btn-grafica" onclick="mostrarGraficaPrestamos()">Gráfica</button>
                    </div>

                    <canvas id="graficaPrestamos" style="margin-top:20px; display:none;"></canvas>

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

// ===================== DESCARGA PDF =====================
window.descargarPDF = function (titulo, tablaID, nombreArchivo) {
    const { jsPDF } = window.jspdf;
    const doc = new jsPDF();

    doc.setFontSize(18);
    doc.text(titulo, 14, 20);

    doc.autoTable({
        html: "#" + tablaID,
        startY: 30,
        theme: "striped",
        headStyles: { fillColor: [33, 150, 243] }
    });

    doc.save(nombreArchivo + ".pdf");
};

window.descargarPDFUsuarios = () => descargarPDF("Reporte de Usuarios", "tablaUsuarios", "reporte_usuarios");
window.descargarPDFLaboratorios = () => descargarPDF("Reporte de Laboratorios", "tablaLabs", "reporte_laboratorios");
window.descargarPDFElementos = () => descargarPDF("Reporte de Elementos", "tablaElementos", "reporte_elementos");
window.descargarPDFReservas = () => descargarPDF("Reporte de Reservas", "tablaReservas", "reporte_reservas");
window.descargarPDFPrestamos = () => descargarPDF("Reporte de Préstamos", "tablaPrestamos", "reporte_prestamos");

// ===================== GRÁFICAS =====================
let graficaActiva = null;

window.generarGrafica = function(idCanvas, etiquetas, datos, etiquetaY) {
    const canvas = document.getElementById(idCanvas);
    canvas.style.display = "block";

    if (graficaActiva) graficaActiva.destroy();

    graficaActiva = new Chart(canvas.getContext('2d'), {
        type: "bar",
        data: {
            labels: etiquetas,
            datasets: [{
                label: etiquetaY,
                data: datos,
            }]
        },
        options: {
            responsive: true,
            scales: {
                y: { beginAtZero: true }
            }
        }
    });
};

// ------------------ GRÁFICAS REPARADAS ------------------
window.mostrarGraficaUsuarios = async function() {
    const usuarios = await api.get(API_ENDPOINTS.usuarios.listar);
    const activos = usuarios.filter(u => u.activo).length;
    const inactivos = usuarios.length - activos;

    generarGrafica("graficaUsuarios", ["Activos", "Inactivos"], [activos, inactivos], "Cantidad de Usuarios");
};

window.mostrarGraficaLaboratorios = async function() {
    const labs = await api.get(API_ENDPOINTS.laboratorios.listar);
    generarGrafica("graficaLaboratorios", labs.map(l => l.nombre), labs.map(l => l.capacidad), "Capacidad");
};

window.mostrarGraficaElementos = async function() {
    const elementos = await api.get(API_ENDPOINTS.elementos.listar);
    generarGrafica("graficaElementos", elementos.map(e => e.nombre), elementos.map(e => e.cantidadTotal), "Cantidad Total");
};

window.mostrarGraficaReservas = async function() {
    const reservas = await api.get(API_ENDPOINTS.reservas.listar);
    const meses = Array.from({ length: 12 }, (_, i) => i + 1);
    const conteo = meses.map(m => reservas.filter(r => new Date(r.fechaInicio).getMonth() + 1 === m).length);
    generarGrafica("graficaReservas", meses, conteo, "Reservas por Mes");
};

window.mostrarGraficaPrestamos = async function() {
    const prestamos = await api.get(API_ENDPOINTS.prestamos.listar);
    const mapa = {};
    prestamos.forEach(p => mapa[p.usuario?.nombre || "?" ] = (mapa[p.usuario?.nombre || "?"] || 0) + 1);
    generarGrafica("graficaPrestamos", Object.keys(mapa), Object.values(mapa), "Préstamos por Usuario");
};
