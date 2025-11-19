import { cerrarModal, formatearFecha } from './ui.js';

export async function mostrarReservas() {
  try {
    const reservas = await api.get(API_ENDPOINTS.reservas);
    const pendientes = reservas.filter(r => r.estado === 'Pendiente');
    
    let html = `
      <div class="modal">
        <div class="modal-content">
          <span class="close">&times;</span>
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
                  <th>Tipo</th>
                  <th>Estado</th>
                  <th>Acciones</th>
                </tr>
              </thead>
              <tbody>
                ${pendientes.length > 0 ? pendientes.map(r => `
                  <tr>
                    <td>${r.id}</td>
                    <td>${r.usuario.nombre} ${r.usuario.apellido}</td>
                    <td>${r.laboratorio.nombre}</td>
                    <td>${formatearFecha(r.fechaInicio)}</td>
                    <td>${formatearFecha(r.fechaFin)}</td>
                    <td><span class="badge-tipo">${r.tipoReserva || 'N/A'}</span></td>
                    <td><span class="badge pendiente">${r.estado}</span></td>
                    <td>
                      <button class="btn-ver-detalles" data-id="${r.id}">👁 Ver</button>
                      <button class="btn-aprobar" data-id="${r.id}">✓ Aprobar</button>
                      <button class="btn-rechazar" data-id="${r.id}">✗ Rechazar</button>
                    </td>
                  </tr>
                `).join('') : '<tr><td colspan="8" style="text-align:center">No hay reservas pendientes</td></tr>'}
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
                  <th>Tipo</th>
                  <th>Estado</th>
                  <th>Acciones</th>
                </tr>
              </thead>
              <tbody>
                ${reservas.map(r => `
                  <tr>
                    <td>${r.id}</td>
                    <td>${r.usuario.nombre} ${r.usuario.apellido}</td>
                    <td>${r.laboratorio.nombre}</td>
                    <td>${formatearFecha(r.fechaInicio)}</td>
                    <td><span class="badge-tipo">${r.tipoReserva || 'N/A'}</span></td>
                    <td><span class="badge ${r.estado.toLowerCase()}">${r.estado}</span></td>
                    <td>
                      <button class="btn-ver-detalles" data-id="${r.id}">👁 Ver Detalles</button>
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
    
    // Configurar event listeners
    const modal = document.querySelector('.modal');
    
    modal.querySelector('.close').addEventListener('click', cerrarModal);
    
    modal.addEventListener('click', (e) => {
      if (e.target === modal) cerrarModal();
    });
    
    modal.querySelectorAll('.btn-ver-detalles').forEach(btn => {
      btn.addEventListener('click', () => verDetallesReserva(btn.dataset.id));
    });
    
    modal.querySelectorAll('.btn-aprobar').forEach(btn => {
      btn.addEventListener('click', () => aprobarReserva(btn.dataset.id));
    });
    
    modal.querySelectorAll('.btn-rechazar').forEach(btn => {
      btn.addEventListener('click', () => rechazarReserva(btn.dataset.id));
    });
    
  } catch (error) {
    console.error('Error:', error);
    alert('Error al cargar las reservas');
  }
}

