import { cerrarModal, formatearFecha } from './ui.js';

/* ============================================
   Agrupar préstamos por usuario + fecha
============================================ */
function agruparPrestamos(prestamos) {
  const grupos = {};

  prestamos.forEach(p => {
    const clave = `${p.usuario.id}_${p.fechaPrestamo}`;
    if (!grupos[clave]) {
      grupos[clave] = {
        usuario: p.usuario,
        fechaPrestamo: p.fechaPrestamo,
        estado: p.estado,
        elementos: [],
        ids: []
      };
    }
    grupos[clave].elementos.push(p.elemento);
    grupos[clave].ids.push(p.id);
  });

  return Object.values(grupos);
}

/* ============================================
   Cargar vista principal de préstamos
============================================ */
export async function mostrarProductos() {
  try {
    const prestamos = await api.get(API_ENDPOINTS.prestamos.listar);

    const pendientes = prestamos.filter(p => p.estado === 'Pendiente');
    const aprobados = prestamos.filter(p => p.estado === 'Aprobado');
    const activos = prestamos.filter(p => p.estado === 'Prestado');

    const gruposPendientes = agruparPrestamos(pendientes);
    const gruposAprobados = agruparPrestamos(aprobados);
    const gruposActivos = agruparPrestamos(activos);

    let html = `
      <div class="modal">
        <div class="modal-content">
          <span class="close">&times;</span>
          <h2>Gestión de Préstamos</h2>

          <!-- =======================
               PENDIENTES
          ======================= -->
          <h3>Solicitudes Pendientes (${gruposPendientes.length})</h3>
          <div class="tabla-container">
            <table>
              <thead>
                <tr>
                  <th>Usuario</th>
                  <th>Elementos Solicitados</th>
                  <th>Fecha</th>
                  <th>Estado</th>
                  <th>Acciones</th>
                </tr>
              </thead>
              <tbody>
                ${gruposPendientes.length > 0 ? gruposPendientes.map(gr =>
                  `<tr>
                    <td>${gr.usuario.nombre} ${gr.usuario.apellido}</td>
                    <td><ul>${gr.elementos.map(e => `<li>${e.nombre}</li>`).join('')}</ul></td>
                    <td>${formatearFecha(gr.fechaPrestamo)}</td>
                    <td><span class="badge pendiente">Pendiente</span></td>
                    <td>
                      <button class="btn-aprobar" data-ids="${gr.ids.join(',')}">✓ Aprobar</button>
                      <button class="btn-rechazar" data-ids="${gr.ids.join(',')}">✗ Rechazar</button>
                    </td>
                  </tr>`
                ).join('') : `<tr><td colspan="5" style="text-align:center">Sin solicitudes</td></tr>`}
              </tbody>
            </table>
          </div>


          <!-- =======================
               APROBADOS (LISTOS PARA ENTREGAR)
          ======================= -->
          <h3>Préstamos Aprobados - Pendientes de Entregar (${gruposAprobados.length})</h3>
          <div class="tabla-container">
            <table>
              <thead>
                <tr>
                  <th>Usuario</th>
                  <th>Elementos</th>
                  <th>Fecha</th>
                  <th>Estado</th>
                  <th>Acciones</th>
                </tr>
              </thead>
              <tbody>
                ${gruposAprobados.length > 0 ? gruposAprobados.map(gr =>
                  `<tr>
                    <td>${gr.usuario.nombre} ${gr.usuario.apellido}</td>
                    <td><ul>${gr.elementos.map(e => `<li>${e.nombre}</li>`).join('')}</ul></td>
                    <td>${formatearFecha(gr.fechaPrestamo)}</td>
                    <td><span class="badge aprobado">Aprobado</span></td>
                    <td>
                      <button class="btn-prestar" data-ids="${gr.ids.join(',')}">📦 Marcar Prestado</button>
                    </td>
                  </tr>`
                ).join('') : `<tr><td colspan="5" style="text-align:center">No hay préstamos aprobados</td></tr>`}
              </tbody>
            </table>
          </div>


          <!-- =======================
               ACTIVOS (PRESTADOS)
          ======================= -->
          <h3>Préstamos Activos (${gruposActivos.length})</h3>
          <div class="tabla-container">
            <table>
              <thead>
                <tr>
                  <th>Usuario</th>
                  <th>Elementos Prestados</th>
                  <th>Fecha</th>
                  <th>Estado</th>
                  <th>Acciones</th>
                </tr>
              </thead>
              <tbody>
                ${gruposActivos.length > 0 ? gruposActivos.map(gr =>
                  `<tr>
                    <td>${gr.usuario.nombre} ${gr.usuario.apellido}</td>
                    <td><ul>${gr.elementos.map(e => `<li>${e.nombre}</li>`).join('')}</ul></td>
                    <td>${formatearFecha(gr.fechaPrestamo)}</td>
                    <td><span class="badge activo">Prestado</span></td>
                    <td><button class="btn-devolver" data-ids="${gr.ids.join(',')}">↩ Marcar Devuelto</button></td>
                  </tr>`
                ).join('') : `<tr><td colspan="5" style="text-align:center">No hay activos</td></tr>`}
              </tbody>
            </table>
          </div>
        </div>
      </div>
    `;

    document.body.insertAdjacentHTML("beforeend", html);

    const modal = document.querySelector(".modal");
    modal.querySelector(".close").addEventListener("click", cerrarModal);
    modal.addEventListener("click", e => e.target === modal && cerrarModal());

    /* ===============================
       Eventos
    =============================== */
    modal.querySelectorAll(".btn-aprobar").forEach(btn =>
      btn.addEventListener("click", () => aprobarPrestamos(btn.dataset.ids.split(",")))
    );

    modal.querySelectorAll(".btn-rechazar").forEach(btn =>
      btn.addEventListener("click", () => rechazarPrestamos(btn.dataset.ids.split(",")))
    );

    modal.querySelectorAll(".btn-prestar").forEach(btn =>
      btn.addEventListener("click", () => marcarComoPrestado(btn.dataset.ids.split(",")))
    );

    modal.querySelectorAll(".btn-devolver").forEach(btn =>
      btn.addEventListener("click", () => devolverPrestamos(btn.dataset.ids.split(",")))
    );

  } catch (error) {
    console.error(error);
    alert("Error cargando préstamos");
  }
}

