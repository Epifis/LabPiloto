// load-test-40tps.js
import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
  scenarios: {
    constant_load: {
      executor: 'constant-arrival-rate',
      rate: 40,        // 40 TPS
      timeUnit: '1s', 
      duration: '5m',  // 5 minutos para empezar
      preAllocatedVUs: 10,
      maxVUs: 50,
    },
  },
  thresholds: {
    http_req_duration: ['p(95)<2000'],  // 95% bajo 2 segundos
    http_req_failed: ['rate<0.01'],     // Error rate < 1%
  },
};

const BASE_URL = 'https://labpiloto.com';

export default function() {
  // Distribución realista de requests
  const random = Math.random();
  
  if (random < 0.4) {
    // 40%: Consultar laboratorios
    const res = http.get(`${BASE_URL}/api/laboratorios/disponibles`);
    check(res, { 'labs API ok': (r) => r.status === 200 });
  } 
  else if (random < 0.7) {
    // 30%: Consultar cursos  
    const res = http.get(`${BASE_URL}/api/cursos`);
    check(res, { 'cursos API ok': (r) => r.status === 200 });
  }
  else if (random < 0.9) {
    // 20%: Página principal
    const res = http.get(BASE_URL);
    check(res, { 'home page ok': (r) => r.status === 200 });
  }
  else {
    // 10%: Múltiples requests (usuario navegando)
    http.batch([
      ['GET', BASE_URL],
      ['GET', `${BASE_URL}/api/laboratorios/disponibles`],
    ]);
  }

  // Tiempo entre requests (0.5-2 segundos)
  sleep(Math.random() * 1.5 + 0.5);
}
