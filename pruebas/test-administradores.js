// test-administradores.js - VERSIÓN MEJORADA CON MANEJO DE ERRORES
const API_BASE = 'https://labpiloto.com/api';

const administradores = [
  { correo: 'admin@upc.edu.co', password: 'admin123' },
  { correo: 'ana.admin@upc.edu.co', password: 'admin123' },
  { correo: 'carlos.admin@upc.edu.co', password: 'admin123' },
  { correo: 'maria.admin@upc.edu.co', password: 'admin123' },
  { correo: 'pedro.admin@upc.edu.co', password: 'admin123' },
  { correo: 'laura.admin@upc.edu.co', password: 'admin123' }
  // Reducimos a 6 admins para mejor manejo
];

class AdministradorSimulado {
  constructor(credenciales, id) {
    this.credenciales = credenciales;
    this.id = id;
    this.token = null;
    this.activo = false;
    this.intentosFallidos = 0;
  }

  async autenticar() {
    try {
      const response = await fetch(`${API_BASE}/auth/login`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(this.credenciales)
      });

      if (response.ok) {
        const data = await response.json();
        this.token = data.token;
        this.intentosFallidos = 0;
        console.log(`🔐 Admin ${this.id} autenticado: ${this.credenciales.correo}`);
        return true;
      } else {
        const errorText = await response.text();
        console.log(`❌ Error autenticando admin ${this.id}: ${response.status} - ${errorText}`);
        this.intentosFallidos++;
        return false;
      }
    } catch (error) {
      console.error(`❌ Error autenticando admin ${this.id}:`, error.message);
      this.intentosFallidos++;
      return false;
    }
  }

  async obtenerConFiltro(endpoint, estado = '') {
    try {
      const url = estado ? `${endpoint}?estado=${estado}` : endpoint;
      const response = await fetch(url, {
        headers: { 'Authorization': `Bearer ${this.token}` }
      });
      
      if (response.ok) {
        return await response.json();
      } else if (response.status === 401) {
        console.log(`🔐 Admin ${this.id} necesita reautenticarse`);
        await this.autenticar();
      }
      return [];
    } catch (error) {
      console.error(`❌ Error en petición admin ${this.id}:`, error.message);
      return [];
    }
  }

  async obtenerReservasPendientes() {
    return await this.obtenerConFiltro(`${API_BASE}/reservas`, 'Pendiente');
  }

  async obtenerPrestamosPendientes() {
    return await this.obtenerConFiltro(`${API_BASE}/prestamos`, 'Pendiente');
  }

  async obtenerPrestamosAprobados() {
    return await this.obtenerConFiltro(`${API_BASE}/prestamos`, 'Aprobado');
  }

  async obtenerPrestamosPrestados() {
    return await this.obtenerConFiltro(`${API_BASE}/prestamos`, 'Prestado');
  }

  async ejecutarAccionConReintento(accion, id, tipo) {
    try {
      const response = await fetch(`${API_BASE}/${tipo}/${id}/${accion}`, {
        method: 'PUT',
        headers: { 
          'Authorization': `Bearer ${this.token}`,
          'Content-Type': 'application/json'
        }
      });

      const responseText = await response.text();
      
      if (response.ok) {
        console.log(`✅ Admin ${this.id} ${accion} ${tipo} ${id} - Status: ${response.status}`);
        return true;
      } else if (response.status === 400) {
        console.log(`⚠️  Admin ${this.id}: ${tipo} ${id} ya estaba en ese estado o datos inválidos - Status: ${response.status}`);
        return false; // No reintentar para 400
      } else if (response.status === 401) {
        console.log(`🔐 Admin ${this.id} necesita reautenticarse`);
        await this.autenticar();
        return false;
      } else {
        console.log(`❌ Admin ${this.id} error en ${accion} ${tipo} ${id} - Status: ${response.status} - ${responseText}`);
        return false;
      }
    } catch (error) {
      console.error(`❌ Error en ${accion} ${tipo} admin ${this.id}:`, error.message);
      return false;
    }
  }

  async ejecutarAccion() {
    // Reautenticar si hay muchos intentos fallidos
    if (this.intentosFallidos > 3) {
      console.log(`🔄 Admin ${this.id} reintentando autenticación después de ${this.intentosFallidos} fallos`);
      await this.autenticar();
      return;
    }

    if (!this.token) {
      const autenticado = await this.autenticar();
      if (!autenticado) return;
    }

    const tipoAccion = Math.random();
    
    try {
      if (tipoAccion < 0.35) {
        // Gestionar reservas (35% probabilidad)
        const reservas = await this.obtenerReservasPendientes();
        if (reservas.length > 0) {
          const reserva = reservas[Math.floor(Math.random() * reservas.length)];
          const aprobar = Math.random() > 0.3; // 70% aprobar, 30% rechazar
          const accion = aprobar ? 'aprobar' : 'rechazar';
          await this.ejecutarAccionConReintento(accion, reserva.id, 'reservas');
        } else {
          console.log(`📭 Admin ${this.id}: No hay reservas pendientes`);
        }
      } else if (tipoAccion < 0.65) {
        // Gestionar préstamos pendientes (30% probabilidad)
        const prestamos = await this.obtenerPrestamosPendientes();
        if (prestamos.length > 0) {
          const prestamo = prestamos[Math.floor(Math.random() * prestamos.length)];
          const aprobar = Math.random() > 0.3; // 70% aprobar, 30% rechazar
          const accion = aprobar ? 'aprobar' : 'rechazar';
          await this.ejecutarAccionConReintento(accion, prestamo.id, 'prestamos');
        } else {
          console.log(`📭 Admin ${this.id}: No hay préstamos pendientes`);
        }
      } else if (tipoAccion < 0.85) {
        // Gestionar préstamos aprobados (20% probabilidad) - Marcar como prestado
        const prestamosAprobados = await this.obtenerPrestamosAprobados();
        if (prestamosAprobados.length > 0) {
          const prestamo = prestamosAprobados[Math.floor(Math.random() * prestamosAprobados.length)];
          await this.ejecutarAccionConReintento('prestar', prestamo.id, 'prestamos');
        } else {
          console.log(`📭 Admin ${this.id}: No hay préstamos aprobados para entregar`);
        }
      } else {
        // Gestionar préstamos prestados (15% probabilidad) - Marcar como devuelto
        const prestamosPrestados = await this.obtenerPrestamosPrestados();
        if (prestamosPrestados.length > 0) {
          const prestamo = prestamosPrestados[Math.floor(Math.random() * prestamosPrestados.length)];
          await this.ejecutarAccionConReintento('devolver', prestamo.id, 'prestamos');
        } else {
          console.log(`📭 Admin ${this.id}: No hay préstamos activos para devolver`);
        }
      }
      
    } catch (error) {
      console.error(`❌ Error en acción admin ${this.id}:`, error.message);
      this.intentosFallidos++;
    }
  }

  iniciar() {
    this.activo = true;
    console.log(`👨‍💼 Admin ${this.id} iniciado: ${this.credenciales.correo}`);
    
    const ciclo = () => {
      if (this.activo) {
        setTimeout(async () => {
          await this.ejecutarAccion();
          ciclo();
        }, 6000 + Math.random() * 3000); // 6-9 segundos entre acciones
      }
    };
    
    ciclo();
  }

  detener() {
    this.activo = false;
    console.log(`🛑 Admin ${this.id} detenido`);
  }
}

