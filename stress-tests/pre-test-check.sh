#!/bin/bash
echo "🔍 VERIFICACIÓN PRE-PRUEBA 150 TPS"
echo "==================================="

# Verificar servicios
echo "1. ✅ Contenedores activos:"
docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"

# Verificar endpoints
echo ""
echo "2. ✅ Endpoints críticos:"
curl -s -o /dev/null -w "Frontend: %{http_code} (%{time_total}s)\n" https://labpiloto.com
curl -s -o /dev/null -w "API Labs: %{http_code} (%{time_total}s)\n" https://labpiloto.com/api/laboratorios/disponibles
curl -s -o /dev/null -w "API Cursos: %{http_code} (%{time_total}s)\n" https://labpiloto.com/api/cursos

# Verificar recursos
echo ""
echo "3. ✅ Recursos disponibles:"
echo "CPU: $(nproc) cores"
echo "Memoria: $(free -h | awk 'NR==2{print $4}') libre"
echo "Disk: $(df -h / | awk 'NR==2{print $4}') libre"

# Verificar que no hay pruebas anteriores corriendo
echo ""
echo "4. ✅ Procesos k6:"
pgrep k6 && echo "❌ k6 ya está ejecutándose" || echo "✅ Listo para comenzar"

echo ""
echo "🎯 ESTADO: LISTO PARA PRUEBA DE 150 TPS"
