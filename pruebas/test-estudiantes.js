// test-estudiantes.js - VERSIÓN FINAL CORREGIDA
const API_BASE = 'https://labpiloto.com/api';

// Lista de estudiantes SOLO con correos de prueba
const estudiantes = [
  { id: 26, documento: '123654', nombre: 'prueba', apellido: 'Sin apellido', correo: 'prueba@gmail.co', programa: 'ing' },
  { id: 54, documento: 'EST001', nombre: 'Juan', apellido: 'Perez', correo: 'est1@pruebas.com', programa: 'Ingenieria de Sistemas' },
  { id: 55, documento: 'EST002', nombre: 'Maria', apellido: 'Gonzalez', correo: 'est2@pruebas.com', programa: 'Ingenieria Civil' },
  { id: 56, documento: 'EST003', nombre: 'Carlos', apellido: 'Lopez', correo: 'est3@pruebas.com', programa: 'Ingenieria Mecatronica' },
  { id: 57, documento: 'EST004', nombre: 'Ana', apellido: 'Martinez', correo: 'est4@pruebas.com', programa: 'Economia' },
  { id: 58, documento: 'EST005', nombre: 'Luis', apellido: 'Rodriguez', correo: 'est5@pruebas.com', programa: 'Finanzas' },
  { id: 59, documento: 'EST006', nombre: 'Laura', apellido: 'Hernandez', correo: 'est6@pruebas.com', programa: 'Fisica' },
  { id: 60, documento: 'EST007', nombre: 'David', apellido: 'Garcia', correo: 'est7@pruebas.com', programa: 'Ingenieria de Sistemas' },
  { id: 61, documento: 'EST008', nombre: 'Sofia', apellido: 'Sanchez', correo: 'est8@pruebas.com', programa: 'Ingenieria Civil' },
  { id: 62, documento: 'EST009', nombre: 'Miguel', apellido: 'Ramirez', correo: 'est9@pruebas.com', programa: 'Ingenieria Mecatronica' }
];

const laboratorios = [
  { id: 1, nombre: 'LAB DESARROLLO WEB', ubicacion: 'F201', capacidad: 24 },
  { id: 2, nombre: 'LAB IOT', ubicacion: 'F202', capacidad: 20 },
  { id: 3, nombre: 'LAB FÍSICA 1', ubicacion: 'F101', capacidad: 30 },
  { id: 4, nombre: 'LAB DESARROLLO WEB', ubicacion: 'F201', capacidad: 24 },
  { id: 5, nombre: 'LAB IOT', ubicacion: 'F202', capacidad: 20 },
  { id: 6, nombre: 'LAB FISICA 1', ubicacion: 'F101', capacidad: 30 },
  { id: 7, nombre: 'LAB ELECTRONICA', ubicacion: 'F103', capacidad: 26 },
  { id: 8, nombre: 'SALA ESTUDIO 1', ubicacion: 'F103B', capacidad: 25 },
  { id: 9, nombre: 'Laboratorio Prueba', ubicacion: 'F500', capacidad: 500 }
];

const productos = [
  { id: 1, nombre: 'Laptop', categoria: 'Equipos' },
  { id: 2, nombre: 'Sensor Temperatura', categoria: 'Sensores' },
  { id: 3, nombre: 'Arduino Uno', categoria: 'Electrónica' },
  { id: 4, nombre: 'Multímetro', categoria: 'Instrumentos' },
  { id: 5, nombre: 'Laptop Dell Latitude', categoria: 'Equipos' },
  { id: 6, nombre: 'Sensor Temperatura DHT22', categoria: 'Sensores' },
  { id: 7, nombre: 'Arduino Uno R3', categoria: 'Electrónica' },
  { id: 8, nombre: 'Multímetro Digital', categoria: 'Instrumentos' },
  { id: 9, nombre: 'Protoboard 830pts', categoria: 'Componentes' },
  { id: 10, nombre: 'Raspberry Pi 4', categoria: 'Equipos' },
  { id: 11, nombre: 'Osciloscopio 100MHz', categoria: 'Instrumentos' }
];

