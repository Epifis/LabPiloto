#!/bin/bash
echo "🔍 MONITOREO AVANZADO - Iniciado: $(date)"
echo "Intervalo: 10 segundos"
echo "========================================"

for i in {1..30}; do  # 30 iteraciones = 5 minutos
    echo "--- Medición #$i - $(date '+%H:%M:%S') ---"
    
    # Docker stats detallado
    echo "🟢 CONTENEDORES:"
    docker stats --no-stream --format "table {{.Name}}\t{{.CPUPerc}}\t{{.MemPerc}}\t{{.MemUsage}}\t{{.NetIO}}" | tail -3
    
    # System resources
    echo "🟢 SISTEMA:"
    echo "  CPU: $(top -bn1 | grep "Cpu(s)" | sed "s/.*, *\([0-9.]*\)%* id.*/\1/" | awk '{print 100 - $1}')%"
    echo "  Memoria: $(free -m | awk 'NR==2{printf "%.1f%% (%sMB libres)", $3*100/$2, $4}')"
    echo "  Disk: $(df -h / | awk 'NR==2{print $5 " used (" $4 " free)"}')"
    
    # Application metrics
    echo "🟢 APLICACIÓN:"
    curl -s -o /dev/null -w "  Frontend: %{http_code} (%{time_total}s)\n" https://labpiloto.com
    curl -s -o /dev/null -w "  API Labs: %{http_code} (%{time_total}s)\n" https://labpiloto.com/api/laboratorios/disponibles
    curl -s -o /dev/null -w "  API Cursos: %{http_code} (%{time_total}s)\n" https://labpiloto.com/api/cursos
    
    # Network connections
    echo "🟢 RED:"
    netstat -an | grep :443 | grep ESTABLISHED | wc -l | xargs echo "  Conexiones HTTPS:"
    
    echo ""
    sleep 10
done

echo "✅ Monitoreo completado: $(date)"
