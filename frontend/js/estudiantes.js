// estudiantes.js - Manejo de estudiantes adicionales en reservas

console.log('estudiantes.js cargado');

document.addEventListener('DOMContentLoaded', function() {
  console.log('Inicializando gestión de estudiantes adicionales');
  
  const masEstudiantes = document.getElementById('masEstudiantes');
  const listaEstudiantes = document.getElementById('listaEstudiantes');
  const btnAgregarEstudiante = document.getElementById('btnAgregarEstudiante');
  const estudiantesContainer = document.getElementById('estudiantesContainer');

  // Mostrar/ocultar sección de estudiantes adicionales
  if (masEstudiantes && listaEstudiantes) {
    masEstudiantes.addEventListener('change', function() {
      console.log('Cambio en masEstudiantes:', this.value);
      if (this.value === 'si') {
        listaEstudiantes.hidden = false;
      } else {
        listaEstudiantes.hidden = true;
        // Limpiar estudiantes agregados si oculta la sección
        if (estudiantesContainer) {
          estudiantesContainer.innerHTML = '';
        }
      }
    });
    console.log('Event listener agregado a masEstudiantes');
  } else {
    console.warn('No se encontraron elementos masEstudiantes o listaEstudiantes');
  }

  // Agregar nuevo estudiante
  if (btnAgregarEstudiante && estudiantesContainer) {
    btnAgregarEstudiante.addEventListener('click', function() {
      console.log('Agregando nuevo estudiante');
      const estudianteDiv = document.createElement('div');
      estudianteDiv.classList.add('estudiante-extra');
      
      const estudianteCount = estudiantesContainer.children.length + 1;
      
      estudianteDiv.innerHTML = `
        <div class="estudiante-header">
          <h5>Estudiante ${estudianteCount}</h5>
          <button type="button" class="btn-eliminar-estudiante" title="Eliminar">✖</button>
        </div>
        
        <label>Nombre:
          <input type="text" name="estudiante_nombre[]" required class="estudiante-input">
        </label>
        
        <label>Apellido:
          <input type="text" name="estudiante_apellido[]" required class="estudiante-input">
        </label>
        
        <label>Código o Documento:
          <input type="text" name="estudiante_documento[]" class="estudiante-input">
        </label>
      `;
      
      estudiantesContainer.appendChild(estudianteDiv);
      
      // Event listener para eliminar este estudiante
      const btnEliminar = estudianteDiv.querySelector('.btn-eliminar-estudiante');
      btnEliminar.addEventListener('click', function() {
        console.log('Eliminando estudiante');
        estudianteDiv.remove();
        actualizarNumerosEstudiantes();
      });
      
      console.log('Estudiante agregado. Total:', estudiantesContainer.children.length);
    });
    console.log('Event listener agregado a btnAgregarEstudiante');
  } else {
    console.warn('No se encontraron elementos btnAgregarEstudiante o estudiantesContainer');
  }
});

// Actualizar números de estudiantes después de eliminar
function actualizarNumerosEstudiantes() {
  const estudiantesContainer = document.getElementById('estudiantesContainer');
  if (!estudiantesContainer) return;
  
  const estudiantes = estudiantesContainer.querySelectorAll('.estudiante-extra');
  estudiantes.forEach((est, index) => {
    const header = est.querySelector('.estudiante-header h5');
    if (header) {
      header.textContent = `Estudiante ${index + 1}`;
    }
  });
  console.log('Números de estudiantes actualizados');
}

// Función para obtener estudiantes adicionales del formulario
export function obtenerEstudiantesAdicionales() {
  const masEstudiantes = document.getElementById('masEstudiantes');
  
  if (!masEstudiantes || masEstudiantes.value === 'no') {
    console.log('No hay estudiantes adicionales seleccionados');
    return [];
  }
  
  const estudiantes = [];
  const estudianteDivs = document.querySelectorAll('.estudiante-extra');
  
  console.log('Procesando', estudianteDivs.length, 'estudiantes adicionales');
  
  estudianteDivs.forEach((div, index) => {
    const nombre = div.querySelector('input[name="estudiante_nombre[]"]')?.value;
    const apellido = div.querySelector('input[name="estudiante_apellido[]"]')?.value;
    const documento = div.querySelector('input[name="estudiante_documento[]"]')?.value;
    
    if (nombre && apellido) {
      estudiantes.push({
        nombre: nombre.trim(),
        apellido: apellido.trim(),
        documento: documento?.trim() || null
      });
      console.log(`Estudiante ${index + 1}:`, { nombre, apellido, documento });
    }
  });
  
  console.log('Total estudiantes procesados:', estudiantes.length);
  return estudiantes;
}

// Función para validar estudiantes adicionales
export function validarEstudiantesAdicionales() {
  const masEstudiantes = document.getElementById('masEstudiantes');
  
  if (!masEstudiantes || masEstudiantes.value === 'no') {
    console.log('Validación: No hay estudiantes adicionales para validar');
    return true;
  }
  
  const estudianteDivs = document.querySelectorAll('.estudiante-extra');
  
  if (estudianteDivs.length === 0) {
    alert('Debe agregar al menos un estudiante o cambiar la opción a "No"');
    console.warn('Validación fallida: No hay estudiantes agregados');
    return false;
  }
  
  let valido = true;
  
  estudianteDivs.forEach((div, index) => {
    const nombre = div.querySelector('input[name="estudiante_nombre[]"]')?.value;
    const apellido = div.querySelector('input[name="estudiante_apellido[]"]')?.value;
    
    if (!nombre || !apellido) {
      alert(`Complete los datos del estudiante ${index + 1}`);
      console.warn(`Validación fallida: Datos incompletos en estudiante ${index + 1}`);
      valido = false;
    }
  });
  
  console.log('Resultado validación:', valido);
  return valido;
}

// Exponer funciones globalmente para otros scripts
window.obtenerEstudiantesAdicionales = obtenerEstudiantesAdicionales;
window.validarEstudiantesAdicionales = validarEstudiantesAdicionales;