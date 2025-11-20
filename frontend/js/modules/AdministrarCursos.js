import { cerrarModal, formatFecha, setupFiltroTabla } from './ui.js';

// ========== PANEL DE ADMINISTRAR CURSOS ==========
async function mostrarAdminCursos() {
    try {
        // Cargar datos necesarios
        const [profesores, cursos, laboratorios, reservasExistentes] = await Promise.all([
            api.get(API_ENDPOINTS.usuarios).then(users => 
                users.filter(u => u.rol === 'profesor' || u.rol === 'administrador')
            ),
            api.get(API_ENDPOINTS.cursos || `${API_BASE_URL}/cursos`).catch(() => []),
            api.get(API_ENDPOINTS.laboratorios),
            api.get(API_ENDPOINTS.reservas)
        ]);

        let html = `
            <div class="modal">
                <div class="modal-content" style="max-width: 1200px;">
                    <span class="close" onclick="cerrarModal()">&times;</span>
                    <h2>📚 Administrar Cursos y Clases</h2>
                    
                    <div class="admin-cursos-container">
                        <!-- Formulario para crear clases recurrentes -->
                        <div class="form-section">
                            <h3>➕ Programar Clase Recurrente</h3>
                            <form id="formClaseRecurrente" class="formulario">
                                <div class="form-row">
                                    <label>Profesor:
                                        <select id="selectProfesor" required>
                                            <option value="">Seleccionar profesor...</option>
                                            ${profesores.map(p => `
                                                <option value="${p.id}">${p.nombre} ${p.apellido}</option>
                                            `).join('')}
                                        </select>
                                    </label>
                                    
                                    <label>Curso (NRC):
                                        <select id="selectCurso" required>
                                            <option value="">Seleccionar curso...</option>
                                            ${cursos.map(c => `
                                                <option value="${c.nrc}">${c.nrc} - ${c.nombre}</option>
                                            `).join('')}
                                        </select>
                                    </label>
                                </div>
                                
                                <div class="form-row">
                                    <label>Laboratorio:
                                        <select id="selectLaboratorio" required onchange="actualizarCapacidad(this)">
                                            <option value="">Seleccionar laboratorio...</option>
                                            ${laboratorios.map(l => `
                                                <option value="${l.id}" data-capacidad="${l.capacidadDisponible}">
                                                    ${l.nombre} - Cap. disponible: ${l.capacidadDisponible}/${l.capacidad}
                                                </option>
                                            `).join('')}
                                        </select>
                                    </label>
                                    
                                    <label>Cantidad Estudiantes:
                                        <select id="selectCantidad" required>
                                            <option value="">Selecciona cantidad...</option>
                                            ${Array.from({length: 20}, (_, i) => i + 1).map(num => `
                                                <option value="${num}">${num} estudiante${num > 1 ? 's' : ''}</option>
                                            `).join('')}
                                        </select>
                                    </label>
                                </div>
                                
                                <div class="form-row">
                                    <label>Días de la Semana:
                                        <div class="dias-checkbox">
                                            <label><input type="checkbox" name="dias" value="1"> Lunes</label>
                                            <label><input type="checkbox" name="dias" value="2"> Martes</label>
                                            <label><input type="checkbox" name="dias" value="3"> Miércoles</label>
                                            <label><input type="checkbox" name="dias" value="4"> Jueves</label>
                                            <label><input type="checkbox" name="dias" value="5"> Viernes</label>
                                            <label><input type="checkbox" name="dias" value="6"> Sábado</label>
                                        </div>
                                    </label>
                                </div>
                                
                                <div class="form-row">
                                    <label>Hora Inicio:
                                        <select id="selectHoraInicio" required>
                                            <option value="">Selecciona hora...</option>
                                            ${generarOpcionesHoras()}
                                        </select>
                                    </label>
                                    
                                    <label>Hora Fin:
                                        <select id="selectHoraFin" required>
                                            <option value="">Selecciona hora...</option>
                                            ${generarOpcionesHoras()}
                                        </select>
                                    </label>
                                </div>
                                
                                <div class="form-row">
                                    <label>Fecha Inicio:
                                        <input type="date" id="fechaInicio" required>
                                    </label>
                                    
                                    <label>Semanas de Duración:
                                        <select id="selectSemanas" required>
                                            <option value="4">1 mes (4 semanas)</option>
                                            <option value="8">2 meses (8 semanas)</option>
                                            <option value="12">3 meses (12 semanas)</option>
                                        </select>
                                    </label>
                                </div>
                                
                                <button type="submit" class="btn-form">📅 Programar Clases Recurrentes</button>
                            </form>
                        </div>
                        
                        <!-- Lista de clases programadas -->
                        <div class="clases-section">
                            <h3>📋 Clases Programadas</h3>
                            <input type="text" id="filtroClases" placeholder="Filtrar por profesor o curso..." class="input-filtro">
                            
                            <div class="tabla-container">
                                <table id="tablaClases">
                                    <thead>
                                        <tr>
                                            <th>ID</th>
                                            <th>Profesor</th>
                                            <th>Curso</th>
                                            <th>Laboratorio</th>
                                            <th>Fecha</th>
                                            <th>Horario</th>
                                            <th>Estado</th>
                                            <th>Acciones</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        ${reservasExistentes.filter(r => r.tipoReserva === 'clase').map(r => `
                                            <tr>
                                                <td>${r.id}</td>
                                                <td>${r.usuario?.nombre || 'N/A'} ${r.usuario?.apellido || ''}</td>
                                                <td>${r.nrcCurso || 'N/A'}</td>
                                                <td>${r.laboratorio?.nombre || 'N/A'}</td>
                                                <td>${r.fechaInicio ? formatFecha(r.fechaInicio).split(' ')[0] : 'N/A'}</td>
                                                <td>${r.fechaInicio ? formatFecha(r.fechaInicio).split(' ')[1] : 'N/A'} - ${r.fechaFin ? formatFecha(r.fechaFin).split(' ')[1] : 'N/A'}</td>
                                                <td><span class="badge ${r.estado?.toLowerCase()}">${r.estado || 'N/A'}</span></td>
                                                <td>
                                                    <button class="btn-cancelar btn-sm" onclick="cancelarClase(${r.id})">🗑 Cancelar</button>
                                                </td>
                                            </tr>
                                        `).join('')}
                                    </tbody>
                                </table>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        `;

        document.body.insertAdjacentHTML('beforeend', html);
        
        // Configurar fecha mínima como hoy
        const fechaInput = document.getElementById('fechaInicio');
        fechaInput.min = new Date().toISOString().split('T')[0];
        fechaInput.value = new Date().toISOString().split('T')[0];
        
        // Configurar evento del formulario
        document.getElementById('formClaseRecurrente').addEventListener('submit', programarClasesRecurrentes);
        setupFiltroTabla("filtroClases", "tablaClases");
        
    } catch (error) {
        console.error('Error:', error);
        alert('Error al cargar datos para administrar cursos');
    }
}

