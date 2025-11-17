// events.js - Versión simplificada y funcional
console.log('Iniciando events.js...');

document.addEventListener('DOMContentLoaded', function() {
  console.log('DOM cargado');

  // Obtener elementos
  const btnLaboratorio = document.getElementById('btnLaboratorio');
  const btnProductos = document.getElementById('btnProductos');
  const reservaLaboratorio = document.getElementById('reservaLaboratorio');
  const reservaProductos = document.getElementById('reservaProductos');
  const formLab = document.getElementById('formLab');
  const formProd = document.getElementById('formProd');

  console.log('Elementos:', {
    btnLaboratorio,
    btnProductos,
    reservaLaboratorio,
    reservaProductos,
    formLab,
    formProd
  });

  // Evento: Mostrar formulario de laboratorio
  if (btnLaboratorio) {
    btnLaboratorio.addEventListener('click', function() {
      console.log('Click en Reserva de Laboratorio');
      reservaLaboratorio.hidden = false;
      reservaProductos.hidden = true;
      cargarLaboratorios();
    });
    console.log('Event listener agregado a btnLaboratorio');
  } else {
    console.error('No se encontró btnLaboratorio');
  }

  // Evento: Mostrar formulario de productos
  if (btnProductos) {
    btnProductos.addEventListener('click', function() {
      console.log('Click en Reserva de Productos');
      reservaProductos.hidden = false;
      reservaLaboratorio.hidden = true;
      cargarElementos();
    });
    console.log('Event listener agregado a btnProductos');
  } else {
    console.error('No se encontró btnProductos');
  }

  // Función: Cargar laboratorios desde API
  async function cargarLaboratorios() {
    console.log('Cargando laboratorios...');
    try {
      const url = `${API_ENDPOINTS.laboratorios}/disponibles`;
      console.log('URL:', url);
      const laboratorios = await api.get(url);
      console.log('Laboratorios recibidos:', laboratorios);
      
      const select = document.querySelector('#formLab select');
      if (select) {
        select.innerHTML = laboratorios.map(lab => 
          `<option value="${lab.id}">${lab.nombre} - ${lab.ubicacion}</option>`
        ).join('');
        console.log('Laboratorios cargados en select');
      }
    } catch (error) {
      console.error('Error cargando laboratorios:', error);
      alert('Error al cargar laboratorios. Verifica que el backend esté corriendo.');
    }
  }

  // Función: Cargar elementos desde API
  async function cargarElementos() {
    console.log('Cargando elementos...');
    try {
      const url = `${API_ENDPOINTS.elementos}/disponibles`;
      console.log('URL:', url);
      const elementos = await api.get(url);
      console.log('Elementos recibidos:', elementos);
      
      const select = document.getElementById('producto');
      if (select) {
        select.innerHTML = elementos.map(elem => 
          `<option value="${elem.id}">${elem.nombre}</option>`
        ).join('');
        console.log('Elementos cargados en select');
      }
    } catch (error) {
      console.error('Error cargando elementos:', error);
      alert('Error al cargar elementos. Verifica que el backend esté corriendo.');
    }
  }

  // Evento: Enviar reserva de laboratorio
  if (formLab) {
    formLab.addEventListener('submit', async function(e) {
      e.preventDefault();
      console.log('Enviando reserva de laboratorio...');
      
      const inputs = formLab.querySelectorAll('input[type="text"]');
      const nombre = inputs[0].value;
      const correo = formLab.querySelector('input[type="email"]').value;
      const carrera = inputs[1].value;
      const labId = formLab.querySelector('select').value;
      const hora = formLab.querySelector('input[type="time"]').value;

      console.log('Datos del formulario:', { nombre, correo, carrera, labId, hora });

      try {
        // Buscar o crear usuario
        let usuario = await buscarOCrearUsuario(nombre, correo);
        console.log('Usuario:', usuario);

        // Crear fechas
        const hoy = new Date().toISOString().split('T')[0];
        const fechaInicio = `${hoy}T${hora}:00`;
        const horaFin = agregarHoras(hora, 2);
        const fechaFin = `${hoy}T${horaFin}:00`;

        const reserva = {
          fechaInicio: fechaInicio,
          fechaFin: fechaFin,
          tipoReserva: "practica_libre",
          usuario: { id: usuario.id },
          laboratorio: { id: parseInt(labId) }
        };

        console.log('Enviando reserva:', reserva);
        const response = await api.post(`${API_ENDPOINTS.reservas}/solicitar`, reserva);
        console.log('Respuesta:', response);
        
        alert('¡Reserva de laboratorio enviada exitosamente!\n\nEstado: Pendiente de aprobación');
        formLab.reset();
        reservaLaboratorio.hidden = true;
      } catch (error) {
        console.error('Error al enviar reserva:', error);
        alert('Error al enviar la reserva. Revisa la consola para más detalles.');
      }
    });
  }

  // Evento: Enviar reserva de productos
  if (formProd) {
    formProd.addEventListener('submit', async function(e) {
      e.preventDefault();
      console.log('Enviando solicitud de préstamo...');
      
      const nombre = formProd.querySelector('input[type="text"]').value;
      const correo = formProd.querySelector('input[type="email"]').value;
      const elementoId = document.getElementById('producto').value;

      console.log('Datos del formulario:', { nombre, correo, elementoId });

      try {
        // Buscar o crear usuario
        let usuario = await buscarOCrearUsuario(nombre, correo);
        console.log('Usuario:', usuario);

        const prestamo = {
          usuario: { id: usuario.id },
          elemento: { id: parseInt(elementoId) }
        };

        console.log('Enviando préstamo:', prestamo);
        const response = await api.post(`${API_ENDPOINTS.prestamos}/solicitar`, prestamo);
        console.log('Respuesta:', response);
        
        alert('¡Solicitud de préstamo enviada exitosamente!\n\nEstado: Pendiente de aprobación');
        formProd.reset();
        reservaProductos.hidden = true;
      } catch (error) {
        console.error('Error al enviar préstamo:', error);
        alert('Error al enviar la solicitud. Revisa la consola para más detalles.');
      }
    });
  }

  // Función auxiliar: Buscar o crear usuario
  async function buscarOCrearUsuario(nombre, correo) {
    console.log('🔍 Buscando usuario:', correo);
    try {
      const usuarios = await api.get(API_ENDPOINTS.usuarios);
      let usuario = usuarios.find(u => u.correo === correo);

      if (!usuario) {
        console.log('Usuario no encontrado, creando nuevo...');
        const [nombreParte, apellidoParte] = nombre.split(' ');
        usuario = await api.post(`${API_ENDPOINTS.usuarios}/registrar`, {
          nombre: nombreParte || nombre,
          apellido: apellidoParte || 'N/A',
          correo: correo,
          rol: 'estudiante'
        });
        console.log('Usuario creado:', usuario);
      } else {
        console.log('Usuario encontrado:', usuario);
      }

      return usuario;
    } catch (error) {
      console.error('Error gestionando usuario:', error);
      throw error;
    }
  }

  // Función auxiliar: Agregar horas
  function agregarHoras(hora, horas) {
    const [h, m] = hora.split(':');
    const nuevaHora = (parseInt(h) + horas) % 24;
    return `${nuevaHora.toString().padStart(2, '0')}:${m}`;
  }

  console.log('events.js inicializado completamente');
});

  // 🔹 Evento: Agregar otro producto
  const btnAgregarProducto = document.getElementById('btnAgregarProducto');
  if (btnAgregarProducto) {
    btnAgregarProducto.addEventListener('click', async function() {
      console.log('Agregando otro producto...');
      
      const container = document.getElementById('productosContainer');
      if (!container) return;

      // Crear un nuevo bloque de producto
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
      `;
      container.appendChild(nuevo);

      // Cargar elementos en el nuevo select
      try {
        const url = `${API_ENDPOINTS.elementos}/disponibles`;
        const elementos = await api.get(url);
        const select = nuevo.querySelector('select');
        select.innerHTML = elementos.map(elem => 
          `<option value="${elem.id}">${elem.nombre}</option>`
        ).join('');
        console.log('Elementos cargados en nuevo select');
      } catch (error) {
        console.error('Error cargando elementos en nuevo producto:', error);
      }
    });
  }

