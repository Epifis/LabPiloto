// events.js - Versi贸n con validaciones de fecha y hora
console.log('Iniciando events.js...');

document.addEventListener('DOMContentLoaded', function () {
  console.log('DOM cargado - events.js');

  // Cargar y mostrar datos del usuario inmediatamente
  cargarDatosUsuario();

  // Configurar restricciones de fecha y hora
  configurarValidacionesFechaHora();

  const btnLaboratorio = document.getElementById('btnLaboratorio');
  const btnProductos = document.getElementById('btnProductos');
  const reservaLaboratorio = document.getElementById('reservaLaboratorio');
  const reservaProductos = document.getElementById('reservaProductos');
  const formLab = document.getElementById('formLab');
  const formProd = document.getElementById('formProd');

  // Funci贸n para configurar validaciones de fecha y hora
  function configurarValidacionesFechaHora() {
    // Obtener fecha actual en formato YYYY-MM-DD
    const hoy = new Date();
    const fechaMinima = hoy.toISOString().split('T')[0];

    // Configurar inputs de fecha en ambos formularios
    const inputsFecha = document.querySelectorAll('input[type="date"]');
    inputsFecha.forEach(input => {
      input.setAttribute('min', fechaMinima);
      
      // Validaci贸n adicional al cambiar la fecha
      input.addEventListener('change', function() {
        if (this.value < fechaMinima) {
          alert('No puedes seleccionar una fecha anterior a hoy');
          this.value = fechaMinima;
        }
      });
    });

    // Configurar inputs de hora en ambos formularios
    const inputsHora = document.querySelectorAll('input[type="time"]');
    inputsHora.forEach(input => {
      input.setAttribute('min', '06:00');
      input.setAttribute('max', '21:00');
      
      // Validaci贸n adicional al cambiar la hora
      input.addEventListener('change', function() {
        const hora = this.value;
        if (hora < '06:00' || hora > '21:00') {
          alert('El horario debe estar entre las 6:00 AM y las 9:00 PM');
          this.value = hora < '06:00' ? '06:00' : '21:00';
        }
      });
    });
  }

  // Funci贸n para validar rango de horas
  function validarRangoHoras(horaInicio, horaFin) {
    if (horaInicio >= horaFin) {
      alert('La hora de entrega debe ser posterior a la hora de inicio');
      return false;
    }
    
    // Convertir a minutos para validar el rango permitido
    const [hiHoras, hiMinutos] = horaInicio.split(':').map(Number);
    const [hfHoras, hfMinutos] = horaFin.split(':').map(Number);
    
    const inicioEnMinutos = hiHoras * 60 + hiMinutos;
    const finEnMinutos = hfHoras * 60 + hfMinutos;
    
    const horaMinima = 6 * 60; // 6:00 AM en minutos
    const horaMaxima = 21 * 60; // 9:00 PM en minutos
    
    if (inicioEnMinutos < horaMinima || finEnMinutos > horaMaxima) {
      alert('El horario debe estar entre las 6:00 AM y las 9:00 PM');
      return false;
    }
    
    return true;
  }

  // Evento: Mostrar formulario de laboratorio
  if (btnLaboratorio) {
    btnLaboratorio.addEventListener('click', function () {
      console.log('Click en Reserva de Laboratorio');
      reservaLaboratorio.hidden = !reservaLaboratorio.hidden;

      if (!reservaLaboratorio.hidden) {
        reservaProductos.hidden = true;
        cargarLaboratorios();
        autocompletarFormularioLaboratorio();
        configurarValidacionesFechaHora(); // Reconfigurar al mostrar
      }
    });
  }

  // Evento: Mostrar formulario de productos
  if (btnProductos) {
    btnProductos.addEventListener('click', function () {
      console.log('Click en Reserva de Productos');
      reservaProductos.hidden = !reservaProductos.hidden;

      if (!reservaProductos.hidden) {
        reservaLaboratorio.hidden = true;
        cargarElementos();
        autocompletarFormularioProductos();
        configurarValidacionesFechaHora(); // Reconfigurar al mostrar
      }
    });
  }

  // Cargar datos del usuario en la p谩gina principal
  function cargarDatosUsuario() {
    const userData = JSON.parse(localStorage.getItem('userData'));
    if (!userData) {
      console.log('No hay usuario autenticado');
      return;
    }

    console.log('Datos del usuario cargados:', userData);

    // Mostrar informaci贸n del usuario en la p谩gina principal
    const inicioSection = document.getElementById('inicio');
    if (inicioSection) {
      const titulo = inicioSection.querySelector('h2');
      if (titulo) {
        titulo.innerHTML = `Bienvenido, <span style="color: #2c5aa0;">${userData.nombre || ''} ${userData.apellido || ''}</span>`;
      }

      // Agregar informaci贸n del usuario
      const infoUsuario = document.createElement('div');
      infoUsuario.style.marginTop = '15px';
      infoUsuario.style.padding = '10px';
      infoUsuario.style.backgroundColor = '#f0f8ff';
      infoUsuario.style.borderRadius = '5px';
      infoUsuario.style.fontSize = '14px';
      infoUsuario.innerHTML = `
        <strong>Informaci贸n de tu cuenta:</strong><br>
        ${userData.correo || ''}<br>
        ${userData.programa || 'Estudiante'} | ${userData.documento || ''}
      `;

      // Insertar despu茅s del t铆tulo
      titulo.parentNode.insertBefore(infoUsuario, titulo.nextSibling);
    }
  }

  function autocompletarFormularioLaboratorio() {
    const userData = JSON.parse(localStorage.getItem('userData'));
    if (!userData) {
      console.log('No hay usuario autenticado para autocompletar laboratorio');
      return;
    }

    // Mostrar informaci贸n del usuario en el panel
    document.getElementById('infoNombreLab').textContent = `${userData.nombre || ''} ${userData.apellido || ''}`.trim();
    document.getElementById('infoCorreoLab').textContent = userData.correo || '';
    document.getElementById('infoCarreraLab').textContent = userData.programa || '';
    document.getElementById('infoDocumentoLab').textContent = userData.documento || '';

    console.log('Informaci贸n de usuario mostrada en laboratorio');
  }

  function autocompletarFormularioProductos() {
    const userData = JSON.parse(localStorage.getItem('userData'));
    if (!userData) return;

    // Mostrar informaci贸n del usuario en el panel
    document.getElementById('infoNombreProd').textContent = `${userData.nombre || ''} ${userData.apellido || ''}`.trim();
    document.getElementById('infoCorreoProd').textContent = userData.correo || '';
    document.getElementById('infoDocumentoProd').textContent = userData.documento || '';

    console.log('Informaci贸n de usuario mostrada en productos');
  }

  // Cargar laboratorios
  async function cargarLaboratorios() {
    console.log('Cargando laboratorios...');
    try {
      const laboratorios = await api.get(API_ENDPOINTS.laboratorios.disponibles);
      const select = document.querySelector('#formLab select');
      if (select) {
        select.innerHTML = '<option value="">Seleccione un laboratorio</option>' +
          laboratorios.map(lab =>
            `<option value="${lab.id}">${lab.nombre} - ${lab.ubicacion}</option>`
          ).join('');
        console.log('Laboratorios cargados:', laboratorios.length);
      }
    } catch (error) {
      console.error('Error al cargar laboratorios:', error);
      alert('Error al cargar laboratorios.');
    }
  }

  // Cargar elementos
  async function cargarElementos() {
    console.log('Cargando elementos...');
    try {
      const elementos = await api.get(API_ENDPOINTS.elementos.disponibles);
      const select = document.getElementById('producto');
      if (select) {
        select.innerHTML = '<option value="">Seleccione un producto</option>' +
          elementos.map(elem =>
            `<option value="${elem.id}">${elem.nombre}</option>`
          ).join('');
        console.log('Elementos cargados:', elementos.length);
      }
    } catch (error) {
      console.error('Error al cargar elementos:', error);
      alert('Error al cargar elementos.');
    }
  }

  // Evento: Enviar reserva de laboratorio CON VALIDACIONES
  if (formLab) {
    formLab.addEventListener('submit', async function (e) {
      e.preventDefault();
      console.log('Enviando reserva de laboratorio...');

      // Verificar autenticaci贸n
      const userData = JSON.parse(localStorage.getItem('userData'));
      if (!userData || !userData.id) {
        alert('Debe iniciar sesi贸n para realizar una reserva');
        window.location.href = 'login-estudiante.html';
        return;
      }

      if (window.validarEstudiantesAdicionales && !window.validarEstudiantesAdicionales()) {
        return;
      }

      const labId = formLab.querySelector('select').value;
      const fecha = formLab.querySelector('input[type="date"]').value;
      const horaInicio = formLab.querySelectorAll('input[type="time"]')[0].value;
      const horaFin = formLab.querySelectorAll('input[type="time"]')[1].value;

      // Validaciones b谩sicas
      if (!labId) {
        alert('Por favor seleccione un laboratorio');
        return;
      }
      if (!fecha) {
        alert('Por favor seleccione una fecha');
        return;
      }

      // Validar rango de horas
      if (!validarRangoHoras(horaInicio, horaFin)) {
        return;
      }

      try {
        const fechaInicio = `${fecha}T${horaInicio}:00`;
        const fechaFin = `${fecha}T${horaFin}:00`;

        let invitados = [];
        if (window.obtenerEstudiantesAdicionales) {
          const estudiantesAdicionales = window.obtenerEstudiantesAdicionales();
          console.log('Estudiantes adicionales obtenidos:', estudiantesAdicionales);

          invitados = estudiantesAdicionales.map(est => {
            const nombreCompleto = est.nombre.trim();
            const partes = nombreCompleto.split(/\s+/);
            const nombreInv = partes[0] || nombreCompleto;
            const apellidoInv = partes.slice(1).join(' ') || '';

            return {
              nombre: nombreInv,
              apellido: apellidoInv,
              documento: est.documento || ''
            };
          });
        }

        const reserva = {
          fechaInicio,
          fechaFin,
          tipoReserva: 'practica_libre',
          usuario: { id: userData.id },
          laboratorio: { id: parseInt(labId) },
          invitados: invitados.length > 0 ? invitados : null,
          cantidadEstudiantes: 1 + invitados.length
        };

        console.log('Datos de reserva a enviar:', JSON.stringify(reserva, null, 2));

        const response = await api.post(API_ENDPOINTS.reservas.solicitar, reserva);
        console.log('Respuesta del servidor:', response);

        alert(`Reserva realizada exitosamente! Total de estudiantes: ${reserva.cantidadEstudiantes}`);
        formLab.reset();

        // Limpiar estudiantes adicionales
        const estudiantesContainer = document.getElementById('estudiantesContainer');
        if (estudiantesContainer) {
          estudiantesContainer.innerHTML = '';
        }
        const listaEst = document.getElementById('listaEstudiantes');
        if (listaEst) listaEst.hidden = true;
        const masEst = document.getElementById('masEstudiantes');
        if (masEst) masEst.value = 'no';

        reservaLaboratorio.hidden = true;
      } catch (error) {
        console.error('Error completo:', error);
        alert('Error al enviar reserva: ' + (error.message || 'Error desconocido'));
      }
    });
  }

  // Evento: Enviar reserva de productos CON VALIDACIONES
  if (formProd) {
    formProd.addEventListener('submit', async function (e) {
      e.preventDefault();
      console.log('Enviando solicitud de prestamo...');

      // Verificar autenticaci贸n
      const userData = JSON.parse(localStorage.getItem('userData'));
      if (!userData || !userData.id) {
        alert('Debe iniciar sesion para solicitar un prestamo');
        window.location.href = 'login-estudiante.html';
        return;
      }

      // Validar fecha y hora
      const fecha = formProd.querySelector('input[type="date"]').value;
      const horaInicio = formProd.querySelectorAll('input[type="time"]')[0].value;
      const horaFin = formProd.querySelectorAll('input[type="time"]')[1].value;

      if (!fecha) {
        alert('Por favor seleccione una fecha');
        return;
      }

      // Validar rango de horas
      if (!validarRangoHoras(horaInicio, horaFin)) {
        return;
      }

      const productosItems = document.querySelectorAll('#productosContainer .producto-item');
      const elementos = [];

      productosItems.forEach((item) => {
        const select = item.querySelector('select');
        const inputCantidad = item.querySelector('input[type="number"]');

        const elementoId = select?.value;
        const cantidad = parseInt(inputCantidad?.value) || 1;

        if (elementoId && elementoId !== '') {
          for (let i = 0; i < cantidad; i++) {
            elementos.push(elementoId);
          }
        }
      });

      if (elementos.length === 0) {
        alert('Debe seleccionar al menos un producto');
        return;
      }

      try {
        const prestamos = elementos.map((elementoId) => ({
          usuario: { id: userData.id },
          elemento: { id: parseInt(elementoId) }
        }));

        console.log('Prestamos a crear:', JSON.stringify(prestamos, null, 2));

        // Enviar cada pr茅stamo individualmente
        const promesas = prestamos.map((prestamo) => {
          return api.post(API_ENDPOINTS.prestamos.solicitar, prestamo);
        });

        const resultados = await Promise.all(promesas);
        console.log('Resultados:', resultados);

        alert(`Solicitud de ${elementos.length} elemento(s) enviada exitosamente!`);
        formProd.reset();

        // Limpiar productos adicionales
        const container = document.getElementById('productosContainer');
        const items = container.querySelectorAll('.producto-item');
        items.forEach((item, index) => {
          if (index > 0) item.remove();
        });

        reservaProductos.hidden = true;
      } catch (error) {
        console.error('Error completo al enviar solicitud:', error);
        alert('Error al enviar la solicitud: ' + (error.message || 'Error desconocido'));
      }
    });
  }

  // Agregar otro producto
  const btnAgregarProducto = document.getElementById('btnAgregarProducto');
  if (btnAgregarProducto) {
    btnAgregarProducto.addEventListener('click', async function () {
      const container = document.getElementById('productosContainer');
      if (!container) return;

      const nuevo = document.createElement('div');
      nuevo.classList.add('producto-item');
      nuevo.innerHTML = `
        <label>Producto:
          <select class="producto-extra" required>
            <option value="">Cargando...</option>
          </select>
        </label>
        <label>Cantidad:
          <input type="number" min="1" value="1" required>
        </label>
        <button type="button" class="btn-eliminar-producto">鉁� Eliminar</button>
      `;
      container.appendChild(nuevo);

      const btnEliminar = nuevo.querySelector('.btn-eliminar-producto');
      btnEliminar.addEventListener('click', () => nuevo.remove());

      try {
        const elementos = await api.get(API_ENDPOINTS.elementos.disponibles);
        const select = nuevo.querySelector('select');
        select.innerHTML = '<option value="">Seleccione un producto</option>' +
          elementos.map(elem =>
            `<option value="${elem.id}">${elem.nombre}</option>`
          ).join('');
      } catch (error) {
        console.error('Error:', error);
      }
    });
  }

  // ==== Envío de contacto al backend (pegar dentro del DOMContentLoaded principal) ====
const formContacto = document.getElementById('formContacto');
const contactStatus = document.getElementById('contactStatus');

if (formContacto) {
  formContacto.addEventListener('submit', async function (e) {
    e.preventDefault();

    const nombre = document.getElementById('contactNombre')?.value.trim() || '';
    const correo = document.getElementById('contactCorreo')?.value.trim() || '';
    const asunto = document.getElementById('contactAsunto')?.value.trim() || 'Soporte LabPilot';
    const mensaje = document.getElementById('contactMensaje')?.value.trim() || '';

    if (!nombre || !correo || !mensaje) {
      alert('Por favor completa nombre, correo y mensaje.');
      return;
    }

    if (contactStatus) {
      contactStatus.style.display = 'block';
      contactStatus.style.color = 'black';
      contactStatus.textContent = 'Enviando mensaje...';
    }

    try {
      const payload = { nombre, correo, asunto, mensaje };
      await api.post(API_ENDPOINTS.contacto, payload);

      if (contactStatus) {
        contactStatus.style.color = 'green';
        contactStatus.textContent = 'Mensaje enviado correctamente. Gracias.';
      }

      formContacto.reset();
    } catch (err) {
      console.error('Error enviando contacto al backend:', err);
      if (contactStatus) {
        contactStatus.style.color = 'crimson';
        contactStatus.textContent = 'Error al enviar el mensaje. Intenta de nuevo.';
      }
    }
  });
}
});
