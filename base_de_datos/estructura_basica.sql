
-- ==========================================================
-- ELIMINAR TABLAS EXISTENTES (SOLO PARA DESARROLLO)
-- ==========================================================
DROP TABLE IF EXISTS public.laboratorio_horario CASCADE;
DROP TABLE IF EXISTS public.reserva_invitado CASCADE;
DROP TABLE IF EXISTS public.prestamo CASCADE;
DROP TABLE IF EXISTS public.reserva CASCADE;
DROP TABLE IF EXISTS public.notificacion CASCADE;
DROP TABLE IF EXISTS public.solicitud_admin CASCADE;
DROP TABLE IF EXISTS public.configuracion_sistema CASCADE;
DROP TABLE IF EXISTS public.permisos_rol CASCADE;
DROP TABLE IF EXISTS public.elemento CASCADE;
DROP TABLE IF EXISTS public.laboratorio CASCADE;
DROP TABLE IF EXISTS public.usuario CASCADE;
DROP TABLE IF EXISTS public.curso CASCADE;

-- ==========================================================
-- CREACIÓN DE TABLAS BASE
-- ==========================================================

CREATE TABLE public.usuario (
    id_usuario SERIAL PRIMARY KEY,
    documento VARCHAR(30) UNIQUE,
    nombre VARCHAR(100) NOT NULL,
    apellido VARCHAR(100) NOT NULL,
    correo VARCHAR(150) UNIQUE NOT NULL,
    rol VARCHAR(20) NOT NULL CHECK (rol IN ('superAdmin','administrador','profesor', 'estudiante')),
    password VARCHAR(255),
    programa VARCHAR(150),
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    activo BOOLEAN DEFAULT true
);

CREATE TABLE public.laboratorio (
    id_laboratorio SERIAL PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    ubicacion VARCHAR(150),
    capacidad INTEGER NOT NULL,
    capacidad_disponible INTEGER NOT NULL,
    estado VARCHAR(20) DEFAULT 'Activo',
    descripcion TEXT
);

CREATE TABLE public.elemento (
    id_elemento SERIAL PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    descripcion TEXT,
    estado VARCHAR(20) DEFAULT 'Disponible',
    cantidad_total INTEGER NOT NULL,
    cantidad_disponible INTEGER NOT NULL,
    categoria VARCHAR(50)
);

-- ==========================================================
-- NUEVA TABLA CURSO
-- ==========================================================

