import { cerrarModal, formatearFecha, setupFiltroTabla } from './ui.js';

async function mostrarAdminCursos() {
    try {
        const [profesores, cursos, laboratorios, reservasExistentes] = await Promise.all([
            api.get(API_ENDPOINTS.usuarios.listar).then(users => 
                users.filter(u => u.rol === 'profesor' || u.rol === 'administrador')
            ),
            api.get(API_ENDPOINTS.cursos.listar).catch(() => []),
            api.get(API_ENDPOINTS.laboratorios.listar),
            api.get(API_ENDPOINTS.reservas.listar)
        ]);

        let html = `
            <div class="modal">
                <div class="modal-content" style="max-width: 1400px;">
                    <span class="close" onclick="cerrarModal()">&times;</span>
                    <h2>Administrar Cursos y Clases</h2>
                    
                    <div class="admin-cursos-container">
                        <!-- Sección de Gestión de Cursos -->
                        <div class="form-section">
                            <h3>Gestión de Cursos (Materias)</h3>
                            
                            <input type="text" id="filtroCursos" placeholder="Filtrar por NRC o nombre..." class="input-filtro">
                            
                            <div class="tabla-container" style="max-height: 300px; overflow-y: auto;">
                                <table id="tablaCursos">
                                    <thead>
                                        <tr>
                                            <th>NRC</th>
                                            <th>Nombre del Curso</th>
                                            <th>Fecha Creación</th>
                                            <th>Acciones</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        ${cursos.map(c => `
                                            <tr id="curso-${c.nrc}">
                                                <td>${c.nrc}</td>
                                                <td>${c.nombre}</td>
                                                <td>${c.fechaCreacion ? new Date(c.fechaCreacion).toLocaleDateString('es-CO') : '-'}</td>
                                                <td>
                                                    <button onclick="editarCurso('${c.nrc}')">✏️ Editar</button>
                                                    <button onclick="eliminarCurso('${c.nrc}')" class="btn-cancelar">🗑️ Eliminar</button>
                                                </td>
                                            </tr>
                                        `).join('')}
                                    </tbody>
                                </table>
                            </div>

                            <h4>Agregar nuevo curso</h4>
                            <div style="display: flex; gap: 10px; margin-top: 10px;">
                                <input type="text" id="cursoNrc" placeholder="NRC (Código único)" style="flex: 1;">
                                <input type="text" id="cursoNombre" placeholder="Nombre del curso" style="flex: 2;">
                                <button onclick="agregarCurso()">Agregar Curso</button>
                            </div>
                        </div>

                        <!-- Sección de Programar Clases -->
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
                                                <td>${r.fechaInicio ? formatearFecha(r.fechaInicio).split(' ')[0] : 'N/A'}</td>
                                                <td>${r.fechaInicio ? formatearFecha(r.fechaInicio).split(' ')[1] : 'N/A'} - ${r.fechaFin ? formatearFecha(r.fechaFin).split(' ')[1] : 'N/A'}</td>
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
        
        const fechaInput = document.getElementById('fechaInicio');
        fechaInput.min = new Date().toISOString().split('T')[0];
        fechaInput.value = new Date().toISOString().split('T')[0];
        
        document.getElementById('formClaseRecurrente').addEventListener('submit', programarClasesRecurrentes);
        setupFiltroTabla("filtroClases", "tablaClases");
        setupFiltroTabla("filtroCursos", "tablaCursos");
        
    } catch (error) {
        console.error('Error:', error);
        alert('Error al cargar datos para administrar cursos');
    }
}

// ================ GESTIÓN DE CURSOS ================

window.editarCurso = async function (nrc) {
    try {
        const curso = await api.get(API_ENDPOINTS.cursos.obtenerPorNrc(nrc));

        const nuevoNombre = prompt("Nuevo nombre del curso:", curso.nombre);

        if (!nuevoNombre || nuevoNombre.trim() === '') {
            return alert("El nombre del curso es obligatorio");
        }

        const payload = {
            nrc: curso.nrc,
            nombre: nuevoNombre.trim()
        };

        const actualizado = await api.put(API_ENDPOINTS.cursos.actualizar(nrc), payload);

        const fila = document.querySelector(`#curso-${nrc}`);
        if (fila) {
            fila.children[1].textContent = actualizado.nombre;
        }

        // Actualizar también el select de cursos
        const selectCurso = document.getElementById('selectCurso');
        if (selectCurso) {
            const option = selectCurso.querySelector(`option[value="${nrc}"]`);
            if (option) {
                option.textContent = `${nrc} - ${actualizado.nombre}`;
            }
        }

        alert('✅ Curso actualizado exitosamente');

    } catch (e) {
        console.error(e);
        alert("Error al editar curso");
    }
};

window.eliminarCurso = async function (nrc) {
    if (!confirm(`¿Estás seguro de eliminar el curso ${nrc}?\n\nNOTA: Si hay clases programadas con este curso, también se eliminarán.`)) {
        return;
    }

    try {
        await api.delete(API_ENDPOINTS.cursos.eliminar(nrc));
        
        const fila = document.querySelector(`#curso-${nrc}`);
        if (fila) {
            fila.remove();
        }

        // Actualizar también el select de cursos
        const selectCurso = document.getElementById('selectCurso');
        if (selectCurso) {
            const option = selectCurso.querySelector(`option[value="${nrc}"]`);
            if (option) {
                option.remove();
            }
        }

        alert('✅ Curso eliminado exitosamente');

    } catch (e) {
        console.error(e);
        alert("Error al eliminar curso: " + (e.message || "Puede que haya clases asociadas a este curso"));
    }
};

