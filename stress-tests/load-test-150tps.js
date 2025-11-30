// load-test-150tps-optimized.js
import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
  scenarios: {
    constant_load: {
      executor: 'constant-arrival-rate',
      rate: 150,        // 150 TPS objetivo
      timeUnit: '1s',
      duration: '10m',  // 10 minutos a 150 TPS
      preAllocatedVUs: 50,
      maxVUs: 100,      // Dentro de tu límite
    },
  },
  thresholds: {
    http_req_duration: ['p(95)<3000', 'p(99)<5000'],
    http_req_failed: ['rate<0.02'],
  },
};

const BASE_URL = 'https://labpiloto.com';

export default function() {
  const random = Math.random();
  
  if (random < 0.4) {
    const res = http.get(`${BASE_URL}/api/laboratorios/disponibles`);
    check(res, { 'status 200': (r) => r.status === 200 });
  }
  else if (random < 0.7) {
    const res = http.get(`${BASE_URL}/api/cursos`);
    check(res, { 'status 200': (r) => r.status === 200 });
  }
  else {
    const res = http.get(BASE_URL);
    check(res, { 'status 200': (r) => r.status === 200 });
  }

  sleep(0.1); // Mínimo tiempo entre iteraciones
}
