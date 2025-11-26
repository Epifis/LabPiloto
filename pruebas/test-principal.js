// test-principal.js - VERSIÓN PARALELA
const { ejecutarPruebasEstudiantes } = require('./test-estudiantes');
const { ejecutarPruebasAdministradores } = require('./test-administradores');

console.log('🎯 INICIANDO PRUEBAS COMPLETAS DEL SISTEMA');
console.log('==========================================');

const inicio = Date.now();

// Ejecutar en paralelo pero con un pequeño delay entre ellas
setTimeout(() => {
    console.log('\n📝 Iniciando pruebas de estudiantes...');
    ejecutarPruebasEstudiantes().then(() => {
        console.log('✅ Pruebas de estudiantes completadas');
    });
}, 1000);

setTimeout(() => {
    console.log('\n👨‍💼 Iniciando pruebas de administradores...');
    ejecutarPruebasAdministradores().then(() => {
        console.log('✅ Pruebas de administradores completadas');
    });
}, 2000);

// Monitorear finalización
Promise.allSettled([
    ejecutarPruebasEstudiantes(),
    ejecutarPruebasAdministradores()
]).then((results) => {
    const fin = Date.now();
    const duracion = (fin - inicio) / 1000;
    
    console.log('\n📊 RESUMEN FINAL DE PRUEBAS');
    console.log('==========================');
    console.log(`⏱️  Duración total: ${duracion.toFixed(2)} segundos`);
    console.log(`📈 Throughput estimado: ${(30 / duracion).toFixed(2)} ops/segundo`);
    
    results.forEach((result, index) => {
        const nombre = index === 0 ? 'Estudiantes' : 'Administradores';
        if (result.status === 'fulfilled') {
            console.log(`✅ ${nombre}: Completado exitosamente`);
        } else {
            console.log(`❌ ${nombre}: Falló - ${result.reason}`);
        }
    });
});
