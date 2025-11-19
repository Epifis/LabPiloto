-- ==========================================================
-- LABORATORIO UNIVERSIDAD - DATOS DE PRUEBA (FINAL)
-- - Horarios: Realistas (dentro de 06:00 - 21:00, Lun-Sáb)
-- ==========================================================

-- ===============================
-- Asegurar superadmin base (si no existe)
-- ===============================
DO $$
BEGIN
  IF NOT EXISTS (SELECT 1 FROM public.usuario WHERE rol = 'superAdmin') THEN
    INSERT INTO public.usuario (documento, nombre, apellido, correo, rol, password, programa)
    VALUES ('0000000000', 'Super', 'Admin', 'superadmin@upc.edu.co', 'superAdmin',
            '$2a$12$aU7bPcHXZGBQojtoGFGPPuCKsZLo7KmKYM3ssAaGtqO/7gM1b.rBC', 'Administración del Sistema');
  END IF;
END;
$$;

-- ===============================
-- INSERTAR USUARIOS (administradores, profesores, estudiantes)
-- ===============================
-- Administradores
INSERT INTO public.usuario (documento, nombre, apellido, correo, rol, password, programa)
VALUES
('1029384756','Maribel','Sistema','maribel@upc.edu.co','administrador','$2a$12$aU7bPcHXZGBQojtoGFGPPuCKsZLo7KmKYM3ssAaGtqO/7gM1b.rBC','Ingeniería de Sistemas'),
('1092837465','Carlos','Admin','carlos.admin@upc.edu.co','administrador','$2a$12$aU7bPcHXZGBQojtoGFGPPuCKsZLo7KmKYM3ssAaGtqO/7gM1b.rBC','Ingeniería Electrónica');

-- Profesores
INSERT INTO public.usuario (documento, nombre, apellido, correo, rol, programa)
VALUES
('1083746574','Luis','Herrera','luis.herrera@unipiloto.edu.co','profesor','Ingeniería Electrónica'),
('1092837464','Manuel','Vinchira','manuel.vinchira@unipiloto.edu.co','profesor','Ingeniería de Sistemas'),
('1029384765','Victor','Gonzalez','victor.gonzalez@upc.edu.co','profesor','Ingeniería Mecatrónica'),
('1074938275','Ana','Martinez','ana.martinez@upc.edu.co','profesor','Ingeniería de Telecomunicaciones'),
('800001','Sandra','Ortiz','sandra.ortiz@upc.edu.co','profesor','Ing. Industrial'),
('800002','Julian','Rivas','julian.rivas@upc.edu.co','profesor','Ing. Sistemas'),
('800003','Fernando','Diaz','fernando.diaz@upc.edu.co','profesor','Ing. Mecatrónica');

-- Estudiantes
INSERT INTO public.usuario (documento, nombre, apellido, correo, rol, programa)
VALUES
('1002345678','Daniel','Parra','daniel.parra@upc.edu.co','estudiante','Ingeniería de Sistemas'),
('1002345679','Juan','Camacho','juan.camacho@upc.edu.co','estudiante','Ingeniería Electrónica'),
('1002345680','Alexandra','Tinjaca','alexandra.tinjaca@upc.edu.co','estudiante','Ingeniería de Software'),
('1002345681','Miguel','Ardila','miguel.ardila@upc.edu.co','estudiante','Ingeniería Mecánica'),
('1002345682','Alejandro','Jimenez','alejandro.jimenez@upc.edu.co','estudiante','Ingeniería Industrial'),
('1002345683','Brayan','Albornoz','brayan.albornoz@upc.edu.co','estudiante','Ingeniería de Sistemas'),
('1002345684','Cristian','Cortes','cristian.cortes@upc.edu.co','estudiante','Ingeniería de Sistemas'),
('1002345685','Danna','Segura','danna.segura@upc.edu.co','estudiante','Ingeniería Electrónica'),
('1002345686','David','Chacon','david.chacon@upc.edu.co','estudiante','Ingeniería de Software'),
('1002345687','Sofia','Torres','sofia.torres@upc.edu.co','estudiante','Ingeniería de Telecomunicaciones');

-- ===============================
-- INSERTAR LABORATORIOS
-- ===============================
INSERT INTO public.laboratorio (nombre, ubicacion, capacidad, capacidad_disponible, estado, descripcion) VALUES
('LAB DESARROLLO WEB', 'F201', 24, 24, 'Activo', 'Laboratorio para desarrollo de aplicaciones web'),
('LAB IOT', 'F202', 20, 20, 'Activo', 'Laboratorio para Internet de las Cosas'),
('LAB FISICA 1', 'F101', 30, 30, 'Activo', 'Laboratorio para prácticas de física básica'),
('LAB ELECTRONICA', 'F103', 26, 26, 'Activo', 'Laboratorio de electrónica'),
('SALA ESTUDIO 1', 'F103B', 25, 25, 'Activo', 'Sala de estudio libre');