// 🔥 FUNCIÓN SIMPLIFICADA PARA PROGRAMAR CLASES
async function programarClasesRecurrentes(e) {
    e.preventDefault();
    
    const form = e.target;
    const submitBtn = form.querySelector('button[type="submit"]');
    
    try {
        submitBtn.disabled = true;
        submitBtn.textContent = '⏳ Verificando...';
        
        // Obtener datos básicos
        const idLaboratorio = parseInt(form.querySelector('#selectLaboratorio').value);
        const cantidadEstudiantes = parseInt(form.querySelector('#selectCantidad').value);
        const diasSeleccionados = Array.from(form.querySelectorAll('input[name="dias"]:checked'))
            .map(cb => parseInt(cb.value));
        
        // Validaciones simples
        if (diasSeleccionados.length === 0) {
            throw new Error('Selecciona al menos un día de la semana');
        }

        // Construir objeto de datos
        const datosClase = {
            idUsuario: parseInt(form.querySelector('#selectProfesor').value),
            idLaboratorio: idLaboratorio,
            nrcCurso: form.querySelector('#selectCurso').value,
            diasSemana: diasSeleccionados,
            horaInicio: form.querySelector('#selectHoraInicio').value + ':00',
            horaFin: form.querySelector('#selectHoraFin').value + ':00',
            fechaInicio: form.querySelector('#fechaInicio').value,
            cantidadSemanas: parseInt(form.querySelector('#selectSemanas').value),
            cantidadEstudiantes: cantidadEstudiantes,
            tipoReserva: "clase"
        };

        // 🔥 VERIFICAR CONFLICTOS SIMPLE
        const conflictos = await verificarConflictosSimples(datosClase);
        if (conflictos.length > 0) {
            mostrarConflictos(conflictos);
            return;
        }

        // Enviar al backend
        const resultado = await api.post(`${API_BASE_URL}/reservas/recurrentes`, datosClase);
        
        alert(`✅ ${resultado.totalReservas} clases programadas exitosamente`);
        form.reset();
        cerrarModal();
        setTimeout(() => mostrarAdminCursos(), 1000);
        
    } catch (error) {
        console.error('Error:', error);
        alert(`❌ Error: ${error.message}`);
    } finally {
        submitBtn.disabled = false;
        submitBtn.textContent = '📅 Programar Clases Recurrentes';
    }
}

