#!/bin/bash
echo "🔍 MONITOREO BÁSICO - Iniciado: $(date)"
echo "========================================"

for i in {1..12}; do  # 12 iteraciones = 1 minuto
    echo "--- Medición #$i - $(date '+%H:%M:%S') ---"
    
    # Docker stats
    echo "Contenedores:"
    docker stats --no-stream --format "table {{.Name}}\t{{.CPUPerc}}\t{{.MemUsage}}" | tail -3
    
    # System resources
    echo "Sistema:"
    echo "  CPU: $(top -bn1 | grep "Cpu(s)" | sed "s/.*, *\([0-9.]*\)%* id.*/\1/" | awk '{print 100 - $1}')%"
    echo "  Memoria: $(free -m | awk 'NR==2{printf "%.1f%%", $3*100/$2}')"
    
    # Application health
    echo "Aplicación:"
    curl -s -o /dev/null -w "  Frontend: %{http_code} (%{time_total}s)\n" https://labpiloto.com
    curl -s -o /dev/null -w "  API Labs: %{http_code} (%{time_total}s)\n" https://labpiloto.com/api/laboratorios/disponibles
    
    echo ""
    sleep 5
done

echo "✅ Monitoreo completado: $(date)"
