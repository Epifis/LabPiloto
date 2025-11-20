// load-test-150tps.js
import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
  scenarios: {
    ramp_up: {
      executor: 'ramping-arrival-rate',
      startRate: 50,      // Empezar con 50 TPS
      timeUnit: '1s',
      stages: [
        { target: 100, duration: '1m' },   // 0-1min: 50→100 TPS
        { target: 150, duration: '2m' },   // 1-3min: 100→150 TPS
        { target: 150, duration: '10m' },  // 3-13min: 150 TPS sostenido
        { target: 0, duration: '1m' },     // 13-14min: Descenso
      ],
      preAllocatedVUs: 50,
      maxVUs: 200,
    },
  },
  thresholds: {
    http_req_duration: ['p(95)<3000', 'p(99)<5000'],
    http_req_failed: ['rate<0.02'],    // < 2% de errores
    checks: ['rate>0.98'],             // > 98% checks exitosos
  },
};

const BASE_URL = 'https://labpiloto.com';

export default function() {
  // Simular comportamiento real de usuarios
  const userType = Math.random();
  
  let requests = [];

  if (userType < 0.3) {
    // 30%: Usuarios explorando (múltiples requests)
    requests = [
      ['GET', BASE_URL],
      ['GET', `${BASE_URL}/api/laboratorios/disponibles`],
      ['GET', `${BASE_URL}/api/cursos`],
    ];
  } else if (userType < 0.6) {
    // 30%: Usuarios enfocados en laboratorios
    requests = [
      ['GET', `${BASE_URL}/api/laboratorios/disponibles`],
      ['GET', `${BASE_URL}/api/laboratorios/disponibles`], // Doble consulta
    ];
  } else if (userType < 0.8) {
    // 20%: Usuarios rápidos (solo página principal)
    requests = [['GET', BASE_URL]];
  } else {
    // 20%: Usuarios mixtos
    requests = [
      ['GET', BASE_URL],
      ['GET', `${BASE_URL}/api/cursos`],
    ];
  }

  // Ejecutar requests en paralelo
  const responses = http.batch(requests);
  
  // Verificar respuestas
  responses.forEach((res, index) => {
    check(res, {
      [`request ${index} status 200`]: (r) => r.status === 200,
      [`request ${index} response time < 5s`]: (r) => r.timings.duration < 5000,
    });
  });

  // Tiempo entre iteraciones (0.1-3 segundos)
  sleep(Math.random() * 2.9 + 0.1);
}
