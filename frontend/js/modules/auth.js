// 🚨 Bloquear DOM inmediatamente
document.documentElement.style.display = "none";

// Definir API_BASE_URL si no está definida (para compatibilidad)
if (typeof API_BASE_URL === 'undefined' && typeof window.API_BASE_URL !== 'undefined') {
    const API_BASE_URL = window.API_BASE_URL;
}

// 🚨 Bloquear DOM inmediatamente para páginas admin
const adminPages = ['admin.html', 'registrar-admin.html'];
const currentPage = window.location.pathname.split('/').pop();

if (adminPages.includes(currentPage)) {
    document.documentElement.style.display = "none";
}

// =======================
// 🔒 Verificación con backend
// =======================
export async function verificarAutenticacion() {
    try {
        const token = localStorage.getItem('authToken');
        
        if (!token) {
            redirigirLogin();
            return null;
        }

        // Validar token con el backend
        const response = await fetch(`${API_BASE_URL}/auth/verify`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${token}`
            }
        });

        if (!response.ok) {
            redirigirLogin();
            return null;
        }

        const userData = await response.json();

        // Verificar rol de administrador
        if (!userData.rol || !['superAdmin', 'administrador'].includes(userData.rol)) {
            redirigirLogin();
            return null;
        }

        // Sesión válida: mostrar página
        document.documentElement.style.display = "";
        return userData;

    } catch (error) {
        console.error('Error de autenticación:', error);
        redirigirLogin();
        return null;
    }
}

// =======================
// 🔄 Verificación continua (cada 30 segundos)
// =======================
let verificacionInterval;

export function iniciarVerificacionContinua() {
    verificacionInterval = setInterval(async () => {
        const user = await verificarAutenticacion();
        if (!user) {
            detenerVerificacion();
        }
    }, 30000); // 30 segundos
}

export function detenerVerificacion() {
    if (verificacionInterval) {
        clearInterval(verificacionInterval);
    }
}

// =======================
// 🚪 Logout seguro
// =======================
export async function logout() {
    const token = localStorage.getItem('authToken');
    
    if (token) {
        try {
            // Invalidar token en el backend
            await fetch(`${API_BASE_URL}/auth/logout`, {
                method: 'POST',
                headers: {
                    'Authorization': `Bearer ${token}`
                }
            });
        } catch (error) {
            console.error('Error al cerrar sesión:', error);
        }
    }
    
    localStorage.removeItem('authToken');
    localStorage.removeItem('adminUser');
    redirigirLogin();
}

// =======================
// 🔐 Helpers
// =======================
function redirigirLogin() {
    localStorage.removeItem('authToken');
    localStorage.removeItem('adminUser');
    window.location.replace("login-admin.html");
}

// Prevenir navegación con botón "Atrás"
window.addEventListener('pageshow', (event) => {
    if (event.persisted) {
        window.location.reload();
    }
});

// Detectar si el usuario intenta manipular localStorage
const originalSetItem = localStorage.setItem;
localStorage.setItem = function(key, value) {
    if (key === 'authToken' || key === 'adminUser') {
        // Si alguien intenta modificar manualmente, revalidar
        setTimeout(() => verificarAutenticacion(), 100);
    }
    originalSetItem.apply(this, arguments);
};
