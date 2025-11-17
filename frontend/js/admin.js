// Importar módulos
import { verificarAutenticacion, logout } from './modules/auth.js';
import { mostrarReservas } from './modules/reservas.js';
import { mostrarProductos } from './modules/prestamos.js';
import { mostrarAdminCursos } from './modules/AdministrarCursos.js';
import { 
    mostrarReportes, 
    reporteUsuarios, 
    reporteLaboratorios, 
    reporteElementos, 
    reporteReservas, 
    reportePrestamos,
    volverAReportes 
} from './modules/reportes.js';
import { setupModalClicks, cerrarModal } from './modules/ui.js';

// Hacer funciones disponibles globalmente para los onclick
window.cerrarModal = cerrarModal;
window.reporteUsuarios = reporteUsuarios;
window.reporteLaboratorios = reporteLaboratorios;
window.reporteElementos = reporteElementos;
window.reporteReservas = reporteReservas;
window.reportePrestamos = reportePrestamos;
window.volverAReportes = volverAReportes;
window.mostrarReservas = mostrarReservas; // 
window.mostrarAdminCursos = mostrarAdminCursos;

// Verificar autenticación al cargar la página
window.addEventListener('DOMContentLoaded', () => {
    const user = verificarAutenticacion();
    if (!user) return;

    // Configurar eventos
    setupEventListeners();
    setupModalClicks();
});

// Configurar event listeners
function setupEventListeners() {
    const volver = document.getElementById("volver");
    const btnReservas = document.getElementById("btnReservas");
    const btnPrestamos = document.getElementById("btnPrestamos");
    const btnReportes = document.getElementById("btnReportes");
    const btnAdminCursos = document.getElementById("btnAdminCursos");

    volver.addEventListener("click", () => {
        window.location.href = "index.html";
    });

    // Conectar botones
    btnReservas.addEventListener("click", mostrarReservas);
    btnPrestamos.addEventListener("click", mostrarProductos);
    btnReportes.addEventListener("click", mostrarReportes);
    btnAdminCursos.addEventListener("click", mostrarAdminCursos);

    console.log("Panel de administración cargado correctamente.");
}

// Hacer logout disponible globalmente
window.logout = logout;