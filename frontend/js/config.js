// config.js - CORREGIDO - Protección de rutas arreglada

// 1. CONFIGURACIÓN GLOBAL
window.API_BASE_URL = 'https://labpiloto.com/api';

window.API_ENDPOINTS = {
  // ============ AUTENTICACIÓN ============
  auth: {
    login: `${window.API_BASE_URL}/auth/login`,
    verify: `${window.API_BASE_URL}/auth/verify`,
    logout: `${window.API_BASE_URL}/auth/logout`,
    
    estudiante: {
      registro: `${window.API_BASE_URL}/auth/estudiante/registro`,
      validarCodigo: `${window.API_BASE_URL}/auth/estudiante/validar-codigo-verificacion`,
      login: `${window.API_BASE_URL}/auth/estudiante/login`,
      verifyMfa: `${window.API_BASE_URL}/auth/estudiante/verify-mfa`,
      reenviarCodigo: `${window.API_BASE_URL}/auth/estudiante/reenviar-codigo`
    },
    
    profesor: {
      registro: `${window.API_BASE_URL}/auth/profesor/registro`,
      login: `${window.API_BASE_URL}/auth/profesor/login`,
      verifyMfa: `${window.API_BASE_URL}/auth/profesor/verify-mfa`,
      reenviarCodigo: `${window.API_BASE_URL}/auth/profesor/reenviar-codigo`
    },
    
    admin: {
      login: `${window.API_BASE_URL}/auth/admin/login`,
      verifyMfa: `${window.API_BASE_URL}/auth/admin/verify-mfa`,
      reenviarCodigo: `${window.API_BASE_URL}/auth/admin/reenviar-codigo`
    },
    
    contrato: {
      obtener: `${window.API_BASE_URL}/auth/contrato`,
      verificarAceptacion: `${window.API_BASE_URL}/auth/contrato/verificar-aceptacion`,
      aceptar: `${window.API_BASE_URL}/auth/contrato/aceptar`,
      firmarEstudiante: (token) => `${window.API_BASE_URL}/auth/firmar-contrato-estudiante/${token}`,
      verificarEstudiante: (token) => `${window.API_BASE_URL}/auth/verificar-estudiante/${token}`
    }
  },
  
  // ============ MFA ============
  mfa: {
    start: `${window.API_BASE_URL}/mfa/start`,
    verifyLogin: `${window.API_BASE_URL}/mfa/verify-login`,
    reenviar: `${window.API_BASE_URL}/mfa/reenviar`
  },
  
  // ============ USUARIOS ============
  usuarios: {
    base: `${window.API_BASE_URL}/usuarios`,
    listar: `${window.API_BASE_URL}/usuarios`,
    registrar: `${window.API_BASE_URL}/usuarios/registrar`,
    verificarCodigo: `${window.API_BASE_URL}/usuarios/verificar-codigo`,
    reenviarCodigo: `${window.API_BASE_URL}/usuarios/reenviar-codigo`,
    firmarContrato: (token) => `${window.API_BASE_URL}/usuarios/firmar-contrato/${token}`,
    obtener: (id) => `${window.API_BASE_URL}/usuarios/${id}`,
    actualizar: (id) => `${window.API_BASE_URL}/usuarios/${id}`,
    eliminar: (id) => `${window.API_BASE_URL}/usuarios/${id}`,
    cambiarEstado: (id) => `${window.API_BASE_URL}/usuarios/${id}/estado`
  },
  
  // ============ LABORATORIOS ============
  laboratorios: {
    base: `${window.API_BASE_URL}/laboratorios`,
    listar: `${window.API_BASE_URL}/laboratorios`,
    disponibles: `${window.API_BASE_URL}/laboratorios/disponibles`,
    obtener: (id) => `${window.API_BASE_URL}/laboratorios/${id}`,
    crear: `${window.API_BASE_URL}/laboratorios`,
    actualizar: (id) => `${window.API_BASE_URL}/laboratorios/${id}`,
    eliminar: (id) => `${window.API_BASE_URL}/laboratorios/${id}`,
    cambiarEstado: (id) => `${window.API_BASE_URL}/laboratorios/${id}/estado`
  },
  
  // ============ ELEMENTOS ============
  elementos: {
    base: `${window.API_BASE_URL}/elementos`,
    listar: `${window.API_BASE_URL}/elementos`,
    disponibles: `${window.API_BASE_URL}/elementos/disponibles`,
    obtener: (id) => `${window.API_BASE_URL}/elementos/${id}`,
    crear: `${window.API_BASE_URL}/elementos`,
    actualizar: (id) => `${window.API_BASE_URL}/elementos/${id}`,
    eliminar: (id) => `${window.API_BASE_URL}/elementos/${id}`,
    actualizarCantidad: (id) => `${window.API_BASE_URL}/elementos/${id}/cantidad`
  },
  
  // ============ RESERVAS ============
  reservas: {
    base: `${window.API_BASE_URL}/reservas`,
    listar: `${window.API_BASE_URL}/reservas`,
    solicitar: `${window.API_BASE_URL}/reservas/solicitar`,
    pendientes: `${window.API_BASE_URL}/reservas/pendientes`,
    disponibilidad: `${window.API_BASE_URL}/reservas/disponibilidad`,
    recurrentes: `${window.API_BASE_URL}/reservas/recurrentes`,
    obtener: (id) => `${window.API_BASE_URL}/reservas/${id}`,
    porUsuario: (usuarioId) => `${window.API_BASE_URL}/reservas/usuario/${usuarioId}`,
    aprobar: (id) => `${window.API_BASE_URL}/reservas/${id}/aprobar`,
    rechazar: (id) => `${window.API_BASE_URL}/reservas/${id}/rechazar`,
    cancelar: (id) => `${window.API_BASE_URL}/reservas/${id}/cancelar`,
    activar: (id) => `${window.API_BASE_URL}/reservas/${id}/activar`,
    completar: (id) => `${window.API_BASE_URL}/reservas/${id}/completar`
  },
  
  // ============ PRÉSTAMOS ============
  prestamos: {
    base: `${window.API_BASE_URL}/prestamos`,
    listar: `${window.API_BASE_URL}/prestamos`,
    solicitar: `${window.API_BASE_URL}/prestamos/solicitar`,
    pendientes: `${window.API_BASE_URL}/prestamos/pendientes`,
    obtener: (id) => `${window.API_BASE_URL}/prestamos/${id}`,
    porUsuario: (usuarioId) => `${window.API_BASE_URL}/prestamos/usuario/${usuarioId}`,
    aprobar: (id) => `${window.API_BASE_URL}/prestamos/${id}/aprobar`,
    prestar: (id) => `${window.API_BASE_URL}/prestamos/${id}/prestar`,
    rechazar: (id) => `${window.API_BASE_URL}/prestamos/${id}/rechazar`,
    devolver: (id) => `${window.API_BASE_URL}/prestamos/${id}/devolver`,
    aprobarLote: `${window.API_BASE_URL}/prestamos/aprobar-lote`,
    rechazarLote: `${window.API_BASE_URL}/prestamos/rechazar-lote`
  },
  
  // ============ CURSOS ============
  cursos: {
    base: `${window.API_BASE_URL}/cursos`,
    listar: `${window.API_BASE_URL}/cursos`,
    obtenerPorNrc: (nrc) => `${window.API_BASE_URL}/cursos/${nrc}`,
    crear: `${window.API_BASE_URL}/cursos`,
    actualizar: (nrc) => `${window.API_BASE_URL}/cursos/${nrc}`,
    eliminar: (nrc) => `${window.API_BASE_URL}/cursos/${nrc}`
  },
  
  // ============ ADMINS ============
  admins: {
    base: `${window.API_BASE_URL}/admins`,
    solicitar: `${window.API_BASE_URL}/admins/solicitar`,
    aprobar: (token) => `${window.API_BASE_URL}/admins/aprobar/${token}`,
    rechazar: (token) => `${window.API_BASE_URL}/admins/rechazar/${token}`,
    firmarContrato: (token) => `${window.API_BASE_URL}/admins/firmar-contrato/${token}`
  }
};