CREATE TABLE public.curso (
    nrc VARCHAR(20) PRIMARY KEY,
    nombre VARCHAR(200) NOT NULL,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ==========================================================
-- TABLA HORARIOS PERMITIDOS POR LABORATORIO
-- ==========================================================

CREATE TABLE public.laboratorio_horario (
    id_horario SERIAL PRIMARY KEY,
    id_laboratorio INTEGER NOT NULL REFERENCES public.laboratorio(id_laboratorio) ON DELETE CASCADE,
    dia_semana INTEGER NOT NULL CHECK (dia_semana BETWEEN 1 AND 6),  -- 1=Lunes ... 6=Sábado
    hora_inicio TIME NOT NULL,
    hora_fin TIME NOT NULL,
    CHECK (hora_inicio < hora_fin)
);

-- ==========================================================
-- TABLA RESERVA MEJORADA (ÚNICA PARA TODO)
-- ==========================================================

CREATE TABLE public.reserva (
    id_reserva SERIAL PRIMARY KEY,
    fecha_inicio TIMESTAMP NOT NULL,
    fecha_fin TIMESTAMP NOT NULL,
    estado VARCHAR(20) DEFAULT 'Pendiente'
        CHECK (estado IN ('Pendiente', 'Confirmada', 'Cancelada', 'Completada', 'Rechazada', 'Activa')),

    -- Relaciones principales
    id_usuario INTEGER NOT NULL REFERENCES public.usuario(id_usuario) ON DELETE CASCADE,
    id_laboratorio INTEGER NOT NULL REFERENCES public.laboratorio(id_laboratorio) ON DELETE CASCADE,
    
    -- Nuevos campos para curso y tipo
    nrc_curso VARCHAR(20) REFERENCES public.curso(nrc) ON DELETE SET NULL,
    tipo_reserva VARCHAR(20) NOT NULL CHECK (tipo_reserva IN ('clase', 'practica_libre')),
    
    -- Para identificar reservas recurrentes
    grupo_recurrencia UUID, -- Identificador único para el grupo de reservas recurrentes
    es_recurrente BOOLEAN DEFAULT FALSE,
    
    -- Capacidad
    cantidad_estudiantes INTEGER DEFAULT 1 CHECK (cantidad_estudiantes > 0),
    
    -- Información adicional
    titulo VARCHAR(200),
    descripcion TEXT,
    
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ==========================================================
-- TABLA INVITADOS (MANTENIENDO ESTRUCTURA ORIGINAL)
-- ==========================================================

CREATE TABLE public.reserva_invitado (
    id_invitado SERIAL PRIMARY KEY,
    id_reserva INTEGER NOT NULL REFERENCES public.reserva(id_reserva) ON DELETE CASCADE,
    documento VARCHAR(30),
    nombre VARCHAR(100) NOT NULL,
    apellido VARCHAR(100) NOT NULL
);

-- ==========================================================
-- TABLA PRÉSTAMOS (SIN CAMBIOS - MANTENIENDO ESTRUCTURA ORIGINAL)
-- ==========================================================

CREATE TABLE public.prestamo (
    id_prestamo SERIAL PRIMARY KEY,
    fecha_prestamo TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_devolucion TIMESTAMP,
    estado VARCHAR(20) DEFAULT 'Pendiente'
        CHECK (estado IN ('Pendiente', 'Aprobado', 'Rechazado', 'Prestado', 'Devuelto', 'Atrasado', 'Perdido')),
    id_usuario INTEGER NOT NULL REFERENCES public.usuario(id_usuario) ON DELETE CASCADE,
    id_elemento INTEGER NOT NULL REFERENCES public.elemento(id_elemento) ON DELETE CASCADE,
    observaciones TEXT
);

-- ==========================================================
-- TABLAS ADICIONALES (MANTENIENDO ESTRUCTURA ORIGINAL)
-- ==========================================================

CREATE TABLE public.notificacion (
    id_notificacion SERIAL PRIMARY KEY,
    id_usuario INTEGER REFERENCES public.usuario(id_usuario) ON DELETE CASCADE,
    tipo VARCHAR(50),
    titulo VARCHAR(200),
    mensaje TEXT,
    fecha TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    leida BOOLEAN DEFAULT false,
    prioridad VARCHAR(20) DEFAULT 'Normal',
    url_accion VARCHAR(500)
);

CREATE TABLE public.permisos_rol (
    id_permiso SERIAL PRIMARY KEY,
    rol VARCHAR(50) NOT NULL,
    permiso VARCHAR(100) NOT NULL,
    descripcion TEXT,
    categoria VARCHAR(50),
    activo BOOLEAN DEFAULT true,
    UNIQUE(rol, permiso)
);

CREATE TABLE public.configuracion_sistema (
    id_configuracion SERIAL PRIMARY KEY,
    clave VARCHAR(100) UNIQUE NOT NULL,
    valor TEXT,
    descripcion TEXT,
    tipo VARCHAR(50),
    categoria VARCHAR(50),
    modificable BOOLEAN DEFAULT true,
    fecha_modificacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    usuario_modificacion INTEGER REFERENCES public.usuario(id_usuario) ON DELETE SET NULL
);

CREATE TABLE solicitud_admin (
    id_solicitud SERIAL PRIMARY KEY,
    nombre VARCHAR(50),
    apellido VARCHAR(50),
    correo VARCHAR(100) UNIQUE,
    password VARCHAR(100),
    estado VARCHAR(20) CHECK (estado IN ('Aprobada', 'Rechazada', 'Pendiente')),
    fecha_solicitud TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    token_validacion VARCHAR(255)
);

-- ==========================================================
-- ÍNDICES
-- ==========================================================

CREATE INDEX idx_usuario_correo ON public.usuario(correo);
CREATE INDEX idx_reserva_laboratorio ON public.reserva(id_laboratorio);
CREATE INDEX idx_reserva_fechas ON public.reserva(fecha_inicio, fecha_fin);
CREATE INDEX idx_reserva_tipo ON public.reserva(tipo_reserva);
CREATE INDEX idx_reserva_grupo ON public.reserva(grupo_recurrencia);
CREATE INDEX idx_reserva_curso ON public.reserva(nrc_curso);
CREATE INDEX idx_lab_horario_laboratorio ON public.laboratorio_horario(id_laboratorio);

-- ==========================================================
-- DATOS INICIALES
-- ==========================================================

INSERT INTO public.usuario (documento, nombre, apellido, correo, rol, password, programa)
VALUES 
('0000', 'Super', 'Admin', 'superadmin@upc.edu.co', 'superAdmin',
 '$2a$12$aU7bPcHXZGBQojtoGFGPPuCKsZLo7KmKYM3ssAaGtqO/7gM1b.rBC',
 'Administración de Sistemas'),

('1111', 'Admin', 'Sistema', 'admin@upc.edu.co', 'administrador',
 '$2a$12$aU7bPcHXZGBQojtoGFGPPuCKsZLo7KmKYM3ssAaGtqO/7gM1b.rBC',
 'Ingeniería de Software'),

('2222', 'Juan', 'Pérez', 'juan.perez@upc.edu.co', 'estudiante',
 '$2a$12$aU7bPcHXZGBQojtoGFGPPuCKsZLo7KmKYM3ssAaGtqO/7gM1b.rBC',
 'Ingeniería de Software');

INSERT INTO public.curso (nrc, nombre) VALUES
('12345', 'Programación Web Avanzada'),
('12346', 'Base de Datos II'),
('12347', 'Inteligencia Artificial');

INSERT INTO public.laboratorio (nombre, ubicacion, capacidad, capacidad_disponible, estado, descripcion)
VALUES
('LAB DESARROLLO WEB', 'F201', 24, 24, 'Activo', 'Laboratorio para desarrollo de aplicaciones web'),
('LAB IOT', 'F202', 20, 20, 'Activo', 'Laboratorio para Internet de las Cosas'),
('LAB FÍSICA 1', 'F101', 30, 30, 'Activo', 'Laboratorio para prácticas de física básica');

INSERT INTO public.elemento (nombre, descripcion, cantidad_total, cantidad_disponible, categoria)
VALUES
('Laptop', 'Computadores portátiles para prácticas', 10, 10, 'Equipos'),
('Sensor Temperatura', 'Sensores digitales para medición', 15, 15, 'Sensores'),
('Arduino Uno', 'Placas de desarrollo Arduino', 20, 20, 'Electrónica'),
('Multímetro', 'Instrumentos para mediciones eléctricas', 8, 8, 'Instrumentos');

-- Insertar horarios permitidos (Lun-Sab, 6AM - 9PM)
INSERT INTO laboratorio_horario(id_laboratorio, dia_semana, hora_inicio, hora_fin)
SELECT id_laboratorio, d, '06:00', '21:00'
FROM laboratorio, generate_series(1,6) d;

-- TRIGGERS
DROP TRIGGER IF EXISTS trigger_prevent_last_superadmin_delete ON usuario;
DROP TRIGGER IF EXISTS trigger_prevent_last_superadmin_update ON usuario;
DROP TRIGGER IF EXISTS trigger_prevent_last_superadmin_deactivate ON usuario;
DROP FUNCTION IF EXISTS prevent_last_superadmin_deletion();
DROP FUNCTION IF EXISTS prevent_last_superadmin_role_change();
DROP FUNCTION IF EXISTS prevent_last_superadmin_deactivate();


-- 1. Trigger para prevenir ELIMINAR el último superAdmin
CREATE OR REPLACE FUNCTION prevent_last_superadmin_deletion()
RETURNS TRIGGER AS $$
BEGIN
    -- Si se está intentando eliminar un superAdmin (con 'A' mayúscula)
    IF OLD.rol = 'superAdmin' THEN
        -- Contar cuántos superAdmins quedan (excluyendo el que se va a eliminar)
        IF (SELECT COUNT(*) FROM usuario WHERE rol = 'superAdmin' AND id_usuario != OLD.id_usuario) = 0 THEN
            RAISE EXCEPTION 'No se puede eliminar el último superAdmin del sistema';
        END IF;
    END IF;
    RETURN OLD;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_prevent_last_superadmin_delete
    BEFORE DELETE ON usuario
    FOR EACH ROW
    EXECUTE FUNCTION prevent_last_superadmin_deletion();

-- 2. Trigger para prevenir ACTUALIZAR el rol del último superAdmin
CREATE OR REPLACE FUNCTION prevent_last_superadmin_role_change()
RETURNS TRIGGER AS $$
BEGIN
    -- Si se está cambiando un superAdmin a otro rol
    IF OLD.rol = 'superAdmin' AND NEW.rol != 'superAdmin' THEN
        -- Verificar si es el último superAdmin
        IF (SELECT COUNT(*) FROM usuario WHERE rol = 'superAdmin' AND id_usuario != OLD.id_usuario) = 0 THEN
            RAISE EXCEPTION 'No se puede cambiar el rol del último superAdmin del sistema';
        END IF;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_prevent_last_superadmin_update
    BEFORE UPDATE ON usuario
    FOR EACH ROW
    EXECUTE FUNCTION prevent_last_superadmin_role_change();

-- 3. Trigger para prevenir DESACTIVAR el último superAdmin
CREATE OR REPLACE FUNCTION prevent_last_superadmin_deactivate()
RETURNS TRIGGER AS $$
BEGIN
    -- Si se está desactivando un superAdmin
    IF OLD.rol = 'superAdmin' AND OLD.activo = true AND NEW.activo = false THEN
        -- Verificar si es el último superAdmin activo
        IF (SELECT COUNT(*) FROM usuario WHERE rol = 'superAdmin' AND activo = true AND id_usuario != OLD.id_usuario) = 0 THEN
            RAISE EXCEPTION 'No se puede desactivar el último superAdmin activo del sistema';
        END IF;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_prevent_last_superadmin_deactivate
    BEFORE UPDATE ON usuario
    FOR EACH ROW
    EXECUTE FUNCTION prevent_last_superadmin_deactivate();