/* ============================================
   APROBAR
============================================ */
export async function aprobarPrestamos(ids) {
  try {
    await api.put(`${API_ENDPOINTS.prestamos.listar}/aprobar-lote`, ids);

    alert("Préstamos aprobados y notificados por correo");
    cerrarModal();
    mostrarProductos();
  } catch (e) {
    alert("Error aprobando préstamos");
  }
}

/* ============================================
   RECHAZAR
============================================ */
export async function rechazarPrestamos(ids) {
  if (!confirm("¿Rechazar los préstamos seleccionados?")) return;

  try {
    await api.put(`${API_ENDPOINTS.prestamos.listar}/rechazar-lote`, ids);

    alert("Préstamos rechazados y notificados por correo");
    cerrarModal();
    mostrarProductos();
  } catch (e) {
    alert("Error rechazando");
  }
}

/* ============================================
   🔥 NUEVO – MARCAR COMO PRESTADO
============================================ */
export async function marcarComoPrestado(ids) {
  if (!confirm("¿Confirmar entrega física de los elementos?")) return;

  try {
    const promesas = ids.map(id =>
      api.put(`${API_ENDPOINTS.prestamos.listar}/${id}/prestar`)
    );

    await Promise.all(promesas);

    alert("Elementos marcados como PRESTADOS");
    cerrarModal();
    mostrarProductos();

  } catch (error) {
    console.error(error);
    alert("Error al marcar como prestado");
  }
}

/* ============================================
   DEVOLVER
============================================ */
export async function devolverPrestamos(ids) {
  if (!confirm("¿Confirmar devolución?")) return;

  try {
    await Promise.all(ids.map(id => api.put(`${API_ENDPOINTS.prestamos.listar}/${id}/devolver`)));
    alert("Devolución correcta");
    cerrarModal();
    mostrarProductos();
  } catch (e) {
    alert("Error devolviendo préstamo");
  }
}
