#!/bin/bash
echo "🔍 MONITOREO 150 TPS - Iniciado: $(date)"
echo "Duración: 14 minutos | Intervalo: 15 segundos"
echo "=============================================="

LOG_FILE="reports/raw-data/monitoring-150tps-$(date +%Y%m%d-%H%M).log"
echo "Inicio: $(date)" > $LOG_FILE

for i in {1..56}; do  # 56 iteraciones = 14 minutos
    TIMESTAMP=$(date '+%Y-%m-%d %H:%M:%S')
    echo "--- Medición #$i - $TIMESTAMP ---" | tee -a $LOG_FILE
    
    # Docker stats detallado
    echo "🐳 DOCKER CONTAINERS:" | tee -a $LOG_FILE
    docker stats --no-stream --format "table {{.Name}}\t{{.CPUPerc}}\t{{.MemPerc}}\t{{.MemUsage}}\t{{.NetIO}}" 2>/dev/null | tee -a $LOG_FILE
    
    # System resources
    echo "🖥️  SYSTEM RESOURCES:" | tee -a $LOG_FILE
    CPU_USAGE=$(top -bn1 | grep "Cpu(s)" | sed "s/.*, *\([0-9.]*\)%* id.*/\1/" | awk '{print 100 - $1}')
    MEMORY_USAGE=$(free -m | awk 'NR==2{printf "%.1f%% (%sMB free)", $3*100/$2, $4}')
    DISK_USAGE=$(df -h / | awk 'NR==2{print $5 " used (" $4 " free)"}')
    LOAD_AVG=$(uptime | awk -F'load average:' '{print $2}')
    
    echo "  CPU: $CPU_USAGE% | Load: $LOAD_AVG" | tee -a $LOG_FILE
    echo "  Memory: $MEMORY_USAGE" | tee -a $LOG_FILE
    echo "  Disk: $DISK_USAGE" | tee -a $LOG_FILE
    
    # Application health checks
    echo "🌐 APPLICATION HEALTH:" | tee -a $LOG_FILE
    {
        echo -n "  Frontend: "
        curl -s -o /dev/null -w "%{http_code} (%{time_total}s)\n" https://labpiloto.com --connect-timeout 5
    } | tee -a $LOG_FILE
    
    {
        echo -n "  API Labs: "
        curl -s -o /dev/null -w "%{http_code} (%{time_total}s)\n" https://labpiloto.com/api/laboratorios/disponibles --connect-timeout 5
    } | tee -a $LOG_FILE
    
    {
        echo -n "  API Cursos: "
        curl -s -o /dev/null -w "%{http_code} (%{time_total}s)\n" https://labpiloto.com/api/cursos --connect-timeout 5
    } | tee -a $LOG_FILE
    
    # Network and connections
    echo "📡 NETWORK METRICS:" | tee -a $LOG_FILE
    CONNECTIONS=$(netstat -an | grep :443 | grep ESTABLISHED | wc -l)
    echo "  HTTPS Connections: $CONNECTIONS" | tee -a $LOG_FILE
    
    # Database connections (si es posible)
    echo "🗄️  DATABASE:" | tee -a $LOG_FILE
    docker logs backend --tail 3 2>/dev/null | grep -i "connection\|pool\|error" | tail -2 | tee -a $LOG_FILE || echo "  No DB logs available" | tee -a $LOG_FILE
    
    echo "" | tee -a $LOG_FILE
    sleep 15
done

echo "✅ Monitoreo completado: $(date)" | tee -a $LOG_FILE
