

# LabPilot - AWS Deployment

## Configuración de Producción

### Archivos CRÍTICOS (NO están en GitHub):
- `src/main/resources/application-aws.properties`
- Certificados SSL en `/etc/letsencrypt/`
- Configuración Nginx en `/etc/nginx/conf.d/labpiloto.conf`

### Después de git pull:
```bash
./scripts/restore-config.sh
./scripts/deploy-safe.sh
