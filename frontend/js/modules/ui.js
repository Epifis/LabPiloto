// Funciones auxiliares de UI
export function cerrarModal() {
    const modal = document.querySelector('.modal');
    if (modal) modal.remove();
}

export function formatearFecha(fecha) {
    const d = new Date(fecha);
    return d.toLocaleString('es-CO', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit'
    });
}

export function formatFecha(f) {
    if (!f) return "";
    return new Date(f).toLocaleString();
}

// Cerrar modal al hacer clic fuera
export function setupModalClicks() {
    window.onclick = function(event) {
        const modal = document.querySelector('.modal');
        if (event.target === modal) {
            cerrarModal();
        }
    }
}

// Función para aplicar filtros a tablas
export function setupFiltroTabla(inputId, tablaId) {
    const input = document.getElementById(inputId);
    if (input) {
        input.addEventListener("input", (e) => {
            const texto = e.target.value.toLowerCase();
            document.querySelectorAll(`#${tablaId} tbody tr`).forEach(row => {
                row.style.display = row.innerText.toLowerCase().includes(texto) ? "" : "none";
            });
        });
    }
}