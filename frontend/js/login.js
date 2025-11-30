// login.js - Sistema de Login Unificado CORREGIDO v2

const form = document.getElementById('formLogin');
const btnSubmit = document.getElementById('btnSubmit');
const loading = document.getElementById('loading');
const errorMessage = document.getElementById('errorMessage');
const successMessage = document.getElementById('successMessage');

// Estado para manejo de MFA
let mfaEnProgreso = false;
let correoMfa = null;
let idUsuarioMfa = null;
let tipoUsuario = null;

form.addEventListener('submit', async (e) => {
  e.preventDefault();

  errorMessage.classList.remove('active');
  successMessage.classList.remove('active');

  const correo = document.getElementById('correo').value.trim();
  const password = document.getElementById('password').value;

  // Validaciones básicas
  if (!correo || !password) {
    mostrarError('Por favor completa todos los campos');
    return;
  }

  if (!correo.includes('@') || !correo.includes('.')) {
    mostrarError('Por favor ingresa un correo electrónico válido');
    return;
  }

  btnSubmit.disabled = true;
  loading.classList.add('active');

  try {
    console.log('═══════════════════════════════════════');
    console.log('🔐 INICIANDO LOGIN FRONTEND');
    console.log('═══════════════════════════════════════');
    console.log('📧 Correo:', correo);
    
    // PASO 1: Intentar login con endpoint principal
    console.log('📡 Llamando a:', API_ENDPOINTS.auth.login);
    
    const loginData = await apiService.post(API_ENDPOINTS.auth.login, {
      correo,
      password
    });
    
    console.log('✅ Respuesta recibida:', loginData);
    console.log('🎭 Rol devuelto:', loginData.rol);
    console.log('🎫 JWT presente:', !!(loginData.jwt || loginData.token));
    
    // PASO 2: Verificar si el login fue exitoso con JWT
    if (loginData.jwt || loginData.token) {
      // Login exitoso - tiene JWT directo (superAdmin, admin sin MFA, o usuarios sin MFA)
      console.log('✅ Login exitoso con JWT directo');
      guardarSesion(loginData);
      mostrarExito('¡Login exitoso! Redirigiendo...');
      
      setTimeout(() => {
        redirigirDashboard(loginData.rol);
      }, 1500);
      return;
    }
    
    // PASO 3: Si no tiene JWT, debe requerir MFA
    if (loginData.message || loginData.idUsuario) {
      console.log('🔐 Login requiere MFA');
      idUsuarioMfa = loginData.idUsuario;
      manejarMfa(correo, loginData.message || 'Código MFA enviado a tu correo');
      return;
    }
    
    // Si llegamos aquí, la respuesta no es la esperada
    console.error('❌ Respuesta inesperada del servidor:', loginData);
    throw new Error('Respuesta inesperada del servidor');

  } catch (err) {
    console.error('❌ Error en login:', err);
    
    // Extraer mensaje de error
    let errorMsg = 'Error al iniciar sesión';
    
    if (err.response && err.response.error) {
      errorMsg = err.response.error;
    } else if (err.message) {
      errorMsg = err.message;
    }
    
    mostrarError(errorMsg);
  } finally {
    btnSubmit.disabled = false;
    loading.classList.remove('active');
  }
});

// =======================
// Manejo de MFA
// =======================
function manejarMfa(correo, mensaje) {
  mfaEnProgreso = true;
  correoMfa = correo;

  console.log('🔐 Mostrando formulario MFA');
  console.log('📧 Correo:', correo);
  console.log('🆔 ID Usuario:', idUsuarioMfa);

  // Mostrar formulario de MFA
  const container = document.querySelector('.container');
  container.innerHTML = `
    <h2>🔐 Verificación de Seguridad</h2>
    <p class="subtitle">Se ha enviado un código a tu correo</p>
    
    <div class="info-box">
      ℹ️ ${mensaje}
    </div>
    
    <form id="formMfa">
      <div class="form-group">
        <label for="codigoMfa">Código de verificación *</label>
        <input type="text" id="codigoMfa" placeholder="123456" maxlength="6" required autofocus>
        <small>El código expira en 5 minutos</small>
      </div>
      
      <button type="submit" id="btnVerificarMfa">Verificar Código</button>
      
      <div class="form-footer">
        <button type="button" id="btnReenviarMfa" class="btn-link">
          ¿No recibiste el código? Reenviar
        </button>
        <button type="button" id="btnCancelarMfa" class="btn-link">
          Volver al login
        </button>
      </div>
      
      <div class="loading" id="loadingMfa">⏳ Verificando código...</div>
      <div class="error-message" id="errorMfa"></div>
      <div class="success-message" id="successMfa"></div>
    </form>
  `;

  configurarFormularioMfa();
}

