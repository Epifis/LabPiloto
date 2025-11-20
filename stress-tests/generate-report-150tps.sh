#!/bin/bash
TEST_ID=$1
REPORT_FILE="reports/summary/report-$TEST_ID.md"

# Extraer métricas básicas del log de k6
K6_LOG="reports/raw-data/k6-output-$TEST_ID.log"
MONITOR_LOG="reports/raw-data/monitoring-150tps-${TEST_ID#150tps-}.log"

# Función para extraer métricas
extract_metric() {
    grep "$1" $K6_LOG | tail -1 | awk '{print $2}'
}

# Extraer métricas clave
TPS=$(extract_metric "http_reqs.*http_reqs")
P95_RESPONSE=$(extract_metric "p\(95\)=")
ERROR_RATE=$(extract_metric "http_req_failed")
CHECKS_PASSED=$(grep "checks.*100.00%" $K6_LOG | wc -l)

# Generar reporte
echo "# 🚀 Reporte de Prueba 150 TPS - LabPilot" > $REPORT_FILE
echo "**ID de Prueba:** $TEST_ID" >> $REPORT_FILE
echo "**Fecha:** $(date)" >> $REPORT_FILE
echo "**Duración:** 14 minutos (ramp-up + sostenido)" >> $REPORT_FILE
echo "" >> $REPORT_FILE

echo "## 🎯 Resumen Ejecutivo" >> $REPORT_FILE
echo "" >> $REPORT_FILE

echo "## 📊 Métricas de Performance" >> $REPORT_FILE
echo "| Métrica | Resultado | Objetivo | Estado |" >> $REPORT_FILE
echo "|---------|-----------|----------|--------|" >> $REPORT_FILE
echo "| Throughput (TPS) | $TPS | 150 | 🔍 |" >> $REPORT_FILE
echo "| Response Time (p95) | $P95_RESPONSE | <3000ms | 🔍 |" >> $REPORT_FILE
echo "| Error Rate | $ERROR_RATE | <2% | 🔍 |" >> $REPORT_FILE
echo "| Checks Passed | $CHECKS_PASSED | >98% | 🔍 |" >> $REPORT_FILE
echo "" >> $REPORT_FILE

echo "## 📈 Análisis de Recursos" >> $REPORT_FILE
echo "### Uso Máximo Observado" >> $REPORT_FILE
echo "- **CPU Backend:** \`$(grep "backend" $MONITOR_LOG 2>/dev/null | grep "CPU" | awk '{print $2}' | sort -nr | head -1 || echo "N/A")\`" >> $REPORT_FILE
echo "- **Memoria Backend:** \`$(grep "backend" $MONITOR_LOG 2>/dev/null | grep "MEM" | awk '{print $4}' | sort -nr | head -1 || echo "N/A")\`" >> $REPORT_FILE
echo "- **Conexiones HTTPS:** \`$(grep "HTTPS Connections" $MONITOR_LOG 2>/dev/null | awk '{print $4}' | sort -nr | head -1 || echo "N/A")\`" >> $REPORT_FILE
echo "" >> $REPORT_FILE

echo "## 🔍 Hallazgos Clave" >> $REPORT_FILE
echo "- [ ] Sistema mantuvo 150 TPS" >> $REPORT_FILE
echo "- [ ] Response times dentro de límites" >> $REPORT_FILE
echo "- [ ] Sin errores de aplicación" >> $REPORT_FILE
echo "- [ ] Recursos dentro de capacidad" >> $REPORT_FILE
echo "" >> $REPORT_FILE

echo "## 🎯 Conclusión" >> $REPORT_FILE
echo "*Análisis en progreso...*" >> $REPORT_FILE
echo "" >> $REPORT_FILE

echo "## 📋 Recomendaciones" >> $REPORT_FILE
echo "1. Revisar logs completos en: \`reports/raw-data/\`" >> $REPORT_FILE
echo "2. Analizar métricas de base de datos" >> $REPORT_FILE
echo "3. Verificar logs de aplicación durante carga" >> $REPORT_FILE
echo "" >> $REPORT_FILE

echo "📄 Reporte generado: $REPORT_FILE"