function generarFechaAleatoria() {
  const start = new Date('2025-11-24');
  const end = new Date('2025-12-25');
  const fecha = new Date(start.getTime() + Math.random() * (end.getTime() - start.getTime()));
  return fecha.toISOString().split('T')[0];
}

function generarHoraAleatoria() {
  const horas = ['08:00', '09:00', '10:00', '11:00', '14:00', '15:00', '16:00'];
  return horas[Math.floor(Math.random() * horas.length)];
}

async function crearReservaLaboratorio(estudiante) {
  try {
    const lab = laboratorios[Math.floor(Math.random() * laboratorios.length)];

    const reserva = {
      fechaInicio: `${generarFechaAleatoria()}T${generarHoraAleatoria()}:00`,
      fechaFin: `${generarFechaAleatoria()}T17:00:00`,
      tipoReserva: 'practica_libre',
      usuario: { id: estudiante.id },
      laboratorio: { id: lab.id },
      cantidadEstudiantes: Math.floor(Math.random() * 3) + 1,
      invitados: []
    };

    console.log(`📤 Enviando reserva para ${estudiante.nombre}...`);

    const response = await fetch(`${API_BASE}/reservas/solicitar`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(reserva)
    });

    const responseText = await response.text();
    console.log(`📥 Respuesta reserva ${estudiante.nombre}: ${response.status} - ${responseText.substring(0, 100)}`);

    return response.ok;
  } catch (error) {
    console.error(`❌ Error en reserva de ${estudiante.nombre}:`, error.message);
    return false;
  }
}

async function crearPrestamoProducto(estudiante) {
  try {
    const producto = productos[Math.floor(Math.random() * productos.length)];

    // ✅ CORREGIDO: Estructura correcta para préstamos
    const prestamo = {
      usuario: { id: estudiante.id },
      elemento: { id: producto.id }  // ✅ Cambiado de "elementos" array a "elemento" objeto
    };

    console.log(`📤 Enviando préstamo para ${estudiante.nombre} - Producto: ${producto.nombre}...`);

    // ✅ CORREGIDO: Endpoint correcto para préstamos
    const response = await fetch(`${API_BASE}/prestamos/solicitar`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(prestamo)
    });

    const responseText = await response.text();
    console.log(`📥 Respuesta préstamo ${estudiante.nombre}: ${response.status} - ${responseText.substring(0, 100)}`);

    return response.ok;
  } catch (error) {
    console.error(`❌ Error en préstamo de ${estudiante.nombre}:`, error.message);
    return false;
  }
}

// Ejecutar pruebas
async function ejecutarPruebasEstudiantes() {
  console.log('🚀 Iniciando pruebas SEGURAS de estudiantes (10 operaciones en 1 minuto)...');
  console.log('📧 Usando SOLO correos de prueba: @pruebas.com, @gmail.co');

  const totalOperaciones = 10;
  const duracion = 1 * 60 * 1000;
  const intervalo = duracion / totalOperaciones;

  let operacionesCompletadas = 0;
  let exitosas = 0;
  let fallidas = 0;

  const promesas = [];

  for (let i = 0; i < totalOperaciones; i++) {
    const promesa = new Promise((resolve) => {
      setTimeout(async () => {
        const estudiante = estudiantes[Math.floor(Math.random() * estudiantes.length)];
        const esReserva = Math.random() > 0.3;

        const exito = esReserva ?
          await crearReservaLaboratorio(estudiante) :
          await crearPrestamoProducto(estudiante);

        operacionesCompletadas++;
        if (exito) exitosas++; else fallidas++;

        console.log(`📊 Progreso: ${operacionesCompletadas}/${totalOperaciones} | Éxitos: ${exitosas} | Fallos: ${fallidas}`);
        resolve();
      }, i * intervalo);
    });
    promesas.push(promesa);
  }

  await Promise.all(promesas);
  console.log('🎉 Pruebas de estudiantes completadas');
  console.log(`📈 Resumen: ${exitosas} exitosas, ${fallidas} fallidas`);
}

// Exportar para test-principal.js
module.exports = { ejecutarPruebasEstudiantes };

// Ejecutar directamente si se llama solo
if (require.main === module) {
  ejecutarPruebasEstudiantes().catch(console.error);
}
