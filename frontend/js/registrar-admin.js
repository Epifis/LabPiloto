const API_BASE = 'https://labpilot-backend-r1dm.onrender.com/api';

const form = document.getElementById('formAdmin');
const btnSubmit = document.getElementById('btnSubmit');
const loading = document.getElementById('loading');
const errorMessage = document.getElementById('errorMessage');

form.addEventListener('submit', async (e) => {
    e.preventDefault();

    // Limpiar errores previos
    errorMessage.classList.remove('active');
    errorMessage.textContent = '';

    // Obtener valores
    const nombre = document.getElementById('nombre').value.trim();
    const apellido = document.getElementById('apellido').value.trim();
    const correo = document.getElementById('correo').value.trim();
    const password = document.getElementById('password').value;
    const confirm = document.getElementById('confirmPassword').value;

    // Validación de contraseñas
    if (password !== confirm) {
        mostrarError('Las contraseñas no coinciden');
        return;
    }

    if (password.length < 6) {
        mostrarError('La contraseña debe tener al menos 6 caracteres');
        return;
    }

    // Deshabilitar botón y mostrar loading
    btnSubmit.disabled = true;
    loading.classList.add('active');

    try {
        const response = await fetch(`${API_BASE}/solicitar`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Accept': 'application/json'
            },
            body: JSON.stringify({ nombre, apellido, correo, password })
        });

        const text = await response.text();

        if (!response.ok) {
            throw new Error(text || 'Error al enviar la solicitud');
        }

        // Éxito
        alert('✅ ' + text + '\n\nRecibirás un correo cuando tu solicitud sea procesada.');
        form.reset();

    } catch (err) {
        console.error('Error:', err);
        mostrarError(err.message);
    } finally {
        btnSubmit.disabled = false;
        loading.classList.remove('active');
    }
});

function mostrarError(mensaje) {
    errorMessage.textContent = '❌ ' + mensaje;
    errorMessage.classList.add('active');

    // Auto-ocultar después de 5 segundos
    setTimeout(() => {
        errorMessage.classList.remove('active');
    }, 5000);
}
