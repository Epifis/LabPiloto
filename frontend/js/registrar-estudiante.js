const API_BASE = 'https://labpiloto.com/api';

class RegistroEstudiante {
    constructor() {
        this.correoRegistrado = '';
        this.idUsuario = null;
        this.init();
    }

    init() {
        this.setupEventListeners();
        this.setupProgramaListener();
    }

    setupEventListeners() {
        // Paso 1: Siguiente
        document.getElementById('btnSiguiente').addEventListener('click', () => this.procesarPaso1());
        
        // Paso 2: Verificar código
        document.getElementById('btnVerificar').addEventListener('click', () => this.verificarCodigo());
        
        // Paso 2: Reenviar código
        document.getElementById('btnReenviar').addEventListener('click', () => this.reenviarCodigo());
        
        // Paso 2: Volver
        document.getElementById('btnVolver').addEventListener('click', () => this.volverPaso1());
        
        // Enter en código MFA
        document.getElementById('codigoMfa').addEventListener('keypress', (e) => {
            if (e.key === 'Enter') {
                this.verificarCodigo();
            }
        });
    }

    setupProgramaListener() {
        const programaSelect = document.getElementById('programa');
        const otroProgramaGroup = document.getElementById('otroProgramaGroup');
        const otroProgramaInput = document.getElementById('otroPrograma');

        if (programaSelect) {
            programaSelect.addEventListener('change', function() {
                if (this.value === 'otro') {
                    otroProgramaGroup.style.display = 'block';
                    otroProgramaInput.required = true;
                } else {
                    otroProgramaGroup.style.display = 'none';
                    otroProgramaInput.required = false;
                }
            });
        }
    }

    async procesarPaso1() {
        // Validar formulario
        if (!this.validarFormulario()) {
            return;
        }

        this.mostrarLoading(true);
        this.ocultarMensajes();

        try {
            const datos = this.obtenerDatosRegistro();
            
            console.log('📤 Enviando registro de estudiante:', datos);
            
            // ✅ CORREGIDO: Usar la ruta correcta del backend según tu controlador
            const response = await fetch(`${API_BASE}/usuarios/registrar`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Accept': 'application/json'
                },
                body: JSON.stringify(datos)
            });

            const data = await response.json();
            console.log('📥 Respuesta:', data);

            if (!response.ok) {
                throw new Error(data.error || data.message || 'Error al registrar estudiante');
            }