function configurarFormularioMfa() {
  const formMfa = document.getElementById('formMfa');
  const btnVerificar = document.getElementById('btnVerificarMfa');
  const loadingMfa = document.getElementById('loadingMfa');
  const errorMfa = document.getElementById('errorMfa');
  const successMfa = document.getElementById('successMfa');

  // Verificar código
  formMfa.addEventListener('submit', async (e) => {
    e.preventDefault();
    
    const codigo = document.getElementById('codigoMfa').value.trim();
    
    if (codigo.length !== 6) {
      mostrarErrorMfa('El código debe tener 6 dígitos');
      return;
    }

    btnVerificar.disabled = true;
    loadingMfa.classList.add('active');

    try {
      console.log('🔍 Verificando código MFA...');
      console.log('🆔 ID Usuario:', idUsuarioMfa);
      console.log('🔢 Código:', codigo);
      
      // Usar endpoint genérico de MFA
      const data = await apiService.post(API_ENDPOINTS.mfa.verifyLogin, {
        idUsuario: idUsuarioMfa,
        codigo: codigo
      });

      console.log('✅ Verificación MFA exitosa:', data);

      guardarSesion(data);
      mostrarExitoMfa('✅ Verificación exitosa! Redirigiendo...');
      
      setTimeout(() => {
        redirigirDashboard(data.rol);
      }, 1500);

    } catch (err) {
      console.error('❌ Error al verificar MFA:', err);
      
      let errorMsg = 'Código inválido o expirado';
      if (err.response && err.response.error) {
        errorMsg = err.response.error;
      } else if (err.message) {
        errorMsg = err.message;
      }
      
      mostrarErrorMfa(errorMsg);
    } finally {
      btnVerificar.disabled = false;
      loadingMfa.classList.remove('active');
    }
  });

  // Reenviar código
  document.getElementById('btnReenviarMfa').addEventListener('click', async () => {
    try {
      loadingMfa.classList.add('active');
      console.log('📨 Reenviando código MFA...');
      
      await apiService.post(API_ENDPOINTS.mfa.reenviar, {
        idUsuario: idUsuarioMfa
      });
      
      mostrarExitoMfa('✅ Código reenviado a tu correo');
    } catch (err) {
      console.error('❌ Error al reenviar código:', err);
      
      let errorMsg = 'Error al reenviar código';
      if (err.response && err.response.error) {
        errorMsg = err.response.error;
      } else if (err.message) {
        errorMsg = err.message;
      }
      
      mostrarErrorMfa(errorMsg);
    } finally {
      loadingMfa.classList.remove('active');
    }
  });

  // Cancelar y volver al login
  document.getElementById('btnCancelarMfa').addEventListener('click', () => {
    window.location.reload();
  });

  function mostrarErrorMfa(msg) {
    errorMfa.textContent = msg;
    errorMfa.classList.add('active');
    successMfa.classList.remove('active');
  }

  function mostrarExitoMfa(msg) {
    successMfa.textContent = msg;
    successMfa.classList.add('active');
    errorMfa.classList.remove('active');
  }
}

// =======================
// Funciones auxiliares
// =======================
function guardarSesion(data) {
  console.log('═══════════════════════════════════════');
  console.log('💾 GUARDANDO SESIÓN');
  console.log('═══════════════════════════════════════');
  
  const token = data.jwt || data.token;
  const rol = data.rol;
  
  console.log('🎫 Token JWT:', token ? token.substring(0, 30) + '...' : 'NO ENCONTRADO');
  console.log('🎭 Rol:', rol);
  console.log('👤 Usuario:', data.nombre, data.apellido);
  console.log('📧 Correo:', data.correo);
  
  if (!token) {
    console.error('❌ ERROR: No se recibió token JWT');
    throw new Error('No se recibió token de autenticación');
  }
  
  if (!rol) {
    console.error('❌ ERROR: No se recibió rol de usuario');
    throw new Error('No se recibió información de rol');
  }
  
  localStorage.setItem('authToken', token);
  
  const userData = {
    id: data.id || data.id_usuario,
    nombre: data.nombre,
    apellido: data.apellido,
    correo: data.correo,
    rol: rol,
    documento: data.documento,
    programa: data.programa || data.departamento,
    especialidad: data.especialidad
  };
  
  localStorage.setItem('userData', JSON.stringify(userData));
  
  console.log('✅ Sesión guardada en localStorage');
  console.log('📦 userData:', userData);
  console.log('═══════════════════════════════════════');
}