// Ejecutar pruebas de administradores
async function ejecutarPruebasAdministradores() {
  console.log('🚀 Iniciando pruebas MEJORADAS de administradores (6 administradores simultáneos)...');
  console.log('🔑 Todas las contraseñas: admin123');
  console.log('🔄 Con manejo mejorado de errores y reintentos\n');
  
  const admins = administradores.map((credenciales, index) => 
    new AdministradorSimulado(credenciales, index + 1)
  );

  // Autenticar todos primero
  console.log('🔐 Autenticando administradores...');
  for (const admin of admins) {
    await admin.autenticar();
  }

  // Iniciar ciclos de trabajo
  console.log('\n👨‍💼 Iniciando ciclos de trabajo de administradores...');
  admins.forEach(admin => admin.iniciar());

  // Ejecutar por 10 minutos
  console.log('\n⏰ Las pruebas se ejecutarán por 10 minutos...\n');
  setTimeout(() => {
    console.log('\n🛑 Deteniendo pruebas de administradores después de 10 minutos...');
    admins.forEach(admin => admin.detener());
    
    console.log('\n🎉 Pruebas de administradores completadas');
    console.log('📊 Los logs muestran el comportamiento real del sistema');
  }, 10 * 60 * 1000);
}

// Exportar para test-principal.js
module.exports = { ejecutarPruebasAdministradores };

// Ejecutar directamente si se llama solo
if (require.main === module) {
  ejecutarPruebasAdministradores().catch(console.error);
}
