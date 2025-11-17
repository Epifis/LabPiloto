package edu.upc.labpilot.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

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

    // Enviar solicitud al administrador con enlaces de aprobación/rechazo
    public void enviarSolicitudRegistro(String nombre, String correoSolicitante, String rol, String token) {
        try {
            String urlAprobar = baseUrl + "/api/admins/aprobar/" + token;
            String urlRechazar = baseUrl + "/api/admins/rechazar/" + token;
            
            String asunto = "📋 Solicitud de registro de nuevo administrador";
            String mensaje = "Se ha recibido una nueva solicitud de cuenta de administrador.\n\n"
                    + "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n"
                    + "📋 INFORMACIÓN DEL SOLICITANTE\n"
                    + "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n"
                    + "👤 Nombre: " + nombre + "\n"
                    + "📧 Correo: " + correoSolicitante + "\n"
                    + "🔐 Rol solicitado: " + rol + "\n\n"
                    + "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n"
                    + "⚡ ACCIONES DISPONIBLES\n"
                    + "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n"
                    + "✅ APROBAR SOLICITUD:\n"
                    + urlAprobar + "\n\n"
                    + "❌ RECHAZAR SOLICITUD:\n"
                    + urlRechazar + "\n\n"
                    + "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n"
                    + "⚠️ Importante: Estos enlaces son de un solo uso.\n"
                    + "Una vez que hagas clic en uno de ellos, la solicitud será procesada\n"
                    + "y el solicitante recibirá un correo con la decisión.\n\n"
                    + "Sistema de Gestión de Laboratorios - LabPilot UPC";

            SimpleMailMessage mailMessage = new SimpleMailMessage();
            mailMessage.setTo(correoAdministrador);
            mailMessage.setSubject(asunto);
            mailMessage.setText(mensaje);
            mailMessage.setFrom(correoRemitente);

            mailSender.send(mailMessage);

            System.out.println("✅ Correo de solicitud enviado a " + correoAdministrador);
        } catch (Exception e) {
            System.err.println("❌ Error al enviar correo de solicitud: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Notificar aprobación al solicitante
    public void notificarAprobacion(String correoSolicitante, String nombre) {
        try {
            String asunto = "🎉 Tu cuenta de administrador ha sido aprobada";
            String mensaje = "¡Hola " + nombre + "!\n\n"
                    + "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n"
                    + "✅ SOLICITUD APROBADA\n"
                    + "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n"
                    + "Nos complace informarte que tu solicitud de cuenta de administrador\n"
                    + "ha sido APROBADA exitosamente.\n\n"
                    + "📧 Correo: " + correoSolicitante + "\n"
                    + "🔐 Rol asignado: Administrador\n\n"
                    + "Ya puedes iniciar sesión en el sistema con las credenciales que\n"
                    + "proporcionaste durante el registro.\n\n"
                    + "🔗 Acceso al sistema:\n"
                    + baseUrl + "\n\n"
                    + "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n"
                    + "Si tienes alguna duda o problema, no dudes en contactarnos.\n\n"
                    + "¡Bienvenido al equipo!\n\n"
                    + "Sistema de Gestión de Laboratorios - LabPilot UPC";

            SimpleMailMessage mailMessage = new SimpleMailMessage();
            mailMessage.setTo(correoSolicitante);
            mailMessage.setSubject(asunto);
            mailMessage.setText(mensaje);
            mailMessage.setFrom(correoRemitente);

            mailSender.send(mailMessage);

            System.out.println("✅ Correo de aprobación enviado a " + correoSolicitante);
        } catch (Exception e) {
            System.err.println("❌ Error al enviar correo de aprobación: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Notificar rechazo al solicitante
    public void notificarRechazo(String correoSolicitante, String nombre) {
        try {
            String asunto = "❌ Solicitud de cuenta de administrador rechazada";
            String mensaje = "Hola " + nombre + ",\n\n"
                    + "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n"
                    + "❌ SOLICITUD RECHAZADA\n"
                    + "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n"
                    + "Lamentamos informarte que tu solicitud de cuenta de administrador\n"
                    + "ha sido RECHAZADA.\n\n"
                    + "📧 Correo: " + correoSolicitante + "\n\n"
                    + "Si consideras que esto es un error o deseas más información,\n"
                    + "por favor contacta directamente con el administrador del sistema.\n\n"
                    + "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n"
                    + "Sistema de Gestión de Laboratorios - LabPilot UPC";

            SimpleMailMessage mailMessage = new SimpleMailMessage();
            mailMessage.setTo(correoSolicitante);
            mailMessage.setSubject(asunto);
            mailMessage.setText(mensaje);
            mailMessage.setFrom(correoRemitente);

            mailSender.send(mailMessage);

            System.out.println("✅ Correo de rechazo enviado a " + correoSolicitante);
        } catch (Exception e) {
            System.err.println("❌ Error al enviar correo de rechazo: " + e.getMessage());
            e.printStackTrace();
        }
    }
}