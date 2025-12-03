package edu.upc.labpilot.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${admin.validation.email}")
    private String correoAdministrador;

    @Value("${spring.mail.username}")
    private String correoRemitente;

    @Value("${app.base.url:http://localhost:8080}")
    private String baseUrl;

    // 🔐 ENVÍO DE CÓDIGOS MFA PARA LOGIN
    public void enviarCodigoMFA(String correo, String nombre, String codigo) {
        String asunto = "🔐 Código de Verificación - LabPilot";
        String mensaje = String.format("""
            Hola %s,

            Tu código de verificación MFA es:

            🎯 %s

            ⏰ Válido por 10 minutos
            🔒 No lo compartas con nadie

            Sistema LabPilot - UPC
            """, nombre, codigo);

        enviarCorreoSimple(correo, asunto, mensaje);
    }

    // ✅ VERIFICACIÓN DE CORREO PARA ESTUDIANTES (NUEVO FLUJO)
    public void enviarCodigoVerificacionCorreo(String correo, String nombre, String codigo) {
        String asunto = "✅ Verifica tu correo - LabPilot UPC";
        String mensaje = String.format("""
            Hola %s,

            🎓 Bienvenido/a al Sistema de Laboratorios LabPilot UPC

            Para verificar tu correo electrónico, utiliza el siguiente código:

            🎯 CÓDIGO DE VERIFICACIÓN: %s

            ⏰ Este código expira en 15 minutos.
            🔒 No lo compartas con nadie.

            Después de verificar tu correo, recibirás el contrato de responsabilidad para firmar.

            Sistema LabPilot - Universidad Piloto de Colombia
            """, nombre, codigo);

        enviarCorreoSimple(correo, asunto, mensaje);
        System.out.println("✅ Código verificación enviado a: " + correo);
    }

    // 📋 ENVÍO DE CONTRATO PARA ESTUDIANTES (DESPUÉS DE VERIFICAR CORREO)
    public void enviarContratoEstudiante(String correo, String nombre, String token) {
        String enlaceFirma = baseUrl + "/api/auth/firmar-contrato-estudiante/" + token;

        String asunto = "📋 Firma de Contrato - Estudiante LabPilot";
        String mensaje = String.format("""
            Estimado/a %s,

            ✅ Tu correo ha sido verificado exitosamente.

            📋 CONTRATO DE RESPONSABILIDAD
            Para activar tu cuenta, debes firmar el Contrato de Uso y Responsabilidad.

            Como estudiante del sistema, te comprometes a:
            • Utilizar los recursos exclusivamente para fines académicos
            • Cuidar los equipos y laboratorios
            • Reportar daños o irregularidades
            • Cumplir con horarios y normas establecidas

            ⚡ FIRMAR CONTRATO:
            %s

            ⚠️ IMPORTANTE: Tu cuenta se activará SOLO después de firmar este contrato.

            Sistema LabPilot - Universidad Piloto de Colombia
            """, nombre, enlaceFirma);

        enviarCorreoSimple(correo, asunto, mensaje);
        System.out.println("✅ Contrato estudiante enviado a: " + correo);
    }

    // 🔐 VERIFICACIÓN DE CORREO VÍA ENLACE (ALTERNATIVA)
    public void enviarCorreoVerificacionEstudiante(String correo, String nombre, String token) {
        String enlaceVerificacion = baseUrl + "/api/auth/verificar-estudiante/" + token;

        String asunto = "✅ Verifica tu correo - Estudiante LabPilot";
        String mensaje = String.format("""
            Hola %s,

            🎓 Bienvenido/a al Sistema de Laboratorios LabPilot UPC

            Para completar tu registro como estudiante, verifica tu correo haciendo clic en el siguiente enlace:

            🔗 %s

            Después de verificar tu correo, recibirás el contrato de responsabilidad para firmar.

            Sistema LabPilot - Universidad Piloto de Colombia
            """, nombre, enlaceVerificacion);

        enviarCorreoSimple(correo, asunto, mensaje);
        System.out.println("✅ Correo verificación enviado a: " + correo);
    }

    // 📋 SOLICITUD ADMIN CON CONTRATO
    public void enviarSolicitudAdmin(String nombre, String correoSolicitante, String token) {
        String enlaceContrato = baseUrl + "/api/admins/firmar-contrato/" + token;

        String asunto = "📋 Nueva Solicitud Admin - " + nombre;
        String mensaje = String.format("""
            📋 SOLICITUD DE ADMINISTRADOR

            👤 Nombre: %s
            📧 Correo: %s

            ⚡ ACCIÓN REQUERIDA:
            🔗 %s

            Este enlace permite:
            • Ver el contrato de responsabilidad
            • Firmar digitalmente
            • Activar la cuenta

            Sistema LabPilot - UPC
            """, nombre, correoSolicitante, enlaceContrato);

        enviarCorreoSimple(correoAdministrador, asunto, mensaje);
    }

    // 📝 NOTIFICAR CONTRATO FIRMADO Y ACTIVACIÓN
    public void notificarAdminActivado(String correo, String nombre) {
        String asunto = "🎉 Cuenta de Administrador Activada";
        String mensaje = String.format("""
            ¡Hola %s!

            ✅ Tu cuenta de administrador ha sido activada exitosamente.

            🔐 Rol: Administrador
            📧 Usuario: %s
            🔗 Acceso: %s

            ¡Bienvenido al equipo!

            Sistema LabPilot - UPC
            """, nombre, correo, baseUrl);

        enviarCorreoSimple(correo, asunto, mensaje);
    }

    // ❌ NOTIFICAR RECHAZO ADMIN
    public void notificarRechazoAdmin(String correo, String nombre) {
        String asunto = "❌ Solicitud de Administrador Rechazada";
        String mensaje = String.format("""
            Hola %s,

            Lamentamos informarte que tu solicitud de cuenta de administrador
            ha sido rechazada.

            Para más información, contacta al administrador del sistema.

            Sistema LabPilot - UPC
            """, nombre);

        enviarCorreoSimple(correo, asunto, mensaje);
    }

    // 📦 NOTIFICACIONES PRÉSTAMOS
    public void notificarPrestamoAprobado(String correo, String nombre, String fecha, List<String> elementos) {
        String asunto = "📦 Préstamo Aprobado - LabPilot";
        String elementosStr = String.join("\n• ", elementos);

        String mensaje = String.format("""
            Hola %s,

            ✅ Tu préstamo ha sido APROBADO

            📅 Fecha: %s
            📋 Elementos:
            • %s

            Puedes recoger los elementos en el laboratorio.

            Sistema LabPilot - UPC
            """, nombre, fecha, elementosStr);

        enviarCorreoSimple(correo, asunto, mensaje);
    }

    public void notificarPrestamoRechazado(String correo, String nombre) {
        String asunto = "❌ Préstamo Rechazado - LabPilot";
        String mensaje = String.format("""
            Hola %s,

            Tu solicitud de préstamo ha sido rechazada.

            Contacta al laboratorio para más información.

            Sistema LabPilot - UPC
            """, nombre);

        enviarCorreoSimple(correo, asunto, mensaje);
    }

    // 🏫 NOTIFICACIONES RESERVAS
    public void notificarReservaAprobada(String correo, String nombre, String lab, String inicio) {
        String asunto = "🏫 Reserva Aprobada - LabPilot";
        String mensaje = String.format("""
            Hola %s,

            ✅ Tu reserva ha sido APROBADA

            🏫 Laboratorio: %s
            📅 Inicio: %s

            ¡Disfruta tu uso del laboratorio!
            ¡No olvides tu bata!
            

            Sistema LabPilot - UPC
            """, nombre, lab, inicio);

        enviarCorreoSimple(correo, asunto, mensaje);
    }

    public void notificarReservaRechazada(String correo, String nombre) {
        String asunto = "❌ Reserva Rechazada - LabPilot";
        String mensaje = String.format("""
            Hola %s,

            Tu reserva ha sido rechazada.

            Contacta al laboratorio para más información.

            Sistema LabPilot - UPC
            """, nombre);

        enviarCorreoSimple(correo, asunto, mensaje);
    }

    // 🔄 MÉTODO PRIVADO PARA ENVÍO
    private void enviarCorreoSimple(String destino, String asunto, String contenido) {
        try {
            SimpleMailMessage mensaje = new SimpleMailMessage();
            mensaje.setTo(destino);
            mensaje.setSubject(asunto);
            mensaje.setText(contenido);
            mensaje.setFrom(correoRemitente);

            mailSender.send(mensaje);
            System.out.println("✅ Correo enviado a: " + destino);
        } catch (Exception e) {
            System.err.println("❌ Error enviando correo a " + destino + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * ✅ Enviar contrato de responsabilidad al SOLICITANTE de admin
     */
    public void enviarContratoSolicitante(String correoSolicitante, String nombreSolicitante, String token) {
        String enlaceFirma = baseUrl + "/api/admins/firmar-contrato/" + token;

        String asunto = "📋 Firma de Contrato de Responsabilidad - LabPilot UPC";
        String mensaje = String.format("""
        Estimado/a %s,

        ✅ Su solicitud de cuenta de administrador ha sido PRE-APROBADA.

        📋 CONTRATO DE RESPONSABILIDAD
        Antes de activar su cuenta, debe leer y firmar el Contrato de Responsabilidad.

        Como administrador del sistema, usted será responsable de:
        • Gestionar usuarios, laboratorios y equipos
        • Aprobar/rechazar reservas y préstamos  
        • Mantener la integridad y seguridad del sistema
        • Proteger la información confidencial
        • Cumplir con políticas de seguridad

        ⚡ FIRMAR CONTRATO:
        %s

        ⚠️ IMPORTANTE: Su cuenta se activará SOLO después de firmar este contrato.

        Sistema LabPilot - Universidad UPC
        """, nombreSolicitante, enlaceFirma);

        enviarCorreoSimple(correoSolicitante, asunto, mensaje);
    }

    /**
     * ✅ Notificar al SuperAdmin sobre nueva solicitud
     */
    public void notificarSolicitudAdmin(String nombreSolicitante, String correoSolicitante, String token) {
        String enlaceAprobar = baseUrl + "/api/admins/aprobar/" + token;
        String enlaceRechazar = baseUrl + "/api/admins/rechazar/" + token;

        String asunto = "📋 Nueva Solicitud de Administrador - " + nombreSolicitante;
        String mensaje = String.format("""
        📋 SOLICITUD DE ADMINISTRADOR PENDIENTE

        👤 Solicitante: %s
        📧 Correo: %s

        ⚡ ACCIONES DISPONIBLES:
        ✅ APROBAR: %s
        ❌ RECHAZAR: %s

        Sistema LabPilot - UPC
        """, nombreSolicitante, correoSolicitante, enlaceAprobar, enlaceRechazar);

        enviarCorreoSimple(correoAdministrador, asunto, mensaje);
    }

    // 🔄 REENVIAR CÓDIGO MFA PARA LOGIN
    public void reenviarCodigoMFA(String correo, String nombre, String codigo) {
        String asunto = "🔐 Código de Verificación - LabPilot";
        String mensaje = String.format("""
            Hola %s,

            Se ha solicitado un reenvío de tu código de verificación MFA:

            🎯 %s

            ⏰ Válido por 10 minutos
            🔒 No lo compartas con nadie

            Si no solicitaste este código, ignora este mensaje.

            Sistema LabPilot - UPC
            """, nombre, codigo);

        enviarCorreoSimple(correo, asunto, mensaje);
        System.out.println("✅ Código MFA reenviado a: " + correo);
    }

    // 🔄 REENVIAR CÓDIGO DE VERIFICACIÓN PARA REGISTRO
    public void reenviarCodigoVerificacion(String correo, String nombre, String codigo) {
        String asunto = "✅ Código de Verificación - LabPilot UPC";
        String mensaje = String.format("""
            Hola %s,

            Se ha solicitado un reenvío de tu código de verificación:

            🎯 CÓDIGO DE VERIFICACIÓN: %s

            ⏰ Este código expira en 15 minutos.
            🔒 No lo compartas con nadie.

            Sistema LabPilot - Universidad Piloto de Colombia
            """, nombre, codigo);

        enviarCorreoSimple(correo, asunto, mensaje);
        System.out.println("✅ Código verificación reenviado a: " + correo);
    }

    // 📧 NOTIFICACIÓN GENÉRICA
    public void enviarNotificacionGenerica(String correo, String asunto, String mensaje) {
        try {
            SimpleMailMessage email = new SimpleMailMessage();
            email.setTo(correo);
            email.setSubject(asunto);
            email.setText(mensaje);
            email.setFrom(correoRemitente);

            mailSender.send(email);
            System.out.println("✅ Notificación enviada a: " + correo);
        } catch (Exception e) {
            System.err.println("❌ Error enviando notificación a " + correo + ": " + e.getMessage());
        }
    }
}