export async function verDetallesReserva(id) {
  try {
    // Obtener detalles completos de la reserva
    const reserva = await api.get(`${API_ENDPOINTS.reservas}/${id}`);
    
    // Los invitados deberían venir incluidos en reserva.invitados
    // Si no vienen, este array estará vacío
    const invitados = reserva.invitados || [];
    
    const detalleHtml = `
      <div class="modal-detalle">
        <div class="modal-content-detalle">
          <span class="close">&times;</span>
          <h2>Detalles de la Reserva #${reserva.id}</h2>
          
          <div class="detalle-grid">
            <div class="detalle-seccion">
              <h3>📋 Información General</h3>
              <p><strong>Estado:</strong> <span class="badge ${reserva.estado.toLowerCase()}">${reserva.estado}</span></p>
              <p><strong>Tipo:</strong> ${reserva.tipoReserva || 'No especificado'}</p>
              <p><strong>Título:</strong> ${reserva.titulo || 'Sin título'}</p>
              <p><strong>Descripción:</strong> ${reserva.descripcion || 'Sin descripción'}</p>
            </div>
            
            <div class="detalle-seccion">
              <h3>👤 Usuario Solicitante</h3>
              <p><strong>Nombre:</strong> ${reserva.usuario.nombre} ${reserva.usuario.apellido}</p>
              <p><strong>Correo:</strong> ${reserva.usuario.correo}</p>
              <p><strong>Programa:</strong> ${reserva.usuario.programa || 'No especificado'}</p>
            </div>
            
            <div class="detalle-seccion">
              <h3>🏫 Laboratorio</h3>
              <p><strong>Nombre:</strong> ${reserva.laboratorio.nombre}</p>
              <p><strong>Ubicación:</strong> ${reserva.laboratorio.ubicacion}</p>
              <p><strong>Capacidad:</strong> ${reserva.laboratorio.capacidad} personas</p>
            </div>
            
            <div class="detalle-seccion">
              <h3>📅 Fechas y Horarios</h3>
              <p><strong>Inicio:</strong> ${formatearFecha(reserva.fechaInicio)}</p>
              <p><strong>Fin:</strong> ${formatearFecha(reserva.fechaFin)}</p>
              <p><strong>Duración:</strong> ${calcularDuracion(reserva.fechaInicio, reserva.fechaFin)}</p>
            </div>
            
            ${reserva.nrcCurso ? `
            <div class="detalle-seccion">
              <h3>📚 Curso</h3>
              <p><strong>NRC:</strong> ${reserva.nrcCurso}</p>
              <p><strong>Nombre:</strong> ${reserva.curso?.nombre || 'No disponible'}</p>
            </div>
            ` : ''}
            
            ${invitados && invitados.length > 0 ? `
            <div class="detalle-seccion full-width">
              <h3>👥 Estudiantes Adicionales (${invitados.length})</h3>
              <table class="tabla-invitados">
                <thead>
                  <tr>
                    <th>Documento</th>
                    <th>Nombre</th>
                    <th>Apellido</th>
                  </tr>
                </thead>
                <tbody>
                  ${invitados.map(inv => `
                    <tr>
                      <td>${inv.documento || 'N/A'}</td>
                      <td>${inv.nombre}</td>
                      <td>${inv.apellido}</td>
                    </tr>
                  `).join('')}
                </tbody>
              </table>
            </div>
            ` : ''}
            
            <div class="detalle-seccion">
              <h3>ℹ️ Información Adicional</h3>
              <p><strong>Cantidad Estudiantes:</strong> ${reserva.cantidadEstudiantes || 1}</p>
              <p><strong>Es Recurrente:</strong> ${reserva.esRecurrente ? 'Sí' : 'No'}</p>
              <p><strong>Fecha Creación:</strong> ${formatearFecha(reserva.fechaCreacion)}</p>
            </div>
          </div>
          
          <div class="acciones-detalle">
            ${reserva.estado === 'Pendiente' ? `
              <button class="btn-aprobar" data-id="${reserva.id}">✓ Aprobar Reserva</button>
              <button class="btn-rechazar" data-id="${reserva.id}">✗ Rechazar Reserva</button>
            ` : ''}
            <button class="btn-cerrar">Cerrar</button>
          </div>
        </div>
      </div>
    `;
    
    document.body.insertAdjacentHTML('beforeend', detalleHtml);
    
    // Event listeners para el modal de detalles
    const modalDetalle = document.querySelector('.modal-detalle');
    
    modalDetalle.querySelector('.close').addEventListener('click', () => {
      modalDetalle.remove();
    });
    
    modalDetalle.querySelector('.btn-cerrar').addEventListener('click', () => {
      modalDetalle.remove();
    });
    
    modalDetalle.addEventListener('click', (e) => {
      if (e.target === modalDetalle) modalDetalle.remove();
    });
    
    const btnAprobarDetalle = modalDetalle.querySelector('.btn-aprobar');
    if (btnAprobarDetalle) {
      btnAprobarDetalle.addEventListener('click', async () => {
        modalDetalle.remove();
        await aprobarReserva(reserva.id);
      });
    }
    
    const btnRechazarDetalle = modalDetalle.querySelector('.btn-rechazar');
    if (btnRechazarDetalle) {
      btnRechazarDetalle.addEventListener('click', async () => {
        modalDetalle.remove();
        await rechazarReserva(reserva.id);
      });
    }
    
  } catch (error) {
    console.error('Error al cargar detalles:', error);
    alert('Error al cargar los detalles de la reserva');
  }
}

function calcularDuracion(inicio, fin) {
  const diff = new Date(fin) - new Date(inicio);
  const horas = Math.floor(diff / (1000 * 60 * 60));
  const minutos = Math.floor((diff % (1000 * 60 * 60)) / (1000 * 60));
  return `${horas}h ${minutos}m`;
}

export async function aprobarReserva(id) {
  try {
    await api.put(`${API_ENDPOINTS.reservas}/${id}/aprobar`, {});
    alert('Reserva aprobada exitosamente');
    cerrarModal();
    document.querySelector('.modal-detalle')?.remove();
    mostrarReservas();
  } catch (error) {
    console.error('Error al aprobar:', error);
    alert('Error al aprobar la reserva');
  }
}

export async function rechazarReserva(id) {
  if (confirm('¿Estás seguro de rechazar esta reserva?')) {
    try {
      await api.put(`${API_ENDPOINTS.reservas}/${id}/rechazar`, {});
      alert('Reserva rechazada');
      cerrarModal();
      document.querySelector('.modal-detalle')?.remove();
      mostrarReservas();
    } catch (error) {
      console.error('Error al rechazar:', error);
      alert('Error al rechazar la reserva');
    }
  }
}