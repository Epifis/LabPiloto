import { cerrarModal } from './ui.js';

export async function mostrarReservas() {
  try {
    const reservas = await api.get(API_ENDPOINTS.reservas);
    const pendientes = reservas.filter(r => r.estado === 'Pendiente');
    
    let html = `
      <div class="modal">
        <div class="modal-content">
          <span class="close" onclick="cerrarModal()">&times;</span>
          <h2>Gestión de Reservas</h2>
          <h3>Reservas Pendientes (${pendientes.length})</h3>
          <div class="tabla-container">
            <table>
              <thead>
                <tr>
                  <th>ID</th>
                  <th>Usuario</th>
                  <th>Laboratorio</th>
                  <th>Fecha Inicio</th>
                  <th>Fecha Fin</th>
                  <th>Estado</th>
                  <th>Acciones</th>
                </tr>
              </thead>
              <tbody>
                ${pendientes.map(r => `
                  <tr>
                    <td>${r.id}</td>
                    <td>${r.usuario.nombre} ${r.usuario.apellido}</td>
                    <td>${r.laboratorio.nombre}</td>
                    <td>${formatearFecha(r.fechaInicio)}</td>
                    <td>${formatearFecha(r.fechaFin)}</td>
                    <td><span class="badge pendiente">${r.estado}</span></td>
                    <td>
                      <button class="btn-aprobar" onclick="aprobarReserva(${r.id})">✓ Aprobar</button>
                      <button class="btn-rechazar" onclick="rechazarReserva(${r.id})">✗ Rechazar</button>
                    </td>
                  </tr>
                `).join('')}
              </tbody>
            </table>
          </div>
          
          <h3>Todas las Reservas (${reservas.length})</h3>
          <div class="tabla-container">
            <table>
              <thead>
                <tr>
                  <th>ID</th>
                  <th>Usuario</th>
                  <th>Laboratorio</th>
                  <th>Fecha Inicio</th>
                  <th>Estado</th>
                </tr>
              </thead>
              <tbody>
                ${reservas.map(r => `
                  <tr>
                    <td>${r.id}</td>
                    <td>${r.usuario.nombre} ${r.usuario.apellido}</td>
                    <td>${r.laboratorio.nombre}</td>
                    <td>${formatearFecha(r.fechaInicio)}</td>
                    <td><span class="badge ${r.estado.toLowerCase()}">${r.estado}</span></td>
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
    alert('Error al cargar las reservas');
  }
}

export async function aprobarReserva(id) {
  try {
    await api.put(`${API_ENDPOINTS.reservas}/${id}/aprobar`, {});
    alert('Reserva aprobada exitosamente');
    cerrarModal();
    mostrarReservas();
  } catch (error) {
    alert('Error al aprobar la reserva');
  }
}

export async function rechazarReserva(id) {
  if (confirm('¿Estás seguro de rechazar esta reserva?')) {
    try {
      await api.put(`${API_ENDPOINTS.reservas}/${id}/rechazar`, {});
      alert('Reserva rechazada');
      cerrarModal();
      mostrarReservas();
    } catch (error) {
      alert('Error al rechazar la reserva');
    }
  }
}