-- ===============================
-- INSERTAR HORARIOS PERMITIDOS (Lun-Sab 06:00-21:00)
-- Utilizamos la tabla laboratorio_horario existente (nombre: laboratorio_horario)
-- ===============================
-- Para cada laboratorio, insertar días 1..6 con 06:00-21:00
INSERT INTO public.laboratorio_horario (id_laboratorio, dia_semana, hora_inicio, hora_fin)
SELECT l.id_laboratorio, d, '06:00', '21:00'
FROM public.laboratorio l, generate_series(1,6) d;

-- ===============================
-- INSERTAR CURSOS (vinculados a profesores)
-- ===============================
-- Buscamos ids de profesores por correo para vincularlos
INSERT INTO public.curso (nrc, nombre)
VALUES
('12355','Programación Web'),
('12555','Laboratorio IoT'),
('12665','Física Experimental'),
('16345','Electrónica Digital'),
('16765','Automatización Industrial'),
('19895','Redes Avanzadas');

-- ===============================
-- INSERTAR ELEMENTOS (equipos, sensores, etc.)
-- ===============================
INSERT INTO public.elemento (nombre, descripcion, estado, cantidad_total, cantidad_disponible, categoria) VALUES
('Laptop Dell Latitude', 'Computadores portátiles para desarrollo', 'Disponible', 16, 16, 'Equipos'),
('Sensor Temperatura DHT22', 'Sensor digital de temperatura y humedad', 'Disponible', 10, 10, 'Sensores'),
('Arduino Uno R3', 'Placa de desarrollo básica', 'Disponible', 12, 12, 'Electrónica'),
('Multímetro Digital', 'Medición de voltaje y corriente', 'Disponible', 10, 10, 'Instrumentos'),
('Protoboard 830pts', 'Tableros para prototipado sin soldadura', 'Disponible', 20, 20, 'Componentes'),
('Raspberry Pi 4', 'Microcomputador para proyectos', 'Disponible', 8, 8, 'Equipos'),
('Osciloscopio 100MHz', 'Instrumento de medida', 'Disponible', 4, 4, 'Instrumentos');

-- ===============================
-- PRÉSTAMOS (más datos)
-- ===============================
INSERT INTO public.prestamo (fecha_prestamo, fecha_devolucion, estado, id_usuario, id_elemento, observaciones) VALUES
 (now() - interval '2 days', NULL, 'Prestado', (SELECT id_usuario FROM public.usuario WHERE correo='daniel.parra@upc.edu.co'), (SELECT id_elemento FROM public.elemento WHERE nombre='Laptop Dell Latitude'), 'Práctica final'),
 (now() - interval '10 days', now() - interval '3 days', 'Devuelto', (SELECT id_usuario FROM public.usuario WHERE correo='alexandra.tinjaca@upc.edu.co'), (SELECT id_elemento FROM public.elemento WHERE nombre='Arduino Uno R3'), 'Proyecto entregado'),
 (now() - interval '15 days', NULL, 'Atrasado', (SELECT id_usuario FROM public.usuario WHERE correo='juan.camacho@upc.edu.co'), (SELECT id_elemento FROM public.elemento WHERE nombre='Multímetro Digital'), 'Retraso por viaje');

-- ===============================
-- NOTIFICACIONES DE PRUEBA
-- ===============================
INSERT INTO public.notificacion (id_usuario, tipo, titulo, mensaje, prioridad) VALUES
((SELECT id_usuario FROM public.usuario WHERE correo='daniel.parra@upc.edu.co'),'Reserva','Reserva Confirmada','Su reserva del laboratorio fue confirmada','Normal'),
((SELECT id_usuario FROM public.usuario WHERE correo='luis.herrera@unipiloto.edu.co'),'Sistema','Nueva Recurrencia','Se creó la recurrencia para su curso','Alta'),
((SELECT id_usuario FROM public.usuario WHERE correo='julian.rivas@upc.edu.co'),'Sistema','Recordatorio','Recuerde revisar su calendario de clases','Normal');

-- ===============================
-- CONSULTAS DE VERIFICACIÓN / RESUMEN
-- ===============================
SELECT '--- RESUMEN DE DATOS DE PRUEBA ---' AS info;
SELECT 'Usuarios total: ' || COUNT(*) FROM public.usuario;
SELECT 'Laboratorios total: ' || COUNT(*) FROM public.laboratorio;
SELECT 'Horarios permitidos total: ' || COUNT(*) FROM public.laboratorio_horario;
SELECT 'Cursos total: ' || COUNT(*) FROM public.curso;
SELECT 'Reservas normales total: ' || COUNT(*) FROM public.reserva;
SELECT 'Prestamos total: ' || COUNT(*) FROM public.prestamo;
SELECT 'Invitados total: ' || COUNT(*) FROM public.reserva_invitado;
SELECT 'Notificaciones total: ' || COUNT(*) FROM public.notificacion;

SELECT '--- FIN SCRIPT DE DATOS DE PRUEBA ---' AS fin;-- =============================================