function redirigirDashboard(rol) {
  console.log('═══════════════════════════════════════');
  console.log('🚀 REDIRIGIENDO DASHBOARD');
  console.log('═══════════════════════════════════════');
  console.log('🎭 Rol recibido:', rol);
  console.log('📊 Tipo de dato:', typeof rol);
  
  if (!rol) {
    console.error('❌ ERROR: Rol no definido');
    console.log('🔄 Redirigiendo a login por seguridad');
    window.location.href = 'login-estudiante.html';
    return;
  }
  
  // Normalizar el rol
  const rolOriginal = rol;
  const rolNormalizado = String(rol).toLowerCase().trim();
  
  console.log('🔄 Rol normalizado:', rolNormalizado);
  
  // Mapeo de roles a dashboards
  const dashboards = {
    // Estudiantes
    'estudiante': 'dashboard-estudiante.html',
    
    // Profesores
    'profesor': 'dashboard-profesor.html',
    
    // Administradores (todas las variantes)
    'administrador': 'admin.html',
    'admin': 'admin.html',
    'superadmin': 'admin.html',
    'super_admin': 'admin.html',
    'super admin': 'admin.html'
  };
  
  // Buscar destino exacto primero
  let destino = dashboards[rolNormalizado];
  
  // Si no hay coincidencia exacta, buscar por palabra clave
  if (!destino) {
    console.log('⚠️ No se encontró coincidencia exacta, buscando por palabra clave...');
    
    if (rolNormalizado.includes('admin')) {
      console.log('✅ Rol contiene "admin"');
      destino = 'admin.html';
    } else if (rolNormalizado.includes('profesor')) {
      console.log('✅ Rol contiene "profesor"');
      destino = 'dashboard-profesor.html';
    } else if (rolNormalizado.includes('estudiante')) {
      console.log('✅ Rol contiene "estudiante"');
      destino = 'dashboard-estudiante.html';
    }
  }
  
  // Fallback por defecto
  if (!destino) {
    console.warn('⚠️ Rol no reconocido, usando dashboard estudiante por defecto');
    destino = 'dashboard-estudiante.html';
  }
  
  console.log('✅ Destino determinado:', destino);
  console.log('🔄 Redirigiendo en 1 segundo...');
  console.log('═══════════════════════════════════════');
  
  window.location.href = destino;
}

function mostrarError(mensaje) {
  errorMessage.textContent = mensaje;
  errorMessage.classList.add('active');
  successMessage.classList.remove('active');
}

function mostrarExito(mensaje) {
  successMessage.textContent = mensaje;
  successMessage.classList.add('active');
  errorMessage.classList.remove('active');
}

// Verificar si ya hay sesión activa al cargar la página
document.addEventListener('DOMContentLoaded', () => {
  console.log('═══════════════════════════════════════');
  console.log('🚀 PÁGINA DE LOGIN CARGADA');
  console.log('═══════════════════════════════════════');
  
  const usuario = authHelper.getUsuarioActual();
  const token = localStorage.getItem('authToken');
  
  console.log('🔍 Verificando sesión existente...');
  console.log('🎫 Token presente:', !!token);
  console.log('👤 Usuario presente:', !!usuario);
  
  if (usuario && usuario.rol && token) {
    console.log('✅ Sesión activa detectada');
    console.log('👤 Usuario:', usuario.nombre, usuario.apellido);
    console.log('🎭 Rol:', usuario.rol);
    console.log('🔄 Redirigiendo a dashboard correspondiente...');
    redirigirDashboard(usuario.rol);
  } else {
    console.log('ℹ️ No hay sesión activa, mostrando formulario de login');
  }
  
});
