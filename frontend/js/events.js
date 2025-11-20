// events.js - Versión con integración de estudiantes adicionales (ARREGLADO)
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
      reservaLaboratorio.hidden = !reservaLaboratorio.hidden;
      
      if (!reservaLaboratorio.hidden) {
        reservaProductos.hidden = true;
        cargarLaboratorios();
      }
    });
    console.log('Event listener agregado a btnLaboratorio');
  } else {
    console.error('No se encontró btnLaboratorio');
  }

  // Evento: Mostrar formulario de productos
  if (btnProductos) {
    btnProductos.addEventListener('click', function() {
      console.log('Click en Reserva de Productos');
      reservaProductos.hidden = !reservaProductos.hidden;
      
      if (!reservaProductos.hidden) {
        reservaLaboratorio.hidden = true;
        cargarElementos();
      }
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
      const laboratorios = await api.get(url);
      
      const select = document.querySelector('#formLab select');
      if (select) {
        select.innerHTML = laboratorios.map(lab => 
          `<option value="${lab.id}">${lab.nombre} - ${lab.ubicacion}</option>`
        ).join('');
        console.log('Laboratorios cargados:', laboratorios.length);
      }
    } catch (error) {
      console.error('Error al cargar laboratorios:', error);
      alert('Error al cargar laboratorios.');
    }
  }

  // Función: Cargar elementos desde API
  async function cargarElementos() {
    console.log('Cargando elementos...');
    try {
      const url = `${API_ENDPOINTS.elementos}/disponibles`;
      const elementos = await api.get(url);
      
      const select = document.getElementById('producto');
      if (select) {
        select.innerHTML = elementos.map(elem => 
          `<option value="${elem.id}">${elem.nombre}</option>`
        ).join('');
        console.log('Elementos cargados:', elementos.length);
      }
    } catch (error) {
      console.error('Error al cargar elementos:', error);
      alert('Error al cargar elementos.');
    }
  }

  // Evento: Enviar reserva de laboratorio CON INVITADOS
  if (formLab) {
    formLab.addEventListener('submit', async function(e) {
      e.preventDefault();
      console.log('Enviando reserva de laboratorio...');
      
      // Validar estudiantes adicionales si están habilitados
      if (window.validarEstudiantesAdicionales && !window.validarEstudiantesAdicionales()) {
        console.warn('Validación de estudiantes falló');
        return;
      }
      
      const inputs = formLab.querySelectorAll('input[type="text"]');
      const nombre = inputs[0].value;
      const correo = formLab.querySelector('input[type="email"]').value;
      const carrera = inputs[1].value;
      const documento = inputs[2].value;
      const labId = formLab.querySelector('select').value;
      const fecha = formLab.querySelector('input[type="date"]').value;
      const horaInicio = formLab.querySelectorAll('input[type="time"]')[0].value;
      const horaFin = formLab.querySelectorAll('input[type="time"]')[1].value;

      try {
        // ✅ ARREGLADO: Buscar o crear usuario usando el endpoint correcto
        let usuario = await buscarOCrearUsuarioParaReserva(nombre, correo, documento, carrera);
        
        const fechaInicio = `${fecha}T${horaInicio}:00`;
        const fechaFin = `${fecha}T${horaFin}:00`;

        // Obtener estudiantes adicionales (invitados)
        let invitados = [];
        if (window.obtenerEstudiantesAdicionales) {
          const estudiantesAdicionales = window.obtenerEstudiantesAdicionales();
          console.log('Estudiantes adicionales obtenidos:', estudiantesAdicionales);
          
          // ✅ Convertir al formato correcto para ReservaInvitado (SIN correo)
          invitados = estudiantesAdicionales.map(est => {
            // Separar nombre completo en nombre y apellido
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
          
          console.log('Invitados procesados:', invitados);
        }

        const reserva = {
          fechaInicio,
          fechaFin,
          tipoReserva: 'practica_libre',
          usuario: { id: usuario.id },
          laboratorio: { id: parseInt(labId) },
          invitados: invitados.length > 0 ? invitados : null,
          cantidadEstudiantes: 1 + invitados.length
        };

        console.log('Datos de reserva a enviar:', JSON.stringify(reserva, null, 2));

        const response = await api.post(`${API_ENDPOINTS.reservas}/solicitar`, reserva);
        console.log('Respuesta del servidor:', response);
        
        alert(`¡Reserva realizada exitosamente! Total de estudiantes: ${reserva.cantidadEstudiantes}`);
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
    console.log('Event listener agregado a formLab');
  }

  // Evento: Enviar reserva de productos (MÚLTIPLES ELEMENTOS)
  if (formProd) {
    formProd.addEventListener('submit', async function(e) {
      e.preventDefault();
      console.log('Enviando solicitud de préstamo...');
      
      const inputs = formProd.querySelectorAll('input[type="text"]');
      const nombre = inputs[0].value;
      const correo = formProd.querySelector('input[type="email"]').value;
      const documento = inputs[1].value;
      
      // Obtener todos los productos seleccionados
      const productosItems = document.querySelectorAll('#productosContainer .producto-item');
      const elementos = [];
      
      console.log('Cantidad de productos en el formulario:', productosItems.length);
      
      productosItems.forEach((item, index) => {
        const select = item.querySelector('select');
        const inputCantidad = item.querySelector('input[type="number"]');
        
        console.log(`Producto ${index + 1}:`, {
          select: select?.value,
          cantidad: inputCantidad?.value
        });
        
        const elementoId = select?.value;
        const cantidad = parseInt(inputCantidad?.value) || 1;
        
        if (elementoId && elementoId !== '') {
          // Agregar el elemento tantas veces como la cantidad
          for (let i = 0; i < cantidad; i++) {
            elementos.push(elementoId);
          }
          console.log(`Agregados ${cantidad} elementos con ID ${elementoId}`);
        }
      });
      
      console.log('Total de elementos a solicitar:', elementos.length);
      console.log('IDs de elementos:', elementos);
      
      if (elementos.length === 0) {
        alert('Debe seleccionar al menos un producto');
        return;
      }

      try {
        // ✅ ARREGLADO: Buscar o crear usuario usando el método correcto
        let usuario = await buscarOCrearUsuarioParaPrestamo(nombre, correo, documento);
        console.log('Usuario:', usuario.id);

        // Crear un préstamo por cada elemento
        console.log('Creando', elementos.length, 'préstamos...');
        
        const prestamos = elementos.map((elementoId, index) => ({
          usuario: { id: usuario.id },
          elemento: { id: parseInt(elementoId) }
        }));
        
        console.log('Préstamos a crear:', JSON.stringify(prestamos, null, 2));
        
        // Crear todos los préstamos en paralelo
        const promesas = prestamos.map((prestamo, index) => {
          console.log(`Enviando préstamo ${index + 1}/${prestamos.length}`);
          return api.post(`${API_ENDPOINTS.prestamos}/solicitar`, prestamo);
        });
        
        const resultados = await Promise.all(promesas);
        console.log('Resultados:', resultados);
        
        alert(`¡Solicitud de ${elementos.length} elemento(s) enviada exitosamente!`);
        formProd.reset();
        
        // Limpiar productos adicionales
        const container = document.getElementById('productosContainer');
        const items = container.querySelectorAll('.producto-item');
        items.forEach((item, index) => {
          if (index > 0) item.remove(); // Dejar solo el primero
        });
        
        reservaProductos.hidden = true;
      } catch (error) {
        console.error('Error completo al enviar solicitud:', error);
        alert('Error al enviar la solicitud: ' + (error.message || 'Error desconocido'));
      }
    });
    console.log('Event listener agregado a formProd');
  }
});

// Agregar otro producto
const btnAgregarProducto = document.getElementById('btnAgregarProducto');
if (btnAgregarProducto) {
  btnAgregarProducto.addEventListener('click', async function() {
    console.log('Agregando nuevo producto');
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
      <button type="button" class="btn-eliminar-producto">✖ Eliminar</button>
    `;
    container.appendChild(nuevo);

    // Event listener para eliminar
    const btnEliminar = nuevo.querySelector('.btn-eliminar-producto');
    btnEliminar.addEventListener('click', () => nuevo.remove());

    try {
      const url = `${API_ENDPOINTS.elementos}/disponibles`;
      const elementos = await api.get(url);
      const select = nuevo.querySelector('select');
      select.innerHTML = elementos.map(elem => 
        `<option value="${elem.id}">${elem.nombre}</option>`
      ).join('');
    } catch (error) {
      console.error('Error:', error);
    }
  });
  console.log('Event listener agregado a btnAgregarProducto');
}

// ✅ ARREGLADO: Función auxiliar para buscar o crear usuario (RESERVAS)
async function buscarOCrearUsuarioParaReserva(nombreCompleto, correo, documento, programa) {
  console.log('Buscando/creando usuario para reserva:', { nombreCompleto, correo, documento, programa });
  
  try {
    // Intentar buscar por correo primero
    const usuarios = await api.get(API_ENDPOINTS.usuarios);
    let usuario = usuarios.find(u => u.correo === correo);
    
    if (usuario) {
      console.log('Usuario encontrado:', usuario.id);
      return usuario;
    }
    
    // Si no existe, crear nuevo usando el endpoint /registrar
    const [nombreParte, ...apellidoPartes] = nombreCompleto.trim().split(/\s+/);
    const apellido = apellidoPartes.join(' ') || apellidoPartes[0] || 'Sin apellido';
    
    const nuevoUsuario = {
      nombre: nombreParte || nombreCompleto,
      apellido: apellido,
      correo: correo.trim(),
      documento: documento ? documento.trim() : '',
      programa: programa ? programa.trim() : '',
      rol: 'estudiante',
      password: '',
      activo: true
    };
    
    console.log('Creando nuevo usuario:', JSON.stringify(nuevoUsuario, null, 2));
    
    try {
      usuario = await api.post(`${API_ENDPOINTS.usuarios}/registrar`, nuevoUsuario);
      console.log('Usuario creado exitosamente:', usuario.id);
      return usuario;
    } catch (registroError) {
      console.error('Error detallado del servidor:', registroError);
      
      // Intentar obtener más detalles del error
      if (registroError.response) {
        console.error('Respuesta del servidor:', registroError.response);
        throw new Error(`Error del servidor: ${registroError.response.error || registroError.response}`);
      }
      
      throw new Error(`No se pudo crear el usuario. Verifica que todos los campos estén correctos.`);
    }
    
  } catch (error) {
    console.error('Error al buscar/crear usuario:', error);
    throw error;
  }
}

// ✅ ARREGLADO: Función auxiliar para buscar o crear usuario (PRÉSTAMOS)
async function buscarOCrearUsuarioParaPrestamo(nombreCompleto, correo, documento) {
  console.log('Buscando/creando usuario para préstamo:', { nombreCompleto, correo, documento });
  
  try {
    // Intentar buscar por correo primero
    const usuarios = await api.get(API_ENDPOINTS.usuarios);
    let usuario = usuarios.find(u => u.correo === correo);
    
    if (usuario) {
      console.log('Usuario encontrado:', usuario.id);
      return usuario;
    }
    
    // Si no existe, crear nuevo usando el endpoint /registrar
    const [nombreParte, ...apellidoPartes] = nombreCompleto.trim().split(/\s+/);
    const apellido = apellidoPartes.join(' ') || '';
    
    const nuevoUsuario = {
      nombre: nombreParte || nombreCompleto,
      apellido: apellido,
      correo: correo,
      documento: documento || '',
      programa: '',
      rol: 'estudiante',
      password: '', // Sin contraseña para usuarios públicos
      activo: true
    };
    
    console.log('Creando nuevo usuario:', nuevoUsuario);
    usuario = await api.post(`${API_ENDPOINTS.usuarios}/registrar`, nuevoUsuario);
    console.log('Usuario creado:', usuario.id);
    return usuario;
    
  } catch (error) {
    console.error('Error al buscar/crear usuario:', error);
    throw new Error('No se pudo crear el usuario: ' + (error.message || 'Error desconocido'));
  }
}
