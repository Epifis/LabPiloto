const API_BASE = 'https://labpiloto.com/api';

async function pruebaSimple() {
  console.log('🧪 Probando estructura CORRECTA del JSON...');
  
  const reservaEjemplo = {
    fechaInicio: '2025-11-25T10:00:00',
    fechaFin: '2025-11-25T12:00:00',
    tipoReserva: 'practica_libre',  // ✅ CORRECTO
    usuario: { id: 22 },            // ✅ CORRECTO - objeto con id
    laboratorio: { id: 1 },         // ✅ CORRECTO - objeto con id
    cantidadEstudiantes: 2,
    invitados: []  // ✅ Array vacío si no hay invitados
  };

  console.log('📤 Enviando:', JSON.stringify(reservaEjemplo, null, 2));
  
  try {
    // ✅ CORREGIR endpoint: /reservas/solicitar
    const response = await fetch(`${API_BASE}/reservas/solicitar`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(reservaEjemplo)
    });
    
    console.log(`📥 Status: ${response.status}`);
    const text = await response.text();
    console.log(`📥 Respuesta: ${text}`);
    
  } catch (error) {
    console.error('❌ Error:', error.message);
  }
}

pruebaSimple();
