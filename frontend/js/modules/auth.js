// Verificar autenticación al cargar la página
export function verificarAutenticacion() {
    const adminUser = localStorage.getItem('adminUser');
    
    if (!adminUser) {
        alert('⚠️ Debes iniciar sesión primero');
        window.location.href = 'login-admin.html';
        return null;
    }
    
    const user = JSON.parse(adminUser);
    console.log('Usuario autenticado:', user);
    return user;
}

// Función para cerrar sesión
export function logout() {
    localStorage.removeItem('adminUser');
    window.location.href = 'login-admin.html';
}