window.agregarCurso = async function () {
    const nrc = document.getElementById('cursoNrc').value.trim();
    const nombre = document.getElementById('cursoNombre').value.trim();

    if (!nrc || !nombre) {
        return alert("Por favor complete todos los campos (NRC y nombre del curso)");
    }

    if (nrc.length > 20) {
        return alert("El NRC no puede tener más de 20 caracteres");
    }

    if (nombre.length > 200) {
        return alert("El nombre del curso no puede tener más de 200 caracteres");
    }

    try {
        const nuevoCurso = await api.post(API_ENDPOINTS.cursos.crear, {
            nrc,
            nombre
        });

        // Agregar a la tabla
        const tbody = document.querySelector('#tablaCursos tbody');
        const nuevaFila = document.createElement('tr');
        nuevaFila.id = `curso-${nrc}`;
        nuevaFila.innerHTML = `
            <td>${nrc}</td>
            <td>${nombre}</td>
            <td>${new Date().toLocaleDateString('es-CO')}</td>
            <td>
                <button onclick="editarCurso('${nrc}')">✏️ Editar</button>
                <button onclick="eliminarCurso('${nrc}')" class="btn-cancelar">🗑️ Eliminar</button>
            </td>
        `;
        tbody.appendChild(nuevaFila);

        // Agregar al select de cursos
        const selectCurso = document.getElementById('selectCurso');
        if (selectCurso) {
            const nuevaOpcion = document.createElement('option');
            nuevaOpcion.value = nrc;
            nuevaOpcion.textContent = `${nrc} - ${nombre}`;
            selectCurso.appendChild(nuevaOpcion);
        }

        // Limpiar campos
        document.getElementById('cursoNrc').value = '';
        document.getElementById('cursoNombre').value = '';

        alert('✅ Curso agregado exitosamente');

    } catch (e) {
        console.error(e);
        alert("Error agregando curso: " + (e.message || "Verifica que el NRC no esté duplicado."));
    }
};

// ================ PROGRAMACIÓN DE CLASES ================

async function programarClasesRecurrentes(e) {
    e.preventDefault();
    
    const form = e.target;
    const submitBtn = form.querySelector('button[type="submit"]');
    
    try {
        submitBtn.disabled = true;
        submitBtn.textContent = '⏳ Verificando...';
        
        const idLaboratorio = parseInt(form.querySelector('#selectLaboratorio').value);
        const cantidadEstudiantes = parseInt(form.querySelector('#selectCantidad').value);
        const diasSeleccionados = Array.from(form.querySelectorAll('input[name="dias"]:checked'))
            .map(cb => parseInt(cb.value));
        
        if (diasSeleccionados.length === 0) {
            throw new Error('Selecciona al menos un día de la semana');
        }

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

        const conflictos = await verificarConflictosSimples(datosClase);
        if (conflictos.length > 0) {
            mostrarConflictos(conflictos);
            return;
        }

        const resultado = await api.post(API_ENDPOINTS.reservas.recurrentes, datosClase);
        
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

async function verificarConflictosSimples(datosClase) {
    try {
        const todasReservas = await api.get(API_ENDPOINTS.reservas.listar);
        const conflictos = [];
        
        const reservasConfirmadas = todasReservas.filter(r => 
            r.laboratorio.id === datosClase.idLaboratorio && 
            r.estado === 'Confirmada'
        );

        const fechas = generarFechasSimples(datosClase.fechaInicio, datosClase.cantidadSemanas, datosClase.diasSemana);
        
        fechas.forEach(fecha => {
            const inicioClase = new Date(`${fecha}T${datosClase.horaInicio}`);
            const finClase = new Date(`${fecha}T${datosClase.horaFin}`);
            
            reservasConfirmadas.forEach(reserva => {
                const inicioReserva = new Date(reserva.fechaInicio);
                const finReserva = new Date(reserva.fechaFin);
                
                if (inicioClase < finReserva && finClase > inicioReserva) {
                    conflictos.push({
                        fecha: fecha,
                        reserva: reserva,
                        tipo: reserva.tipoReserva,
                        usuario: reserva.usuario,
                        horario: `${formatearFecha(reserva.fechaInicio).split(' ')[1]} - ${formatearFecha(reserva.fechaFin).split(' ')[1]}`
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

window.cancelarClase = async (id) => {
    if (confirm('¿Estás seguro de cancelar esta clase?')) {
        try {
            await api.put(API_ENDPOINTS.reservas.cancelar(id), {});
            alert('✅ Clase cancelada exitosamente');
            cerrarModal();
            setTimeout(() => mostrarAdminCursos(), 1000);
        } catch (error) {
            alert('❌ Error al cancelar la clase');
        }
    }
};

export { mostrarAdminCursos };
