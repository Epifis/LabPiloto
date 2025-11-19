import { cerrarModal, formatearFecha } from './ui.js';

// Agrupar préstamos por usuario y fecha
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

export async function mostrarProductos() {
  try {
    const prestamos = await api.get(API_ENDPOINTS.prestamos);
    const pendientes = prestamos.filter(p => p.estado === 'Pendiente');
    const activos = prestamos.filter(p => p.estado === 'Aprobado' || p.estado === 'Prestado');
    
    // Agrupar préstamos por usuario
    const gruposPendientes = agruparPrestamos(pendientes);
    const gruposActivos = agruparPrestamos(activos);
    
    let html = `
      <div class="modal">
        <div class="modal-content">
          <span class="close">&times;</span>
          <h2>Gestión de Préstamos</h2>
          
          <h3>Solicitudes Pendientes (${gruposPendientes.length})</h3>
          <div class="tabla-container">
            <table>
              <thead>
                <tr>
                  <th>Usuario</th>
                  <th>Elementos Solicitados</th>
                  <th>Fecha Préstamo</th>
                  <th>Estado</th>
                  <th>Acciones</th>
                </tr>
              </thead>
              <tbody>
                ${gruposPendientes.length > 0 ? gruposPendientes.map(grupo => `
                  <tr>
                    <td>${grupo.usuario.nombre} ${grupo.usuario.apellido}</td>
                    <td>
                      <ul style="margin: 0; padding-left: 20px;">
                        ${grupo.elementos.map(e => `<li>${e.nombre}</li>`).join('')}
                      </ul>
                    </td>
                    <td>${formatearFecha(grupo.fechaPrestamo)}</td>
                    <td><span class="badge pendiente">${grupo.estado}</span></td>
                    <td>
                      <button class="btn-aprobar" data-ids="${grupo.ids.join(',')}">✓ Aprobar Todo</button>
                      <button class="btn-rechazar" data-ids="${grupo.ids.join(',')}">✗ Rechazar Todo</button>
                    </td>
                  </tr>
                `).join('') : '<tr><td colspan="5" style="text-align:center">No hay solicitudes pendientes</td></tr>'}
              </tbody>
            </table>
          </div>

          <h3>Préstamos Activos (${gruposActivos.length})</h3>
          <div class="tabla-container">
            <table>
              <thead>
                <tr>
                  <th>Usuario</th>
                  <th>Elementos Prestados</th>
                  <th>Fecha Préstamo</th>
                  <th>Estado</th>
                  <th>Acciones</th>
                </tr>
              </thead>
              <tbody>
                ${gruposActivos.length > 0 ? gruposActivos.map(grupo => `
                  <tr>
                    <td>${grupo.usuario.nombre} ${grupo.usuario.apellido}</td>
                    <td>
                      <ul style="margin: 0; padding-left: 20px;">
                        ${grupo.elementos.map(e => `<li>${e.nombre}</li>`).join('')}
                      </ul>
                    </td>
                    <td>${formatearFecha(grupo.fechaPrestamo)}</td>
                    <td><span class="badge aprobado">${grupo.estado}</span></td>
                    <td>
                      <button class="btn-devolver" data-ids="${grupo.ids.join(',')}">↩ Marcar Devuelto</button>
                    </td>
                  </tr>
                `).join('') : '<tr><td colspan="5" style="text-align:center">No hay préstamos activos</td></tr>'}
              </tbody>
            </table>
          </div>

          <h3>Historial Completo (${prestamos.length})</h3>
          <div class="tabla-container">
            <table>
              <thead>
                <tr>
                  <th>ID</th>
                  <th>Usuario</th>
                  <th>Elemento</th>
                  <th>Fecha</th>
                  <th>Estado</th>
                  <th>Acciones</th>
                </tr>
              </thead>
              <tbody>
                ${prestamos.map(p => `
                  <tr>
                    <td>${p.id}</td>
                    <td>${p.usuario.nombre} ${p.usuario.apellido}</td>
                    <td>${p.elemento.nombre}</td>
                    <td>${formatearFecha(p.fechaPrestamo)}</td>
                    <td><span class="badge ${p.estado.toLowerCase()}">${p.estado}</span></td>
                    <td>
                      <button class="btn-ver-detalles" data-id="${p.id}">👁 Ver Detalles</button>
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
    
    // Cerrar modal
    modal.querySelector('.close').addEventListener('click', cerrarModal);
    
    // Click fuera del modal
    modal.addEventListener('click', (e) => {
      if (e.target === modal) cerrarModal();
    });
    
    // Botones de aprobar
    modal.querySelectorAll('.btn-aprobar').forEach(btn => {
      btn.addEventListener('click', () => {
        const ids = btn.dataset.ids.split(',').map(id => parseInt(id));
        aprobarPrestamos(ids);
      });
    });
    
    // Botones de rechazar
    modal.querySelectorAll('.btn-rechazar').forEach(btn => {
      btn.addEventListener('click', () => {
        const ids = btn.dataset.ids.split(',').map(id => parseInt(id));
        rechazarPrestamos(ids);
      });
    });
    
    // Botones de devolver
    modal.querySelectorAll('.btn-devolver').forEach(btn => {
      btn.addEventListener('click', () => {
        const ids = btn.dataset.ids.split(',').map(id => parseInt(id));
        devolverPrestamos(ids);
      });
    });
    
  } catch (error) {
    console.error('Error:', error);
    alert('Error al cargar los préstamos');
  }
}

export async function aprobarPrestamos(ids) {
  try {
    const promesas = ids.map(id => api.put(`${API_ENDPOINTS.prestamos}/${id}/aprobar`, {}));
    await Promise.all(promesas);
    alert('Préstamos aprobados exitosamente');
    cerrarModal();
    mostrarProductos();
  } catch (error) {
    console.error('Error al aprobar:', error);
    alert('Error al aprobar los préstamos');
  }
}

export async function rechazarPrestamos(ids) {
  if (confirm(`¿Estás seguro de rechazar ${ids.length} préstamo(s)?`)) {
    try {
      const promesas = ids.map(id => api.put(`${API_ENDPOINTS.prestamos}/${id}/rechazar`, {}));
      await Promise.all(promesas);
      alert('Préstamos rechazados');
      cerrarModal();
      mostrarProductos();
    } catch (error) {
      console.error('Error al rechazar:', error);
      alert('Error al rechazar los préstamos');
    }
  }
}

export async function devolverPrestamos(ids) {
  if (confirm(`¿Confirmar devolución de ${ids.length} préstamo(s)?`)) {
    try {
      const promesas = ids.map(id => api.put(`${API_ENDPOINTS.prestamos}/${id}/devolver`, {}));
      await Promise.all(promesas);
      alert('Préstamos marcados como devueltos');
      cerrarModal();
      mostrarProductos();
    } catch (error) {
      console.error('Error al marcar como devuelto:', error);
      alert('Error al procesar las devoluciones');
    }
  }
}