// 2. SERVICIO API MEJORADO
window.api = {
  async get(url) {
    console.log('GET:', url);
    const token = localStorage.getItem('authToken');
    
    const response = await fetch(url, {
      headers: {
        'Authorization': token ? `Bearer ${token}` : '',
        'Content-Type': 'application/json'
      }
    });
    
    if (!response.ok) {
      const errorText = await response.text();
      console.error('Error en GET:', errorText);
      throw new Error(`Error: ${response.status}`);
    }
    
    return response.json();
  },

  async post(url, data) {
    console.log('POST:', url);
    console.log('Data:', JSON.stringify(data, null, 2));
    
    const token = localStorage.getItem('authToken');
    
    const response = await fetch(url, {
      method: 'POST',
      headers: {
        'Authorization': token ? `Bearer ${token}` : '',
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(data)
    });

    const responseText = await response.text();
    console.log('Respuesta del servidor:', responseText);
    
    if (!response.ok) {
      console.error('Error en POST:', response.status, responseText);
      
      try {
        const errorData = JSON.parse(responseText);
        const error = new Error(`Error: ${response.status}`);
        error.response = errorData;
        throw error;
      } catch (parseError) {
        throw new Error(`Error: ${response.status} - ${responseText}`);
      }
    }

    try {
      return JSON.parse(responseText);
    } catch (e) {
      return responseText;
    }
  },

  async put(url, data) {
    console.log('PUT:', url);
    console.log('Data:', JSON.stringify(data, null, 2));
    
    const token = localStorage.getItem('authToken');
    
    const response = await fetch(url, {
      method: 'PUT',
      headers: {
        'Authorization': token ? `Bearer ${token}` : '',
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(data)
    });

    const responseText = await response.text();
    console.log('Respuesta del servidor:', responseText);

    if (!response.ok) {
      console.error('Error en PUT:', response.status, responseText);
      
      try {
        const errorData = JSON.parse(responseText);
        const error = new Error(`Error: ${response.status}`);
        error.response = errorData;
        throw error;
      } catch (parseError) {
        throw new Error(`Error: ${response.status} - ${responseText}`);
      }
    }

    try {
      return JSON.parse(responseText);
    } catch (e) {
      return responseText;
    }
  },

  async delete(url) {
    console.log('DELETE:', url);
    const token = localStorage.getItem('authToken');
    
    const response = await fetch(url, {
      method: 'DELETE',
      headers: {
        'Authorization': token ? `Bearer ${token}` : ''
      }
    });

    if (!response.ok) {
      const errorText = await response.text();
      console.error('Error en DELETE:', errorText);
      throw new Error(`Error: ${response.status}`);
    }

    const text = await response.text();
    return text ? JSON.parse(text) : null;
  }
};

// 3. COMPATIBILIDAD
window.apiService = window.api;

// 4. HELPER DE AUTENTICACIÓN
window.authHelper = {
  cerrarSesion() {
    const token = localStorage.getItem('authToken');
    if (token) {
      api.post(API_ENDPOINTS.auth.logout).catch(() => {});
    }
    
    localStorage.removeItem('authToken');
    localStorage.removeItem('userData');
    localStorage.removeItem('adminUser');
    window.location.href = 'login-estudiante.html';
  },
  
  getUsuarioActual() {
    try {
      const userData = localStorage.getItem('userData');
      return userData ? JSON.parse(userData) : null;
    } catch (e) {
      return null;
    }
  },
  
  estaAutenticado() {
    return !!localStorage.getItem('authToken') && !!localStorage.getItem('userData');
  },
  
  // ✅ NUEVA FUNCIÓN: Verificar si el usuario es admin
  esAdmin(rol) {
    if (!rol) return false;
    const rolNormalizado = String(rol).toLowerCase().trim();
    return rolNormalizado === 'admin' || 
           rolNormalizado === 'administrador' || 
           rolNormalizado === 'superadmin' || 
           rolNormalizado.includes('admin');
  }
};

// 5. PROTECCIÓN DE RUTAS - CORREGIDA
const currentPage = window.location.pathname.split('/').pop();
const publicPages = ['login-estudiante.html', 'login-profesor.html', 'login-admin.html'];

// Si la página actual NO está en la lista de páginas públicas, requiere autenticación
if (!publicPages.includes(currentPage)) {
    const token = localStorage.getItem('authToken');
    const userData = localStorage.getItem('userData');
    
    if (!token || !userData) {
        console.log('🚫 No autenticado, redirigiendo a login...');
        window.location.href = 'login-estudiante.html';
    } else {
        const user = JSON.parse(userData);
        console.log('✅ Usuario autenticado:', user);
        console.log('🎭 Rol del usuario:', user.rol);
        
        // ✅ VERIFICACIÓN DE ROLES PARA ADMIN.HTML - CORREGIDA
        const adminPages = ['admin.html', 'registrar-admin.html'];
        if (adminPages.includes(currentPage)) {
            console.log('🔐 Verificando acceso a página de administración...');
            console.log('🎭 Rol recibido:', user.rol);
            
            // Usar la función helper para verificar si es admin
            if (!authHelper.esAdmin(user.rol)) {
                console.log('❌ Usuario NO es admin, redirigiendo a index...');
                console.log('   Rol actual:', user.rol);
                window.location.href = 'index.html';
            } else {
                console.log('✅ Usuario es admin, acceso permitido');
            }
        }
    }
}

// 6. UTILIDADES
window.utils = {
  formatearFecha(fecha) {
    const d = new Date(fecha);
    return d.toLocaleDateString('es-CO', {
      year: 'numeric',
      month: 'long',
      day: 'numeric'
    });
  },
  
  formatearHora(fecha) {
    const d = new Date(fecha);
    return d.toLocaleTimeString('es-CO', {
      hour: '2-digit',
      minute: '2-digit'
    });
  },
  
  validarEmail(email) {
    const re = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return re.test(email);
  }
};

console.log('✅ Configuración cargada correctamente');
console.log('🔗 API Base URL:', window.API_BASE_URL);