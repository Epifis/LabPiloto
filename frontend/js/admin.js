if (typeof API_BASE_URL === 'undefined') {
    console.error('❌ API_BASE_URL no está definida. Cargando config.js...');
    
    // Cargar config.js dinámicamente si no está disponible
    await new Promise((resolve) => {
        const script = document.createElement('script');
        script.src = 'js/config.js';
        script.onload = resolve;
        document.head.appendChild(script);
    });
}
import { verificarAutenticacion, logout } from './modules/auth.js';
import { mostrarReservas } from './modules/reservas.js';
import { mostrarProductos } from './modules/prestamos.js';
import { mostrarAdminCursos } from './modules/AdministrarCursos.js';
import { 
    mostrarReportes, 
    reporteLaboratorios, 
    reporteElementos, 
    reporteReservas, 
    reportePrestamos,
    volverAReportes 
} from './modules/reportes.js';
import { setupModalClicks, cerrarModal } from './modules/ui.js';
import { reporteInventario } from './modules/inventario.js';
import { reporteLaboratorio } from './modules/laboratorios.js';    
import { reporteUsuarios } from './modules/gestor.js'; 

window.cerrarModal = cerrarModal;
window.reporteUsuarios = reporteUsuarios; 
window.reporteLaboratorios = reporteLaboratorios;
window.reporteElementos = reporteElementos;
window.reporteReservas = reporteReservas;
window.reportePrestamos = reportePrestamos;
window.volverAReportes = volverAReportes;
window.mostrarReservas = mostrarReservas;
window.mostrarAdminCursos = mostrarAdminCursos;
window.reporteInventario = reporteInventario;
window.reporteLaboratorio = reporteLaboratorio;

window.addEventListener('DOMContentLoaded', async () => {
    const user = await verificarAutenticacion();
    if (!user) return;

    controlarAccesoSuperAdmin(user);

    setupEventListeners();
    setupModalClicks();
});
function controlarAccesoSuperAdmin(user) {
    const linkRegistrarAdmin = document.getElementById("link-registrar-admin");

    // Si NO es superAdmin → eliminar el link completamente
    if (!user || user.rol !== "superAdmin") {
        if (linkRegistrarAdmin) {
            linkRegistrarAdmin.remove();
        }
    }
}

function setupEventListeners() {
    const volver = document.getElementById("volver");
    const btnReservas = document.getElementById("btnReservas");
    const btnPrestamos = document.getElementById("btnPrestamos");
    const btnReportes = document.getElementById("btnReportes");
    const btnAdminCursos = document.getElementById("btnAdminCursos");
    const btnInventario = document.getElementById("btnInventario");
    const btnLaboratorios = document.getElementById("btnLaboratorios");
    const btnGestor = document.getElementById("btnGestor");

   const logoutBtn = document.getElementById("logoutBtn");

   if (logoutBtn) {
     logoutBtn.addEventListener("click", () => {
        // Limpiar todo
        localStorage.removeItem('authToken');
        localStorage.removeItem('userData');
        localStorage.removeItem('adminUser');

        // Redirigir
        window.location.href = 'login-estudiante.html';
        });
    }   

    if (btnReservas) btnReservas.addEventListener("click", mostrarReservas);
    if (btnPrestamos) btnPrestamos.addEventListener("click", mostrarProductos);
    if (btnReportes) btnReportes.addEventListener("click", mostrarReportes);
    if (btnAdminCursos) btnAdminCursos.addEventListener("click", mostrarAdminCursos);
    if (btnInventario) btnInventario.addEventListener("click", reporteInventario);
    if (btnLaboratorios) btnLaboratorios.addEventListener("click", reporteLaboratorio);
    if (btnGestor) btnGestor.addEventListener("click", reporteUsuarios);
   
    console.log("Panel de administración cargado correctamente.");
}
// Hacer logout disponible globalmente
window.logout = logout;
