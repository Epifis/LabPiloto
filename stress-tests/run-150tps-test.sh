#!/bin/bash
echo "🚀 INICIANDO PRUEBA DE 150 TPS"
echo "Timestamp: $(date)"
echo "================================"

# Crear directorios de reportes
mkdir -p reports/{raw-data,summary,charts}

# Nombre único para esta ejecución
TEST_ID="150tps-$(date +%Y%m%d-%H%M)"
echo "ID de prueba: $TEST_ID"

# Iniciar monitoreo en background
echo "📊 Iniciando monitoreo..."
./monitor-150tps.sh &

# Esperar 10 segundos para que el monitoreo se estabilice
sleep 10

# Ejecutar prueba de carga
echo "🔥 Ejecutando prueba de 150 TPS..."
k6 run load-test-150tps.js \
  --out json=reports/raw-data/k6-$TEST_ID.json \
  --out influxdb=http://localhost:8086/k6 2>/dev/null \
  > reports/raw-data/k6-output-$TEST_ID.log 2>&1

K6_EXIT_CODE=$?

# Esperar a que el monitoreo termine
wait

echo "📈 Generando reporte..."
./generate-report-150tps.sh $TEST_ID

echo "✅ Prueba completada: $(date)"
echo "📁 Reportes guardados en: reports/summary/report-$TEST_ID.md"