            if (data.mensaje && data.idUsuario) {
                this.correoRegistrado = datos.correo;
                this.idUsuario = data.idUsuario;
                
                this.mostrarPaso2();
                this.mostrarMensaje('success', '✅ ' + (data.mensaje || 'Se ha enviado un código de verificación a tu correo electrónico.'));
            } else {
                throw new Error(data.error || 'Error en el registro');
            }
            
        } catch (error) {
            console.error('❌ Error en registro:', error);
            this.mostrarMensaje('error', '❌ ' + error.message);
        } finally {
            this.mostrarLoading(false);
        }
    }

    async verificarCodigo() {
        const codigo = document.getElementById('codigoMfa').value.trim();
        
        if (!codigo) {
            this.mostrarMensaje('error', 'Por favor ingresa el código de verificación');
            return;
        }

        if (codigo.length !== 6) {
            this.mostrarMensaje('error', 'El código debe tener 6 dígitos');
            return;
        }

        this.mostrarLoading(true);
        this.ocultarMensajes();

        try {
            console.log('🔍 Verificando código para:', this.correoRegistrado);
            
            const response = await fetch(`${API_BASE}/usuarios/verificar-codigo`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Accept': 'application/json'
                },
                body: JSON.stringify({
                    correo: this.correoRegistrado,
                    codigo: codigo
                })
            });

            const data = await response.json();
            console.log('✅ Código verificado:', data);

            if (!response.ok) {
                throw new Error(data.error || data.message || 'Error en la verificación');
            }

            if (data.correoVerificado) {
                this.mostrarMensaje('success', '✅ Correo verificado exitosamente. Se ha enviado el contrato para firma a tu correo.');
                
                // Redirigir después de 3 segundos
                setTimeout(() => {
                    window.location.href = 'login-estudiante.html?registro=exitoso';
                }, 3000);
            } else {
                throw new Error(data.error || 'Error en la verificación');
            }

        } catch (error) {
            console.error('❌ Error verificando código:', error);
            this.mostrarMensaje('error', '❌ ' + error.message);
        } finally {
            this.mostrarLoading(false);
        }
    }

    async reenviarCodigo() {
        if (!this.correoRegistrado) {
            this.mostrarMensaje('error', 'No hay un correo registrado para reenviar el código');
            return;
        }

        this.mostrarLoading(true);
        this.ocultarMensajes();

        try {
            console.log('🔄 Reenviando código a:', this.correoRegistrado);
            
            const response = await fetch(`${API_BASE}/usuarios/reenviar-codigo`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Accept': 'application/json'
                },
                body: JSON.stringify({
                    correo: this.correoRegistrado
                })
            });

            const data = await response.json();
            console.log('✅ Código reenviado:', data);

            if (!response.ok) {
                throw new Error(data.error || data.message || 'Error al reenviar código');
            }

            this.mostrarMensaje('success', '✅ ' + (data.mensaje || 'Se ha reenviado el código de verificación a tu correo.'));

        } catch (error) {
            console.error('❌ Error reenviando código:', error);
            this.mostrarMensaje('error', '❌ ' + error.message);
        } finally {
            this.mostrarLoading(false);
        }
    }

    validarFormulario() {
        const campos = [
            'nombre', 'apellido', 'correo', 'documento', 'programa', 'password', 'confirmPassword'
        ];

        // Validar campos requeridos
        for (const campo of campos) {
            const elemento = document.getElementById(campo);
            if (!elemento || !elemento.value.trim()) {
                this.mostrarMensaje('error', `El campo ${campo} es requerido`);
                if (elemento) elemento.focus();
                return false;
            }
        }

        // Validar programa "otro"
        const programa = document.getElementById('programa').value;
        if (programa === 'otro') {
            const otroPrograma = document.getElementById('otroPrograma').value.trim();
            if (!otroPrograma) {
                this.mostrarMensaje('error', 'Por favor especifica tu programa académico');
                document.getElementById('otroPrograma').focus();
                return false;
            }
        }

        // Validar email
        const correo = document.getElementById('correo').value;
        if (!this.validarEmail(correo)) {
            this.mostrarMensaje('error', 'Por favor ingresa un correo electrónico válido');
            document.getElementById('correo').focus();
            return false;
        }

        // Validar contraseñas
        const password = document.getElementById('password').value;
        const confirmPassword = document.getElementById('confirmPassword').value;

        if (password.length < 6) {
            this.mostrarMensaje('error', 'La contraseña debe tener al menos 6 caracteres');
            document.getElementById('password').focus();
            return false;
        }

        if (password !== confirmPassword) {
            this.mostrarMensaje('error', 'Las contraseñas no coinciden');
            document.getElementById('confirmPassword').focus();
            return false;
        }

        // Validar términos
        if (!document.getElementById('aceptaTerminos').checked) {
            this.mostrarMensaje('error', 'Debes aceptar los términos y condiciones');
            return false;
        }

        return true;
    }

    obtenerDatosRegistro() {
        const programa = document.getElementById('programa').value;
        const programaFinal = programa === 'otro' 
            ? document.getElementById('otroPrograma').value 
            : programa;

        return {
            nombre: document.getElementById('nombre').value.trim(),
            apellido: document.getElementById('apellido').value.trim(),
            correo: document.getElementById('correo').value.trim(),
            documento: document.getElementById('documento').value.trim(),
            programa: programaFinal,
            password: document.getElementById('password').value,
            aceptaTerminos: true
        };
    }

    mostrarPaso2() {
        document.getElementById('paso1').style.display = 'none';
        document.getElementById('paso2').style.display = 'block';
        document.getElementById('codigoMfa').focus();
    }

    volverPaso1() {
        document.getElementById('paso2').style.display = 'none';
        document.getElementById('paso1').style.display = 'block';
        document.getElementById('codigoMfa').value = '';
    }

    validarEmail(email) {
        const re = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        return re.test(email);
    }

    mostrarLoading(mostrar) {
        const loading = document.getElementById('loading');
        if (mostrar) {
            loading.classList.add('active');
        } else {
            loading.classList.remove('active');
        }
    }

    mostrarMensaje(tipo, mensaje) {
        this.ocultarMensajes();
        
        const elemento = tipo === 'error' 
            ? document.getElementById('errorMessage')
            : document.getElementById('successMessage');
            
        if (elemento) {
            elemento.textContent = mensaje;
            elemento.classList.add('active');
            
            // Auto-ocultar mensajes de éxito después de 5 segundos
            if (tipo === 'success') {
                setTimeout(() => {
                    elemento.classList.remove('active');
                }, 5000);
            }
        }
    }

    ocultarMensajes() {
        const errorMsg = document.getElementById('errorMessage');
        const successMsg = document.getElementById('successMessage');
        
        if (errorMsg) errorMsg.classList.remove('active');
        if (successMsg) successMsg.classList.remove('active');
    }
}

// Inicializar cuando el DOM esté listo
document.addEventListener('DOMContentLoaded', () => {
    new RegistroEstudiante();
});