// 🔥 VERIFICACIÓN SIMPLE DE CONFLICTOS
async function verificarConflictosSimples(datosClase) {
    try {
        const todasReservas = await api.get(API_ENDPOINTS.reservas);
        const conflictos = [];
        
        // Solo verificar reservas confirmadas en el mismo laboratorio
        const reservasConfirmadas = todasReservas.filter(r => 
            r.laboratorio.id === datosClase.idLaboratorio && 
            r.estado === 'Confirmada'
        );

        // Generar fechas a verificar
        const fechas = generarFechasSimples(datosClase.fechaInicio, datosClase.cantidadSemanas, datosClase.diasSemana);
        
        fechas.forEach(fecha => {
            const inicioClase = new Date(`${fecha}T${datosClase.horaInicio}`);
            const finClase = new Date(`${fecha}T${datosClase.horaFin}`);
            
            reservasConfirmadas.forEach(reserva => {
                const inicioReserva = new Date(reserva.fechaInicio);
                const finReserva = new Date(reserva.fechaFin);
                
                // Verificar solapamiento
                if (inicioClase < finReserva && finClase > inicioReserva) {
                    conflictos.push({
                        fecha: fecha,
                        reserva: reserva,
                        tipo: reserva.tipoReserva,
                        usuario: reserva.usuario,
                        horario: `${formatFecha(reserva.fechaInicio).split(' ')[1]} - ${formatFecha(reserva.fechaFin).split(' ')[1]}`
                    });
                }
            });
        });
        
        return conflictos;
    } catch (error) {
        console.error('Error verificando conflictos:', error);
        return [];
    }
}

// 🔥 MOSTRAR CONFLICTOS DE FORMA CLARA
function mostrarConflictos(conflictos) {
    let mensaje = '🚫 Ya existen reservas confirmadas en los horarios seleccionados:\n\n';
    
    conflictos.forEach((conflicto, index) => {
        mensaje += `${index + 1}. ${conflicto.fecha} - ${conflicto.horario}\n`;
        mensaje += `   👤 ${conflicto.usuario.nombre} ${conflicto.usuario.apellido}\n`;
        mensaje += `   📝 ${conflicto.tipo === 'clase' ? 'Clase' : 'Práctica libre'}\n\n`;
    });
    
    mensaje += 'Por favor, selecciona otros horarios o días.';
    alert(mensaje);
}

// 🔥 FUNCIONES AUXILIARES SIMPLES
function generarFechasSimples(fechaInicio, cantidadSemanas, diasSemana) {
    const fechas = [];
    const inicio = new Date(fechaInicio);
    const fin = new Date(inicio);
    fin.setDate(fin.getDate() + (cantidadSemanas * 7));

    const fechaActual = new Date(inicio);
    
    while (fechaActual <= fin) {
        const diaSemana = fechaActual.getDay() === 0 ? 7 : fechaActual.getDay();
        if (diasSemana.includes(diaSemana)) {
            fechas.push(fechaActual.toISOString().split('T')[0]);
        }
        fechaActual.setDate(fechaActual.getDate() + 1);
    }

    return fechas;
}

function generarOpcionesHoras() {
    const horas = [];
    for (let i = 6; i <= 21; i++) {
        for (let j = 0; j < 60; j += 30) {
            const hora = i.toString().padStart(2, '0');
            const minuto = j.toString().padStart(2, '0');
            horas.push(`<option value="${hora}:${minuto}">${hora}:${minuto}</option>`);
        }
    }
    return horas.join('');
}

// 🔥 ACTUALIZAR OPCIONES DE CAPACIDAD
window.actualizarCapacidad = function(select) {
    const cantidadSelect = document.getElementById('selectCantidad');
    if (!select.value) {
        cantidadSelect.innerHTML = '<option value="">Selecciona laboratorio primero</option>';
        return;
    }
    
    const capacidadDisponible = parseInt(select.options[select.selectedIndex].dataset.capacidad);
    
    let opciones = '<option value="">Selecciona cantidad...</option>';
    for (let i = 1; i <= Math.min(capacidadDisponible, 20); i++) {
        opciones += `<option value="${i}">${i} estudiante${i > 1 ? 's' : ''}</option>`;
    }
    
    cantidadSelect.innerHTML = opciones;
    
    if (capacidadDisponible === 0) {
        alert('⚠️ Este laboratorio no tiene capacidad disponible');
    }
}

// 🔥 CANCELAR CLASE SIMPLE
window.cancelarClase = async (id) => {
    if (confirm('¿Estás seguro de cancelar esta clase?')) {
        try {
            await api.put(`${API_ENDPOINTS.reservas}/${id}/cancelar`, {});
            alert('✅ Clase cancelada exitosamente');
            cerrarModal();
            setTimeout(() => mostrarAdminCursos(), 1000);
        } catch (error) {
            alert('❌ Error al cancelar la clase');
        }
    }
};

export { mostrarAdminCursos };
