import { cerrarModal, setupFiltroTabla } from './ui.js';

export async function reporteInventario() {
    try {
        cerrarModal();
        await new Promise(res => setTimeout(res, 50));

        // ✅ CORREGIDO
        const elementos = await api.get(API_ENDPOINTS.elementos.listar);

        const html = `
        <div class="modal">
            <div class="modal-content">
                <span class="close" onclick="cerrarModal()">&times;</span>
                <h2>📦 Inventario de Elementos</h2>

                <input type="text" id="filtroElem" placeholder="Filtrar por nombre o categoría..." class="input-filtro">

                <div class="tabla-container">
                    <table id="tablaElementos">
                        <thead>
                            <tr>
                                <th>Nombre</th>
                                <th>Descripción</th>
                                <th>Total</th>
                                <th>Disponible</th>
                                <th>Estado</th>
                                <th>Categoría</th>
                                <th>Modificar</th>
                            </tr>
                        </thead>
                        <tbody>
                            ${elementos.map(e => `
                                <tr id="fila-${e.id}">
                                    <td>${e.nombre}</td>
                                    <td>${e.descripcion || ''}</td>
                                    <td>${e.cantidadTotal}</td>
                                    <td id="disp-${e.id}">${e.cantidadDisponible}</td>
                                    <td>${e.estado}</td>
                                    <td>${e.categoria || ''}</td>
                                    <td>
                                        <input type="number" id="mod-${e.id}" value="0" style="width:70px;">
                                        <button onclick="aplicarModificacion(${e.id})">Actualizar</button>
                                    </td>
                                </tr>
                            `).join('')}
                        </tbody>
                    </table>
                </div>

                <h3>Agregar nuevo elemento</h3>
                <input type="text" id="nuevoNombre" placeholder="Nombre">
                <input type="text" id="nuevaDescripcion" placeholder="Descripción">
                <input type="number" id="nuevaCantidad" placeholder="Cantidad inicial" min="0">
                <input type="text" id="nuevaCategoria" placeholder="Categoría">
                <button onclick="agregarElemento()">Agregar</button>
            </div>
        </div>
        `;

        document.body.insertAdjacentHTML('beforeend', html);
        setupFiltroTabla('filtroElem', 'tablaElementos');

    } catch (error) {
        console.error(error);
        alert('Error al cargar inventario');
    }
}

// ✅ CORREGIDO
window.aplicarModificacion = async function(id) {
    const input = document.getElementById(`mod-${id}`);
    if (!input) return alert('Elemento no encontrado en la interfaz.');

    let delta = parseInt(input.value, 10);
    if (isNaN(delta) || delta === 0) return alert('Ingresa una cantidad válida (no 0).');

    try {
        input.disabled = true;

        const elemento = await api.get(API_ENDPOINTS.elementos.obtener(id));
        if (!elemento) throw new Error('Elemento no encontrado en servidor.');

        const totalActual = Number(elemento.cantidadTotal ?? 0);
        const dispActual = Number(elemento.cantidadDisponible ?? 0);

        const nuevoTotal = totalActual + delta;
        const nuevoDisp = dispActual + delta;

        if (nuevoTotal < 0 || nuevoDisp < 0) {
            input.disabled = false;
            return alert('La operación deja cantidades negativas. Ajusta el valor.');
        }

        const payload = {
            ...elemento,
            cantidadTotal: nuevoTotal,
            cantidadDisponible: nuevoDisp
        };

        const actualizado = await api.put(API_ENDPOINTS.elementos.actualizar(id), payload);

        const fila = document.getElementById(`fila-${id}`);
        if (fila) {
            const tdTotal = fila.children[2];
            const tdDisp = fila.children[3];
            if (tdTotal) tdTotal.textContent = actualizado.cantidadTotal ?? nuevoTotal;
            if (tdDisp) tdDisp.textContent = actualizado.cantidadDisponible ?? nuevoDisp;
        }

        input.value = 0;
        input.disabled = false;

    } catch (err) {
        console.error('Error al aplicar modificación:', err);
        if (err instanceof Error) {
            alert(`Error al modificar: ${err.message}`);
        } else {
            alert('Error al modificar el elemento (revisa consola).');
        }
        if (input) input.disabled = false;
    }
};

// ✅ CORREGIDO
window.agregarElemento = async function() {
    const nombre = document.getElementById('nuevoNombre').value;
    const descripcion = document.getElementById('nuevaDescripcion').value;
    const cantidad = parseInt(document.getElementById('nuevaCantidad').value) || 0;
    const categoria = document.getElementById('nuevaCategoria').value;

    if (!nombre) return alert('El nombre es obligatorio');

    try {
        await api.post(API_ENDPOINTS.elementos.crear, {
            nombre,
            descripcion,
            cantidadTotal: cantidad,
            cantidadDisponible: cantidad,
            categoria,
            estado: 'Disponible'
        });

        cerrarModal();
        reporteInventario();

    } catch (error) {
        console.error(error);
        alert('Error al agregar elemento');
    }
}