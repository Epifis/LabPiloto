// estudiantes.js - Gestión de estudiantes adicionales (SIN correo)
console.log('Cargando estudiantes.js...');

document.addEventListener('DOMContentLoaded', function() {
  const masEstudiantes = document.getElementById('masEstudiantes');
  const listaEstudiantes = document.getElementById('listaEstudiantes');
  const btnAgregarEstudiante = document.getElementById('btnAgregarEstudiante');

  // Mostrar/ocultar sección de estudiantes adicionales
  if (masEstudiantes) {
    masEstudiantes.addEventListener('change', function() {
      if (this.value === 'si') {
        listaEstudiantes.hidden = false;
      } else {
        listaEstudiantes.hidden = true;
        // Limpiar estudiantes cuando se oculta
        const container = document.getElementById('estudiantesContainer');
        if (container) {
          container.innerHTML = '';
        }
      }
    });
  }

  // Agregar estudiante adicional
  if (btnAgregarEstudiante) {
    btnAgregarEstudiante.addEventListener('click', function() {
      const container = document.getElementById('estudiantesContainer');
      if (!container) return;

      const estudianteDiv = document.createElement('div');
      estudianteDiv.classList.add('estudiante-item');
      estudianteDiv.innerHTML = `
        <h4>Estudiante Adicional</h4>
        <label>Nombre Completo:
          <input type="text" class="estudiante-nombre" required>
        </label>
        <label>Documento:
          <input type="text" class="estudiante-documento" required>
        </label>
        <button type="button" class="btn-eliminar-estudiante">✖ Eliminar</button>
      `;
      
      container.appendChild(estudianteDiv);

      // Agregar evento para eliminar
      const btnEliminar = estudianteDiv.querySelector('.btn-eliminar-estudiante');
      btnEliminar.addEventListener('click', function() {
        estudianteDiv.remove();
      });
    });
  }
});

// Función para validar estudiantes adicionales
window.validarEstudiantesAdicionales = function() {
  const masEstudiantes = document.getElementById('masEstudiantes');
  
  if (masEstudiantes && masEstudiantes.value === 'si') {
    const items = document.querySelectorAll('#estudiantesContainer .estudiante-item');
    
    if (items.length === 0) {
      alert('Debe agregar al menos un estudiante adicional o cambiar la opción a "No"');
      return false;
    }
    
    // Validar que todos los campos estén llenos
    for (let item of items) {
      const nombre = item.querySelector('.estudiante-nombre').value.trim();
      const documento = item.querySelector('.estudiante-documento').value.trim();
      
      if (!nombre || !documento) {
        alert('Todos los campos de estudiantes adicionales son obligatorios');
        return false;
      }
    }
  }
  
  return true;
};

// Función para obtener estudiantes adicionales (SIN correo)
window.obtenerEstudiantesAdicionales = function() {
  const masEstudiantes = document.getElementById('masEstudiantes');
  
  if (!masEstudiantes || masEstudiantes.value !== 'si') {
    return [];
  }
  
  const items = document.querySelectorAll('#estudiantesContainer .estudiante-item');
  const estudiantes = [];
  
  items.forEach(item => {
    const nombre = item.querySelector('.estudiante-nombre').value.trim();
    const documento = item.querySelector('.estudiante-documento').value.trim();
    
    if (nombre && documento) {
      estudiantes.push({
        nombre: nombre,
        documento: documento
      });
    }
  });
  
  return estudiantes;
};

console.log('estudiantes.js cargado completamente');
