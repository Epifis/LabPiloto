import { cerrarModal } from './ui.js';

export async function mostrarProductos() {
  try {
    const prestamos = await api.get(API_ENDPOINTS.prestamos);
    const pendientes = prestamos.filter(p => p.estado === 'Pendiente');
    
    let html = `
      <div class="modal">
        <div class="modal-content">
          <span class="close" onclick="cerrarModal()">&times;</span>
          <h2>Gestión de Préstamos</h2>
          <h3>Solicitudes Pendientes (${pendientes.length})</h3>
          <div class="tabla-container">
            <table>
              <thead>
                <tr>
                  <th>ID</th>
                  <th>Usuario</th>
                  <th>Elemento</th>
                  <th>Fecha Préstamo</th>
                  <th>Estado</th>
                  <th>Acciones</th>
                </tr>
              </thead>
              <tbody>
                ${pendientes.map(p => `
                  <tr>
                    <td>${p.id}</td>
                    <td>${p.usuario.nombre} ${p.usuario.apellido}</td>
                    <td>${p.elemento.nombre}</td>
                    <td>${formatearFecha(p.fechaPrestamo)}</td>
                    <td><span class="badge pendiente">${p.estado}</span></td>
                    <td>
                      <button class="btn-aprobar" onclick="aprobarPrestamo(${p.id})">✓ Aprobar</button>
                      <button class="btn-rechazar" onclick="rechazarPrestamo(${p.id})">✗ Rechazar</button>
                    </td>
                  </tr>
                `).join('')}
              </tbody>
            </table>
          </div>

          <h3>Préstamos Activos</h3>
          <div class="tabla-container">
            <table>
              <thead>
                <tr>
                  <th>ID</th>
                  <th>Usuario</th>
                  <th>Elemento</th>
                  <th>Estado</th>
                  <th>Acciones</th>
                </tr>
              </thead>
              <tbody>
                ${prestamos.filter(p => p.estado === 'Aprobado').map(p => `
                  <tr>
                    <td>${p.id}</td>
                    <td>${p.usuario.nombre} ${p.usuario.apellido}</td>
                    <td>${p.elemento.nombre}</td>
                    <td><span class="badge aprobado">${p.estado}</span></td>
                    <td>
                      <button class="btn-devolver" onclick="devolverPrestamo(${p.id})">↩ Marcar como Devuelto</button>
                    </td>
                  </tr>
                `).join('')}
              </tbody>
            </table>
          </div>
        </div>
      </div>
    `;
    
    document.body.insertAdjacentHTML('beforeend', html);
  } catch (error) {
    console.error('Error:', error);
    alert('Error al cargar los préstamos');
  }
}

export async function aprobarPrestamo(id) {
  try {
    await api.put(`${API_ENDPOINTS.prestamos}/${id}/aprobar`, {});
    alert('Préstamo aprobado exitosamente');
    cerrarModal();
    mostrarProductos();
  } catch (error) {
    alert('Error al aprobar el préstamo');
  }
}

export async function rechazarPrestamo(id) {
  if (confirm('¿Estás seguro de rechazar este préstamo?')) {
    try {
      await api.put(`${API_ENDPOINTS.prestamos}/${id}/rechazar`, {});
      alert('Préstamo rechazado');
      cerrarModal();
      mostrarProductos();
    } catch (error) {
      alert('Error al rechazar el préstamo');
    }
  }
}

export async function devolverPrestamo(id) {
  try {
    await api.put(`${API_ENDPOINTS.prestamos}/${id}/devolver`, {});
    alert('Préstamo marcado como devuelto');
    cerrarModal();
    mostrarProductos();
  } catch (error) {
    alert('Error al marcar como devuelto');
  }
}

